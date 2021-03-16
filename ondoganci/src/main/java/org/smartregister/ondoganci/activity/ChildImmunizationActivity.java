package org.smartregister.ondoganci.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.apache.commons.lang3.tuple.Triple;
import org.smartregister.child.activity.BaseChildImmunizationActivity;
import org.smartregister.child.domain.RegisterClickables;
import org.smartregister.child.toolbar.LocationSwitcherToolbar;
import org.smartregister.child.util.Constants;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.growthmonitoring.domain.HeadWrapper;
import org.smartregister.growthmonitoring.domain.HeightWrapper;
import org.smartregister.growthmonitoring.domain.Weight;
import org.smartregister.growthmonitoring.domain.WeightWrapper;
import org.smartregister.growthmonitoring.repository.WeightRepository;
import org.smartregister.immunization.domain.VaccineWrapper;
import org.smartregister.immunization.job.VaccineSchedulesUpdateJob;
import org.smartregister.location.helper.LocationHelper;
import org.smartregister.ondoganci.application.OndoganciApplication;
import org.smartregister.ondoganci.util.AppConstants;
import org.smartregister.ondoganci.util.AppUtils;
import org.smartregister.ondoganci.util.VaccineUtils;
import org.smartregister.stock.StockLibrary;
import org.smartregister.stock.repository.StockRepository;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

import static org.smartregister.immunization.domain.ServiceSchedule.standardiseCalendarDate;

public class ChildImmunizationActivity extends BaseChildImmunizationActivity {

    private LocationSwitcherToolbar myToolbar;
    public List<Weight> getWeights;
    private final StockRepository stockRepository = StockLibrary.getInstance().getStockRepository();

    @Override
    protected void attachBaseContext(Context base) {
        // get language from prefs
        String lang = AppUtils.getLanguage(base.getApplicationContext());
        super.attachBaseContext(AppUtils.setAppLocale(base, lang));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        VaccineUtils.refreshImmunizationSchedules(childDetails.getCaseId());
        myToolbar = (LocationSwitcherToolbar) this.getToolbar();
        if (myToolbar != null) {
            myToolbar.setNavigationOnClickListener(v -> finish());
        }

    }


