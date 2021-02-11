package org.smartregister.ondoganci.util;

import com.google.android.gms.vision.L;

public class BuildConfig {
    public static final String VERSION_NAME = "1.0.0";
    public static final int VERSION_CODE = 1;
    public static final boolean DEBUG = false;
    public static final long BUILD_TIMESTAMP = System.currentTimeMillis();
    public static final long EVENT_VERSION= System.currentTimeMillis();
    public static final int MAX_SYNC_RETRIES = 3;
    public static final String SYNC_TYPE = "location";
    public static final boolean IS_SYNC_SETTINGS = false;
    public static final int OPENMRS_UNIQUE_ID_INITIAL_BATCH_SIZE = 10;
    public static final int OPENMRS_UNIQUE_ID_BATCH_SIZE = 10;
    public static final int OPENMRS_UNIQUE_ID_SOURCE = 2;
    public static final int VACCINE_SYNC_TIME = 0;
    public static final int DATABASE_VERSION = 11;
    public static final long MAX_SERVER_TIME_DIFFERENCE = 1800000l;
    public static final boolean TIME_CHECK = true;
    public static final int DATA_SYNC_DURATION_MINUTES = 15;
    public static final int VACCINE_SYNC_PROCESSING_MINUTES = 30;
    public static final int IMAGE_UPLOAD_MINUTES = 180;
    public static final int PULL_UNIQUE_IDS_MINUTES = 180;
    public static final int VIEW_SYNC_CONFIGURATIONS_MINUTES = 15;
    public static final int CLIENT_SETTINGS_SYNC_MINUTES = 15;
    public static final int GROWTH_MONITORING_SYNC_TIME = 15;
    public static final int RECURRING_SERVICES_SYNC_PROCESSING_MINUTES = 30;
    public static final int DAILY_TALLIES_GENERATION_MINUTES = 60;
    public static final int COVERAGE_DROPOUT_GENERATION_MINUTES = 60;
    public static final String[] LOCATION_LEVELS = {"Country", "Province", "District", "County", "Sub-county", "Health Facility"};
    public static final String[] HEALTH_FACILITY_LEVELS = {"Country", "Province", "District", "County", "Sub-county", "Health Facility"};
    public static final String[] ALLOWED_LEVELS = {"Health Facility"};
    public static final String DEFAULT_LOCATION = "Health Facility";
}
