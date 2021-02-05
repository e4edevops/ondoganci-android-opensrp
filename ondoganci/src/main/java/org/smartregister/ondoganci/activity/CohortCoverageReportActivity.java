package org.smartregister.ondoganci.activity;

import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Pair;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.commons.lang3.tuple.Triple;
import org.smartregister.immunization.db.VaccineRepo;
import org.smartregister.ondoganci.R;
import org.smartregister.ondoganci.application.OndoganciApplication;
import org.smartregister.ondoganci.contract.NavigationMenuContract;
import org.smartregister.ondoganci.domain.Cohort;
import org.smartregister.ondoganci.domain.CohortIndicator;
import org.smartregister.ondoganci.domain.CoverageHolder;
import org.smartregister.ondoganci.domain.NamedObject;
import org.smartregister.ondoganci.model.ReportGroupingModel;
import org.smartregister.ondoganci.receiver.CoverageDropoutBroadcastReceiver;
import org.smartregister.ondoganci.repository.CohortIndicatorRepository;
import org.smartregister.ondoganci.repository.CohortPatientRepository;
import org.smartregister.ondoganci.repository.CohortRepository;
import org.smartregister.child.toolbar.LocationSwitcherToolbar;
import org.smartregister.ondoganci.util.AppConstants;
import org.smartregister.ondoganci.util.AppUtils;
import org.smartregister.ondoganci.view.CustomHeightSpinner;
import org.smartregister.ondoganci.view.NavDrawerActivity;
import org.smartregister.ondoganci.view.NavigationMenu;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by keyman on 21/12/17.
 */
public class CohortCoverageReportActivity extends BaseReportActivity implements NavDrawerActivity, NavigationMenuContract {


    private NavigationMenu navigationMenu;

    @Nullable
    private String reportGrouping;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        setContentView(R.layout.activity_cohort_coverage_reports);

        createDrawer();

        Intent intent = getIntent();
        if (intent != null) {
            reportGrouping = intent.getStringExtra(AppConstants.IntentKey.REPORT_GROUPING);
        }

        // Set the title dependent on the
        TextView titleTv = findViewById(R.id.title);
        if (titleTv != null && reportGrouping != null) {
            ArrayList<ReportGroupingModel.ReportGrouping> registerModels = (new ReportGroupingModel(this)).getReportGroupings();

            String humanReadableTitle = null;

            for (ReportGroupingModel.ReportGrouping reportGroupingObj : registerModels) {
                if (reportGrouping.equals(reportGroupingObj.getGrouping())) {
                    humanReadableTitle = reportGroupingObj.getDisplayName();
                }
            }

            if (humanReadableTitle != null) {
                titleTv.setText(getString(R.string.cohort_coverage_report));
            }
        }
        ImageView reportSyncBtn = findViewById(R.id.report_sync_btn);
        if (reportSyncBtn != null) {
            reportSyncBtn.setVisibility(View.GONE);
        }

