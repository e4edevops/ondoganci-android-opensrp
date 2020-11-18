package org.smartregister.ondoganci.util;

import android.content.Context;
import android.util.Log;

import org.joda.time.DateTime;
import org.json.JSONObject;
import org.smartregister.child.util.Constants;
import org.smartregister.ondoganci.application.OndoganciApplication;
import org.smartregister.ondoganci.domain.Report;
import org.smartregister.ondoganci.domain.ReportHia2Indicator;
import org.smartregister.ondoganci.sync.ECSyncUpdater;
import org.smartregister.child.util.JsonFormUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import timber.log.Timber;

/**
 * Created by keyman on 08/02/2017.
 */
public class ReportUtils {
    private static final String TAG = ReportUtils.class.getCanonicalName();


    public static void createReport(Context context, List<ReportHia2Indicator> hia2Indicators, Date month, String reportType) {
        try {
            ECSyncUpdater ecUpdater = ECSyncUpdater.getInstance(context);

            String providerId = OndoganciApplication.getInstance().context().allSharedPreferences().fetchRegisteredANM();
            String locationId = OndoganciApplication.getInstance().context().allSharedPreferences().getPreference(Constants.CURRENT_LOCATION_ID);
            Report report = new Report();
            report.setFormSubmissionId(JsonFormUtils.generateRandomUUIDString());
            report.setHia2Indicators(hia2Indicators);
            report.setLocationId(locationId);
            report.setProviderId(providerId);

            // Get the second last day of the month
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(month);
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH) - 2);

            report.setReportDate(new DateTime(calendar.getTime()));
            report.setReportType(reportType);
            JSONObject reportJson = new JSONObject(JsonFormUtils.gson.toJson(report));
            ecUpdater.addReport(reportJson);

        } catch (Exception e) {
            Timber.e(e, e.toString());
        }
    }


}
