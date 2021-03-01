package org.smartregister.ondoganci.util;

import org.smartregister.ondoganci.BuildConfig;

public class AppConstants {


    public static final String MOTHER_DEFAULT_DOB = "01-01-1960";

    public static final int OPENMRS_UNIQUE_ID_INITIAL_BATCH_SIZE = BuildConfig.OPENMRS_UNIQUE_ID_INITIAL_BATCH_SIZE;
    public static final int OPENMRS_UNIQUE_ID_BATCH_SIZE = BuildConfig.OPENMRS_UNIQUE_ID_BATCH_SIZE;
    public static final int OPENMRS_UNIQUE_ID_SOURCE = BuildConfig.OPENMRS_UNIQUE_ID_SOURCE;
    public static final long MAX_SERVER_TIME_DIFFERENCE = BuildConfig.MAX_SERVER_TIME_DIFFERENCE;
    public static final boolean TIME_CHECK = BuildConfig.TIME_CHECK;

    public static final String REACTION_VACCINE = "Reaction_Vaccine";

    public interface LOCALE {
        String ARABIC_LOCALE = "ar";
    }

    public static final String CHILD_TABLE_NAME = "ec_child";
    public static final String MOTHER_TABLE_NAME = "ec_mother";
    public static final String CURRENT_LOCATION_ID = "CURRENT_LOCATION_ID";

    public static final class KEY {
        public static final String MOTHER_BASE_ENTITY_ID = "mother_base_entity_id";
        public static final String CHILD = "child";
        public static final String MOTHER_FIRST_NAME = "mother_first_name";
        public static final String FATHER_FIRST_NAME = "father_first_name";
        public static final String FATHER_LAST_NAME = "father_last_name";
        public static final String FIRST_NAME = "first_name";
        public static final String LAST_NAME = "last_name";
        public static final String BIRTHDATE = "birthdate";
        public static final String DEATHDATE = "deathdate";
        public static final String MOTHER_LAST_NAME = "mother_last_name";
        public static final String MOTHER_GUARDIAN_PHONE_NUMBER = "mother_guardian_phone_number";
        public static final String FIRST_HEALTH_FACILITY_CONTACT = "first_health_facility_contact";
        public static final String ZEIR_ID = "zeir_id";
        public static final String LOST_TO_FOLLOW_UP = "lost_to_follow_up";
        public static final String GENDER = "gender";
        public static final String INACTIVE = "inactive";
        public static final String LAST_INTERACTED_WITH = "last_interacted_with";
        public static final String MOTHER = "mother";
        public static final String ENTITY_ID = "entity_id";
        public static final String VALUE = "value";
        public static final String STEPNAME = "stepName";
        public static final String TITLE = "title";
        public static final String HIA_2_INDICATOR = "hia2_indicator";
        public static final String RELATIONALID = "relationalid";
        public static final String RELATIONAL_ID = "relational_id";
        public static final String ID_LOWER_CASE = "_id";
        public static final String BASE_ENTITY_ID = "base_entity_id";
        public static final String DOB = "dob";//Date Of Birth
        public static final String DOD = "dod";//Date Of Death
        public static final String DATE_REMOVED = "date_removed";
        public static final String MOTHER_NRC_NUMBER = "nrc_number";
        public static final String SECOND_PHONE_NUMBER = "second_phone_number";
        public static final String VIEW_CONFIGURATION_PREFIX = "ViewConfiguration_";
        public static final String HOME_FACILITY = "home_address";
        public static final String APP_ID = "mer_id";
        public static final String MIDDLE_NAME = "middle_name";
        public static final String ADDRESS_3 = "address3";
        public static final String BIRTH_FACILITY_NAME = "Birth_Facility_Name";
        public static final String RESIDENTIAL_AREA = "Residential_Area";
        public static final String ENCOUNTER_TYPE = "encounter_type";
        public static final String BIRTH_REGISTRATION = "Birth Registration";
        public static final String IDENTIFIERS = "identifiers";
        public static final String FIRSTNAME = "firstName";
        public static final String MIDDLENAME = "middleName";
        public static final String LASTNAME = "lastName";
        public static final String ATTRIBUTES = "attributes";
        public static final String VILLAGE = "village";
        public static final String HOME_ADDRESS = "home_address";
        public static final String DOB_UNKNOWN = "dob_unknown";
        public static final String DATE_BIRTH = "Date_Birth";
        public static final String PMTCT_STATUS = "pmtct_status";
        public static final String BIRTH_WEIGHT = "Birth_Weight";
        public static final String BIRTH_HEIGHT = "Birth_Height";
        public static final String BIRTH_HEAD = "Birth_Head_Circumference";
        public static final String NRC_NUMBER = "nrc_number";
        public static final String FATHER_NAME = "father_name";
        public static final String EPI_CARD_NUMBER = "epi_card_number";
        public static final String CLIENT_REG_DATE = "client_reg_date";
        public static final String CARD_ID = "card_id";
        public static final String MOTHER_PHONE_NUMBER = "mother_phone_number";
        public static final String MOTHER_SECOND_PHONE_NUMBER = "mother_second_phone_number";
        public static final String FATHER_PHONE_NUMBER = "father_phone_number";
        public static final String MOTHER_DOB = "mother_dob";
        public static final String FATHER_DOB = "father_dob";
        public static final String M_ZEIR_ID = "M_ZEIR_ID";
        public static final String FATHER_BASE_ENTITY_ID = "father_base_entity_id";
        public static final String FATHER = "father";
        public static final String TODAY = "today";
        public static String SITE_CHARACTERISTICS = "site_characteristics";
        public static String REGISTRATION_DATE = "client_reg_date";
        public static final String FIELDS = "fields";
        public static final String KEY = "key";
        public static final String IS_VACCINE_GROUP = "is_vaccine_group";
        public static final String OPTIONS = "options";
        public static final String MOTHER_NATIONALITY = "mother_nationality";
        public static final String FIRST_BIRTH= "first_birth";
        public static final String RUBELLA_SEROLOGY= "rubella_serology";
        public static final String SEROLOGY_RESULTS= "serology_results";
        public static final String MOTHER_RUBELLA= "mother_rubella";
        public static final String FATHER_NATIONALITY = "father_nationality";
        public static final String FATHER_RELATIONAL_ID= "father_relational_id";
        public static final String MOTHER_NATIONALITY_OTHER = "mother_nationality_other";
        public static final String FATHER_NATIONALITY_OTHER = "father_nationality_other";
        public static final String MOTHER_GUARDIAN_NUMBER = "mother_guardian_number";
        public static final String FATHER_PHONE = "father_phone";
        public static final String MOTHER_TDV_DOSES = "mother_tdv_doses";
        public static final String PROTECTED_AT_BIRTH = "protected_at_birth";
        public static final String SHOW_BCG_SCAR = "show_bcg_scar";
        public static final String SHOW_BCG2_REMINDER = "show_bcg2_reminder";
        public static final String BIRTH_REGISTRATION_NUMBER  = "birth_registration_number";
        public static final String ID  = "id";
        public static final String CHILD_REG = "child_reg";
        public static final String GA_AT_BIRTH = "ga_at_birth";
        public static final String PLACE_OF_BIRTH = "place_of_birth";
        public static final String LOCATION_NAME = "location_name";
    }

