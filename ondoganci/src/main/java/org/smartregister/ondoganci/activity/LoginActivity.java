package org.smartregister.ondoganci.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import org.smartregister.growthmonitoring.service.intent.WeightForHeightIntentService;
import org.smartregister.location.helper.LocationHelper;
import org.smartregister.ondoganci.R;
import org.smartregister.ondoganci.application.OndoganciApplication;
import org.smartregister.ondoganci.presenter.LoginPresenter;
import org.smartregister.ondoganci.util.AppConstants;
import org.smartregister.ondoganci.util.AppUtils;
import org.smartregister.ondoganci.util.SaveSharedPreference;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.view.activity.BaseLoginActivity;
import org.smartregister.view.contract.BaseLoginContract;

public class LoginActivity extends BaseLoginActivity implements BaseLoginContract.View {

    private static final String WFH_CSV_PARSED = "WEIGHT_FOR_HEIGHT_CSV_PARSED";
    private Button login_button;
    private EditText username, password;

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        username = mLoginPresenter.getLoginView().getActivityContext()
                .findViewById(R.id.login_user_name_edit_text);
        password = mLoginPresenter.getLoginView().getActivityContext()
                .findViewById(R.id.login_password_edit_text);
        login_button = mLoginPresenter.getLoginView().getActivityContext()
                .findViewById(R.id.login_login_btn);

        if(SaveSharedPreference.getLoggedStatus(getApplicationContext())){
                    username.setText(SaveSharedPreference.getUsername(getApplicationContext()));
                    password.setText(SaveSharedPreference.getPassword(getApplicationContext()));
                    login_button.performClick();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLoginPresenter.processViewCustomizations();
        if (!mLoginPresenter.isUserLoggedOut()) {
            goToHome(false);
        }
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_login;
    }

    @Override
    protected void initializePresenter() {
        mLoginPresenter = new LoginPresenter(this);
    }

    @Override
    public void goToHome(boolean remote) {
        if (remote) {
            LocationHelper.getInstance().locationIdsFromHierarchy();
            processWeightForHeightZScoreCSV();
        }

        if(username != null && password != null){
            SaveSharedPreference.setUsername(getApplicationContext(), username.getText().toString().trim());
            SaveSharedPreference.setPassword(getApplicationContext(), password.getText().toString().trim());
        }

        if (mLoginPresenter.isServerSettingsSet()) {
            SaveSharedPreference.setLoggedIn(getApplicationContext(), true);

//            Log.d( "Login saved", "done!!!"
//                    + " username: "+SaveSharedPreference.getUsername(getApplicationContext())
//                    +" password: "+SaveSharedPreference.getPassword(getApplicationContext())
//                    +" Login Status: "+SaveSharedPreference.getLoggedStatus(getApplicationContext()));

            Intent intent = new Intent(this, ChildRegisterActivity.class);
            intent.putExtra(AppConstants.IntentKeyUtil.IS_REMOTE_LOGIN, remote);
            startActivity(intent);
        }
        finish();
    }

    @Override
    protected void attachBaseContext(android.content.Context base) {
        // get language from prefs
        String lang = AppUtils.getLanguage(base.getApplicationContext());
        super.attachBaseContext(AppUtils.setAppLocale(base, lang));
    }

    private void processWeightForHeightZScoreCSV() {
        AllSharedPreferences allSharedPreferences = OndoganciApplication.getInstance().getContext().allSharedPreferences();
        if (!allSharedPreferences.getPreference(WFH_CSV_PARSED).equals("true")) {
            WeightForHeightIntentService.startParseWFHZScores(this);
            allSharedPreferences.savePreference(WFH_CSV_PARSED, "true");
        }
    }
}
