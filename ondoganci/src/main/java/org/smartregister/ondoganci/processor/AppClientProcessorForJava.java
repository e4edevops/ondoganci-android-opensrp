package org.smartregister.ondoganci.processor;

import android.content.ContentValues;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTime;
import org.smartregister.child.util.ChildDbUtils;
import org.smartregister.child.util.Constants;
import org.smartregister.child.util.JsonFormUtils;
import org.smartregister.child.util.MoveToMyCatchmentUtils;
import org.smartregister.child.util.Utils;
import org.smartregister.clientandeventmodel.DateUtil;
import org.smartregister.commonregistry.AllCommonsRepository;
import org.smartregister.domain.db.Client;
import org.smartregister.domain.db.Event;
import org.smartregister.domain.db.EventClient;
import org.smartregister.domain.jsonmapping.ClientClassification;
import org.smartregister.domain.jsonmapping.ClientField;
import org.smartregister.domain.jsonmapping.Column;
import org.smartregister.domain.jsonmapping.Table;
import org.smartregister.growthmonitoring.domain.Head;
import org.smartregister.growthmonitoring.domain.Height;
import org.smartregister.growthmonitoring.domain.Weight;
import org.smartregister.growthmonitoring.repository.HeadRepository;
import org.smartregister.growthmonitoring.repository.HeightRepository;
import org.smartregister.growthmonitoring.repository.WeightRepository;
import org.smartregister.growthmonitoring.service.intent.HeadIntentService;
import org.smartregister.growthmonitoring.service.intent.HeightIntentService;
import org.smartregister.growthmonitoring.service.intent.WeightIntentService;
import org.smartregister.immunization.domain.ServiceRecord;
import org.smartregister.immunization.domain.ServiceSchedule;
import org.smartregister.immunization.domain.ServiceType;
import org.smartregister.immunization.domain.Vaccine;
import org.smartregister.immunization.domain.VaccineSchedule;
import org.smartregister.immunization.repository.RecurringServiceRecordRepository;
import org.smartregister.immunization.repository.RecurringServiceTypeRepository;
import org.smartregister.immunization.repository.VaccineRepository;
import org.smartregister.immunization.service.intent.RecurringIntentService;
import org.smartregister.immunization.service.intent.VaccineIntentService;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.sync.ClientProcessorForJava;
import org.smartregister.sync.MiniClientProcessorForJava;
import org.smartregister.ondoganci.application.OndoganciApplication;
import org.smartregister.ondoganci.util.AppConstants;
import org.smartregister.ondoganci.util.AppExecutors;
import org.smartregister.ondoganci.util.AppUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import timber.log.Timber;

public class AppClientProcessorForJava extends ClientProcessorForJava {

    private static AppClientProcessorForJava instance;

    private HashMap<String, MiniClientProcessorForJava> processorMap = new HashMap<>();
    private HashMap<MiniClientProcessorForJava, List<Event>> unsyncEventsPerProcessor = new HashMap<>();
    private AppExecutors appExecutors = new AppExecutors();
    private HashMap<String, DateTime> clientsForAlertUpdates = new HashMap<>();

    private AppClientProcessorForJava(Context context) {
        super(context);
    }

    public void addMiniProcessors(MiniClientProcessorForJava... miniClientProcessorsForJava) {
        for (MiniClientProcessorForJava miniClientProcessorForJava : miniClientProcessorsForJava) {
            unsyncEventsPerProcessor.put(miniClientProcessorForJava, new ArrayList<Event>());

            HashSet<String> eventTypes = miniClientProcessorForJava.getEventTypes();

            for (String eventType : eventTypes) {
                processorMap.put(eventType, miniClientProcessorForJava);
            }
        }
    }

    public static AppClientProcessorForJava getInstance(Context context) {
        if (instance == null) {
            instance = new AppClientProcessorForJava(context);
        }
        return instance;
    }

