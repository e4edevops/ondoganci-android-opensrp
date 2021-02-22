package org.smartregister.ondoganci.repository;

import android.content.Context;
import androidx.annotation.NonNull;

import net.sqlcipher.database.SQLiteDatabase;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.AllConstants;
import org.smartregister.child.util.ChildDbMigrations;
import org.smartregister.child.util.Utils;
import org.smartregister.configurableviews.repository.ConfigurableViewsRepository;
import org.smartregister.domain.db.Column;
import org.smartregister.growthmonitoring.repository.HeadRepository;
import org.smartregister.growthmonitoring.repository.HeadZScoreRepository;
import org.smartregister.growthmonitoring.repository.HeightRepository;
import org.smartregister.growthmonitoring.repository.HeightZScoreRepository;
import org.smartregister.growthmonitoring.repository.WeightForHeightRepository;
import org.smartregister.growthmonitoring.repository.WeightRepository;
import org.smartregister.growthmonitoring.repository.WeightZScoreRepository;
import org.smartregister.immunization.repository.RecurringServiceRecordRepository;
import org.smartregister.immunization.repository.RecurringServiceTypeRepository;
import org.smartregister.immunization.repository.VaccineRepository;
import org.smartregister.immunization.repository.VaccineTypeRepository;
import org.smartregister.immunization.util.IMDatabaseUtils;
import org.smartregister.reporting.ReportingLibrary;
import org.smartregister.reporting.repository.DailyIndicatorCountRepository;
import org.smartregister.reporting.repository.IndicatorQueryRepository;
import org.smartregister.reporting.repository.IndicatorRepository;
import org.smartregister.repository.AlertRepository;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.repository.Hia2ReportRepository;
import org.smartregister.repository.LocationRepository;
import org.smartregister.repository.LocationTagRepository;
import org.smartregister.repository.Repository;
import org.smartregister.repository.SettingsRepository;
import org.smartregister.repository.UniqueIdRepository;
import org.smartregister.ondoganci.BuildConfig;
import org.smartregister.ondoganci.application.OndoganciApplication;
import org.smartregister.ondoganci.util.AppConstants;
import org.smartregister.stock.StockLibrary;
import org.smartregister.stock.repository.StockRepository;
import org.smartregister.stock.repository.StockTypeRepository;
import org.smartregister.stock.util.StockUtils;
import org.smartregister.util.DatabaseMigrationUtils;

import java.util.ArrayList;

import timber.log.Timber;

public class OndoganciRepository extends Repository {

    private SQLiteDatabase readableDatabase;
    private SQLiteDatabase writableDatabase;

    private Context context;
    private String appVersionCodePref = AppConstants.Pref.APP_VERSION_CODE;

