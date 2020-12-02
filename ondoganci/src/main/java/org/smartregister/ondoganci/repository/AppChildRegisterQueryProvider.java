package org.smartregister.ondoganci.repository;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.child.provider.RegisterQueryProvider;
import org.smartregister.child.util.Constants;
import org.smartregister.ondoganci.util.AppConstants;

import static org.smartregister.ondoganci.util.AppConstants.KEY.*;
import static org.smartregister.ondoganci.util.TableUtil.getAllClientColumn;
import static org.smartregister.ondoganci.util.TableUtil.getChildDetailsColumn;
import static org.smartregister.ondoganci.util.TableUtil.getFatherDetailsColumn;
import static org.smartregister.ondoganci.util.TableUtil.getMotherDetailsColumn;

public class AppChildRegisterQueryProvider extends RegisterQueryProvider {

    @Override
    public String mainRegisterQuery() {
        return "select " + StringUtils.join(mainColumns(), ",") + " from " + getChildDetailsTable() + " " +
                "join " + getMotherDetailsTable() + " on " + getChildDetailsTable() + "." + Constants.KEY.RELATIONAL_ID + " = " + getMotherDetailsTable() + "." + Constants.KEY.BASE_ENTITY_ID + " " +
                "left join " + getFatherDetailsTable() + " on " + getChildDetailsTable() + "." + Constants.KEY.FATHER_RELATIONAL_ID + " = " + getFatherDetailsTable() + "." + Constants.KEY.BASE_ENTITY_ID + " " +
                "join " + getDemographicTable() + " on " + getDemographicTable() + "." + Constants.KEY.BASE_ENTITY_ID + " = " + getChildDetailsTable() + "." + Constants.KEY.BASE_ENTITY_ID + " " +
                "join " + getDemographicTable() + " mother on mother." + Constants.KEY.BASE_ENTITY_ID + " = " + getMotherDetailsTable() + "." + Constants.KEY.BASE_ENTITY_ID + " " +
                "left join " + getDemographicTable() + " father on father." + Constants.KEY.BASE_ENTITY_ID + " = " + getFatherDetailsTable() + "." + Constants.KEY.BASE_ENTITY_ID;
    }

    private String getFatherDetailsTable() {
        return AppConstants.TABLE_NAME.FATHER_DETAILS;
    }

    @Override
    public String[] mainColumns() {
        return new String[]{
                getAllClientColumn(ID) + "as _id",
                getAllClientColumn(RELATIONALID),
                getAllClientColumn(ZEIR_ID),
                getAllClientColumn(GENDER),
                getAllClientColumn(BASE_ENTITY_ID),
                getAllClientColumn(FIRST_NAME),
                getAllClientColumn(LAST_NAME),
                getAllClientColumn(HOME_ADDRESS),
                getAllClientColumn(DOB),
                getAllClientColumn(REGISTRATION_DATE),
                getAllClientColumn(LAST_INTERACTED_WITH),
                getChildDetailsColumn(INACTIVE),
                getChildDetailsColumn(LOST_TO_FOLLOW_UP),
                getChildDetailsColumn(RELATIONAL_ID),
                getChildDetailsColumn(SHOW_BCG_SCAR),
                getChildDetailsColumn(SHOW_BCG2_REMINDER),
                "mother.first_name                     as " + AppConstants.KEY.MOTHER_FIRST_NAME,
                "mother.last_name                      as " + AppConstants.KEY.MOTHER_LAST_NAME,
                "mother.dob                            as " + MOTHER_DOB
        };
    }
}
