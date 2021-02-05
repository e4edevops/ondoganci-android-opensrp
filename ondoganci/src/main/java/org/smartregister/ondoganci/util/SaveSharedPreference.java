package org.smartregister.ondoganci.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import static org.smartregister.ondoganci.util.PreferencesUtility.LOGGED_IN_PREF;
import static org.smartregister.ondoganci.util.PreferencesUtility.PASS_WORD;
import static org.smartregister.ondoganci.util.PreferencesUtility.PREFERRED_CONTACT;
import static org.smartregister.ondoganci.util.PreferencesUtility.USER_NAME;

public class SaveSharedPreference {

//    public static String preferenceName = "Ondoganci_login";
    static SharedPreferences getPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * Set the Login Status
     * @param context
     * @param loggedIn
     */
    public static void setLoggedIn(Context context, boolean loggedIn) {
//        SharedPreferences.Editor editor = context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE).edit();
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putBoolean(LOGGED_IN_PREF, loggedIn);
        editor.apply();
    }

    public static void setUsername(Context context, String username) {
//        SharedPreferences.Editor editor = context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE).edit();
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putString(USER_NAME, username);
        editor.apply();
    }

    public static void setPassword(Context context, String password) {
//        SharedPreferences.Editor editor = context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE).edit();
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putString(PASS_WORD, password);
        editor.apply();
    }

    public static void setPreferredContact(Context context, String preferredContact) {
//        SharedPreferences.Editor editor = context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE).edit();
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putString(PREFERRED_CONTACT, preferredContact);
        editor.apply();
    }

    /**
     * Get the Login Status
     * @param context
     * @return boolean: login status
     */
    public static boolean getLoggedStatus(Context context) {
//        return context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE).getBoolean(LOGGED_IN_PREF, false);
        return getPreferences(context).getBoolean(LOGGED_IN_PREF, false);
    }

    public static String getUsername(Context context) {
//        return context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE).getString(USER_NAME, "");
        return getPreferences(context).getString(USER_NAME, "");
    }

    public static String getPassword(Context context) {
//        return context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE).getString(PASS_WORD, "");
        return getPreferences(context).getString(PASS_WORD, "");
    }

    public static String getPreferredContact(Context context) {
//        return context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE).getString(PREFERRED_CONTACT, "");
        return getPreferences(context).getString(PREFERRED_CONTACT, "");
    }
}