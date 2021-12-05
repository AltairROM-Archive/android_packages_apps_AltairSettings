/*
 * Copyright (C) 2013 The CyanogenMod Project
 * Copyright (C) 2017-2020 The LineageOS project
 * Copyright (C) 2019-2020 Altair ROM
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.altair.settings.fragments;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.provider.SearchIndexableResource;
import android.text.format.DateFormat;
import android.view.View;

import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import com.altair.settings.utils.DeviceUtils;
import com.altair.settings.utils.StatusBarIcon;
import com.altair.settings.utils.TelephonyUtils;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settingslib.search.SearchIndexable;

import com.lineage.support.preferences.SystemSettingSwitchPreference;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import lineageos.preference.LineageSystemSettingListPreference;
import lineageos.providers.LineageSettings;

@SearchIndexable
public class CustomStatusBarSettings extends DashboardFragment implements
        Preference.OnPreferenceChangeListener, Indexable {
    private static final String TAG = "CustomStatusBarSettings";

    private static final String KEY_NETWORK_TRAFFIC_SETTINGS = "network_traffic_settings";
    private static final String KEY_USE_OLD_MOBILETYPE = "use_old_mobiletype";
    private static final String KEY_DATA_DISABLED_ICON = "data_disabled_icon";
    private static final String KEY_ROAMING_INDICATOR_ICON = "roaming_indicator_icon";
    private static final String KEY_SHOW_FOURG_ICON = "show_fourg_icon";
    private static final String KEY_SHOW_VOLTE_ICON = "show_volte_icon";
    private static final String KEY_SHOW_VOWIFI_ICON = "show_vowifi_icon";
    private static final String KEY_VOLTE_VOWIFI_OVERRIDE = "volte_vowifi_override";

    private static final String STATUS_BAR_SHOW_CLOCK = "status_bar_show_clock";
    private static final String STATUS_BAR_CLOCK = "status_bar_clock";
    private static final String CLOCK_SECONDS = "clock_seconds";
    private static final String STATUS_BAR_CLOCK_STYLE = "status_bar_clock";
    private static final String STATUS_BAR_AM_PM = "status_bar_am_pm";
    private static final String STATUS_BAR_SHOW_BATTERY = "status_bar_show_battery";
    private static final String STATUS_BAR_BATTERY_STYLE = "status_bar_battery_style";
    private static final String STATUS_BAR_SHOW_BATTERY_PERCENT = "status_bar_show_battery_percent";

    private static final int STATUS_BAR_BATTERY_STYLE_TEXT = 2;

    private static final String CATEGORY_NETWORK = "network_category";

    private StatusBarIcon mClockIcon;
    private StatusBarIcon mBatteryIcon;

    private SwitchPreference mStatusBarShowClock;
    private LineageSystemSettingListPreference mStatusBarClock;
    private LineageSystemSettingListPreference mStatusBarAmPm;

    private SwitchPreference mStatusBarShowBattery;
    private LineageSystemSettingListPreference mStatusBarBattery;
    private LineageSystemSettingListPreference mStatusBarBatteryShowPercent;

    private PreferenceScreen mNetworkTrafficPref;

    private boolean mHasNotch;

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.menu_status_bar_settings;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context context = getContext();
        ContentResolver resolver = getActivity().getContentResolver();

        final PreferenceScreen prefScreen = getPreferenceScreen();
        final PreferenceCategory networkCategory = prefScreen.findPreference(CATEGORY_NETWORK);

        SystemSettingSwitchPreference useOldMobileType = findPreference(KEY_USE_OLD_MOBILETYPE);
        SystemSettingSwitchPreference dataDisabledIcon = findPreference(KEY_DATA_DISABLED_ICON);
        SystemSettingSwitchPreference roamingIndicatorIcon = findPreference(KEY_ROAMING_INDICATOR_ICON);
        SystemSettingSwitchPreference showFourgIcon = findPreference(KEY_SHOW_FOURG_ICON);
        SystemSettingSwitchPreference showVoLTEIcon = findPreference(KEY_SHOW_VOLTE_ICON);
        SystemSettingSwitchPreference showVoWiFiIcon = findPreference(KEY_SHOW_VOWIFI_ICON);
        SystemSettingSwitchPreference voLTEvoWiFiOverride = findPreference(KEY_VOLTE_VOWIFI_OVERRIDE);

        if (!TelephonyUtils.isVoiceCapable(getActivity())) {
            networkCategory.removePreference(useOldMobileType);
            networkCategory.removePreference(dataDisabledIcon);
            networkCategory.removePreference(roamingIndicatorIcon);
            networkCategory.removePreference(showFourgIcon);
            networkCategory.removePreference(showVoLTEIcon);
            networkCategory.removePreference(showVoWiFiIcon);
            networkCategory.removePreference(voLTEvoWiFiOverride);
        }

        mNetworkTrafficPref = findPreference(KEY_NETWORK_TRAFFIC_SETTINGS);
        mHasNotch = DeviceUtils.hasNotch(getActivity());
        if (mHasNotch) {
            networkCategory.removePreference(mNetworkTrafficPref);
        }

        mClockIcon = new StatusBarIcon(context, "clock");

        mStatusBarShowClock = findPreference(STATUS_BAR_SHOW_CLOCK);
        mStatusBarShowClock.setOnPreferenceChangeListener(this);

        mStatusBarAmPm = findPreference(STATUS_BAR_AM_PM);
        mStatusBarClock = findPreference(STATUS_BAR_CLOCK);
        mStatusBarClock.setOnPreferenceChangeListener(this);

        mStatusBarShowBattery = findPreference(STATUS_BAR_SHOW_BATTERY);
        mStatusBarShowBattery.setOnPreferenceChangeListener(this);

        mBatteryIcon = new StatusBarIcon(context, "battery");

        mStatusBarBatteryShowPercent = findPreference(STATUS_BAR_SHOW_BATTERY_PERCENT);
        mStatusBarBattery = findPreference(STATUS_BAR_BATTERY_STYLE);
        mStatusBarBattery.setOnPreferenceChangeListener(this);
        enableStatusBarBatteryDependents(mStatusBarBattery.getIntValue(2));
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.ALTAIR;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    public void onResume() {
        super.onResume();

        boolean iconEnabled = mClockIcon.isEnabled();
        mStatusBarShowClock.setChecked(iconEnabled);

        if (DateFormat.is24HourFormat(getActivity())) {
            mStatusBarAmPm.setEnabled(false);
            mStatusBarAmPm.setSummary(R.string.status_bar_am_pm_info);
        }

        final boolean disallowCenteredClock = mHasNotch || getNetworkTrafficStatus() != 0;

        // Adjust status bar preferences for RTL
        if (getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            if (disallowCenteredClock) {
                mStatusBarClock.setEntries(R.array.status_bar_clock_position_entries_notch_rtl);
                mStatusBarClock.setEntryValues(R.array.status_bar_clock_position_values_notch_rtl);
            } else {
                mStatusBarClock.setEntries(R.array.status_bar_clock_position_entries_rtl);
                mStatusBarClock.setEntryValues(R.array.status_bar_clock_position_values_rtl);
            }
        } else if (disallowCenteredClock) {
            mStatusBarClock.setEntries(R.array.status_bar_clock_position_entries_notch);
            mStatusBarClock.setEntryValues(R.array.status_bar_clock_position_values_notch);
        } else {
            mStatusBarClock.setEntries(R.array.status_bar_clock_position_entries);
            mStatusBarClock.setEntryValues(R.array.status_bar_clock_position_values);
        }

        // Disable network traffic preferences if clock is centered in the status bar
        updateNetworkTrafficStatus(getClockPosition());

        iconEnabled = mBatteryIcon.isEnabled();
        mStatusBarShowBattery.setChecked(iconEnabled);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        if (preference == mStatusBarShowClock) {
            mClockIcon.setEnabled((Boolean) newValue);
            if (DateFormat.is24HourFormat(getActivity())) {
                mStatusBarAmPm.setEnabled(false);
            }
            return true;
        } else if (preference == mStatusBarShowBattery) {
            mBatteryIcon.setEnabled((Boolean) newValue);
            return true;
        } else if (key == STATUS_BAR_BATTERY_STYLE) {
            int value = Integer.parseInt((String) newValue);
            enableStatusBarBatteryDependents(value);
        } else if (key == STATUS_BAR_CLOCK_STYLE) {
            int value = Integer.parseInt((String) newValue);
            updateNetworkTrafficStatus(value);
        }
        return true;
    }

    private void enableStatusBarBatteryDependents(int batteryIconStyle) {
        mStatusBarBatteryShowPercent.setEnabled(batteryIconStyle != STATUS_BAR_BATTERY_STYLE_TEXT);
    }

    private void updateNetworkTrafficStatus(int clockPosition) {
        if (mHasNotch) {
            // Unconditional no network traffic for you
            return;
        }
        boolean isClockCentered = clockPosition == 1;
        mNetworkTrafficPref.setEnabled(!isClockCentered);
        mNetworkTrafficPref.setSummary(getResources().getString(isClockCentered ?
                R.string.network_traffic_disabled_clock :
                R.string.network_traffic_settings_summary
        ));
    }

    private int getNetworkTrafficStatus() {
        return LineageSettings.Secure.getInt(getActivity().getContentResolver(),
                LineageSettings.Secure.NETWORK_TRAFFIC_MODE, 0);
    }

    private int getClockPosition() {
        return LineageSettings.System.getInt(getActivity().getContentResolver(),
                STATUS_BAR_CLOCK_STYLE, 2);
    }

    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();
                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.menu_status_bar_settings;
                    result.add(sir);

                    return result;
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);

                    if (DeviceUtils.hasNotch(context)) {
                        keys.add(KEY_NETWORK_TRAFFIC_SETTINGS);
                    }
                    return keys;
                }
            };
}