    public OndoganciRepository(@NonNull Context context, @NonNull org.smartregister.Context openSRPContext) {
        super(context, AllConstants.DATABASE_NAME, BuildConfig.DATABASE_VERSION, openSRPContext.session(),
                OndoganciApplication.createCommonFtsObject(context), openSRPContext.sharedRepositoriesArray());
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        super.onCreate(database);
        EventClientRepository
                .createTable(database, EventClientRepository.Table.client, EventClientRepository.client_column.values());
        EventClientRepository
                .createTable(database, EventClientRepository.Table.event, EventClientRepository.event_column.values());
        ConfigurableViewsRepository.createTable(database);
        LocationRepository.createTable(database);

        UniqueIdRepository.createTable(database);

        LocationTagRepository.createTable(database);

        SettingsRepository.onUpgrade(database);
        WeightRepository.createTable(database);
        HeightRepository.createTable(database);
        HeadRepository.createTable(database);
        VaccineRepository.createTable(database);
        WeightForHeightRepository.createTable(database);

        ClientRegisterTypeRepository.createTable(database);
        ChildAlertUpdatedRepository.createTable(database);

        StockRepository.createTable(database);

        //reporting
        IndicatorRepository.createTable(database);
        IndicatorQueryRepository.createTable(database);
        DailyIndicatorCountRepository.createTable(database);
        MonthlyTalliesRepository.createTable(database);

        EventClientRepository.createTable(database, Hia2ReportRepository.Table.hia2_report, Hia2ReportRepository.report_column.values());

        runLegacyUpgrades(database);

        onUpgrade(database, 10, BuildConfig.DATABASE_VERSION);

        // initialize from yml file
        ReportingLibrary reportingLibraryInstance = ReportingLibrary.getInstance();
        // Check if indicator data initialised
        String indicatorDataInitialisedPref = AppConstants.Pref.INDICATOR_DATA_INITIALISED;
        boolean indicatorDataInitialised = Boolean.parseBoolean(reportingLibraryInstance.getContext()
                .allSharedPreferences().getPreference(indicatorDataInitialisedPref));
        boolean isUpdated = checkIfAppUpdated();
        if (!indicatorDataInitialised || isUpdated) {
            Timber.d("Initialising indicator repositories!!");
            String indicatorsConfigFile = AppConstants.File.INDICATOR_CONFIG_FILE;
            reportingLibraryInstance.initIndicatorData(indicatorsConfigFile, database); // This will persist the data in the DB
            reportingLibraryInstance.getContext().allSharedPreferences().savePreference(indicatorDataInitialisedPref, "true");
            reportingLibraryInstance.getContext().allSharedPreferences().savePreference(appVersionCodePref, String.valueOf(BuildConfig.VERSION_CODE));
        }
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Timber.w("Upgrading database from version %d to %d, which will destroy all old data", oldVersion, newVersion);

        int upgradeTo = oldVersion + 1;
        while (upgradeTo <= newVersion) {
            switch (upgradeTo) {
                case 2:
                    upgradeToVersion2(db);
                    break;
                case 3:
                    upgradeToVersion3(db);
                    break;
                case 4:
                    upgradeToVersion4(db);
                    break;
                case 5:
                    upgradeToVersion5(db);
                    break;
                case 6:
                    upgradeToVersion6(db);
                    break;
                case 7:
                    upgradeToVersion7OutOfArea(db);
                    upgradeToVersion7Stock(db);
                    break;
                case 8:
//                    upgradeToVersion8RecurringServiceUpdate(db);
                    upgradeToVersion8ReportDeceased(db);
                    break;
                case 9:
                    upgradeToVersion9(db);
                    ChildDbMigrations.addShowBcg2ReminderAndBcgScarColumnsToEcChildDetails(db);
                    break;
                case 10:
                    upgradeToVersion10(db);
                    break;
                case 11:
                    upgradeToVersion11Stock(db);
                    break;
                case 12:
                    upgradeToVersion12(db);
                    break;
//                    case 8:
//                    upgradeToVersion8AddServiceGroupColumn(db);
//                    break;
//                case 9:
//                    ChildDbMigrations.addShowBcg2ReminderAndBcgScarColumnsToEcChildDetails(db);
//                    break;
                default:
                    break;
            }
            upgradeTo++;
        }

        ChildDbMigrations.addShowBcg2ReminderAndBcgScarColumnsToEcChildDetails(db);

        IndicatorQueryRepository.performMigrations(db);

    }

    @Override
    public SQLiteDatabase getReadableDatabase() {
        String pass = OndoganciApplication.getInstance().getPassword();
        if (StringUtils.isNotBlank(pass)) {
            return getReadableDatabase(pass);
        }
        return null;
    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
        String pass = OndoganciApplication.getInstance().getPassword();
        if (StringUtils.isNotBlank(pass)) {
            return getWritableDatabase(pass);
        } else {
            throw new IllegalStateException("Password is blank");
        }
    }

    @Override
    public synchronized SQLiteDatabase getWritableDatabase(String password) {
        if (writableDatabase == null || !writableDatabase.isOpen()) {
            if (writableDatabase != null) {
                writableDatabase.close();
            }
            writableDatabase = super.getWritableDatabase(password);
        }
        return writableDatabase;
    }

    @Override
    public synchronized SQLiteDatabase getReadableDatabase(String password) {
        try {
            if (readableDatabase == null || !readableDatabase.isOpen()) {
                if (readableDatabase != null) {
                    readableDatabase.close();
                }
                readableDatabase = super.getReadableDatabase(password);
            }
            return readableDatabase;
        } catch (Exception e) {
            Timber.e(e);
            return null;
        }

    }

    @Override
    public synchronized void close() {
        if (readableDatabase != null) {
            readableDatabase.close();
        }

        if (writableDatabase != null) {
            writableDatabase.close();
        }
        super.close();
    }