    @Override
    public void processClient(List<EventClient> eventClients) throws Exception {
        ClientClassification clientClassification = assetJsonToJava("ec_client_classification.json",
                ClientClassification.class);
        Table vaccineTable = assetJsonToJava("ec_client_vaccine.json", Table.class);
        Table weightTable = assetJsonToJava("ec_client_weight.json", Table.class);
        Table heightTable = assetJsonToJava("ec_client_height.json", Table.class);
        Table headTable = assetJsonToJava("ec_client_head.json", Table.class);
        Table serviceTable = assetJsonToJava("ec_client_service.json", Table.class);

        if (!eventClients.isEmpty()) {
            List<Event> unsyncEvents = new ArrayList<>();
            for (EventClient eventClient : eventClients) {
                Event event = eventClient.getEvent();
                if (event == null) {
                    return;
                }

                String eventType = event.getEventType();
                if (eventType == null) {
                    continue;
                }
                switch (eventType) {
                    case VaccineIntentService.EVENT_TYPE:
                    case VaccineIntentService.EVENT_TYPE_OUT_OF_CATCHMENT:
                        processVaccinationEvent(vaccineTable, eventClient, event, eventType);
                        break;
                    case WeightIntentService.EVENT_TYPE:
                    case WeightIntentService.EVENT_TYPE_OUT_OF_CATCHMENT:
                        processWeightEvent(weightTable, heightTable, headTable, eventClient, eventType);
                        break;

                    case RecurringIntentService.EVENT_TYPE:
                        if (serviceTable == null) {
                            continue;
                        }
                        processService(eventClient, serviceTable);
                        break;
                    case JsonFormUtils.BCG_SCAR_EVENT:
                        processBCGScarEvent(eventClient);
                        break;
                    case MoveToMyCatchmentUtils.MOVE_TO_CATCHMENT_EVENT:
                        unsyncEvents.add(event);
                        break;
                    case Constants.EventType.DEATH:
                        if (processDeathEvent(eventClient)) {
                            unsyncEvents.add(event);
                        }
                        break;
                    case Constants.EventType.ARCHIVE_CHILD_RECORD:
                        if (eventClient.getClient() != null && clientClassification != null) {
                            OndoganciApplication.getInstance().registerTypeRepository().removeAll(event.getBaseEntityId());
                            processEventClient(clientClassification, eventClient, event);
                        }
                        break;
                    case Constants.EventType.FATHER_REGISTRATION:
                    case Constants.EventType.BITRH_REGISTRATION:
                    case Constants.EventType.UPDATE_BITRH_REGISTRATION:
                    case Constants.EventType.NEW_WOMAN_REGISTRATION:
                    case Constants.EventType.UPDATE_FATHER_DETAILS:
                    case Constants.EventType.UPDATE_MOTHER_DETAILS:
                        if (eventType.equals(Constants.EventType.BITRH_REGISTRATION) && eventClient.getClient() != null) {
                            OndoganciApplication.getInstance().registerTypeRepository().add(AppConstants.RegisterType.CHILD, event.getBaseEntityId());
                        }
                        if (clientClassification == null) {
                            continue;
                        }
                        processChildRegistrationAndRelatedEvents(clientClassification, eventClient, event);
                        break;
                    default:
                        if (processorMap.containsKey(eventType)) {
                            try {
                                processEventUsingMiniprocessor(clientClassification, eventClient, eventType);
                            } catch (Exception ex) {
                                Timber.e(ex);
                            }
                        }
                }
            }


            // Unsync events that are should not be in this device
            processUnsyncEvents(unsyncEvents);
            // Process alerts for clients
            Runnable runnable = () -> updateClientAlerts(clientsForAlertUpdates);

            appExecutors.diskIO().execute(runnable);
        }
    }

    private void processEventClient(@NonNull ClientClassification clientClassification, @NonNull EventClient eventClient, @NonNull Event event) {
        Client client = eventClient.getClient();
        if (client != null) {
            try {
                processEvent(event, client, clientClassification);
            } catch (Exception e) {
                Timber.e(e);
            }
        }
    }
    private void updateClientAlerts(@NonNull HashMap<String, DateTime> clientsForAlertUpdates) {
        HashMap<String, DateTime> stringDateTimeHashMap = SerializationUtils.clone(clientsForAlertUpdates);
        for (String baseEntityId : stringDateTimeHashMap.keySet()) {
            DateTime birthDateTime = clientsForAlertUpdates.get(baseEntityId);
            if (birthDateTime != null) {
                VaccineSchedule.updateOfflineAlerts(baseEntityId, birthDateTime, "child");
                ServiceSchedule.updateOfflineAlerts(baseEntityId, birthDateTime);
            }
        }
        clientsForAlertUpdates.clear();
    }

