package com.techmartis.ebillboard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

/**
 * 设置 Activity
 * 功能：
 *  1. 设置自动加载的网址
 *  2. 设置浏览器 User-Agent
 *  3. 启动系统中其他应用程序
 *  4. 访问系统设置
 *  5. 密码保护（默认密码 123）
 */
public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences prefs;

    // 网址设置
    private EditText editHomeUrl;

    // UA 设置
    private RadioGroup radioGroupUA;
    private RadioButton radioUaDefault;
    private RadioButton radioUaDesktop;
    private RadioButton radioUaMobile;
    private RadioButton radioUaCustom;
    private EditText editCustomUA;
    private View layoutCustomUA;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 先初始化 prefs
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // 密码保护：验证通过才进入设置页（回调方式，不阻塞主线程）
        verifyPassword(this::onPasswordVerified);
    }

    /** 密码验证通过后调用 */
    private void onPasswordVerified() {
        setContentView(R.layout.activity_settings);

        // 设置标题栏
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_settings);
        }

        initViews();
        loadCurrentSettings();
        setupListeners();
    }

    // ─── 密码验证 ──────────────────────────────────────────────────────

    /**
     * 弹出密码输入对话框，验证通过后通过回调继续执行。
     * 首次安装时自动将默认密码 "123" 写入 SharedPreferences。
     * @param onSuccess 验证通过后执行的回调
     */
    private void verifyPassword(Runnable onSuccess) {
        // 首次使用时初始化默认密码
        String savedPwd = prefs.getString(Constants.PREF_SETTINGS_PASSWORD, null);
        if (savedPwd == null) {
            savedPwd = Constants.DEFAULT_PASSWORD;
            prefs.edit()
                    .putString(Constants.PREF_SETTINGS_PASSWORD, savedPwd)
                    .apply();
        }

        final String finalSavedPwd = savedPwd;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.pwd_title);
        builder.setMessage(R.string.pwd_hint);

        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_password, (ViewGroup) null);
        EditText etPwd = dialogView.findViewById(R.id.et_password);
        builder.setView(dialogView);

        builder.setPositiveButton("确定", null); // 先设 null，手动控制 dismiss
        builder.setNegativeButton("取消", (d, w) -> finish());
        builder.setCancelable(false);

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> {
            Button btnPositive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            btnPositive.setOnClickListener(v -> {
                String input = etPwd.getText().toString().trim();
                if (finalSavedPwd.equals(input)) {
                    dialog.dismiss();
                    onSuccess.run(); // 验证通过，继续执行回调
                } else {
                    etPwd.setError(getString(R.string.pwd_error));
                }
            });
        });

        dialog.show();
    }

    // ─── 视图初始化 ──────────────────────────────────────────────────

    private void initViews() {
        editHomeUrl = findViewById(R.id.edit_home_url);

        radioGroupUA = findViewById(R.id.radio_group_ua);
        radioUaDefault = findViewById(R.id.radio_ua_default);
        radioUaDesktop = findViewById(R.id.radio_ua_desktop);
        radioUaMobile = findViewById(R.id.radio_ua_mobile);
        radioUaCustom = findViewById(R.id.radio_ua_custom);
        editCustomUA = findViewById(R.id.edit_custom_ua);
        layoutCustomUA = findViewById(R.id.layout_custom_ua);

        // 按钮
        Button btnSaveUrl = findViewById(R.id.btn_save_url);
        Button btnSaveUA = findViewById(R.id.btn_save_ua);
        Button btnLaunchApps = findViewById(R.id.btn_launch_apps);
        Button btnSystemSettings = findViewById(R.id.btn_system_settings);
        Button btnWifiSettings = findViewById(R.id.btn_wifi_settings);
        Button btnDisplaySettings = findViewById(R.id.btn_display_settings);
        Button btnDateTimeSettings = findViewById(R.id.btn_datetime_settings);
        Button btnSoundSettings = findViewById(R.id.btn_sound_settings);
        Button btnDevOptions = findViewById(R.id.btn_dev_options);
        Button btnChangePwd = findViewById(R.id.btn_change_pwd);

        btnSaveUrl.setOnClickListener(v -> saveHomeUrl());
        btnSaveUA.setOnClickListener(v -> saveUserAgent());
        btnLaunchApps.setOnClickListener(v -> openAppList());
        btnSystemSettings.setOnClickListener(v -> openSystemSettings(Settings.ACTION_SETTINGS));
        btnWifiSettings.setOnClickListener(v -> openSystemSettings(Settings.ACTION_WIFI_SETTINGS));
        btnDisplaySettings.setOnClickListener(v -> openSystemSettings(Settings.ACTION_DISPLAY_SETTINGS));
        btnDateTimeSettings.setOnClickListener(v -> openSystemSettings(Settings.ACTION_DATE_SETTINGS));
        btnSoundSettings.setOnClickListener(v -> openSystemSettings(Settings.ACTION_SOUND_SETTINGS));
        btnDevOptions.setOnClickListener(v -> openSystemSettings(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS));
        btnChangePwd.setOnClickListener(v -> showChangePasswordDialog());
    }

    private void loadCurrentSettings() {
        // 加载当前网址
        String url = prefs.getString(Constants.PREF_HOME_URL, Constants.DEFAULT_URL);
        editHomeUrl.setText(url);

        // 加载 UA 设置
        String uaPreset = prefs.getString(Constants.PREF_UA_PRESET, Constants.UA_PRESET_DEFAULT);
        String customUA = prefs.getString(Constants.PREF_USER_AGENT, "");

        switch (uaPreset) {
            case Constants.UA_PRESET_DESKTOP:
                radioUaDesktop.setChecked(true);
                layoutCustomUA.setVisibility(View.GONE);
                break;
            case Constants.UA_PRESET_MOBILE:
                radioUaMobile.setChecked(true);
                layoutCustomUA.setVisibility(View.GONE);
                break;
            case Constants.UA_PRESET_CUSTOM:
                radioUaCustom.setChecked(true);
                layoutCustomUA.setVisibility(View.VISIBLE);
                editCustomUA.setText(customUA);
                break;
            default:
                radioUaDefault.setChecked(true);
                layoutCustomUA.setVisibility(View.GONE);
                break;
        }
    }

    private void setupListeners() {
        // UA 单选按钮切换时显示/隐藏自定义输入框
        radioGroupUA.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_ua_custom) {
                layoutCustomUA.setVisibility(View.VISIBLE);
            } else {
                layoutCustomUA.setVisibility(View.GONE);
            }
        });
    }

    // ─── 保存网址 ──────────────────────────────────────────────────────

    private void saveHomeUrl() {
        String url = editHomeUrl.getText().toString().trim();
        if (TextUtils.isEmpty(url)) {
            editHomeUrl.setError(getString(R.string.error_url_empty));
            return;
        }
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
            editHomeUrl.setText(url);
        }
        prefs.edit().putString(Constants.PREF_HOME_URL, url).apply();
        Toast.makeText(this, R.string.msg_saved, Toast.LENGTH_SHORT).show();
    }

    // ─── 保存 UA ───────────────────────────────────────────────────────

    private void saveUserAgent() {
        int checkedId = radioGroupUA.getCheckedRadioButtonId();
        String uaPreset;
        String uaValue = "";

        if (checkedId == R.id.radio_ua_desktop) {
            uaPreset = Constants.UA_PRESET_DESKTOP;
            uaValue = Constants.UA_DESKTOP;
        } else if (checkedId == R.id.radio_ua_mobile) {
            uaPreset = Constants.UA_PRESET_MOBILE;
            uaValue = Constants.UA_MOBILE;
        } else if (checkedId == R.id.radio_ua_custom) {
            uaPreset = Constants.UA_PRESET_CUSTOM;
            uaValue = editCustomUA.getText().toString().trim();
            if (TextUtils.isEmpty(uaValue)) {
                editCustomUA.setError(getString(R.string.error_ua_empty));
                return;
            }
        } else {
            uaPreset = Constants.UA_PRESET_DEFAULT;
            uaValue = ""; // 使用系统默认
        }

        prefs.edit()
                .putString(Constants.PREF_UA_PRESET, uaPreset)
                .putString(Constants.PREF_USER_AGENT, uaValue)
                .apply();

        Toast.makeText(this, R.string.msg_saved, Toast.LENGTH_SHORT).show();
    }

    // ─── 打开应用列表 ──────────────────────────────────────────────────

    private void openAppList() {
        startActivity(new Intent(this, AppListActivity.class));
    }

    // ─── 打开系统设置 ──────────────────────────────────────────────────

    private void openSystemSettings(String action) {
        try {
            startActivity(new Intent(action));
        } catch (Exception e) {
            try {
                startActivity(new Intent(Settings.ACTION_SETTINGS));
            } catch (Exception ex) {
                Toast.makeText(this, R.string.msg_settings_unavailable, Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ─── 修改密码对话框 ──────────────────────────────────────────────

    private void showChangePasswordDialog() {
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_set_password, (ViewGroup) null);
        EditText etNewPwd = dialogView.findViewById(R.id.et_new_pwd);
        EditText etConfirmPwd = dialogView.findViewById(R.id.et_confirm_pwd);

        new AlertDialog.Builder(this)
                .setTitle(R.string.pwd_set_title)
                .setView(dialogView)
                .setPositiveButton("确定", (d, w) -> {
                    String newPwd = etNewPwd.getText().toString().trim();
                    String confirmPwd = etConfirmPwd.getText().toString().trim();

                    if (TextUtils.isEmpty(newPwd)) {
                        Toast.makeText(this, R.string.pwd_empty, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (newPwd.length() < 3) {
                        Toast.makeText(this, R.string.pwd_too_short, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!newPwd.equals(confirmPwd)) {
                        Toast.makeText(this, R.string.pwd_mismatch, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    prefs.edit()
                            .putString(Constants.PREF_SETTINGS_PASSWORD, newPwd)
                            .apply();
                    Toast.makeText(this, R.string.pwd_success, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
