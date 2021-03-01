package org.smartregister.ondoganci.util;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.google.common.reflect.TypeToken;
import com.vijay.jsonwizard.constants.JsonFormConstants;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.child.ChildLibrary;
import org.smartregister.child.util.Constants;
import org.smartregister.child.util.JsonFormUtils;
import org.smartregister.child.util.Utils;
import org.smartregister.domain.form.FormLocation;
import org.smartregister.location.helper.LocationHelper;
import org.smartregister.util.AssetHandler;
import org.smartregister.util.FormUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

import static org.smartregister.util.Utils.getValue;

public class AppJsonFormUtils extends JsonFormUtils {

    private static Map<String, String> detailsMap;
    public static final String PMTCT_STATUS_LOWER_CASE = "pmtct_status";

    public static String updateJsonFormWithClientDetails(Context context, Map<String, String> childDetails, List<String> nonEditableFields) {

        try {
            JSONObject birthRegistrationForm = FormUtils.getInstance(context)
                    .getFormJson(Utils.metadata().childRegister.formName);
            updateRegistrationEventType(birthRegistrationForm, childDetails);
            JsonFormUtils.addChildRegLocHierarchyQuestions(birthRegistrationForm);

            birthRegistrationForm.put(JsonFormUtils.ENTITY_ID, childDetails.get(Constants.KEY.BASE_ENTITY_ID));
            birthRegistrationForm.put(JsonFormUtils.RELATIONAL_ID, childDetails.get(RELATIONAL_ID));
//            birthRegistrationForm.put(AppConstants.KEY.FATHER_RELATIONAL_ID, childDetails.get(AppConstants.KEY.FATHER_RELATIONAL_ID));

            birthRegistrationForm.put(JsonFormUtils.CURRENT_ZEIR_ID,
                    Utils.getValue(childDetails, AppConstants.KEY.ZEIR_ID, true).replace("-", ""));
//            birthRegistrationForm.put(JsonFormUtils.CURRENT_OPENSRP_ID,
//                    Utils.getValue(childDetails, Constants.JSON_FORM_KEY.UNIQUE_ID, false));

            JSONObject metadata = birthRegistrationForm.getJSONObject(JsonFormUtils.METADATA);
            metadata.put(JsonFormUtils.ENCOUNTER_LOCATION, ChildLibrary.getInstance()
                    .getLocationPickerView(context).getSelectedItem());

            //inject zeir id into the birthRegistrationForm
            JSONObject stepOne = birthRegistrationForm.getJSONObject(JsonFormUtils.STEP1);
            JSONArray jsonArray = stepOne.getJSONArray(JsonFormUtils.FIELDS);
            updateFormDetailsForEdit(childDetails, jsonArray, nonEditableFields);
            return birthRegistrationForm.toString();
        } catch (Exception e) {
            Timber.e(e, "AppJsonFormUtils --> getMetadataForEditForm");
        }

        return "";
    }

