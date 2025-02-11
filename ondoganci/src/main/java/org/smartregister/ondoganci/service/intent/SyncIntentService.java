package org.smartregister.ondoganci.service.intent;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.util.Pair;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.AllConstants;
import org.smartregister.domain.FetchStatus;
import org.smartregister.domain.Response;
import org.smartregister.domain.db.EventClient;
import org.smartregister.ondoganci.R;
import org.smartregister.ondoganci.application.OndoganciApplication;
import org.smartregister.location.helper.LocationHelper;
import org.smartregister.ondoganci.receiver.SyncStatusBroadcastReceiver;
import org.smartregister.ondoganci.receiver.VaccinatorAlarmReceiver;
import org.smartregister.ondoganci.sync.ECSyncUpdater;
import org.smartregister.sync.ClientProcessorForJava;
import org.smartregister.SyncFilter;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.service.HTTPAgent;

import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.smartregister.ondoganci.util.NetworkUtils;

import timber.log.Timber;

public class SyncIntentService extends IntentService {
    private static final String ADD_URL = "/rest/event/add";
    public static final String SYNC_URL = "/rest/event/sync";

    private Context context;
    private HTTPAgent httpAgent;

    public static final int EVENT_PULL_LIMIT = 500;
    private static final int EVENT_PUSH_LIMIT = 50;

    public static String WAKE_UP = "WAKE_UP";
    private boolean wakeup = false;

    public SyncIntentService() {
        super("SyncIntentService");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        context = getBaseContext();
        httpAgent = OndoganciApplication.getInstance().context().getHttpAgent();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        wakeup = intent.getBooleanExtra(WAKE_UP, false);

        handleSync();

        if (wakeup) {
            VaccinatorAlarmReceiver.completeWakefulIntent(intent);
        }

        Intent serviceIntent = new Intent(context, HIA2IntentService.class);
        startService(serviceIntent);

        Intent idIntent = new Intent(context, PullUniqueIdsIntentService.class);
        startService(idIntent);
    }

    protected void handleSync() {
        sendSyncStatusBroadcastMessage(FetchStatus.fetchStarted);

        doSync();
    }

    private void doSync() {
        if (!NetworkUtils.isNetworkAvailable()) {
            complete(FetchStatus.noConnection);
            return;
        }

        try {
            pushToServer();
            pullECFromServer();

        } catch (Exception e) {
            Log.e(getClass().getName(), e.getMessage(), e);
            complete(FetchStatus.fetchedFailed);
        }
    }

    private void pullECFromServer() {
        fetchRetry(0);
    }