    private boolean processDeathEvent(@NonNull EventClient eventClient) {
        if (eventClient.getEvent().getEntityType().equals(AppConstants.EntityType.CHILD)) {
            return AppUtils.updateChildDeath(eventClient);
        }

        return false;
    }

    private void processUnsyncEvents(@NonNull List<Event> unsyncEvents) {
        if (!unsyncEvents.isEmpty()) {
            unSync(unsyncEvents);
        }

        for (MiniClientProcessorForJava miniClientProcessorForJava : unsyncEventsPerProcessor.keySet()) {
            List<Event> processorUnsyncEvents = unsyncEventsPerProcessor.get(miniClientProcessorForJava);
            miniClientProcessorForJava.unSync(processorUnsyncEvents);
        }
    }

    private void processChildRegistrationAndRelatedEvents(@NonNull ClientClassification clientClassification, @NonNull EventClient eventClient, @NonNull Event event) {
        Client client = eventClient.getClient();
        if (client != null) {
            try {
                processEvent(event, client, clientClassification);

                scheduleUpdatingClientAlerts(client.getBaseEntityId(), client.getBirthdate());
            } catch (Exception e) {
                Timber.e(e);
            }
        }
    }

    private void processEventUsingMiniprocessor(ClientClassification clientClassification, EventClient eventClient, String eventType) throws Exception {
        MiniClientProcessorForJava miniClientProcessorForJava = processorMap.get(eventType);
        if (miniClientProcessorForJava != null) {
            List<Event> processorUnsyncEvents = unsyncEventsPerProcessor.get(miniClientProcessorForJava);
            if (processorUnsyncEvents == null) {
                processorUnsyncEvents = new ArrayList<Event>();
                unsyncEventsPerProcessor.put(miniClientProcessorForJava, processorUnsyncEvents);
            }

            miniClientProcessorForJava.processEventClient(eventClient, processorUnsyncEvents, clientClassification);
        }
    }

    private void processWeightEvent(Table weightTable, Table heightTable, Table headTable, EventClient eventClient, String eventType) throws Exception {
        if (weightTable == null) {
            return;
        }

        if (heightTable == null) {
            return;
        }

        if (headTable == null) {
            return;
        }

        processWeight(eventClient, weightTable,
                eventType.equals(WeightIntentService.EVENT_TYPE_OUT_OF_CATCHMENT));
        processHeight(eventClient, heightTable,
                eventType.equals(HeightIntentService.EVENT_TYPE_OUT_OF_CATCHMENT));
        processHead(eventClient, headTable,
                eventType.equals(HeadIntentService.EVENT_TYPE_OUT_OF_CATCHMENT));
    }

    private void processVaccinationEvent(Table vaccineTable, EventClient eventClient, Event event, String eventType) throws Exception {
        if (vaccineTable == null) {
            return;
        }

        Client client = eventClient.getClient();
        if (!childExists(client.getBaseEntityId())) {
            List<String> createCase = new ArrayList<>();
            createCase.add(Utils.metadata().childRegister.tableName);
            processCaseModel(event, client, createCase);
        }

        processVaccine(eventClient, vaccineTable,
                eventType.equals(VaccineIntentService.EVENT_TYPE_OUT_OF_CATCHMENT));

        scheduleUpdatingClientAlerts(client.getBaseEntityId(), client.getBirthdate());
    }

    private boolean childExists(String entityId) {
        return OndoganciApplication.getInstance().eventClientRepository().checkIfExists(EventClientRepository.Table.client, entityId);
    }

