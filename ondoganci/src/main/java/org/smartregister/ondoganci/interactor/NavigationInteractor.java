package org.smartregister.ondoganci.interactor;

import android.database.Cursor;

import org.smartregister.child.util.Constants;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.cursoradapter.SmartRegisterQueryBuilder;
import org.smartregister.ondoganci.application.OndoganciApplication;
import org.smartregister.ondoganci.contract.NavigationContract;
import org.smartregister.ondoganci.util.AppConstants;
import org.smartregister.ondoganci.util.AppExecutors;

import java.text.MessageFormat;
import java.util.Date;

import timber.log.Timber;

public class NavigationInteractor implements NavigationContract.Interactor {

    private static NavigationInteractor instance;
    private AppExecutors appExecutors = new AppExecutors();

    public static NavigationInteractor getInstance() {
        if (instance == null)
            instance = new NavigationInteractor();

        return instance;
    }

    private int getCount(String tempRegisterType) {
        String registerType = tempRegisterType;
        int count = 0;
        Cursor cursor = null;
        if (AppConstants.RegisterType.OPD.equals(registerType)){
            registerType = "'"+ AppConstants.RegisterType.OPD+"'," + "'"+ AppConstants.RegisterType.ANC+"'," + "'"+ AppConstants.RegisterType.CHILD+"'";
        } else {
            registerType = "'"+registerType+"'";

        }

        String mainCondition = String.format(" where %s is null AND register_type IN (%s) ", AppConstants.TABLE_NAME.ALL_CLIENTS+"."+ AppConstants.KEY.DATE_REMOVED, registerType);

        if (registerType.contains(AppConstants.RegisterType.CHILD)) {
            mainCondition += " AND ( " + Constants.KEY.DOD + " is NULL OR " + Constants.KEY.DOD + " = '' ) ";
        }

        try {
            SmartRegisterQueryBuilder smartRegisterQueryBuilder = new SmartRegisterQueryBuilder();
            String query = MessageFormat.format("select count(*) from {0} inner join client_register_type on ec_client.id=client_register_type.base_entity_id {1}", AppConstants.TABLE_NAME.ALL_CLIENTS, mainCondition);
            query = smartRegisterQueryBuilder.Endquery(query);
            Timber.i("2%s", query);
            cursor = commonRepository(AppConstants.TABLE_NAME.ALL_CLIENTS).rawCustomQueryForAdapter(query);
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } catch (Exception e) {
            Timber.e(e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return count;
    }

    private CommonRepository commonRepository(String tableName) {
        return OndoganciApplication.getInstance().getContext().commonrepository(tableName);
    }

    @Override
    public Date sync() {
        Date syncDate = null;
        try {
            syncDate = new Date(getLastCheckTimeStamp());
        } catch (Exception e) {
            Timber.e(e);
        }

        return syncDate;
    }

    private Long getLastCheckTimeStamp() {
        return OndoganciApplication.getInstance().getEcSyncHelper().getLastCheckTimeStamp();
    }
}