    public static final class DrawerMenu {
        public static final String ALL_FAMILIES = "All Families";
        public static final String ALL_CLIENTS = "All Clients";
        public static final String ANC_CLIENTS = "ANC Clients";
        public static final String CHILD_CLIENTS = "Child Clients";
        public static final String ANC = "ANC";
    }

    public static final class FormTitleUtil {
        public static final String UPDATE_CHILD_FORM = "Update Child Registration";
    }

    public static class CONFIGURATION {
        public static final String LOGIN = "login";
        public static final String CHILD_REGISTER = "child_register";

    }

    public static final class EventType {
        public static final String CHILD_REGISTRATION = "Birth Registration";
        public static final String UPDATE_CHILD_REGISTRATION = "Update Birth Registration";
        public static final String OUT_OF_CATCHMENT = "Out of Catchment";
        public static final String ADVERSE_EFFECTS = "adverse_effects";
    }

    public static class JSON_FORM {
        public static String CHILD_ENROLLMENT = "child_enrollment";
        public static String OUT_OF_CATCHMENT_SERVICE = "out_of_catchment_service";

    }

    public static class RELATIONSHIP {
        public static final String MOTHER = "mother";
        public static final String FATHER = "father";
    }

    public static class TABLE_NAME {
        public static final String ALL_CLIENTS = "ec_client";
        public static final String REGISTER_TYPE = "client_register_type";
        public static final String CHILD_UPDATED_ALERTS = "child_updated_alerts";
        public static final String FATHER_DETAILS = "ec_father_details";
        public static final String MOTHER_DETAILS = "ec_mother_details";
        public static final String CHILD_DETAILS = "ec_child_details";
    }

    public interface Columns {
        interface RegisterType {
            String BASE_ENTITY_ID = "base_entity_id";
            String REGISTER_TYPE = "register_type";
            String DATE_REMOVED = "date_removed";
            String DATE_CREATED = "date_created";
        }
    }

    public static final class ServiceType {

        public static final int AUTO_SYNC = 1;
        public static final int DAILY_TALLIES_GENERATION = 2;
        public static final int COVERAGE_DROPOUT_GENERATION = 3;
        public static final int PULL_UNIQUE_IDS = 4;
        public static final int VACCINE_SYNC_PROCESSING = 5;
        public static final int WEIGHT_SYNC_PROCESSING = 6;
        public static final int RECURRING_SERVICES_SYNC_PROCESSING = 7;
        public static final int IMAGE_UPLOAD = 8;

    }

    public static final class EntityType {
        public static final String CHILD = "child";
    }

    public class IntentKeyUtil {
        public static final String IS_REMOTE_LOGIN = "is_remote_login";
    }

    public interface RegisterType {
        String ANC = "anc";
        String CHILD = "child";
        String OPD = "opd";
    }

    public interface MultiResultProcessor {
        String GROUPING_SEPARATOR = "_";
    }

    public interface IntentKey {
        String REPORT_GROUPING = "report-grouping";
    }

    public interface Pref {
        String APP_VERSION_CODE = "APP_VERSION_CODE";
        String INDICATOR_DATA_INITIALISED = "INDICATOR_DATA_INITIALISED";
    }

    public interface File {
        String INDICATOR_CONFIG_FILE = "config/indicator-definitions.yml";
    }

    public interface ConditionalVaccines {
        String PRETERM_VACCINES = "preterm_vaccines";
    }
}