    private static void updateFormDetailsForEdit(Map<String, String> childDetails, JSONArray jsonArray, List<String> nonEditableFields)
            throws JSONException {
        String prefix;
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            prefix = getPrefix(jsonObject);

            if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase(Constants.KEY.PHOTO)) {
                processPhoto(childDetails.get(Constants.KEY.BASE_ENTITY_ID), jsonObject);
            } else if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase(AppConstants.KEY.DOB_UNKNOWN)) {
                getDobUnknown(childDetails, jsonObject);
            } else if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase(Constants.JSON_FORM_KEY.AGE)) {
                processAge(Utils.getValue(childDetails, AppConstants.KEY.DOB, false), jsonObject);
            } else if (jsonObject.getString(JsonFormConstants.TYPE).equalsIgnoreCase(JsonFormConstants.DATE_PICKER)) {
                processDate(childDetails, prefix, jsonObject);
            } else if (jsonObject.getString(JsonFormUtils.OPENMRS_ENTITY).equalsIgnoreCase(JsonFormUtils.PERSON_INDENTIFIER)) {
                jsonObject.put(JsonFormUtils.VALUE, Utils.getValue(childDetails,
                        jsonObject.getString(JsonFormUtils.OPENMRS_ENTITY_ID).toLowerCase(), true).replace("-", ""));
            } else if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase(AppConstants.KEY.MIDDLE_NAME)) {
                String middleName = Utils.getValue(childDetails, AppConstants.KEY.MIDDLE_NAME, true);
                jsonObject.put(JsonFormUtils.VALUE, middleName);
            } else if (jsonObject.has(JsonFormConstants.TREE)) {
                processLocationTree(childDetails, jsonObject);
            } else if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase("mother_guardian_first_name")) {
                String motherFirstName = Utils.getValue(childDetails, AppConstants.KEY.MOTHER_FIRST_NAME, true);
                jsonObject.put(JsonFormUtils.VALUE, motherFirstName);
            } else if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase("mother_guardian_last_name")) {
                String motherLastName = Utils.getValue(childDetails, AppConstants.KEY.MOTHER_LAST_NAME, true);
                jsonObject.put(JsonFormUtils.VALUE, motherLastName);
            } else if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase(AppConstants.KEY.FATHER_FIRST_NAME)) {
                String fatherFirstName = Utils.getValue(childDetails, AppConstants.KEY.FATHER_FIRST_NAME, true);
                jsonObject.put(JsonFormUtils.VALUE, fatherFirstName);
            } else if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase(AppConstants.KEY.FATHER_LAST_NAME)) {
                String fatherLastName = Utils.getValue(childDetails, AppConstants.KEY.FATHER_LAST_NAME, true);
                jsonObject.put(JsonFormUtils.VALUE, fatherLastName);
            } else if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase("mother_guardian_number")) {
                String motherPhoneNumber = Utils.getValue(childDetails, "mother_phone_number", true);
                jsonObject.put(JsonFormUtils.VALUE, motherPhoneNumber);
            } else if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase(AppConstants.KEY.FATHER_PHONE)) {
                String fatherPhoneNumber = Utils.getValue(childDetails, "father_phone_number", true);
                jsonObject.put(JsonFormUtils.VALUE, fatherPhoneNumber);
            } else if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase(AppConstants.KEY.SECOND_PHONE_NUMBER)) {
                String secondPhone = Utils.getValue(childDetails, "mother_second_phone_number", true);
                jsonObject.put(JsonFormUtils.VALUE, secondPhone);
            } else if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase("Sex")) {
                jsonObject.put(JsonFormUtils.VALUE, childDetails.get(JsonFormUtils.GENDER));
            } else if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase(AppConstants.KEY.BIRTH_WEIGHT)) {
                jsonObject.put(JsonFormUtils.VALUE, childDetails.get(AppConstants.KEY.BIRTH_WEIGHT.toLowerCase()));
            } else {
                jsonObject.put(JsonFormUtils.VALUE, childDetails.get(jsonObject.optString(JsonFormUtils.KEY)));
            }
            jsonObject.put(JsonFormUtils.READ_ONLY, nonEditableFields.contains(jsonObject.getString(JsonFormUtils.KEY)));
        }
    }

    private static void getDobUnknown(Map<String, String> childDetails, JSONObject jsonObject) throws JSONException {
        JSONObject optionsObject = jsonObject.getJSONArray(Constants.JSON_FORM_KEY.OPTIONS).getJSONObject(0);
        optionsObject.put(JsonFormUtils.VALUE,
                Utils.getValue(childDetails, AppConstants.KEY.DOB_UNKNOWN, false));
    }

    @NotNull
    private static String getPrefix(JSONObject jsonObject) throws JSONException {
        String prefix = "";
        if (jsonObject.has(JsonFormUtils.ENTITY_ID)) {
            String entityId = jsonObject.getString(JsonFormUtils.ENTITY_ID);
            if (!TextUtils.isEmpty(entityId) && entityId.equalsIgnoreCase(Constants.KEY.MOTHER))
                prefix = "mother_";
            else if (!TextUtils.isEmpty(entityId) && entityId.equalsIgnoreCase(Constants.KEY.FATHER))
                prefix = "father_";
        }
        return prefix;
    }

    private static void processLocationTree(Map<String, String> childDetails, JSONObject jsonObject) throws JSONException {
        updateHomeFacilityHierarchy(childDetails, jsonObject);
    }

    private static void updateHomeFacilityHierarchy(Map<String, String> childDetails, JSONObject jsonObject) throws JSONException {
        if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase(AppConstants.KEY.HOME_FACILITY)) {
            List<String> homeFacilityHierarchy = LocationHelper.getInstance()
                    .getOpenMrsLocationHierarchy(Utils.getValue(childDetails,
                            AppConstants.KEY.HOME_FACILITY, false), false);
            String homeFacilityHierarchyString = AssetHandler
                    .javaToJsonString(homeFacilityHierarchy, new TypeToken<List<String>>() {
                    }.getType());
            ArrayList<String> allLevels = AppUtils.getHealthFacilityLevels();
            List<FormLocation> entireTree = LocationHelper.getInstance().generateLocationHierarchyTree(true, allLevels);
            String entireTreeString = AssetHandler.javaToJsonString(entireTree, new TypeToken<List<FormLocation>>() {
            }.getType());
            if (StringUtils.isNotBlank(homeFacilityHierarchyString)) {
                jsonObject.put(JsonFormUtils.VALUE, homeFacilityHierarchyString);
                jsonObject.put(JsonFormConstants.TREE, new JSONArray(entireTreeString));
            }
        }
    }

    private static void updateRegistrationEventType(JSONObject form, Map<String, String> childDetails) throws JSONException {
        if (form.has(JsonFormUtils.ENCOUNTER_TYPE) && form.getString(JsonFormUtils.ENCOUNTER_TYPE)
                .equals(Constants.EventType.BITRH_REGISTRATION)) {
            form.put(JsonFormUtils.ENCOUNTER_TYPE, Constants.EventType.UPDATE_BITRH_REGISTRATION);
        }

        if (form.has(JsonFormUtils.STEP1) && form.getJSONObject(JsonFormUtils.STEP1).has(AppConstants.KEY.TITLE) && form.getJSONObject(JsonFormUtils.STEP1).getString(AppConstants.KEY.TITLE)
                .equals(Constants.EventType.BITRH_REGISTRATION)) {
            form.getJSONObject(JsonFormUtils.STEP1).put(AppConstants.KEY.TITLE, AppConstants.FormTitleUtil.UPDATE_CHILD_FORM);
        }

        //Update father details if it exists or create a new one
        if (form.has(Constants.KEY.FATHER) && childDetails.containsKey(AppConstants.KEY.FATHER_RELATIONAL_ID)
                && childDetails.get(AppConstants.KEY.FATHER_RELATIONAL_ID) != null){
            form.getJSONObject(Constants.KEY.FATHER).put(ENCOUNTER_TYPE, Constants.EventType.UPDATE_FATHER_DETAILS);
        }
        if (form.has(Constants.KEY.MOTHER)){
            form.getJSONObject(Constants.KEY.MOTHER).put(ENCOUNTER_TYPE, Constants.EventType.UPDATE_MOTHER_DETAILS);
        }
    }
}
