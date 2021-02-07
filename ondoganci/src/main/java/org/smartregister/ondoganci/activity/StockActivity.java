package org.smartregister.ondoganci.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
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
import org.smartregister.stock.repository.StockRepository;

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

    private void dropInVaccineStockCheck() {
        final int fixedVials = (int) (0.15 * 10000);
        String TotalInformation = "";
        int maxLines = 0;
        StringBuilder sentence = new StringBuilder();
        List<StockType> allStockTypes = StockLibrary.getInstance().getStockTypeRepository().getAllStockTypes(null);
        StockType[] stockTypes = allStockTypes.toArray(new StockType[allStockTypes.size()]);
        StockRepository stockRepository = StockLibrary.getInstance().getStockRepository();
        try {
            for (int i = 0; i <= stockTypes.length; i++) {
                String stockName = stockTypes[i].getName();
                int vials = stockRepository.getBalanceFromNameAndDate(stockName, System.currentTimeMillis());
                if (vials < fixedVials) {
                    sentence.append(stockName).append(" stock is ").append(vials).append(" vials, which is less than 15% ");
                    sentence.append("\n");
                    sentence.append("\n");
                    maxLines++;
//                    Toast.makeText(this, stockName + " Stock is " + vials + "vials, which is less than 15% ", Toast.LENGTH_SHORT).show();
                } else if (vials == fixedVials) {
                    sentence.append(stockName).append(" stock is ").append(vials).append(" vials, which is equal to 15% ");
                    sentence.append("\n");
                    sentence.append("\n");
//                    Toast.makeText(this, stockName + " Stock is " + vials + "vials, which is equal to 15% ", Toast.LENGTH_SHORT).show();
                    maxLines++;
                }
            }
        } catch (Exception e) {
//            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        if(maxLines != 0){
            TotalInformation = sentence.toString();
            showSnackbar(StockActivity.this, TotalInformation, maxLines);
        }
    }

    public void showSnackbar(Activity activity, String message, int maxLines) {
        View rootView = activity.getWindow().getDecorView().findViewById(android.R.id.content);
        final Snackbar snackbar;
        FrameLayout.LayoutParams params;
        TextView textView;
        View view;
        snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("OK", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
            }
        });
        view = snackbar.getView();
        textView = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
        textView.setMaxLines(maxLines * 2);
        textView.setTextSize(16);
        params = (FrameLayout.LayoutParams) view.getLayoutParams();
        params.gravity = Gravity.CENTER_VERTICAL;
        view.setLayoutParams(params);
        snackbar.show();
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
        dropInVaccineStockCheck();
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
