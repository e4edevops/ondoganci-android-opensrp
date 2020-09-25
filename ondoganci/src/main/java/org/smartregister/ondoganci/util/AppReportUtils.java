package org.smartregister.ondoganci.util;

import android.support.annotation.NonNull;

import org.joda.time.DateTime;
import org.json.JSONObject;
import org.smartregister.child.util.Constants;
import org.smartregister.child.util.JsonFormUtils;
import org.smartregister.ondoganci.application.OndoganciApplication;
import org.smartregister.ondoganci.domain.Report;
import org.smartregister.ondoganci.domain.ReportHia2Indicator;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import timber.log.Timber;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 2019-12-02
 */

public class AppReportUtils {

    public static void createReportAndSaveReport(@NonNull List<ReportHia2Indicator> hia2Indicators, @NonNull Date month, @NonNull String reportType) {
        try {
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
            try {
                OndoganciApplication.getInstance().hia2ReportRepository().addReport(reportJson);
            } catch (Exception e) {
                Timber.e(e);
            }

        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @NonNull
    public static String getStringIdentifier(@NonNull String identifierCode) {
        return identifierCode
                .toLowerCase()
                .replace(" ", "_")
                .replace("/", "");
    }
}
