package org.smartregister.ondoganci.application;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.annotation.VisibleForTesting;
import android.support.v7.app.AppCompatDelegate;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.evernote.android.job.JobManager;

import org.jetbrains.annotations.NotNull;
import org.smartregister.Context;
import org.smartregister.CoreLibrary;
import org.smartregister.child.ChildLibrary;
import org.smartregister.child.domain.ChildMetadata;
import org.smartregister.child.util.ChildAppProperties;
import org.smartregister.child.util.DBConstants;
import org.smartregister.commonregistry.CommonFtsObject;
import org.smartregister.configurableviews.ConfigurableViewsLibrary;
import org.smartregister.configurableviews.helper.JsonSpecHelper;
import org.smartregister.growthmonitoring.GrowthMonitoringConfig;
import org.smartregister.growthmonitoring.GrowthMonitoringLibrary;
import org.smartregister.growthmonitoring.repository.HeadRepository;
import org.smartregister.growthmonitoring.repository.HeadZScoreRepository;
import org.smartregister.growthmonitoring.repository.HeightRepository;
import org.smartregister.growthmonitoring.repository.HeightZScoreRepository;
import org.smartregister.growthmonitoring.repository.WeightRepository;
import org.smartregister.growthmonitoring.repository.WeightZScoreRepository;
import org.smartregister.immunization.ImmunizationLibrary;
import org.smartregister.immunization.db.VaccineRepo;
import org.smartregister.immunization.domain.VaccineSchedule;
import org.smartregister.immunization.domain.jsonmapping.Vaccine;
import org.smartregister.immunization.domain.jsonmapping.VaccineGroup;
import org.smartregister.immunization.repository.RecurringServiceRecordRepository;
import org.smartregister.immunization.repository.RecurringServiceTypeRepository;
import org.smartregister.immunization.repository.VaccineRepository;
import org.smartregister.immunization.util.VaccinateActionUtils;
import org.smartregister.immunization.util.VaccinatorUtils;
import org.smartregister.location.helper.LocationHelper;
import org.smartregister.ondoganci.receiver.CoverageDropoutBroadcastReceiver;
import org.smartregister.ondoganci.receiver.Hia2ServiceBroadcastReceiver;
import org.smartregister.ondoganci.receiver.VaccinatorAlarmReceiver;
import org.smartregister.ondoganci.repository.CohortIndicatorRepository;
import org.smartregister.ondoganci.repository.CohortPatientRepository;
import org.smartregister.ondoganci.repository.CohortRepository;
import org.smartregister.ondoganci.repository.CumulativeIndicatorRepository;
import org.smartregister.ondoganci.repository.CumulativePatientRepository;
import org.smartregister.ondoganci.repository.CumulativeRepository;
import org.smartregister.ondoganci.repository.StockHelperRepository;
import org.smartregister.ondoganci.repository.UniqueIdRepository;
import org.smartregister.ondoganci.util.SaveSharedPreference;
import org.smartregister.receiver.SyncStatusBroadcastReceiver;
import org.smartregister.reporting.ReportingLibrary;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.repository.Hia2ReportRepository;
import org.smartregister.repository.Repository;
import org.smartregister.stock.StockLibrary;
import org.smartregister.stock.repository.StockRepository;
import org.smartregister.sync.ClientProcessorForJava;
import org.smartregister.sync.DrishtiSyncScheduler;
import org.smartregister.sync.helper.ECSyncHelper;
import org.smartregister.ondoganci.BuildConfig;
import org.smartregister.ondoganci.activity.ChildFormActivity;
import org.smartregister.ondoganci.activity.ChildImmunizationActivity;
import org.smartregister.ondoganci.activity.ChildProfileActivity;
import org.smartregister.ondoganci.activity.ChildRegisterActivity;
import org.smartregister.ondoganci.activity.LoginActivity;
import org.smartregister.ondoganci.job.AppJobCreator;
import org.smartregister.ondoganci.processor.AppClientProcessorForJava;
import org.smartregister.ondoganci.processor.TripleResultProcessor;
import org.smartregister.ondoganci.repository.AppChildRegisterQueryProvider;
import org.smartregister.ondoganci.repository.ChildAlertUpdatedRepository;
import org.smartregister.ondoganci.repository.ClientRegisterTypeRepository;
import org.smartregister.ondoganci.repository.DailyTalliesRepository;
import org.smartregister.ondoganci.repository.HIA2IndicatorsRepository;
import org.smartregister.ondoganci.repository.MonthlyTalliesRepository;
import org.smartregister.ondoganci.repository.OndoganciRepository;
import org.smartregister.ondoganci.util.AppConstants;
import org.smartregister.ondoganci.util.AppUtils;
import org.smartregister.ondoganci.util.VaccineDuplicate;
import org.smartregister.view.activity.DrishtiApplication;
import org.smartregister.view.receiver.TimeChangedBroadcastReceiver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

