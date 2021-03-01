package org.smartregister.ondoganci.service.intent;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import org.smartregister.ondoganci.application.OndoganciApplication;
import org.smartregister.ondoganci.receiver.VaccinatorAlarmReceiver;
import org.smartregister.sync.intent.SyncIntentService;
import org.smartregister.ondoganci.service.intent.path.PathStockSyncIntentService;
import org.smartregister.ondoganci.service.intent.path.PathZScoreRefreshIntentService;
import org.smartregister.service.ActionService;

import org.smartregister.ondoganci.util.NetworkUtils;
import org.smartregister.ondoganci.util.ServiceTools;
import org.smartregister.sync.intent.ValidateIntentService;

public class ExtendedSyncIntentService extends IntentService {

    private Context context;
    private ActionService actionService;

    public ExtendedSyncIntentService() {
        super("ExtendedSyncIntentService");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        context = getBaseContext();
        actionService = OndoganciApplication.getInstance().context().actionService();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {

//        boolean wakeup = workIntent.getBooleanExtra(SyncIntentService.WAKE_UP, false);
//
//        if (NetworkUtils.isNetworkAvailable()) {
//
//            startStockSync(wakeup);
//
//            actionService.fetchNewActions();
//
//            startSyncValidation(wakeup);
//        }
//        startZscoreRefresh(wakeup);

        VaccinatorAlarmReceiver.completeWakefulIntent(workIntent);
    }

    private void startStockSync(boolean wakeup) {
        ServiceTools.startService(context, PathStockSyncIntentService.class, wakeup);
    }

    private void startSyncValidation(boolean wakeup) {
        ServiceTools.startService(context, ValidateIntentService.class, wakeup);
    }

    private void startZscoreRefresh(boolean wakeup) {
        ServiceTools.startService(context, PathZScoreRefreshIntentService.class, wakeup);
    }

}
