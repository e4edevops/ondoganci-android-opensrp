package org.smartregister.ondoganci.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import org.smartregister.ondoganci.R;
import org.smartregister.ondoganci.contract.NavigationMenuContract;
import org.smartregister.ondoganci.model.ReportGroupingModel;
import org.smartregister.ondoganci.util.AppConstants;
import org.smartregister.ondoganci.util.AppUtils;
import org.smartregister.ondoganci.view.NavDrawerActivity;
import org.smartregister.ondoganci.view.NavigationMenu;
import org.smartregister.stock.StockLibrary;
import org.smartregister.stock.activity.StockControlActivity;
import org.smartregister.stock.adapter.StockGridAdapter;
import org.smartregister.stock.domain.StockType;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by samuelgithengi on 2/14/18.
 */

public class StockActivity extends AppCompatActivity implements NavDrawerActivity, NavigationMenuContract {


    private NavigationMenu navigationMenu;
    private GridView stockGrid;
    @Nullable
    private String reportGrouping;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_stock);
        initialize();
        createDrawer();

    }

    private void initialize() {
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
        stockGrid = (GridView) findViewById(R.id.stockgrid);
    }

    private void refreshAdapter() {
        List<StockType> allStockTypes = StockLibrary.getInstance().getStockTypeRepository().getAllStockTypes(null);
        StockType[] stockTypes = allStockTypes.toArray(new StockType[allStockTypes.size()]);
        StockGridAdapter adapter = new StockGridAdapter(this, stockTypes, StockControlActivity.class);
        stockGrid.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshAdapter();
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
        WeakReference<StockActivity> weakReference = new WeakReference<>(this);
        navigationMenu = NavigationMenu.getInstance(weakReference.get());
    }

    @Override
    public void closeDrawer() {
        if (navigationMenu != null) {
            NavigationMenu.closeDrawer();
        }
    }

}