    @Override
    protected void goToRegisterPage() {
        Intent intent = new Intent(this, ChildRegisterActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();

    }


    @Override
    public void onGrowthRecorded(WeightWrapper weightWrapper, HeightWrapper heightWrapper, HeadWrapper headWrapper) {
        super.onGrowthRecorded(weightWrapper, heightWrapper, headWrapper);
        WeightRepository weightRepository = OndoganciApplication.getInstance().weightRepository();
        sortWeights(weightRepository.findByEntityId(childDetails.entityId()));
        try {
            Weight newWeight = getWeights.get(0);
            Weight oldWelght = getWeights.get(1);
            checkWeight(newWeight, oldWelght);
        } catch (Exception e) {
            Log.e("Weight error", "Error in weight: " + e);
        }

    }

    private void sortWeights(List<Weight> weights) {
        HashMap<Long, Weight> weightHashMap = new HashMap<>();
        for (Weight curWeight : weights) {
            if (curWeight.getDate() != null) {
                Calendar curCalendar = Calendar.getInstance();
                curCalendar.setTime(curWeight.getDate());
                standardiseCalendarDate(curCalendar);

                if (!weightHashMap.containsKey(curCalendar.getTimeInMillis())) {
                    weightHashMap.put(curCalendar.getTimeInMillis(), curWeight);
                } else if (curWeight.getUpdatedAt() > weightHashMap.get(curCalendar.getTimeInMillis()).getUpdatedAt()) {
                    weightHashMap.put(curCalendar.getTimeInMillis(), curWeight);
                }
            }
        }

        List<Long> keys = new ArrayList<>(weightHashMap.keySet());
        Collections.sort(keys, Collections.<Long>reverseOrder());

        List<Weight> result = new ArrayList<>();
        for (Long curKey : keys) {
            result.add(weightHashMap.get(curKey));
        }

        this.getWeights = result;
    }

    private void checkWeight(Weight currentWeight, Weight previousWeight) {
        String current = String.valueOf(currentWeight.getKg());
        String previous = String.valueOf(previousWeight.getKg());

        if (Float.parseFloat(current) != Float.parseFloat(previous)) {
            if (Float.parseFloat(current) <= Float.parseFloat(previous)) {
                float droppedWeight = Float.parseFloat(current) - Float.parseFloat(previous);
                Toast.makeText(getActivity(), "Baby's weight has dropped by " + droppedWeight + "kg", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onVaccinateToday(ArrayList<VaccineWrapper> tags, View v) {
        super.onVaccinateToday(tags, v);
        for (int i = 0; i < tags.size(); i++) {
            VaccineDeduction(tags.get(i).getName(), true);
        }
    }

    @Override
    public void onUndoVaccination(VaccineWrapper tag, View v) {
        super.onUndoVaccination(tag, v);
        VaccineDeduction(tag.getName(), false);
    }

    @Override
    public void onVaccinateEarlier(ArrayList<VaccineWrapper> tags, View v) {
        super.onVaccinateEarlier(tags, v);

        for (int i = 0; i < tags.size(); i++) {
            VaccineDeduction(tags.get(i).getName(), true);
        }
    }

    @Override
    protected int getToolbarId() {
        return LocationSwitcherToolbar.TOOLBAR_ID;
    }

    @Override
    protected int getDrawerLayoutId() {
        return 0;
    }

    @Override
    public void launchDetailActivity(Context fromContext, CommonPersonObjectClient childDetails,
                                     RegisterClickables registerClickables) {

        Intent intent = new Intent(fromContext, ChildDetailTabbedActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(AppConstants.KEY.LOCATION_NAME,
                LocationHelper.getInstance().getOpenMrsLocationId(myToolbar.getCurrentLocation()));
        bundle.putSerializable(Constants.INTENT_KEY.EXTRA_CHILD_DETAILS, childDetails);
        bundle.putSerializable(Constants.INTENT_KEY.BASE_ENTITY_ID, childDetails.getCaseId());
        bundle.putSerializable(Constants.INTENT_KEY.EXTRA_REGISTER_CLICKABLES, registerClickables);
        intent.putExtras(bundle);

        fromContext.startActivity(intent);
    }

    @Override
    protected Activity getActivity() {
        return this;
    }

    @Override
    public boolean isLastModified() {
        OndoganciApplication application = (OndoganciApplication) getApplication();
        return application.isLastModified();
    }

    @Override
    public void setLastModified(boolean lastModified) {
        OndoganciApplication application = (OndoganciApplication) getApplication();
        if (lastModified != application.isLastModified()) {
            application.setLastModified(lastModified);
        }
    }

    @Override
    public void onClick(View view) {
        // Todo
    }

    @Override
    public void onUniqueIdFetched(Triple<String, String, String> triple, String entityId) {
        // Todo
    }

    @Override
    public void onNoUniqueId() {
        // Todo
    }

    @Override
    public void onRegistrationSaved(boolean isEdit) {
        hideProgressDialog();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) return;
    }

    @Override
    public void updateScheduleDate() {
        try {
            Calendar calendar = Calendar.getInstance();
            if (calendar.get(Calendar.HOUR_OF_DAY) != 0 && calendar.get(Calendar.HOUR_OF_DAY) != 1) {
                calendar.set(Calendar.HOUR_OF_DAY, 1);
                long hoursSince1AM = (System.currentTimeMillis() - calendar.getTimeInMillis()) / TimeUnit.HOURS.toMillis(1);
                if (VaccineSchedulesUpdateJob.isLastTimeRunLongerThan(hoursSince1AM) && !OndoganciApplication.getInstance().alertUpdatedRepository().findOne(childDetails.entityId())) {
                    super.updateScheduleDate();
                    OndoganciApplication.getInstance().alertUpdatedRepository().saveOrUpdate(childDetails.entityId());
                }
            }
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        serviceGroupCanvasLL.setVisibility(View.GONE);
    }


    public void VaccineDeduction(String vaccineName, Boolean status) {
        String stockName;
        Log.e("vaccine name", vaccineName);
        switch (vaccineName) {
            case "OPV 0":
            case "OPV 1":
            case "OPV 2":
            case "OPV 3":
            case "OPV 4":
                stockName = "OPV";
                deduction(stockName, status);
                break;
            case "PCV 1":
            case "PCV 2":
            case "PCV 3":
                stockName = "PCV";
                deduction(stockName, status);
                break;
            case "Penta 1":
            case "Penta 2":
            case "Penta 3":
                stockName = "Penta";
                deduction(stockName, status);
                break;
            case "BCG":
                stockName = "BCG";
                deduction(stockName, status);
                break;
            case "HepB":
                stockName = "Hepatitis B";
                deduction(stockName, status);
                break;
            case "Vitamin A 0":
            case "Vitamin A 1":
                stockName = "Vitamin A";
                deduction(stockName, status);
                break;
            case "Measles 1":
            case "MR 1":
                stockName = "M/MR";
                deduction(stockName, status);
                break;
        }
    }

    private void deduction(String stockName, boolean status) {
        int vials = stockRepository.getBalanceFromNameAndDate(stockName, System.currentTimeMillis());
        vials = status ? --vials : ++vials;
        final int final_vials = vials;
        AsyncTask.execute(
                () -> stockRepository.addNewStockVials(stockName, String.valueOf(final_vials))
        );
    }

}
