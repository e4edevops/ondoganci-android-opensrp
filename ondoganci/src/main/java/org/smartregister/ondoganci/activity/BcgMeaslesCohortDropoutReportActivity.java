package org.smartregister.ondoganci.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.util.Pair;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.commons.lang3.tuple.Triple;
import org.smartregister.immunization.db.VaccineRepo;
import org.smartregister.ondoganci.contract.NavigationMenuContract;
import org.smartregister.ondoganci.domain.NamedObject;
import org.smartregister.ondoganci.R;
import org.smartregister.ondoganci.adapter.ExpandedListAdapter;
import org.smartregister.ondoganci.model.ReportGroupingModel;
import org.smartregister.ondoganci.receiver.CoverageDropoutBroadcastReceiver;
import org.smartregister.ondoganci.util.AppConstants;
import org.smartregister.ondoganci.util.AppUtils;
import org.smartregister.ondoganci.view.NavDrawerActivity;
import org.smartregister.ondoganci.view.NavigationMenu;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by keyman on 09/01/18.
 */
public class BcgMeaslesCohortDropoutReportActivity extends BaseReportActivity implements NavDrawerActivity, NavigationMenuContract {
    private ExpandableListView expandableListView;

    private NavigationMenu navigationMenu;

    @Nullable
    private String reportGrouping;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        setContentView(R.layout.activity_dropout_report_template);

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
                titleTv.setText(getString(R.string.bcg_measles_cohort_dropout_report));
            }
        }
        ImageView reportSyncBtn = findViewById(R.id.report_sync_btn);
        if (reportSyncBtn != null) {
            reportSyncBtn.setVisibility(View.GONE);
        }

        expandableListView = (ExpandableListView) findViewById(R.id.expandable_list_view);
        expandableListView.setDivider(null);
        expandableListView.setDividerHeight(0);
    }

    @Override
    protected String getActionType() {
        return CoverageDropoutBroadcastReceiver.TYPE_GENERATE_COHORT_INDICATORS;
    }

    @Override
    protected int getParentNav() {
        return R.id.dropout_reports;
    }

    @SuppressWarnings("unchecked")
    private void updateExpandableList(final LinkedHashMap<String, List<ExpandedListAdapter.ItemData<Triple<String, String, String>, Date>>> map) {
        ExpandedListAdapter<Pair<String, String>, Triple<String, String, String>, Date> expandableListAdapter = new ExpandedListAdapter(BcgMeaslesCohortDropoutReportActivity.this, map, R.layout.dropout_report_cohort_bcg_header, R.layout.dropout_report_item);
        expandableListAdapter.setChildSelectable(false);
        expandableListView.setAdapter(expandableListAdapter);
        expandableListAdapter.notifyDataSetChanged();
    }

    @Override
    protected Map<String, NamedObject<?>> generateReportBackground() {
        LinkedHashMap<String, List<ExpandedListAdapter.ItemData<Triple<String, String, String>, Date>>> linkedHashMap = generateCohortDropoutMap(VaccineRepo.Vaccine.bcg, VaccineRepo.Vaccine.measles1);
        NamedObject<LinkedHashMap<String, List<ExpandedListAdapter.ItemData<Triple<String, String, String>, Date>>>> linkedHashMapNamedObject = new NamedObject<>(LinkedHashMap.class.getName(), linkedHashMap);

        Map<String, NamedObject<?>> map = new HashMap<>();
        map.put(linkedHashMapNamedObject.name, linkedHashMapNamedObject);
        return map;
    }

    @Override
    protected void generateReportUI(Map<String, NamedObject<?>> map, boolean userAction) {
        LinkedHashMap<String, List<ExpandedListAdapter.ItemData<Triple<String, String, String>, Date>>> linkedHashMap = new LinkedHashMap<>();

        if (map.containsKey(LinkedHashMap.class.getName())) {
            NamedObject<?> namedObject = map.get(LinkedHashMap.class.getName());
            if (namedObject != null) {
                linkedHashMap = (LinkedHashMap<String, List<ExpandedListAdapter.ItemData<Triple<String, String, String>, Date>>>) namedObject.object;
            }
        }

        updateExpandableList(linkedHashMap);
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
        WeakReference<BcgMeaslesCohortDropoutReportActivity> weakReference = new WeakReference<>(this);
        navigationMenu = NavigationMenu.getInstance(weakReference.get());
    }

    @Override
    public void closeDrawer() {
        if (navigationMenu != null) {
            NavigationMenu.closeDrawer();
        }
    }

}
