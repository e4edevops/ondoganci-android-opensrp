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
                "join " + getDemographicTable() + " on " + getDemographicTable() + "." + Constants.KEY.BASE_ENTITY_ID + " = " + getChildDetailsTable() + "." + Constants.KEY.BASE_ENTITY_ID + " " +
                "join " + getDemographicTable() + " mother on mother." + Constants.KEY.BASE_ENTITY_ID + " = " + getMotherDetailsTable() + "." + Constants.KEY.BASE_ENTITY_ID;
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
                getChildDetailsColumn(RELATIONALID),
                getAllClientColumn(GENDER),
                getAllClientColumn(BASE_ENTITY_ID),
                getAllClientColumn(FIRST_NAME),
                getAllClientColumn(LAST_NAME),
                "mother.first_name                     as " + AppConstants.KEY.MOTHER_FIRST_NAME,
                "mother.last_name                      as " + AppConstants.KEY.MOTHER_LAST_NAME,
                getAllClientColumn(DOB),
                "mother." + Constants.KEY.DOB_UNKNOWN + " as " + Constants.KEY.MOTHER_DOB_UNKNOWN,
                "mother.dob                            as " + MOTHER_DOB,
                getMotherDetailsColumn(NRC_NUMBER) + " as mother_nrc_number",
                getMotherDetailsColumn(FATHER_NAME),
                getMotherDetailsColumn(EPI_CARD_NUMBER),
                getAllClientColumn(CLIENT_REG_DATE),
                getChildDetailsColumn(PMTCT_STATUS),
                getAllClientColumn(LAST_INTERACTED_WITH),
                getChildDetailsColumn(INACTIVE),
                getChildDetailsColumn(LOST_TO_FOLLOW_UP),
                getChildDetailsColumn(BIRTH_WEIGHT),
                getChildDetailsColumn(BIRTH_HEIGHT),
                getChildDetailsColumn(BIRTH_HEAD),
                getAllClientColumn(REGISTRATION_DATE),
                getChildDetailsColumn(SHOW_BCG_SCAR),
                getChildDetailsColumn(SHOW_BCG2_REMINDER),
                getChildDetailsColumn(FIRST_HEALTH_FACILITY_CONTACT),
                getChildDetailsColumn(MOTHER_GUARDIAN_PHONE_NUMBER),
                getDemographicTable() + "." + "address1",
                getDemographicTable() + "." + "residential_area",
                getDemographicTable() + "." + "residential_area_other",
                getDemographicTable() + "." + "residential_address",
                getDemographicTable() + "." + "address",
        };
    }
}