    private void runLegacyUpgrades(@NonNull SQLiteDatabase database) {
        upgradeToVersion2(database);
        upgradeToVersion3(database);
        upgradeToVersion4(database);
        upgradeToVersion5(database);
        upgradeToVersion6(database);
        upgradeToVersion7OutOfArea(database);
        upgradeToVersion7EventWeightHeightVaccineRecurringChange(database);
        upgradeToVersion7VaccineRecurringServiceRecordChange(database);
        upgradeToVersion7WeightHeightVaccineRecurringServiceChange(database);
        upgradeToVersion7RemoveUnnecessaryTables(database);
        upgradeToVersion7Stock(database);
//        upgradeToVersion8RecurringServiceUpdate(database);
        upgradeToVersion8ReportDeceased(database);
        upgradeToVersion9(database);
        upgradeToVersion10(database);
        upgradeToVersion11Stock(database);
        upgradeToVersion12(database);
    }

    /**
     * Version 16 added service_group column
     *
     * @param database SQLiteDatabase
     */
    private void upgradeToVersion8AddServiceGroupColumn(@NonNull SQLiteDatabase database) {
        try {
            database.execSQL(RecurringServiceTypeRepository.ADD_SERVICE_GROUP_COLUMN);
        } catch (Exception e) {
            Timber.e(e, "upgradeToVersion8AddServiceGroupColumn");
        }
    }

    /**
     * Version 2 added some columns to the ec_child table
     *
     * @param database
     */
    private void upgradeToVersion2(@NonNull SQLiteDatabase database) {
        try {
            // Run insert query
            ArrayList<String> newlyAddedFields = new ArrayList<>();
            newlyAddedFields.add("BCG_2");
            newlyAddedFields.add("inactive");
            newlyAddedFields.add("lost_to_follow_up");

            DatabaseMigrationUtils.addFieldsToFTSTable(database, commonFtsObject, Utils.metadata().childRegister.tableName,
                    newlyAddedFields);
        } catch (Exception e) {
            Timber.e(e, "upgradeToVersion2");
        }
    }

    private void upgradeToVersion3(@NonNull SQLiteDatabase db) {
        try {
            db.execSQL(VaccineRepository.UPDATE_TABLE_ADD_EVENT_ID_COL);
            db.execSQL(VaccineRepository.EVENT_ID_INDEX);
            db.execSQL(WeightRepository.UPDATE_TABLE_ADD_EVENT_ID_COL);
            db.execSQL(WeightRepository.EVENT_ID_INDEX);
            db.execSQL(HeightRepository.UPDATE_TABLE_ADD_EVENT_ID_COL);
            db.execSQL(HeightRepository.EVENT_ID_INDEX);
            db.execSQL(HeadRepository.UPDATE_TABLE_ADD_EVENT_ID_COL);
            db.execSQL(HeadRepository.EVENT_ID_INDEX);
            db.execSQL(VaccineRepository.UPDATE_TABLE_ADD_FORMSUBMISSION_ID_COL);
            db.execSQL(VaccineRepository.FORMSUBMISSION_INDEX);
            db.execSQL(WeightRepository.UPDATE_TABLE_ADD_FORMSUBMISSION_ID_COL);
            db.execSQL(WeightRepository.FORMSUBMISSION_INDEX);
            db.execSQL(HeightRepository.UPDATE_TABLE_ADD_FORMSUBMISSION_ID_COL);
            db.execSQL(HeightRepository.FORMSUBMISSION_INDEX);
            db.execSQL(HeadRepository.UPDATE_TABLE_ADD_FORMSUBMISSION_ID_COL);
            db.execSQL(HeadRepository.FORMSUBMISSION_INDEX);
        } catch (Exception e) {
            Timber.e(e, "upgradeToVersion3");
        }
    }

    private void upgradeToVersion4(@NonNull SQLiteDatabase db) {
        try {
            db.execSQL(AlertRepository.ALTER_ADD_OFFLINE_COLUMN);
            db.execSQL(AlertRepository.OFFLINE_INDEX);
        } catch (Exception e) {
            Timber.e(e, "upgradeToVersion4");
        }
    }