    private Boolean processVaccine(@Nullable EventClient vaccine, @Nullable Table vaccineTable, boolean outOfCatchment) throws Exception {
        try {
            if (vaccine == null || vaccine.getEvent() == null) {
                return false;
            }

            if (vaccineTable == null) {
                return false;
            }

            Timber.d("Starting processVaccine table: %s", vaccineTable.name);

            ContentValues contentValues = processCaseModel(vaccine, vaccineTable);

            // save the values to db
            if (contentValues != null && contentValues.size() > 0) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date date = simpleDateFormat.parse(contentValues.getAsString(VaccineRepository.DATE));

                VaccineRepository vaccineRepository = OndoganciApplication.getInstance().vaccineRepository();
                Vaccine vaccineObj = new Vaccine();
                vaccineObj.setBaseEntityId(contentValues.getAsString(VaccineRepository.BASE_ENTITY_ID));
                vaccineObj.setName(contentValues.getAsString(VaccineRepository.NAME));
                if (contentValues.containsKey(VaccineRepository.CALCULATION)) {
                    vaccineObj.setCalculation(parseInt(contentValues.getAsString(VaccineRepository.CALCULATION)));
                }
                vaccineObj.setDate(date);
                vaccineObj.setAnmId(contentValues.getAsString(VaccineRepository.ANMID));
                vaccineObj.setLocationId(contentValues.getAsString(VaccineRepository.LOCATION_ID));
                vaccineObj.setSyncStatus(VaccineRepository.TYPE_Synced);
                vaccineObj.setFormSubmissionId(vaccine.getEvent().getFormSubmissionId());
                vaccineObj.setEventId(vaccine.getEvent().getEventId());
                vaccineObj.setOutOfCatchment(outOfCatchment ? 1 : 0);

                String createdAtString = contentValues.getAsString(VaccineRepository.CREATED_AT);
                Date createdAt = getDate(createdAtString);
                vaccineObj.setCreatedAt(createdAt);

                Utils.addVaccine(vaccineRepository, vaccineObj);

                Timber.d("Ending processVaccine table: %s", vaccineTable.name);
            }
            return true;

        } catch (Exception e) {
            Timber.e(e, "Process Vaccine Error");
            return null;
        }
    }

    private Boolean processWeight(EventClient weight, Table weightTable, boolean outOfCatchment) throws Exception {

        try {

            if (weight == null || weight.getEvent() == null) {
                return false;
            }

            if (weightTable == null) {
                return false;
            }

            Timber.d("Starting processWeight table: %s", weightTable.name);

            ContentValues contentValues = processCaseModel(weight, weightTable);

            // save the values to db
            if (contentValues != null && contentValues.size() > 0) {
                String eventDateStr = contentValues.getAsString(WeightRepository.DATE);
                Date date = getDate(eventDateStr);

                WeightRepository weightRepository = OndoganciApplication.getInstance().weightRepository();
                Weight weightObj = new Weight();
                weightObj.setBaseEntityId(contentValues.getAsString(WeightRepository.BASE_ENTITY_ID));
                if (contentValues.containsKey(WeightRepository.KG)) {
                    weightObj.setKg(parseFloat(contentValues.getAsString(WeightRepository.KG)));
                }
                weightObj.setDate(date);
                weightObj.setAnmId(contentValues.getAsString(WeightRepository.ANMID));
                weightObj.setLocationId(contentValues.getAsString(WeightRepository.LOCATIONID));
                weightObj.setSyncStatus(WeightRepository.TYPE_Synced);
                weightObj.setFormSubmissionId(weight.getEvent().getFormSubmissionId());
                weightObj.setEventId(weight.getEvent().getEventId());
                weightObj.setOutOfCatchment(outOfCatchment ? 1 : 0);

                if (contentValues.containsKey(WeightRepository.Z_SCORE)) {
                    String zscoreString = contentValues.getAsString(WeightRepository.Z_SCORE);
                    if (NumberUtils.isNumber(zscoreString)) {
                        weightObj.setZScore(Double.valueOf(zscoreString));
                    }
                }

                String createdAtString = contentValues.getAsString(WeightRepository.CREATED_AT);
                Date createdAt = getDate(createdAtString);
                weightObj.setCreatedAt(createdAt);

                weightRepository.add(weightObj);

                Timber.d("Ending processWeight table: %s", weightTable.name);
            }
            return true;

        } catch (Exception e) {
            Timber.e(e, "Process Weight Error");
            return null;
        }
    }

    private Boolean processHeight(@Nullable EventClient height, @Nullable Table heightTable, boolean outOfCatchment) {

        try {

            if (height == null || height.getEvent() == null) {
                return false;
            }

            if (heightTable == null) {
                return false;
            }

            Timber.d("Starting processWeight table: %s", heightTable.name);

            ContentValues contentValues = processCaseModel(height, heightTable);

            // save the values to db
            if (contentValues != null && contentValues.size() > 0) {
                String eventDateStr = contentValues.getAsString(HeightRepository.DATE);
                Date date = getDate(eventDateStr);

                HeightRepository heightRepository = OndoganciApplication.getInstance().heightRepository();
                Height heightObject = new Height();
                heightObject.setBaseEntityId(contentValues.getAsString(WeightRepository.BASE_ENTITY_ID));
                if (contentValues.containsKey(HeightRepository.CM)) {
                    heightObject.setCm(parseFloat(contentValues.getAsString(HeightRepository.CM)));
                }
                heightObject.setDate(date);
                heightObject.setAnmId(contentValues.getAsString(HeightRepository.ANMID));
                heightObject.setLocationId(contentValues.getAsString(HeightRepository.LOCATIONID));
                heightObject.setSyncStatus(HeightRepository.TYPE_Synced);
                heightObject.setFormSubmissionId(height.getEvent().getFormSubmissionId());
                heightObject.setEventId(height.getEvent().getEventId());
                heightObject.setOutOfCatchment(outOfCatchment ? 1 : 0);

                if (contentValues.containsKey(HeightRepository.Z_SCORE)) {
                    String zScoreString = contentValues.getAsString(HeightRepository.Z_SCORE);
                    if (NumberUtils.isNumber(zScoreString)) {
                        heightObject.setZScore(Double.valueOf(zScoreString));
                    }
                }

                String createdAtString = contentValues.getAsString(HeightRepository.CREATED_AT);
                Date createdAt = getDate(createdAtString);
                heightObject.setCreatedAt(createdAt);

                heightRepository.add(heightObject);

                Timber.d("Ending processWeight table: %s", heightTable.name);
            }
            return true;

        } catch (Exception e) {
            Timber.e(e, "Process Height Error");
            return null;
        }
    }

    private Boolean processHead(@Nullable EventClient head, @Nullable Table headTable, boolean outOfCatchment) {

        try {

            if (head == null || head.getEvent() == null) {
                return false;
            }

            if (headTable == null) {
                return false;
            }

            Timber.d("Starting process Head circumference table: %s", headTable.name);

            ContentValues contentValues = processCaseModel(head, headTable);

            // save the values to db
            if (contentValues != null && contentValues.size() > 0) {
                String eventDateStr = contentValues.getAsString(HeadRepository.DATE);
                Date date = getDate(eventDateStr);

                HeadRepository headRepository = OndoganciApplication.getInstance().headRepository();
                Head headObject = new Head();
                headObject.setBaseEntityId(contentValues.getAsString(WeightRepository.BASE_ENTITY_ID));
                if (contentValues.containsKey(HeadRepository.INCH)) {
                    headObject.setInch(parseFloat(contentValues.getAsString(HeadRepository.INCH)));
                }
                headObject.setDate(date);
                headObject.setAnmId(contentValues.getAsString(HeadRepository.ANMID));
                headObject.setLocationId(contentValues.getAsString(HeadRepository.LOCATIONID));
                headObject.setSyncStatus(HeadRepository.TYPE_Synced);
                headObject.setFormSubmissionId(head.getEvent().getFormSubmissionId());
                headObject.setEventId(head.getEvent().getEventId());
                headObject.setOutOfCatchment(outOfCatchment ? 1 : 0);

                if (contentValues.containsKey(HeadRepository.Z_SCORE)) {
                    String zScoreString = contentValues.getAsString(HeadRepository.Z_SCORE);
                    if (NumberUtils.isNumber(zScoreString)) {
                        headObject.setZScore(Double.valueOf(zScoreString));
                    }
                }

                String createdAtString = contentValues.getAsString(HeadRepository.CREATED_AT);
                Date createdAt = getDate(createdAtString);
                headObject.setCreatedAt(createdAt);

                headRepository.add(headObject);

                Timber.d("Ending process Head circumference table: %s", headTable.name);
            }
            return true;

        } catch (Exception e) {
            Timber.e(e, "Process Head circumference Error");
            return null;
        }
    }

    private Boolean processService(EventClient service, Table serviceTable) {
        try {
            if (service == null || service.getEvent() == null) {
                return false;
            }

            if (serviceTable == null) {
                return false;
            }

            Timber.d("Starting processService table: %s", serviceTable.name);

            ContentValues contentValues = processCaseModel(service, serviceTable);

            // save the values to db
            if (contentValues != null && contentValues.size() > 0) {
                String name = getServiceTypeName(contentValues);

                String eventDateStr = contentValues.getAsString(RecurringServiceRecordRepository.DATE);
                Date date = getDate(eventDateStr);

                String value = null;

                if (StringUtils.containsIgnoreCase(name, "ITN")) {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    String itnDateString = contentValues.getAsString("itn_date");
                    if (StringUtils.isNotBlank(itnDateString)) {
                        date = simpleDateFormat.parse(itnDateString);
                    }

                    value = getServiceValue(contentValues);

                }

                List<ServiceType> serviceTypeList = getServiceTypes(name);
                if (serviceTypeList == null || serviceTypeList.isEmpty()) {
                    return false;
                }

                if (date == null) {
                    return false;
                }

                recordServiceRecord(service, contentValues, name, date, value, serviceTypeList);

                Timber.d("Ending processService table: %s", serviceTable.name);
            }
            return true;

        } catch (Exception e) {
            Timber.e(e, "Process Service Error");
            return null;
        }
    }

    @NotNull
    private String getServiceValue(ContentValues contentValues) {
        String value;
        value = RecurringIntentService.ITN_PROVIDED;
        if (contentValues.getAsString("itn_has_net") != null) {
            value = RecurringIntentService.CHILD_HAS_NET;
        }
        return value;
    }

    @Nullable
    private String getServiceTypeName(ContentValues contentValues) {
        String name = contentValues.getAsString(RecurringServiceTypeRepository.NAME);
        if (StringUtils.isNotBlank(name)) {
            name = name.replaceAll("_", " ").replace("dose", "").trim();
        }
        return name;
    }

    private void recordServiceRecord(EventClient service, ContentValues contentValues, String name, Date date, String value, List<ServiceType> serviceTypeList) {
        RecurringServiceRecordRepository recurringServiceRecordRepository = OndoganciApplication.getInstance()
                .recurringServiceRecordRepository();
        ServiceRecord serviceObj = getServiceRecord(service, contentValues, name, date, value, serviceTypeList);
        String createdAtString = contentValues.getAsString(RecurringServiceRecordRepository.CREATED_AT);
        Date createdAt = getDate(createdAtString);
        serviceObj.setCreatedAt(createdAt);

        recurringServiceRecordRepository.add(serviceObj);
    }

    private List<ServiceType> getServiceTypes(String name) {
        RecurringServiceTypeRepository recurringServiceTypeRepository = OndoganciApplication.getInstance()
                .recurringServiceTypeRepository();
        return recurringServiceTypeRepository.searchByName(name);
    }

    private void processBCGScarEvent(EventClient bcgScarEventClient) {
        if (bcgScarEventClient == null || bcgScarEventClient.getEvent() == null) {
            return;
        }

        Event event = bcgScarEventClient.getEvent();
        String baseEntityId = event.getBaseEntityId();
        DateTime eventDate = event.getEventDate();
        long date = 0;
        if (eventDate != null) {
            date = eventDate.getMillis();
        }

        ChildDbUtils.updateChildDetailsValue(Constants.SHOW_BCG_SCAR, String.valueOf(date), baseEntityId);
    }

    private boolean unSync(List<Event> events) {
        try {

            if (events == null || events.isEmpty()) {
                return false;
            }

            ClientField clientField = assetJsonToJava("ec_client_fields.json", ClientField.class);
            return clientField != null;

        } catch (Exception e) {
            Timber.e(e);
        }

        return false;
    }

    @VisibleForTesting
    ContentValues processCaseModel(EventClient eventClient, Table table) {
        try {
            List<Column> columns = table.columns;
            ContentValues contentValues = new ContentValues();

            for (Column column : columns) {
                processCaseModel(eventClient.getEvent(), eventClient.getClient(), column, contentValues);
            }

            return contentValues;
        } catch (Exception e) {
            Timber.e(e);
        }
        return null;
    }

    private Integer parseInt(String string) {
        try {
            return Integer.valueOf(string);
        } catch (NumberFormatException e) {
            Timber.e(e, e.toString());
        }
        return null;
    }

    @Nullable
    private Date getDate(@Nullable String eventDateStr) {
        Date date = null;
        if (StringUtils.isNotBlank(eventDateStr)) {
            try {
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ");
                date = dateFormat.parse(eventDateStr);
            } catch (ParseException e) {
                try {
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                    date = dateFormat.parse(eventDateStr);
                } catch (ParseException pe) {
                    try {
                        date = DateUtil.parseDate(eventDateStr);
                    } catch (ParseException pee) {
                        Timber.e(e);
                    }
                }
            }
        }
        return date;
    }

    private Float parseFloat(String string) {
        try {
            return Float.valueOf(string);
        } catch (NumberFormatException e) {
            Timber.e(e);
        }
        return null;
    }

    @NotNull
    private ServiceRecord getServiceRecord(EventClient service, ContentValues contentValues, String name, Date date,
                                           String value, List<ServiceType> serviceTypeList) {
        ServiceRecord serviceObj = new ServiceRecord();
        serviceObj.setBaseEntityId(contentValues.getAsString(RecurringServiceRecordRepository.BASE_ENTITY_ID));
        serviceObj.setName(name);
        serviceObj.setDate(date);
        serviceObj.setAnmId(contentValues.getAsString(RecurringServiceRecordRepository.ANMID));
        serviceObj.setLocationId(contentValues.getAsString(RecurringServiceRecordRepository.LOCATION_ID));
        serviceObj.setSyncStatus(RecurringServiceRecordRepository.TYPE_Synced);
        serviceObj.setFormSubmissionId(service.getEvent().getFormSubmissionId());
        serviceObj.setEventId(service.getEvent().getEventId()); //FIXME hard coded id
        serviceObj.setValue(value);
        serviceObj.setRecurringServiceId(serviceTypeList.get(0).getId());
        return serviceObj;
    }

    @Override
    public void updateFTSsearch(String tableName, String entityId, ContentValues contentValues) {

        Timber.d("Starting updateFTSsearch table: %s", tableName);

        AllCommonsRepository allCommonsRepository = OndoganciApplication.getInstance().context().
                allCommonsRepositoryobjects(tableName);

        if (allCommonsRepository != null) {
            allCommonsRepository.updateSearch(entityId);
        }

        // Todo: Disable this in favour of the vaccine post-processing at the end :shrug: Might not be the best for real-time updates to the register
        if (contentValues != null && AppConstants.TABLE_NAME.ALL_CLIENTS.equals(tableName)) {
            String dobString = contentValues.getAsString(Constants.KEY.DOB);
            // TODO: Fix this to use the ec_child_details table & fetch the birthDateTime from the ec_client table
            if (StringUtils.isNotBlank(dobString)) {
                DateTime birthDateTime = Utils.dobStringToDateTime(dobString);
                if (birthDateTime != null) {
                    VaccineSchedule.updateOfflineAlerts(entityId, birthDateTime, "child");
                    ServiceSchedule.updateOfflineAlerts(entityId, birthDateTime);
                }
            }
        }

        Timber.d("Finished updateFTSsearch table: %s", tableName);
    }

    @Override
    public String[] getOpenmrsGenIds() {
        return new String[]{"zeir_id"};
    }

    private void scheduleUpdatingClientAlerts(@NonNull String baseEntityId, @NonNull DateTime dateTime) {
        if (!clientsForAlertUpdates.containsKey(baseEntityId)) {
            clientsForAlertUpdates.put(baseEntityId, dateTime);
        }
    }
}