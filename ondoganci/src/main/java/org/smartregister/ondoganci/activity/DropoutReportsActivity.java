package org.smartregister.ondoganci.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import org.smartregister.ondoganci.model.ReportGroupingModel;
import org.smartregister.ondoganci.util.AppConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by keyman on 18/12/17.
 */
public class DropoutReportsActivity extends AppCompatActivity {

    @Nullable
    private String reportGrouping;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dropout_reports);

//        toolbar.setTitle(getString(R.string.side_nav_dropout));

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
        listView.setDivider(null);
        listView.setDividerHeight(0);

        List<String[]> list = new ArrayList<>();
        list.add(new String[]{getString(R.string.bcg_measles_cumulative), getString(R.string.bcg_measles_cohort)});
        list.add(new String[]{getString(R.string.penta_cumulative), getString(R.string.penta_cohort)});
        list.add(new String[]{getString(R.string.measles_cumulative)});

        DropoutArrayAdapter arrayAdapter = new DropoutArrayAdapter(DropoutReportsActivity.this, list);
        listView.setAdapter(arrayAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        LinearLayout hia2 = (LinearLayout) drawer.findViewById(R.id.dropout_reports);
        hia2.setBackgroundColor(getResources().getColor(R.color.tintcolor));
    }

    ////////////////////////////////////////////////////////////////
    // Inner classes
    ////////////////////////////////////////////////////////////////
    private class DropoutArrayAdapter extends ArrayAdapter<String[]> {


        public DropoutArrayAdapter(@NonNull Context context, @NonNull List<String[]> objects) {
            super(context, 0, objects);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View view;
            String[] items = getItem(position);

            if (convertView == null) {
                view = LayoutInflater.from(getContext()).inflate(R.layout.dropout_reports_item, parent, false);
            } else {
                view = convertView;
            }

            if (items == null) {
                return view;
            }

            View rev1 = view.findViewById(R.id.rev1);
            View rev2 = view.findViewById(R.id.rev2);
            View divider = view.findViewById(R.id.adapter_divider_bottom);
            rev2.setVisibility(View.VISIBLE);

            if (items.length > 0) {
                String currentItem = items[0];
                TextView tvName = (TextView) view.findViewById(R.id.tv);
                tvName.setText(currentItem);

                if (currentItem.equals(getString(R.string.bcg_measles_cumulative))) {
                    rev1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(DropoutReportsActivity.this, BcgMeaslesCumulativeDropoutReportActivity.class);
                            startActivity(intent);
                        }
                    });
                } else if (currentItem.equals(getString(R.string.penta_cumulative))) {
                    rev1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(DropoutReportsActivity.this, PentaCumulativeDropoutReportActivity.class);
                            startActivity(intent);
                        }
                    });

                } else if (currentItem.equals(getString(R.string.measles_cumulative))) {
                    rev1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(DropoutReportsActivity.this, MeaslesCumulativeDropoutReportActivity.class);
                            startActivity(intent);
                        }
                    });

                }
            }


            if (items.length > 1) {
                String currentItem = items[1];
                TextView tvName = (TextView) view.findViewById(R.id.tv2);
                tvName.setText(currentItem);

                if (currentItem.equals(getString(R.string.bcg_measles_cohort))) {
                    rev2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(DropoutReportsActivity.this, BcgMeaslesCohortDropoutReportActivity.class);
                            startActivity(intent);
                        }
                    });

                } else if (currentItem.equals(getString(R.string.penta_cohort))) {
                    rev2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(DropoutReportsActivity.this, PentaCohortDropoutReportActivity.class);
                            startActivity(intent);
                        }
                    });
                }
            } else {
                rev2.setVisibility(View.GONE);
                divider.setVisibility(View.GONE);
            }
            // Lookup view for data population
            return view;
        }
    }

}