    private void upgradeToVersion5(@NonNull SQLiteDatabase db) {
        try {
            RecurringServiceTypeRepository.createTable(db);
            RecurringServiceRecordRepository.createTable(db);

            RecurringServiceTypeRepository recurringServiceTypeRepository = OndoganciApplication.getInstance()
                    .recurringServiceTypeRepository();
            IMDatabaseUtils.populateRecurringServices(context, db, recurringServiceTypeRepository);
        } catch (Exception e) {
            Timber.e(e, "upgradeToVersion5");
        }
    }

    private void upgradeToVersion6(@NonNull SQLiteDatabase db) {
        try {
            WeightZScoreRepository.createTable(db);
            db.execSQL(WeightRepository.ALTER_ADD_Z_SCORE_COLUMN);

            HeightZScoreRepository.createTable(db);
            db.execSQL(HeightRepository.ALTER_ADD_Z_SCORE_COLUMN);

            HeadZScoreRepository.createTable(db);
            db.execSQL(HeadRepository.ALTER_ADD_Z_SCORE_COLUMN);
        } catch (Exception e) {
            Timber.e(e, "upgradeToVersion6");
        }
    }

    private void upgradeToVersion7OutOfArea(@NonNull SQLiteDatabase db) {
        try {
            db.execSQL(VaccineRepository.UPDATE_TABLE_ADD_OUT_OF_AREA_COL);
            db.execSQL(VaccineRepository.UPDATE_TABLE_ADD_OUT_OF_AREA_COL_INDEX);
            db.execSQL(WeightRepository.UPDATE_TABLE_ADD_OUT_OF_AREA_COL);
            db.execSQL(WeightRepository.UPDATE_TABLE_ADD_OUT_OF_AREA_COL_INDEX);
            db.execSQL(HeightRepository.UPDATE_TABLE_ADD_OUT_OF_AREA_COL);
            db.execSQL(HeightRepository.UPDATE_TABLE_ADD_OUT_OF_AREA_COL_INDEX);
            db.execSQL(HeadRepository.UPDATE_TABLE_ADD_OUT_OF_AREA_COL);
            db.execSQL(HeadRepository.UPDATE_TABLE_ADD_OUT_OF_AREA_COL_INDEX);
            db.execSQL(VaccineRepository.UPDATE_TABLE_ADD_HIA2_STATUS_COL);

        } catch (Exception e) {
            Timber.e(e, "upgradeToVersion7");
        }
    }

    private void upgradeToVersion7EventWeightHeightVaccineRecurringChange(@NonNull SQLiteDatabase db) {
        try {
            Column[] columns = {EventClientRepository.event_column.formSubmissionId};
            EventClientRepository.createIndex(db, EventClientRepository.Table.event, columns);

            db.execSQL(WeightRepository.ALTER_ADD_CREATED_AT_COLUMN);
            WeightRepository.migrateCreatedAt(db);

            db.execSQL(HeightRepository.ALTER_ADD_CREATED_AT_COLUMN);
            HeightRepository.migrateCreatedAt(db);

            db.execSQL(HeadRepository.ALTER_ADD_CREATED_AT_COLUMN);
            HeadRepository.migrateCreatedAt(db);

            db.execSQL(VaccineRepository.ALTER_ADD_CREATED_AT_COLUMN);
            VaccineRepository.migrateCreatedAt(db);

            db.execSQL(RecurringServiceRecordRepository.ALTER_ADD_CREATED_AT_COLUMN);
            RecurringServiceRecordRepository.migrateCreatedAt(db);

        } catch (Exception e) {
            Timber.e(e, "upgradeToVersion7EventWeightHeightVaccineRecurringChange");
        }
    }

    private void upgradeToVersion7VaccineRecurringServiceRecordChange(@NonNull SQLiteDatabase db) {
        try {
            db.execSQL(VaccineRepository.UPDATE_TABLE_ADD_TEAM_ID_COL);
            db.execSQL(VaccineRepository.UPDATE_TABLE_ADD_TEAM_COL);

            db.execSQL(RecurringServiceRecordRepository.UPDATE_TABLE_ADD_TEAM_ID_COL);
            db.execSQL(RecurringServiceRecordRepository.UPDATE_TABLE_ADD_TEAM_COL);
        } catch (Exception e) {
            Timber.e(e, "upgradeToVersion7VaccineRecurringServiceRecordChange");
        }
    }