    private synchronized void fetchRetry(final int count) {
        try {
            // Fetch locations
            final String locations = LocationHelper.getInstance().locationIdsFromHierarchy();
            if (StringUtils.isBlank(locations)) {
                complete(FetchStatus.fetchedFailed);
                return;
            }

            final ECSyncUpdater ecSyncUpdater = ECSyncUpdater.getInstance(context);
            String baseUrl = OndoganciApplication.getInstance().context().
                    configuration().dristhiBaseURL();
            if (baseUrl.endsWith("/")) {
                baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf("/"));
            }

            Long lastSyncDatetime = ecSyncUpdater.getLastSyncTimeStamp();
            Timber.i("LAST SYNC DT :%s", new DateTime(lastSyncDatetime));

            String url = baseUrl + SYNC_URL + "?" + SyncFilter.LOCATION_ID + "=" + locations + "&serverVersion=" + lastSyncDatetime + "&limit=" + SyncIntentService.EVENT_PULL_LIMIT;
            Timber.i("URL: %s", url);

            if (httpAgent == null) {
                complete(FetchStatus.fetchedFailed);
            }

            Response resp = httpAgent.fetch(url);
            if (resp.isFailure()) {
                fetchFailed(count);
            }

            JSONObject jsonObject = new JSONObject((String) resp.payload());

            int eCount = fetchNumberOfEvents(jsonObject);
            Timber.i("Parse Network Event Count: %s", eCount);

            if (eCount == 0) {
                complete(FetchStatus.nothingFetched);
            } else if (eCount < 0) {
                fetchFailed(count);
            } else if (eCount > 0) {
                final Pair<Long, Long> serverVersionPair = getMinMaxServerVersions(jsonObject);
                long lastServerVersion = serverVersionPair.second - 1;
                if (eCount < EVENT_PULL_LIMIT) {
                    lastServerVersion = serverVersionPair.second;
                }

                ecSyncUpdater.saveAllClientsAndEvents(jsonObject);
                ecSyncUpdater.updateLastSyncTimeStamp(lastServerVersion);

                processClient(serverVersionPair);

                fetchRetry(0);
            }
        } catch (Exception e) {
            Timber.e(e.getCause(), "Fetch Retry Exception: %s", e.getMessage());
            fetchFailed(count);
        }
    }

    public void fetchFailed(int count) {
        if (count < 2) {
            int newCount = count + 1;
            fetchRetry(newCount);
        } else {
            complete(FetchStatus.fetchedFailed);
        }
    }

    private void processClient(Pair<Long, Long> serverVersionPair) {
        try {
            ECSyncUpdater ecUpdater = ECSyncUpdater.getInstance(context);
            List<EventClient> events = ecUpdater.allEventClients(serverVersionPair.first - 1, serverVersionPair.second);
            ClientProcessorForJava.getInstance(context).processClient(events);
            sendSyncStatusBroadcastMessage(FetchStatus.fetched);
        } catch (Exception e) {
            Timber.e(e.getCause(), "Process Client Exception: %s", e.getMessage());
        }
    }

    // PUSH TO SERVER
    private void pushToServer() {
        pushECToServer();
    }

    private void pushECToServer() {
        EventClientRepository db = OndoganciApplication.getInstance().eventClientRepository();
        boolean keepSyncing = true;

        while (keepSyncing) {
            try {
                Map<String, Object> pendingEvents = db.getUnSyncedEvents(EVENT_PUSH_LIMIT);

                if (pendingEvents.isEmpty()) {
                    return;
                }

                String baseUrl = OndoganciApplication.getInstance().context().configuration().dristhiBaseURL();
                if (baseUrl.endsWith(context.getString(R.string.url_separator))) {
                    baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf(context.getString(R.string.url_separator)));
                }
                // create request body
                JSONObject request = new JSONObject();
                if (pendingEvents.containsKey(context.getString(R.string.clients_key))) {
                    request.put(context.getString(R.string.clients_key), pendingEvents.get(context.getString(R.string.clients_key)));
                }
                if (pendingEvents.containsKey(context.getString(R.string.events_key))) {
                    request.put(context.getString(R.string.events_key), pendingEvents.get(context.getString(R.string.events_key)));
                }
                String jsonPayload = request.toString();
                Response<String> response = httpAgent.post(
                        MessageFormat.format("{0}/{1}",
                                baseUrl,
                                ADD_URL),
                        jsonPayload);
                if (response.isFailure()) {
                    Timber.e("Events sync failed.");
                    return;
                }
                db.markEventsAsSynced(pendingEvents);
                Timber.i("Events synced successfully.");
            } catch (Exception e) {
                Timber.e(e);
            }
        }
    }

    private void sendSyncStatusBroadcastMessage(FetchStatus fetchStatus) {
        Intent intent = new Intent();
        intent.setAction(SyncStatusBroadcastReceiver.ACTION_SYNC_STATUS);
        intent.putExtra(SyncStatusBroadcastReceiver.EXTRA_FETCH_STATUS, fetchStatus);
        sendBroadcast(intent);
    }

    private void complete(FetchStatus fetchStatus) {
        Intent intent = new Intent();
        intent.setAction(SyncStatusBroadcastReceiver.ACTION_SYNC_STATUS);
        intent.putExtra(SyncStatusBroadcastReceiver.EXTRA_FETCH_STATUS, fetchStatus);
        intent.putExtra(SyncStatusBroadcastReceiver.EXTRA_COMPLETE_STATUS, true);
        if (wakeup) {
            intent.putExtra(SyncIntentService.WAKE_UP, true);
        }

        sendBroadcast(intent);

        ECSyncUpdater ecSyncUpdater = ECSyncUpdater.getInstance(context);
        ecSyncUpdater.updateLastCheckTimeStamp(new Date().getTime());
    }

    private Pair<Long, Long> getMinMaxServerVersions(JSONObject jsonObject) {
        final String EVENTS = "events";
        final String SERVER_VERSION = "serverVersion";
        try {
            if (jsonObject != null && jsonObject.has(EVENTS)) {
                JSONArray events = jsonObject.getJSONArray(EVENTS);

                long maxServerVersion = Long.MIN_VALUE;
                long minServerVersion = Long.MAX_VALUE;

                for (int i = 0; i < events.length(); i++) {
                    Object o = events.get(i);
                    if (o instanceof JSONObject) {
                        JSONObject jo = (JSONObject) o;
                        if (jo.has(SERVER_VERSION)) {
                            long serverVersion = jo.getLong(SERVER_VERSION);
                            if (serverVersion > maxServerVersion) {
                                maxServerVersion = serverVersion;
                            }

                            if (serverVersion < minServerVersion) {
                                minServerVersion = serverVersion;
                            }
                        }
                    }
                }
                return Pair.create(minServerVersion, maxServerVersion);
            }
        } catch (Exception e) {
            Timber.e(e);
        }
        return Pair.create(0L, 0L);
    }

    private int fetchNumberOfEvents(JSONObject jsonObject) {
        int count = -1;
        final String NO_OF_EVENTS = "no_of_events";
        try {
            if (jsonObject != null && jsonObject.has(NO_OF_EVENTS)) {
                count = jsonObject.getInt(NO_OF_EVENTS);
            }
        } catch (JSONException e) {
            Timber.e(e);
        }
        return count;
    }

}
