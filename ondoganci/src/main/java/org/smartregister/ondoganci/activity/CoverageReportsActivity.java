package org.smartregister.ondoganci.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.commons.lang3.tuple.Triple;
import org.smartregister.child.activity.BaseActivity;
import org.smartregister.domain.FetchStatus;
import org.smartregister.ondoganci.R;
import org.smartregister.child.toolbar.LocationSwitcherToolbar;
import org.smartregister.ondoganci.contract.NavigationMenuContract;
import org.smartregister.ondoganci.model.ReportGroupingModel;
import org.smartregister.ondoganci.util.AppConstants;
import org.smartregister.ondoganci.util.AppUtils;
import org.smartregister.ondoganci.view.NavDrawerActivity;
import org.smartregister.ondoganci.view.NavigationMenu;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by keyman on 18/12/17.
 */
public class CoverageReportsActivity extends AppCompatActivity implements NavDrawerActivity, NavigationMenuContract {

    private NavigationMenu navigationMenu;
    @Nullable
    private String reportGrouping;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coverage_reports);

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
                titleTv.setText(humanReadableTitle + " " + getString(R.string.reports));
            }
        }
        ImageView reportSyncBtn = findViewById(R.id.report_sync_btn);
        if (reportSyncBtn != null) {
            reportSyncBtn.setVisibility(View.GONE);
        }

        ListView listView = (ListView) findViewById(R.id.list_view);

        List<String> list = new ArrayList<>();
        list.add(getString(R.string.cohort_coverage_report));
        list.add(getString(R.string.annual_coverage_report_cso));
        list.add(getString(R.string.annual_coverage_report_ondo));

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(CoverageReportsActivity.this, R.layout.coverage_reports_item, R.id.tv, list);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        Intent intent = new Intent(CoverageReportsActivity.this, CohortCoverageReportActivity.class);
                        startActivity(intent);
                        break;
                    case 1:
                        intent = new Intent(CoverageReportsActivity.this, AnnualCoverageReportCsoActivity.class);
                        startActivity(intent);
                        break;
                    case 2:
                        intent = new Intent(CoverageReportsActivity.this, AnnualCoverageReportOndoGanciActivity.class);
                        startActivity(intent);
                        break;
                    default:
                        break;
                }
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        LinearLayout hia2 = (LinearLayout) drawer.findViewById(R.id.coverage_reports);
        hia2.setBackgroundColor(getResources().getColor(R.color.tintcolor));
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
        WeakReference<CoverageReportsActivity> weakReference = new WeakReference<>(this);
        navigationMenu = NavigationMenu.getInstance(weakReference.get());
    }

    @Override
    public void closeDrawer() {
        if (navigationMenu != null) {
            NavigationMenu.closeDrawer();
        }
    }

}