    private void upgradeToVersion7WeightHeightVaccineRecurringServiceChange(@NonNull SQLiteDatabase db) {
        try {

            db.execSQL(WeightRepository.UPDATE_TABLE_ADD_TEAM_ID_COL);
            db.execSQL(WeightRepository.UPDATE_TABLE_ADD_TEAM_COL);

            db.execSQL(HeightRepository.UPDATE_TABLE_ADD_TEAM_ID_COL);
            db.execSQL(HeightRepository.UPDATE_TABLE_ADD_TEAM_COL);

            db.execSQL(HeadRepository.UPDATE_TABLE_ADD_TEAM_ID_COL);
            db.execSQL(HeadRepository.UPDATE_TABLE_ADD_TEAM_COL);

            db.execSQL(VaccineRepository.UPDATE_TABLE_ADD_CHILD_LOCATION_ID_COL);

            db.execSQL(WeightRepository.UPDATE_TABLE_ADD_CHILD_LOCATION_ID_COL);
            db.execSQL(HeightRepository.UPDATE_TABLE_ADD_CHILD_LOCATION_ID_COL);
            db.execSQL(HeadRepository.UPDATE_TABLE_ADD_CHILD_LOCATION_ID_COL);

            db.execSQL(RecurringServiceRecordRepository.UPDATE_TABLE_ADD_CHILD_LOCATION_ID_COL);
        } catch (Exception e) {
            Timber.e(e, "upgradeToVersion7WeightHeightVaccineRecurringServiceChange");
        }
    }

    private void upgradeToVersion7RemoveUnnecessaryTables(@NonNull SQLiteDatabase db) {
        try {
            db.execSQL("DROP TABLE IF EXISTS address");
            db.execSQL("DROP TABLE IF EXISTS obs");
            if (DatabaseMigrationUtils.isColumnExists(db, "path_reports", Hia2ReportRepository.report_column.json.name()))
                db.execSQL("ALTER TABLE path_reports RENAME TO " + Hia2ReportRepository.Table.hia2_report.name() + ";");
            if (DatabaseMigrationUtils.isColumnExists(db, EventClientRepository.Table.client.name(), "firstName"))
                DatabaseMigrationUtils.recreateSyncTableWithExistingColumnsOnly(db, EventClientRepository.Table.client);
            if (DatabaseMigrationUtils.isColumnExists(db, EventClientRepository.Table.event.name(), "locationId"))
                DatabaseMigrationUtils.recreateSyncTableWithExistingColumnsOnly(db, EventClientRepository.Table.event);


        } catch (Exception e) {
            Timber.e(e, "upgradeToVersion7RemoveUnnecessaryTables");
        }
    }

    private void upgradeToVersion7Stock(@NonNull SQLiteDatabase db) {
        try {
            db.execSQL("DROP TABLE IF EXISTS " + VaccineTypeRepository.VACCINE_Types_TABLE_NAME);
            StockTypeRepository.createTable(db);
            StockUtils.populateStockTypesFromAssets(context, StockLibrary.getInstance().getStockTypeRepository(), db);
            StockRepository.migrateFromOldStockRepository(db, "Stocks");

        } catch (Exception e) {
            Timber.e(e,"upgradeToVersion7Stock");
        }
    }

//    private void upgradeToVersion8RecurringServiceUpdate(SQLiteDatabase db) {
//        try {
//            db.execSQL(MonthlyTalliesRepository.INDEX_UNIQUE);
//            dumpHIA2IndicatorsCSV(db);
//
//            // Recurring service json changed. update
//            RecurringServiceTypeRepository recurringServiceTypeRepository = OndoganciApplication.getInstance().recurringServiceTypeRepository();
//            IMDatabaseUtils.populateRecurringServices(context, db, recurringServiceTypeRepository);
//
//        } catch (Exception e) {
//            Timber.e("upgradeToVersion8RecurringServiceUpdate %s", Log.getStackTraceString(e));
//        }
//    }

    private void upgradeToVersion8ReportDeceased(SQLiteDatabase database) {
        try {

            String ALTER_ADD_DEATHDATE_COLUMN = "ALTER TABLE " + Utils.metadata().childRegister.tableName + " VARCHAR";
            database.execSQL(ALTER_ADD_DEATHDATE_COLUMN);

            ArrayList<String> newlyAddedFields = new ArrayList<>();
            newlyAddedFields.add(AppConstants.KEY.DOD);

            DatabaseMigrationUtils.addFieldsToFTSTable(database, commonFtsObject, Utils.metadata().childRegister.tableName, newlyAddedFields);
        } catch (Exception e) {
            Timber.e("upgradeToVersion8ReportDeceased %s", e.getMessage());
        }
    }

