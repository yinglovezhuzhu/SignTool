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
import android.view.Menu;
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

    private SharedPreferences mSP;
    private  ArrayAdapter<String> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        mSP = getSharedPreferences("app_data", Context.MODE_PRIVATE);

        initView();
    }

    @Override
    public void onBackPressed() {
        exitWithResult(RESULT_CANCELED, null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.history_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                exitWithResult(RESULT_CANCELED, null); // back button
                return true;
            case R.id.action_history_clear:
                new AlertDialog.Builder(HistoryActivity.this)
                        .setMessage(R.string.clear_hsitory)
                        .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mSP.edit().clear().apply();
                                if(null != mAdapter) {
                                    mAdapter.clear();
                                    mAdapter.notifyDataSetChanged();
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
            default:
                break;
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

        @SuppressWarnings("unchecked")
        final Map<String, Long> historyData = (Map<String, Long>) mSP.getAll();
        final List<Map.Entry<String, Long>> entryList = new ArrayList<>(historyData.entrySet());
        // sort
        Collections.sort(entryList, new ValueComparator());
        final List<String> packageNames = new ArrayList<>();
        for(Map.Entry<String, Long> entry : entryList) {
            packageNames.add(entry.getKey());
        }

        mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, packageNames);
        lvList.setAdapter(mAdapter);

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
                                    mSP.edit().remove(packageNames.get(position)).apply();
                                    packageNames.remove(position);
                                    mAdapter.notifyDataSetChanged();
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
