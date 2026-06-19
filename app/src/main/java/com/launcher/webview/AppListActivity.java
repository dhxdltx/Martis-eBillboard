package com.techmartis.ebillboard;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 应用列表 Activity
 * 显示设备上所有已安装的应用，点击可直接启动
 */
public class AppListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private EditText editSearch;
    private AppAdapter adapter;
    private List<AppInfo> allApps = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_app_list);
        }

        recyclerView = findViewById(R.id.recycler_view);
        progressBar = findViewById(R.id.progress_bar);
        editSearch = findViewById(R.id.edit_search);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AppAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        editSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {
                filterApps(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // 异步加载应用列表
        new LoadAppsTask().execute();
    }

    private void filterApps(String keyword) {
        if (adapter == null) return;
        List<AppInfo> filtered = new ArrayList<>();
        String lowerKeyword = keyword.toLowerCase().trim();
        for (AppInfo app : allApps) {
            if (lowerKeyword.isEmpty() ||
                    app.name.toLowerCase().contains(lowerKeyword) ||
                    app.packageName.toLowerCase().contains(lowerKeyword)) {
                filtered.add(app);
            }
        }
        adapter.updateData(filtered);
    }

    // ─── 异步加载任务 ──────────────────────────────────────────────────────────

    @SuppressWarnings("deprecation")
    private class LoadAppsTask extends AsyncTask<Void, Void, List<AppInfo>> {

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<AppInfo> doInBackground(Void... voids) {
            PackageManager pm = getPackageManager();
            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

            List<android.content.pm.ResolveInfo> resolveInfos =
                    pm.queryIntentActivities(mainIntent, 0);

            List<AppInfo> apps = new ArrayList<>();
            for (android.content.pm.ResolveInfo ri : resolveInfos) {
                AppInfo info = new AppInfo();
                info.name = ri.loadLabel(pm).toString();
                info.packageName = ri.activityInfo.packageName;
                info.activityName = ri.activityInfo.name;
                info.icon = ri.loadIcon(pm);
                apps.add(info);
            }

            // 按名称排序
            Collections.sort(apps, (a, b) ->
                    a.name.compareToIgnoreCase(b.name));

            return apps;
        }

        @Override
        protected void onPostExecute(List<AppInfo> result) {
            progressBar.setVisibility(View.GONE);
            allApps = result;
            adapter.updateData(result);
        }
    }

    // ─── RecyclerView Adapter ──────────────────────────────────────────────────

    private class AppAdapter extends RecyclerView.Adapter<AppAdapter.ViewHolder> {

        private List<AppInfo> apps;

        AppAdapter(List<AppInfo> apps) {
            this.apps = apps;
        }

        void updateData(List<AppInfo> newApps) {
            apps = newApps;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_app, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            AppInfo app = apps.get(position);
            holder.textName.setText(app.name);
            holder.textPackage.setText(app.packageName);
            if (app.icon != null) {
                holder.imageIcon.setImageDrawable(app.icon);
            } else {
                holder.imageIcon.setImageResource(android.R.drawable.sym_def_app_icon);
            }
            holder.itemView.setOnClickListener(v -> launchApp(app));
        }

        @Override
        public int getItemCount() {
            return apps.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imageIcon;
            TextView textName;
            TextView textPackage;

            ViewHolder(View v) {
                super(v);
                imageIcon = v.findViewById(R.id.image_app_icon);
                textName = v.findViewById(R.id.text_app_name);
                textPackage = v.findViewById(R.id.text_app_package);
            }
        }
    }

    // ─── 启动应用 ──────────────────────────────────────────────────────────────

    private void launchApp(AppInfo app) {
        try {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setClassName(app.packageName, app.activityName);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this,
                    getString(R.string.msg_launch_failed, app.name),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ─── 数据模型 ──────────────────────────────────────────────────────────────

    static class AppInfo {
        String name;
        String packageName;
        String activityName;
        Drawable icon;
    }
}