public class OndoganciApplication extends DrishtiApplication implements TimeChangedBroadcastReceiver.OnTimeChangedListener {

    private static CommonFtsObject commonFtsObject;
    private static JsonSpecHelper jsonSpecHelper;

    private EventClientRepository eventClientRepository;
    private HIA2IndicatorsRepository hia2IndicatorsRepository;
    private DailyTalliesRepository dailyTalliesRepository;
    private MonthlyTalliesRepository monthlyTalliesRepository;
    private Hia2ReportRepository hia2ReportRepository;
    private StockRepository stockRepository;
    private CohortRepository cohortRepository;
    private CohortIndicatorRepository cohortIndicatorRepository;
    private CohortPatientRepository cohortPatientRepository;
    private CumulativeRepository cumulativeRepository;
    private CumulativeIndicatorRepository cumulativeIndicatorRepository;
    private CumulativePatientRepository cumulativePatientRepository;
    private UniqueIdRepository uniqueIdRepository;

    private String password;
    private boolean lastModified;
    private ECSyncHelper ecSyncHelper;

    private ClientRegisterTypeRepository registerTypeRepository;
    private ChildAlertUpdatedRepository childAlertUpdatedRepository;
    private static List<VaccineGroup> vaccineGroups;

    public static JsonSpecHelper getJsonSpecHelper() {
        return jsonSpecHelper;
    }

    public static CommonFtsObject createCommonFtsObject(android.content.Context context) {
        if (commonFtsObject == null) {
            commonFtsObject = new CommonFtsObject(getFtsTables());
            for (String ftsTable : commonFtsObject.getTables()) {
                commonFtsObject.updateSearchFields(ftsTable, getFtsSearchFields(ftsTable));
                commonFtsObject.updateSortFields(ftsTable, getFtsSortFields(ftsTable, context));
            }
        }
        commonFtsObject.updateAlertScheduleMap(getAlertScheduleMap(context));

        return commonFtsObject;
    }

    private static String[] getFtsTables() {
        return new String[]{DBConstants.RegisterTable.CLIENT, DBConstants.RegisterTable.CHILD_DETAILS};
    }

    private static String[] getFtsSearchFields(String tableName) {
        if (tableName.equalsIgnoreCase(DBConstants.RegisterTable.CLIENT)) {
            return new String[]{
                    DBConstants.KEY.ZEIR_ID,
                    DBConstants.KEY.FIRST_NAME,
                    DBConstants.KEY.LAST_NAME
            };
        } else if (tableName.equalsIgnoreCase(DBConstants.RegisterTable.CHILD_DETAILS)) {
            return new String[]{DBConstants.KEY.LOST_TO_FOLLOW_UP, DBConstants.KEY.INACTIVE};
        }
        return null;
    }

