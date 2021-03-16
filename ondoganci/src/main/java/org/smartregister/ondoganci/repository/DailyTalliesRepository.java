package org.smartregister.ondoganci.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import net.sqlcipher.database.SQLiteDatabase;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.ondoganci.application.OndoganciApplication;
import org.smartregister.ondoganci.domain.Hia2Indicator;
import org.smartregister.reporting.util.Constants;
import org.smartregister.repository.BaseRepository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import timber.log.Timber;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 2019-07-11
 */

public class DailyTalliesRepository extends BaseRepository {

    private static final SimpleDateFormat DAY_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
    private static final String TABLE_NAME = Constants.DailyIndicatorCountRepository.INDICATOR_DAILY_TALLY_TABLE;
    private static final String COLUMN_DAY = Constants.DailyIndicatorCountRepository.DAY;
    private static final String COLUMN_INDICATOR_ID = Constants.DailyIndicatorCountRepository.ID;
    private static final String COLUMN_VALUE = Constants.DailyIndicatorCountRepository.INDICATOR_VALUE;
    private static final String COLUMN_PROVIDER_ID = "provider_id";
    private static final String COLUMN_UPDATED_AT = "updated_at";

    /**
     * Saves a set of tallies
     *
     * @param day        The day the tallies correspond to
     * @param hia2Report Object holding the tallies, the first key in the map holds the indicator
     *                   code, and the second the DHIS id for the indicator. It's expected that
     *                   the inner most map will always hold one value
     */
    public void save(String day, Map<String, Object> hia2Report) {
        SQLiteDatabase database = getWritableDatabase();
        try {
            database.beginTransaction();
            String userName = OndoganciApplication.getInstance().context().allSharedPreferences().fetchRegisteredANM();
            for (String indicatorCode : hia2Report.keySet()) {
                Integer indicatorValue = (Integer) hia2Report.get(indicatorCode);

                // Get the HIA2 Indicator corresponding to the current tally
                Hia2Indicator indicator = OndoganciApplication.getInstance()
                        .hIA2IndicatorsRepository()
                        .findByIndicatorCode(indicatorCode);

                if (indicator != null) {
                    ContentValues cv = new ContentValues();
                    cv.put(DailyTalliesRepository.COLUMN_INDICATOR_ID, indicator.getId());
                    cv.put(DailyTalliesRepository.COLUMN_VALUE, indicatorValue);
                    cv.put(DailyTalliesRepository.COLUMN_PROVIDER_ID, userName);
                    cv.put(DailyTalliesRepository.COLUMN_DAY, StringUtils.isNotBlank(day) ? DAY_FORMAT.parse(day).getTime() : null);
                    cv.put(DailyTalliesRepository.COLUMN_UPDATED_AT, Calendar.getInstance().getTimeInMillis());

                    database.insertWithOnConflict(TABLE_NAME, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
                }
            }
            database.setTransactionSuccessful();
        } catch (SQLException e) {
            Timber.e(e);
        } catch (ParseException e) {
            Timber.e(e);
        } finally {
            database.endTransaction();
        }
    }

    /**
     * Returns a list of dates for distinct months with daily tallies
     *
     * @param dateFormat The format to use to format the months' dates
     * @param startDate  The first date to consider. Set argument to null if you
     *                   don't want this enforced
     * @param endDate    The last date to consider. Set argument to null if you
     *                   don't want this enforced
     * @return A list of months that have daily tallies
     */
    public List<String> findAllDistinctMonths(SimpleDateFormat dateFormat, Date startDate, Date endDate) {
        return findAllDistinctMonths(dateFormat, startDate, endDate, null);
    }

    /**
     * Returns a list of dates for distinct months with daily tallies
     *
     * @param dateFormat The format to use to format the months' dates
     * @param startDate  The first date to consider. Set argument to null if you
     *                   don't want this enforced
     * @param endDate    The last date to consider. Set argument to null if you
     *                   don't want this enforced
     * @return A list of months that have daily tallies
     */
    public List<String> findAllDistinctMonths(SimpleDateFormat dateFormat, Date startDate, Date endDate, @Nullable String grouping) {
        Cursor cursor = null;
        List<String> months = new ArrayList<>();

        try {
            String selectionArgs = "";
            if (startDate != null) {
                selectionArgs = COLUMN_DAY + " >= '" + DAY_FORMAT.format(startDate) + "'";
            }

            if (endDate != null) {
                if (!TextUtils.isEmpty(selectionArgs)) {
                    selectionArgs = selectionArgs + " AND ";
                }

                selectionArgs = selectionArgs + COLUMN_DAY + " <= '" + DAY_FORMAT.format(endDate) +"'";
            }

            selectionArgs += " AND " + Constants.DailyIndicatorCountRepository.INDICATOR_GROUPING + (grouping == null ? " IS NULL" : " = '" + grouping + "'");

            cursor = getReadableDatabase().query(true, TABLE_NAME,
                    new String[]{COLUMN_DAY},
                    selectionArgs, null, null, null, null, null);

            months = getUniqueMonths(dateFormat, cursor);
        } catch (SQLException | ParseException e) {
            Timber.e(e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return months;
    }

    /**
     * Returns a list of unique months formatted in the provided {@link SimpleDateFormat}
     *
     * @param dateFormat The date format to format the months
     * @param cursor     Cursor to get the dates from
     * @return
     */
    private List<String> getUniqueMonths(SimpleDateFormat dateFormat, Cursor cursor) throws ParseException {
        List<String> months = new ArrayList<>();
        if (cursor != null && cursor.getCount() > 0) {
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                Date curMonth = DAY_FORMAT.parse((cursor.getString(0)));
                String month = dateFormat.format(curMonth);
                if (!months.contains(month)) {
                    months.add(month);
                }
            }
        }

        return months;
    }
}