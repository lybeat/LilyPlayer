package com.lybeat.lilyplayer.ui;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;

import com.lybeat.lilyplayer.R;
import com.lybeat.lilyplayer.util.AppInfoUtil;
import com.lybeat.lilyplayer.widget.dialog.RadioDialog;
import com.lybeat.lilyplayer.widget.preference.Preference;

/**
 * Author: lybeat
 * Date: 2016/7/17
 */
public class SettingActivity extends BaseActivity {

    private static final String TAG = "SettingActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common);
        initViews();
    }

    @Override
    protected void initViews() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.common_tool_bar);
        toolbar.setTitle(getResources().getString(R.string.setting));
        toolbar.setTitleTextColor(0xfffafafa);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_ab_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingActivity.this.finish();
            }
        });

        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(R.id.common_container, new SettingFragment()).commit();
    }

    @Override
    protected void initData() {

    }

    public static class SettingFragment extends PreferenceFragment implements
            Preference.OnPreferenceClickListener {

        private Preference actionPreference;
        private Preference decodePreference;
        private Preference updatePreference;
        private Preference aboutPreference;
        private Preference feedbackPreference;

        private String[] actions;
        private String[] actionSummary;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preference_setting);

            actions = getResources().getStringArray(R.array.action_after_play);
            actionSummary = getResources().getStringArray(R.array.action_summary);
            SharedPreferences sp = getActivity().getSharedPreferences("share_data",
                    Context.MODE_PRIVATE);
            int actionIndex = sp.getInt("key_action", 2);
            actionPreference = (Preference) findPreference("action_after_play");
            decodePreference = (Preference) findPreference("decode");
            updatePreference = (Preference) findPreference("version_update");
            aboutPreference = (Preference) findPreference("about");
            feedbackPreference = (Preference) findPreference("feedback");

            actionPreference.setSummary(actionSummary[actionIndex]);
            actionPreference.setOnPreferenceClickListener(this);
            decodePreference.setOnPreferenceClickListener(this);
            updatePreference.setSummary(getString(R.string.current_version) + AppInfoUtil.getVersionName(getActivity()));
            updatePreference.setOnPreferenceClickListener(this);
            aboutPreference.setOnPreferenceClickListener(this);
            feedbackPreference.setOnPreferenceClickListener(this);
        }

        @Override
        public boolean onPreferenceClick(android.preference.Preference preference) {
            switch (preference.getKey()) {
                case "action_after_play":
                    selectAction();
                    break;
                case "decode":
                    break;
                case "version_update":
                    break;
                case "about":
                    break;
                case "feedback":
                    break;
            }

            return true;
        }

        private void selectAction() {
            final RadioDialog radioDialog = new RadioDialog(getActivity(), actions);
            radioDialog.setDialogTitle(getString(R.string.setting_action_after_play));
            radioDialog.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    SharedPreferences sp = getActivity().getSharedPreferences(
                                    "share_data", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putInt("key_action", i);
                            editor.apply();
                            actionPreference.setSummary(actionSummary[i]);
                            radioDialog.dismiss();
                }
            });
        }
    }
}