    private static String[] getFtsSortFields(String tableName, android.content.Context context) {
        if (tableName.equals(AppConstants.TABLE_NAME.ALL_CLIENTS)) {
            List<String> names = new ArrayList<>();
            names.add(AppConstants.KEY.FIRST_NAME);
            names.add(AppConstants.KEY.LAST_NAME);
            names.add(AppConstants.KEY.DOB);
            names.add(AppConstants.KEY.ZEIR_ID);
            names.add(AppConstants.KEY.LAST_INTERACTED_WITH);
            names.add(AppConstants.KEY.DOD);
            names.add(AppConstants.KEY.DATE_REMOVED);
            return names.toArray(new String[0]);
        } else if (tableName.equals(DBConstants.RegisterTable.CHILD_DETAILS)) {
            List<VaccineGroup> vaccineList = VaccinatorUtils.getVaccineGroupsFromVaccineConfigFile(context, VaccinatorUtils.vaccines_file);
            List<String> names = new ArrayList<>();
            names.add(DBConstants.KEY.INACTIVE);
            names.add(DBConstants.KEY.RELATIONAL_ID);
            names.add(DBConstants.KEY.LOST_TO_FOLLOW_UP);

            for (VaccineGroup vaccineGroup : vaccineList) {
                populateAlertColumnNames(vaccineGroup.vaccines, names);
            }

            return names.toArray(new String[0]);

        }
        return null;
    }

    private static void populateAlertColumnNames(List<Vaccine> vaccines, List<String> names) {

        for (Vaccine vaccine : vaccines)
            if (vaccine.getVaccineSeparator() != null && vaccine.getName().contains(vaccine.getVaccineSeparator().trim())) {
                String[] individualVaccines = vaccine.getName().split(vaccine.getVaccineSeparator().trim());

                List<Vaccine> vaccineList = new ArrayList<>();
                for (String individualVaccine : individualVaccines) {
                    Vaccine vaccineClone = new Vaccine();
                    vaccineClone.setName(individualVaccine.trim());
                    vaccineList.add(vaccineClone);

                }
                populateAlertColumnNames(vaccineList, names);


            } else {

                names.add("alerts." + VaccinateActionUtils.addHyphen(vaccine.getName()));
            }
    }


    private static void populateAlertScheduleMap(List<Vaccine> vaccines, Map<String, Pair<String, Boolean>> map) {

        for (Vaccine vaccine : vaccines)
            if (vaccine.getVaccineSeparator() != null && vaccine.getName().contains(vaccine.getVaccineSeparator().trim())) {
                String[] individualVaccines = vaccine.getName().split(vaccine.getVaccineSeparator().trim());

                List<Vaccine> vaccineList = new ArrayList<>();
                for (String individualVaccine : individualVaccines) {
                    Vaccine vaccineClone = new Vaccine();
                    vaccineClone.setName(individualVaccine.trim());
                    vaccineList.add(vaccineClone);

                }
                populateAlertScheduleMap(vaccineList, map);


            } else {

                // TODO: This needs to be fixed because it is a configuration & not a hardcoded string
                map.put(vaccine.name, Pair.create(DBConstants.RegisterTable.CHILD_DETAILS, false));
            }
    }

    private static Map<String, Pair<String, Boolean>> getAlertScheduleMap(android.content.Context context) {
        List<VaccineGroup> vaccines = getVaccineGroups(context);

        Map<String, Pair<String, Boolean>> map = new HashMap<>();

        for (VaccineGroup vaccineGroup : vaccines) {
            populateAlertScheduleMap(vaccineGroup.vaccines, map);
        }
        return map;
    }