    private void upgradeToVersion9(SQLiteDatabase database) {
        try {
            String ALTER_EVENT_TABLE_VALIDATE_COLUMN = "ALTER TABLE " + EventClientRepository.Table.event + " ADD COLUMN " + EventClientRepository.event_column.validationStatus + " VARCHAR";
            database.execSQL(ALTER_EVENT_TABLE_VALIDATE_COLUMN);

            String ALTER_CLIENT_TABLE_VALIDATE_COLUMN = "ALTER TABLE " + EventClientRepository.Table.client + " ADD COLUMN " + EventClientRepository.client_column.validationStatus + " VARCHAR";
            database.execSQL(ALTER_CLIENT_TABLE_VALIDATE_COLUMN);

            String ALTER_REPORT_TABLE_VALIDATE_COLUMN = "ALTER TABLE " + Hia2ReportRepository.Table.hia2_report + " ADD COLUMN " + Hia2ReportRepository.report_column.validationStatus + " VARCHAR";
            database.execSQL(ALTER_REPORT_TABLE_VALIDATE_COLUMN);

            EventClientRepository.createIndex(database, EventClientRepository.Table.event, EventClientRepository.event_column.values());
            EventClientRepository.createIndex(database, EventClientRepository.Table.client, EventClientRepository.client_column.values());
            EventClientRepository.createIndex(database, Hia2ReportRepository.Table.hia2_report, Hia2ReportRepository.report_column.values());

        } catch (Exception e) {
            Timber.e("upgradeToVersion9 %s", e.getMessage());
        }
    }

    private void upgradeToVersion10(SQLiteDatabase database) {
        try {

            CohortRepository.createTable(database);
            CohortIndicatorRepository.createTable(database);
            CohortPatientRepository.createTable(database);

            CumulativeRepository.createTable(database);
            CumulativeIndicatorRepository.createTable(database);
            CumulativePatientRepository.createTable(database);

        } catch (Exception e) {
            Timber.e("upgradeToVersion10 %s", e.getMessage());
        }
    }

    private void upgradeToVersion11Stock(SQLiteDatabase db) {
        try {
            db.execSQL("DROP TABLE IF EXISTS " + VaccineTypeRepository.VACCINE_Types_TABLE_NAME);
            StockTypeRepository.createTable(db);
            StockUtils.populateStockTypesFromAssets(context, StockLibrary.getInstance().getStockTypeRepository(), db);
            StockRepository.migrateFromOldStockRepository(db, "Stocks");

        } catch (Exception e) {
            Timber.e("upgradeToVersion11Stock %s", e.getMessage());
        }
    }

    private void upgradeToVersion12(SQLiteDatabase db) {
        try {
            Column[] columns = {EventClientRepository.event_column.formSubmissionId};
            EventClientRepository.createIndex(db, EventClientRepository.Table.event, columns);

            db.execSQL(WeightRepository.ALTER_ADD_CREATED_AT_COLUMN);
            WeightRepository.migrateCreatedAt(db);

            db.execSQL(HeightRepository.ALTER_ADD_CREATED_AT_COLUMN);
            HeightRepository.migrateCreatedAt(db);

            db.execSQL(HeadRepository.ALTER_ADD_CREATED_AT_COLUMN);
            HeadRepository.migrateCreatedAt(db);

            db.execSQL(VaccineRepository.ALTER_ADD_CREATED_AT_COLUMN);
            VaccineRepository.migrateCreatedAt(db);

            db.execSQL(RecurringServiceRecordRepository.ALTER_ADD_CREATED_AT_COLUMN);
            RecurringServiceRecordRepository.migrateCreatedAt(db);

        } catch (Exception e) {
            Timber.e("upgradeToVersion12 %s", e.getMessage());
        }
    }

    private boolean checkIfAppUpdated() {
        String savedAppVersion = ReportingLibrary.getInstance().getContext().allSharedPreferences().getPreference(appVersionCodePref);
        if (savedAppVersion.isEmpty()) {
            return true;
        } else {
            int savedVersion = Integer.parseInt(savedAppVersion);
            return (BuildConfig.VERSION_CODE > savedVersion);
        }
    }
}
