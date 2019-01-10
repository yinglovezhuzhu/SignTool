package com.xiaoying.signtool;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * <br/>Author：yunying.zhang
 * <br/>Email: yunyingzhang@rastar.com
 * <br/>Date: 2019/1/10
 */
public class SelectAppActivity extends AppCompatActivity {

    private PackageManager mPackageManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_select_app);

        ActionBar actionBar = getSupportActionBar();
        if(null != actionBar) {
            actionBar.setTitle(R.string.select_app);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        ListView lv = findViewById(R.id.lv_apps);
        final ProgressBar pb = findViewById(R.id.pb_loading);
        mPackageManager = getPackageManager();
        final AppAdapter adapter = new AppAdapter(this, mPackageManager);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ApplicationInfo info = adapter.getItem(position);
                exitWithResult(RESULT_OK, info.packageName);
            }
        });

        new AsyncTask<Void, Integer, List<ApplicationInfo>>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pb.setVisibility(View.VISIBLE);
            }

            @Override
            protected List<ApplicationInfo> doInBackground(Void... voids) {
                return mPackageManager.getInstalledApplications(PackageManager.GET_META_DATA);
            }

            @Override
            protected void onPostExecute(List<ApplicationInfo> applicationInfos) {
                super.onPostExecute(applicationInfos);
                adapter.clear(false);
                adapter.addDatas(applicationInfos, true);
                pb.setVisibility(View.GONE);
            }
        }.execute();

    }

    @Override
    public void onBackPressed() {
        exitWithResult(RESULT_CANCELED, "");
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


    private static class AppAdapter extends BaseAdapter {

        private List<ApplicationInfo> mmData = new ArrayList<>();
        private Context mmContext;
        private PackageManager mmPackageManager;

        public AppAdapter(Context context, PackageManager packageManager) {
            this.mmContext = context;
            this.mmPackageManager = packageManager;
        }

        public void addDatas(Collection<ApplicationInfo> datas, boolean notifyDataSetChanged) {
            if(null == datas || datas.isEmpty()) {
                return;
            }
            mmData.addAll(datas);
            if(notifyDataSetChanged) {
                notifyDataSetChanged();
            }
        }

        public void clear(boolean notifyDataSetChanged) {
            mmData.clear();;
            if(notifyDataSetChanged) {
                notifyDataSetChanged();
            }
        }

        @Override
        public int getCount() {
            return mmData.size();
        }

        @Override
        public ApplicationInfo getItem(int position) {
            return mmData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder viewHolder = null;
            if(null == convertView) {
                convertView = View.inflate(mmContext, R.layout.item_apps, null);
                viewHolder = new ViewHolder();
                viewHolder.mmIvIcon = convertView.findViewById(R.id.iv_item_app_icon);
                viewHolder.mmTvName = convertView.findViewById(R.id.tv_item_app_name);
                viewHolder.mmTvPackage = convertView.findViewById(R.id.tv_item_app_package);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            final ApplicationInfo applicationInfo = getItem(position);
            viewHolder.mmIvIcon.setImageDrawable(applicationInfo.loadIcon(mmPackageManager));
            viewHolder.mmTvName.setText(applicationInfo.loadLabel(mmPackageManager));
            viewHolder.mmTvPackage.setText(applicationInfo.packageName);
            return convertView;
        }
    }

    private static class ViewHolder {
        ImageView mmIvIcon;
        TextView mmTvName;
        TextView mmTvPackage;
    }
}