    public static synchronized OndoganciApplication getInstance() {
        return (OndoganciApplication) mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        context = Context.getInstance();

        String lang = AppUtils.getLanguage(getApplicationContext());
        Locale locale = new Locale(lang);
        Resources res = getApplicationContext().getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = locale;
        res.updateConfiguration(conf, dm);

        context.updateApplicationContext(getApplicationContext());
        context.updateCommonFtsObject(createCommonFtsObject(context.applicationContext()));

        //Initialize Modules
        CoreLibrary.init(context, new AppSyncConfiguration(), BuildConfig.BUILD_TIMESTAMP);

        GrowthMonitoringConfig growthMonitoringConfig = new GrowthMonitoringConfig();
        growthMonitoringConfig.setWeightForHeightZScoreFile("weight_for_height.csv");
        GrowthMonitoringLibrary.init(context, getRepository(), BuildConfig.VERSION_CODE, BuildConfig.DATABASE_VERSION, growthMonitoringConfig);
        GrowthMonitoringLibrary.getInstance().setGrowthMonitoringSyncTime(3, TimeUnit.MINUTES);

        ImmunizationLibrary.init(context, getRepository(), createCommonFtsObject(context.applicationContext()),
                BuildConfig.VERSION_CODE, BuildConfig.DATABASE_VERSION);
        ImmunizationLibrary.getInstance().setVaccineSyncTime(3, TimeUnit.MINUTES);
        ImmunizationLibrary.getInstance().getConditionalVaccinesMap().put(AppConstants.ConditionalVaccines.PRETERM_VACCINES, "preterm_vaccines.json");
        fixHardcodedVaccineConfiguration();

        ConfigurableViewsLibrary.init(context);

        ChildLibrary.init(context, getRepository(), getMetadata(), BuildConfig.VERSION_CODE, BuildConfig.DATABASE_VERSION);
        ChildLibrary.getInstance().setApplicationVersionName(BuildConfig.VERSION_NAME);
        ChildLibrary.getInstance().setClientProcessorForJava(getClientProcessor());
        ChildLibrary.getInstance().getProperties().setProperty(ChildAppProperties.KEY.FEATURE_SCAN_QR_ENABLED, "true");

        ReportingLibrary.init(context, getRepository(), null, BuildConfig.VERSION_CODE, BuildConfig.DATABASE_VERSION);
        ReportingLibrary.getInstance().addMultiResultProcessor(new TripleResultProcessor());

        Fabric.with(this, new Crashlytics.Builder().core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build()).build());

        initRepositories();

        Hia2ServiceBroadcastReceiver.init(this);
        SyncStatusBroadcastReceiver.init(this);
        CoverageDropoutBroadcastReceiver.init(this);
        TimeChangedBroadcastReceiver.init(this);
        TimeChangedBroadcastReceiver.getInstance().addOnTimeChangedListener(this);

        SyncStatusBroadcastReceiver.init(this);
        LocationHelper.init(new ArrayList<>(Arrays.asList(BuildConfig.ALLOWED_LEVELS)), BuildConfig.DEFAULT_LOCATION);
        jsonSpecHelper = new JsonSpecHelper(this);

