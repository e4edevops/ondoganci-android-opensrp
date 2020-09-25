package org.smartregister.ondoganci.util;

public class TableUtil {

    public static String getAllClientColumn(String column) {
        return getColumn(AppConstants.TABLE_NAME.ALL_CLIENTS, column);
    }

    public static String getMotherDetailsColumn(String column) {
        return getColumn(AppConstants.TABLE_NAME.MOTHER_DETAILS, column);
    }

    public static String getFatherDetailsColumn(String column) {
        return getColumn(AppConstants.TABLE_NAME.FATHER_DETAILS, column);
    }

    public static String getChildDetailsColumn(String column) {
        return getColumn(AppConstants.TABLE_NAME.CHILD_DETAILS, column);
    }

    private static String getColumn(String tableName, String column) {
        return String.format("%s.%s ", tableName, column);
    }
}