        updateListViewHeader(R.layout.coverage_report_header);
    }

    private void updateCohortSize() {
        Long size = getHolder().getSize();
        if (size == null) {
            size = 0L;
        }

        TextView textView = (TextView) findViewById(R.id.cohort_size_value);
        textView.setText(String.format(getString(R.string.cso_population_value), size));
    }

    private void updateSpinnerSize(List list) {
        if (list != null && list.size() > 12) {
            TypedValue typedValue = new TypedValue();
            CohortCoverageReportActivity.this.getTheme().resolveAttribute(android.R.attr.textAppearanceLarge, typedValue, true);

            int[] attribute = new int[]{R.attr.dropdownListPreferredItemHeight};
            TypedArray array = CohortCoverageReportActivity.this.obtainStyledAttributes(typedValue.resourceId, attribute);
            int heightOfDropoutItem = array.getDimensionPixelSize(0, -1);
            array.recycle();

            CustomHeightSpinner customHeightSpinner = (CustomHeightSpinner) findViewById(R.id.report_spinner);
            customHeightSpinner.updateHeight(heightOfDropoutItem, 12);
        }
    }

    @Override
    protected String getActionType() {
        return CoverageDropoutBroadcastReceiver.TYPE_GENERATE_COHORT_INDICATORS;
    }

    @Override
    protected int getParentNav() {
        return R.id.coverage_reports;
    }

    ////////////////////////////////////////////////////////////////
    // Reporting Methods
    ////////////////////////////////////////////////////////////////

    @Override
    protected <T> View generateView(final View view, final VaccineRepo.Vaccine vaccine, final List<T> indicators) {
        long value = 0;

        CohortIndicator cohortIndicator = retrieveCohortIndicator(indicators, vaccine);
        if (cohortIndicator != null) {
            value = cohortIndicator.getValue();
        }

        boolean finalized = isFinalized(vaccine, getHolder().getDate());

        TextView vaccinatedTextView = (TextView) view.findViewById(R.id.vaccinated);
        vaccinatedTextView.setText(String.valueOf(value));

        int percentage = 0;
        if (value > 0 && getHolder().getSize() != null && getHolder().getSize() > 0) {
            percentage = (int) (value * 100.0 / getHolder().getSize() + 0.5);
        }

        TextView coverageTextView = (TextView) view.findViewById(R.id.coverage);
        coverageTextView.setText(String.format(getString(R.string.coverage_percentage),
                percentage));

        vaccinatedTextView.setTextColor(getResources().getColor(R.color.black));
        coverageTextView.setTextColor(getResources().getColor(R.color.black));

        if (finalized) {
            vaccinatedTextView.setTextColor(getResources().getColor(R.color.bluetext));
            coverageTextView.setTextColor(getResources().getColor(R.color.bluetext));
        }
        return view;
    }

    @Override
    protected Map<String, NamedObject<?>> generateReportBackground() {

        CohortRepository cohortRepository = OndoganciApplication.getInstance().cohortRepository();
        CohortPatientRepository cohortPatientRepository = OndoganciApplication.getInstance().cohortPatientRepository();
        CohortIndicatorRepository cohortIndicatorRepository = OndoganciApplication.getInstance().cohortIndicatorRepository();

        if (cohortRepository == null || cohortPatientRepository == null || cohortIndicatorRepository == null) {
            return null;
        }

        List<Cohort> cohorts = cohortRepository.fetchAll();
        if (cohorts.isEmpty()) {
            return null;
        }

        // Populate the default cohort
        Cohort cohort = cohorts.get(0);

        long cohortSize = cohortPatientRepository.countCohort(cohort.getId());
        CoverageHolder coverageHolder = new CoverageHolder(cohort.getId(), cohort.getMonthAsDate(), cohortSize);

        List<CohortIndicator> indicators = cohortIndicatorRepository.findByCohort(cohort.getId());

        Map<String, NamedObject<?>> map = new HashMap<>();
        NamedObject<List<Cohort>> cohortsNamedObject = new NamedObject<>(Cohort.class.getName(), cohorts);
        map.put(cohortsNamedObject.name, cohortsNamedObject);

        NamedObject<CoverageHolder> cohortHolderNamedObject = new NamedObject<>(CoverageHolder.class.getName(), coverageHolder);
        map.put(cohortHolderNamedObject.name, cohortHolderNamedObject);

        NamedObject<List<CohortIndicator>> indicatorMapNamedObject = new NamedObject<>(CohortIndicator.class.getName(), indicators);
        map.put(indicatorMapNamedObject.name, indicatorMapNamedObject);

        return map;
    }

    @Override
    protected void generateReportUI(Map<String, NamedObject<?>> map, boolean userAction) {
        List<Cohort> cohorts = new ArrayList<>();
        List<CohortIndicator> indicatorList = new ArrayList<>();

        if (map.containsKey(Cohort.class.getName())) {
            NamedObject<?> namedObject = map.get(Cohort.class.getName());
            if (namedObject != null) {
                cohorts = (List<Cohort>) namedObject.object;
            }
        }

        if (map.containsKey(CoverageHolder.class.getName())) {
            NamedObject<?> namedObject = map.get(CoverageHolder.class.getName());
            if (namedObject != null) {
                setHolder((CoverageHolder) namedObject.object);
            }
        }

        if (map.containsKey(CohortIndicator.class.getName())) {
            NamedObject<?> namedObject = map.get(CohortIndicator.class.getName());
            if (namedObject != null) {
                indicatorList = (List<CohortIndicator>) namedObject.object;
            }
        }

        updateReportDates(cohorts, new SimpleDateFormat("MMM yyyy"), null, true);
        updateSpinnerSize(cohorts);
        updateCohortSize();
        updateReportList(indicatorList);
    }

    @Override
    protected Pair<List, Long> updateReportBackground(Long id) {

        CohortIndicatorRepository cohortIndicatorRepository = OndoganciApplication.getInstance().cohortIndicatorRepository();
        CohortPatientRepository cohortPatientRepository = OndoganciApplication.getInstance().cohortPatientRepository();

        if (cohortIndicatorRepository == null || cohortPatientRepository == null) {
            return null;
        }

        List indicators = cohortIndicatorRepository.findByCohort(id);
        long cohortSize = cohortPatientRepository.countCohort(id);

        return Pair.create(indicators, cohortSize);
    }

    @Override
    protected void updateReportUI(Pair<List, Long> pair, boolean userAction) {
        setHolderSize(pair.second);
        updateCohortSize();
        updateReportList(pair.first);
    }


    //................................................
    @Override
    public NavigationMenu getNavigationMenu() {
        return navigationMenu;
    }


    @Override
    protected void attachBaseContext(android.content.Context base) {
        // get language from prefs
        String lang = AppUtils.getLanguage(base.getApplicationContext());
        super.attachBaseContext(AppUtils.setAppLocale(base, lang));
    }

    @Override
    public void finishActivity() {
        finish();
    }

    @Override
    public void openDrawer() {
        if (navigationMenu != null) {
            navigationMenu.openDrawer();
        }
    }

    private void createDrawer() {
        WeakReference<CohortCoverageReportActivity> weakReference = new WeakReference<>(this);
        navigationMenu = NavigationMenu.getInstance(weakReference.get());
    }

    @Override
    public void closeDrawer() {
        if (navigationMenu != null) {
            NavigationMenu.closeDrawer();
        }
    }

}