        StockLibrary.init(context, getRepository(), new StockHelperRepository());
        //init Job Manager
        JobManager.create(this).addJobCreator(new AppJobCreator());
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

    }

    private ChildMetadata getMetadata() {
        ChildMetadata metadata = new ChildMetadata(ChildFormActivity.class, ChildProfileActivity.class,
                ChildImmunizationActivity.class, ChildRegisterActivity.class, true, new AppChildRegisterQueryProvider());
        metadata.updateChildRegister(AppConstants.JSON_FORM.CHILD_ENROLLMENT, AppConstants.TABLE_NAME.ALL_CLIENTS,
                AppConstants.TABLE_NAME.ALL_CLIENTS, AppConstants.EventType.CHILD_REGISTRATION,
                AppConstants.EventType.UPDATE_CHILD_REGISTRATION, AppConstants.EventType.OUT_OF_CATCHMENT, AppConstants.CONFIGURATION.CHILD_REGISTER,
                AppConstants.RELATIONSHIP.MOTHER, AppConstants.JSON_FORM.OUT_OF_CATCHMENT_SERVICE);
        metadata.setupFatherRelation(AppConstants.TABLE_NAME.ALL_CLIENTS, AppConstants.RELATIONSHIP.FATHER);
        //TODO include this metadata.setFieldsWithLocationHierarchy(new HashSet<>(Collections.singletonList(AppConstants.KEY.HOME_ADDRESS)));
        metadata.setFieldsWithLocationHierarchy(new HashSet<>(Arrays.asList("Home_Facility", "Birth_Facility_Name", "Residential_Area")));
        metadata.setLocationLevels(AppUtils.getLocationLevels());
        metadata.setHealthFacilityLevels(AppUtils.getHealthFacilityLevels());
        return metadata;
    }

    private void initRepositories() {
        weightRepository();
        heightRepository();
        headRepository();
        vaccineRepository();
        weightZScoreRepository();
        heightZScoreRepository();
        headZScoreRepository();
    }

    public void initOfflineSchedules() {
        try {
            List<VaccineGroup> childVaccines = VaccinatorUtils.getSupportedVaccines(this);
            List<Vaccine> specialVaccines = VaccinatorUtils.getSpecialVaccines(this);
            VaccineSchedule.init(childVaccines, specialVaccines, AppConstants.KEY.CHILD);
        } catch (Exception e) {
            Timber.e(e, "OndoganciApplication --> initOfflineSchedules");
        }
    }

    public WeightRepository weightRepository() {
        return GrowthMonitoringLibrary.getInstance().weightRepository();
    }

    public HeightRepository heightRepository() {
        return GrowthMonitoringLibrary.getInstance().heightRepository();
    }

    public HeadRepository headRepository() {
        return GrowthMonitoringLibrary.getInstance().headRepository();
    }

    public VaccineRepository vaccineRepository() {
        return ImmunizationLibrary.getInstance().vaccineRepository();
    }

    public WeightZScoreRepository weightZScoreRepository() {
        return GrowthMonitoringLibrary.getInstance().weightZScoreRepository();
    }

    public HeightZScoreRepository heightZScoreRepository() {
        return GrowthMonitoringLibrary.getInstance().heightZScoreRepository();
    }

    public HeadZScoreRepository headZScoreRepository() {
        return GrowthMonitoringLibrary.getInstance().headZScoreRepository();
    }

    @Override
    public void logoutCurrentUser() {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        getApplicationContext().startActivity(intent);
        context.userService().logoutSession();

        SaveSharedPreference.setLoggedIn(getApplicationContext(), false);
        SaveSharedPreference.setUsername(getApplicationContext(), "");
        SaveSharedPreference.setPassword(getApplicationContext(), "");

        Log.d( "Login Cleared", "done!!!"
                + " username: "+SaveSharedPreference.getUsername(getApplicationContext())
                +" password: "+SaveSharedPreference.getPassword(getApplicationContext())
                +" Login Status: "+SaveSharedPreference.getLoggedStatus(getApplicationContext()));
    }

    @Override
    public Repository getRepository() {
        try {
            if (repository == null) {
                repository = new OndoganciRepository(getInstance().getApplicationContext(), context);
                stockRepository();
            }
        } catch (UnsatisfiedLinkError e) {
            Timber.e(e, "OndoganciApplication --> getRepository");
        }
        return repository;
    }

    public String getPassword() {
        if (password == null) {
            String username = getContext().userService().getAllSharedPreferences().fetchRegisteredANM();
            password = getContext().userService().getGroupId(username);
        }
        return password;
    }

    public Context getContext() {
        return context;
    }

    @NotNull
    @Override
    public ClientProcessorForJava getClientProcessor() {
        return AppClientProcessorForJava.getInstance(this);
    }

    @Override
    public void onTerminate() {
        Timber.i("Application is terminating. Stopping sync scheduler and resetting isSyncInProgress setting.");
        cleanUpSyncState();
        TimeChangedBroadcastReceiver.destroy(this);
        SyncStatusBroadcastReceiver.destroy(this);
        super.onTerminate();
    }

    protected void cleanUpSyncState() {
        try {
            DrishtiSyncScheduler.stop(getApplicationContext());
            context.allSharedPreferences().saveIsSyncInProgress(false);
        } catch (Exception e) {
            Timber.e(e, "OndoganciApplication --> cleanUpSyncState");
        }
    }

    @Override
    public void onTimeChanged() {
//        context.userService().forceRemoteLogin();
//        logoutCurrentUser();
    }

    @Override
    public void onTimeZoneChanged() {
//        context.userService().forceRemoteLogin();
//        logoutCurrentUser();
    }

    public Context context() {
        return context;
    }

    public EventClientRepository eventClientRepository() {
        if (eventClientRepository == null) {
            eventClientRepository = new EventClientRepository();
        }
        return eventClientRepository;
    }

    public RecurringServiceTypeRepository recurringServiceTypeRepository() {
        return ImmunizationLibrary.getInstance().recurringServiceTypeRepository();
    }

    public RecurringServiceRecordRepository recurringServiceRecordRepository() {
        return ImmunizationLibrary.getInstance().recurringServiceRecordRepository();
    }

    public boolean isLastModified() {
        return lastModified;
    }

    public void setLastModified(boolean lastModified) {
        this.lastModified = lastModified;
    }

    public ECSyncHelper getEcSyncHelper() {
        if (ecSyncHelper == null) {
            ecSyncHelper = ECSyncHelper.getInstance(getApplicationContext());
        }
        return ecSyncHelper;
    }

    @VisibleForTesting
    protected void fixHardcodedVaccineConfiguration() {
        VaccineRepo.Vaccine[] vaccines = ImmunizationLibrary.getInstance().getVaccines();

        HashMap<String, VaccineDuplicate> replacementVaccines = new HashMap<>();
        replacementVaccines.put("BCG 2", new VaccineDuplicate("BCG 2", VaccineRepo.Vaccine.bcg, 1825, 0, 15, "child"));


        for (VaccineRepo.Vaccine vaccine : vaccines) {
            if (replacementVaccines.containsKey(vaccine.display())) {
                VaccineDuplicate vaccineDuplicate = replacementVaccines.get(vaccine.display());
                vaccine.setCategory(vaccineDuplicate.category());
                vaccine.setExpiryDays(vaccineDuplicate.expiryDays());
                vaccine.setMilestoneGapDays(vaccineDuplicate.milestoneGapDays());
                vaccine.setPrerequisite(vaccineDuplicate.prerequisite());
                vaccine.setPrerequisiteGapDays(vaccineDuplicate.prerequisiteGapDays());
            }
        }

        ImmunizationLibrary.getInstance().setVaccines(vaccines);
    }

    public DailyTalliesRepository dailyTalliesRepository() {
        if (dailyTalliesRepository == null) {
            dailyTalliesRepository = new DailyTalliesRepository();
        }
        return dailyTalliesRepository;
    }

    public HIA2IndicatorsRepository hIA2IndicatorsRepository() {
        if (hia2IndicatorsRepository == null) {
            hia2IndicatorsRepository = new HIA2IndicatorsRepository();
        }
        return hia2IndicatorsRepository;
    }

    public MonthlyTalliesRepository monthlyTalliesRepository() {
        if (monthlyTalliesRepository == null) {
            monthlyTalliesRepository = new MonthlyTalliesRepository();
        }

        return monthlyTalliesRepository;
    }

    public Hia2ReportRepository hia2ReportRepository() {
        if (hia2ReportRepository == null) {
            hia2ReportRepository = new Hia2ReportRepository();
        }
        return hia2ReportRepository;
    }

    public StockRepository stockRepository() {
        if (stockRepository == null) {
            stockRepository = new StockRepository();
        }
        return stockRepository;
    }

    public CohortRepository cohortRepository() {
        if (cohortRepository == null) {
            cohortRepository = new CohortRepository();
        }
        return cohortRepository;
    }

    public CohortIndicatorRepository cohortIndicatorRepository() {
        if (cohortIndicatorRepository == null) {
            cohortIndicatorRepository = new CohortIndicatorRepository();
        }
        return cohortIndicatorRepository;
    }

    public CohortPatientRepository cohortPatientRepository() {
        if (cohortPatientRepository == null) {
            cohortPatientRepository = new CohortPatientRepository();
        }
        return cohortPatientRepository;
    }

    public CumulativeRepository cumulativeRepository() {
        if (cumulativeRepository == null) {
            cumulativeRepository = new CumulativeRepository();
        }
        return cumulativeRepository;
    }

    public CumulativeIndicatorRepository cumulativeIndicatorRepository() {
        if (cumulativeIndicatorRepository == null) {
            cumulativeIndicatorRepository = new CumulativeIndicatorRepository();
        }
        return cumulativeIndicatorRepository;
    }

    public CumulativePatientRepository cumulativePatientRepository() {
        if (cumulativePatientRepository == null) {
            cumulativePatientRepository = new CumulativePatientRepository();
        }
        return cumulativePatientRepository;
    }


    public UniqueIdRepository uniqueIdRepository() {
        if (uniqueIdRepository == null) {
            uniqueIdRepository = new UniqueIdRepository();
        }
        return uniqueIdRepository;
    }

    public static List<VaccineGroup> getVaccineGroups(android.content.Context context) {
        if (vaccineGroups == null) {

            vaccineGroups = VaccinatorUtils.getVaccineGroupsFromVaccineConfigFile(context, VaccinatorUtils.vaccines_file);
        }

        return vaccineGroups;
    }

    @VisibleForTesting
    public void setVaccineGroups(List<VaccineGroup> vaccines) {
        this.vaccineGroups = vaccines;
    }

    public ClientRegisterTypeRepository registerTypeRepository() {
        if (registerTypeRepository == null) {
            this.registerTypeRepository = new ClientRegisterTypeRepository();
        }
        return this.registerTypeRepository;
    }

    public ChildAlertUpdatedRepository alertUpdatedRepository() {
        if (childAlertUpdatedRepository == null) {
            this.childAlertUpdatedRepository = new ChildAlertUpdatedRepository();
        }
        return this.childAlertUpdatedRepository;
    }

    public static void setAlarms(android.content.Context context) {
        VaccinatorAlarmReceiver.setAlarm(context, BuildConfig.GROWTH_MONITORING_SYNC_TIME, AppConstants.ServiceType.WEIGHT_SYNC_PROCESSING);
        VaccinatorAlarmReceiver.setAlarm(context, BuildConfig.VACCINE_SYNC_PROCESSING_MINUTES, AppConstants.ServiceType.VACCINE_SYNC_PROCESSING);
        VaccinatorAlarmReceiver.setAlarm(context, BuildConfig.RECURRING_SERVICES_SYNC_PROCESSING_MINUTES, AppConstants.ServiceType.RECURRING_SERVICES_SYNC_PROCESSING);
        VaccinatorAlarmReceiver.setAlarm(context, BuildConfig.DAILY_TALLIES_GENERATION_MINUTES, AppConstants.ServiceType.DAILY_TALLIES_GENERATION);
        VaccinatorAlarmReceiver.setAlarm(context, BuildConfig.COVERAGE_DROPOUT_GENERATION_MINUTES, AppConstants.ServiceType.COVERAGE_DROPOUT_GENERATION);
        VaccinatorAlarmReceiver.setAlarm(context, BuildConfig.IMAGE_UPLOAD_MINUTES, AppConstants.ServiceType.IMAGE_UPLOAD);
        VaccinatorAlarmReceiver.setAlarm(context, BuildConfig.PULL_UNIQUE_IDS_MINUTES, AppConstants.ServiceType.PULL_UNIQUE_IDS);
    }


}

