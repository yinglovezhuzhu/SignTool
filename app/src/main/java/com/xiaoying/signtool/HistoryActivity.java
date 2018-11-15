package com.xiaoying.signtool;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class HistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        initView();
    }

    @Override
    public void onBackPressed() {
        exitWithResult(RESULT_CANCELED, null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                exitWithResult(RESULT_CANCELED, null); // back button
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView() {

        ActionBar actionBar = getSupportActionBar();
        if(null != actionBar) {
            actionBar.setTitle(R.string.history);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        ListView lvList = findViewById(R.id.lv_history);

        final SharedPreferences sp = getSharedPreferences("app_data", Context.MODE_PRIVATE);
        @SuppressWarnings("unchecked")
        final Map<String, Long> historyData = (Map<String, Long>) sp.getAll();
        final List<Map.Entry<String, Long>> entryList = new ArrayList<>(historyData.entrySet());
        // sort
        Collections.sort(entryList, new ValueComparator());
        final List<String> packageNames = new ArrayList<>();
        for(Map.Entry<String, Long> entry : entryList) {
            packageNames.add(entry.getKey());
        }

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, packageNames);
        lvList.setAdapter(adapter);

        lvList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                exitWithResult(RESULT_OK, packageNames.get(position));
            }
        });

        lvList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                new AlertDialog.Builder(HistoryActivity.this)
                        .setItems(R.array.history_item_opts, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(0 == which) {
                                    packageNames.remove(position);
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .show();
                return true;
            }
        });
    }

    private void exitWithResult(int resultCode, String packageName) {
        if(TextUtils.isEmpty(packageName)) {
            setResult(resultCode);
        } else {
            Intent data = new Intent();
            data.putExtra(MainActivity.EXTRA_PACKAGE_NAME, packageName);
            setResult(resultCode, data);
        }
        finish();
    }


    private static class ValueComparator implements Comparator<Map.Entry<String, Long>> {

        @Override
        public int compare(Map.Entry<String, Long> o1, Map.Entry<String, Long> o2) {
            return o2.getValue().compareTo(o1.getValue());
        }
    }
}
