/*
 * Copyright (C) 2013 The CyanogenMod Project
 * Copyright (C) 2017 The LineageOS Project
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

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.IWindowManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManagerGlobal;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import com.altair.settings.utils.DeviceUtils;
import com.altair.settings.utils.TelephonyUtils;
import com.android.internal.logging.nano.MetricsProto;
import com.android.settingslib.search.SearchIndexable;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.lineage.support.preferences.CustomSeekBarPreference;

import java.util.ArrayList;
import java.util.List;

import lineageos.hardware.LineageHardwareManager;
import lineageos.providers.LineageSettings;

import static org.lineageos.internal.util.DeviceKeysConstants.*;

@SearchIndexable
public class CustomNavigationSettings extends DashboardFragment implements
        Preference.OnPreferenceChangeListener, Indexable {
    private static final String TAG = "CustomNavigationSettings";

    private static final String KEY_NAVIGATION_BAR_ENABLE = "navigation_bar_enable";
    private static final String KEY_NAVIGATION_SYSTEM_TYPE = "gesture_system_navigation_input_summary";
    private static final String KEY_NAVIGATION_ARROW_KEYS = "navigation_bar_menu_arrow_keys";
    private static final String KEY_NAVIGATION_INVERT_LAYOUT = "sysui_nav_bar_inverse";
    private static final String KEY_HOME_LONG_PRESS_ACTION = "hardware_keys_home_long_press";
    private static final String KEY_HOME_LONG_PRESS_CUSTOM_APP = "hardware_keys_home_long_press_custom_app";
    private static final String KEY_HOME_DOUBLE_TAP_ACTION = "hardware_keys_home_double_tap";
    private static final String KEY_HOME_DOUBLE_TAP_CUSTOM_APP = "hardware_keys_home_double_tap_custom_app";
    private static final String KEY_BACK_LONG_PRESS_ACTION = "hardware_keys_back_long_press";
    private static final String KEY_BACK_LONG_PRESS_CUSTOM_APP = "hardware_keys_back_long_press_custom_app";
    private static final String KEY_BACK_DOUBLE_TAP_ACTION = "hardware_keys_back_double_tap";
    private static final String KEY_BACK_DOUBLE_TAP_CUSTOM_APP = "hardware_keys_back_double_tap_custom_app";
    private static final String KEY_APP_SWITCH_PRESS_ACTION = "hardware_keys_app_switch_press";
    private static final String KEY_APP_SWITCH_LONG_PRESS_ACTION = "hardware_keys_app_switch_long_press";
    private static final String KEY_APP_SWITCH_LONG_PRESS_CUSTOM_APP = "hardware_keys_app_switch_long_press_custom_app";
    private static final String KEY_APP_SWITCH_DOUBLE_TAP_ACTION = "hardware_keys_app_switch_double_tap";
    private static final String KEY_APP_SWITCH_DOUBLE_TAP_CUSTOM_APP = "hardware_keys_app_switch_double_tap_custom_app";
    private static final String KEY_MENU_PRESS_ACTION = "hardware_keys_menu_press";
    private static final String KEY_MENU_LONG_PRESS_ACTION = "hardware_keys_menu_long_press";
    private static final String KEY_MENU_DOUBLE_TAP_ACTION = "hardware_keys_menu_double_tap";
    private static final String KEY_ASSIST_PRESS_ACTION = "hardware_keys_assist_press";
    private static final String KEY_ASSIST_LONG_PRESS_ACTION = "hardware_keys_assist_long_press";
    private static final String KEY_ASSIST_DOUBLE_TAP_ACTION = "hardware_keys_assist_double_tap";
    private static final String KEY_EDGE_LONG_SWIPE_ACTION = "navigation_bar_edge_long_swipe";

    private static final String CATEGORY_NAVBAR_OPTIONS = "navigation_bar_options_category";
    private static final String CATEGORY_HOME_KEY = "navigation_home_key";
    private static final String CATEGORY_BACK_KEY = "navigation_back_key";
    private static final String CATEGORY_APP_SWITCH_KEY = "navigation_app_switch_key";
    private static final String CATEGORY_MENU_KEY = "navigation_menu_key";
    private static final String CATEGORY_ASSIST_KEY = "navigation_assist_key";

    private static final int MENU_RESET = Menu.FIRST;

    private SwitchPreference mEnableNavigationBar;

    private Preference mNavigationSystemType;
    private SwitchPreference mNavigationArrowKeys;
    private SwitchPreference mNavigationInvertLayout;
    private ListPreference mEdgeLongSwipeAction;

    private ListPreference mHomeLongPressAction;
    private ListPreference mHomeDoubleTapAction;
    private ListPreference mBackLongPressAction;
    private ListPreference mBackDoubleTapAction;
    private ListPreference mAppSwitchShortPressAction;
    private ListPreference mAppSwitchLongPressAction;
    private ListPreference mAppSwitchDoubleTapAction;
    private ListPreference mMenuShortPressAction;
    private ListPreference mMenuLongPressAction;
    private ListPreference mMenuDoubleTapAction;
    private ListPreference mAssistShortPressAction;
    private ListPreference mAssistLongPressAction;
    private ListPreference mAssistDoubleTapAction;

    private Preference mHomeLongPressCustomApp;
    private Preference mHomeDoubleTapCustomApp;
    private Preference mBackLongPressCustomApp;
    private Preference mBackDoubleTapCustomApp;
    private Preference mAppSwitchLongPressCustomApp;
    private Preference mAppSwitchDoubleTapCustomApp;

    private PreferenceCategory mNavigationBarOptionsCategory;
    private PreferenceCategory mNavigationHomeKeyCategory;
    private PreferenceCategory mNavigationBackKeyCategory;
    private PreferenceCategory mNavigationAppSwitchKeyCategory;
    private PreferenceCategory mNavigationMenuKeyCategory;
    private PreferenceCategory mNavigationAssistKeyCategory;

    private Handler mHandler;

    Action mDefaultHomeLongPressAction;
    Action mDefaultHomeDoubleTapAction;
    Action mDefaultBackLongPressAction;
    Action mDefaultBackDoubleTapAction;
    Action mDefaultAppSwitchShortPressAction;
    Action mDefaultAppSwitchLongPressAction;
    Action mDefaultAppSwitchDoubleTapAction;
    Action mDefaultMenuShortPressAction;
    Action mDefaultMenuLongPressAction;
    Action mDefaultMenuDoubleTapAction;
    Action mDefaultAssistShortPressAction;
    Action mDefaultAssistLongPressAction;
    Action mDefaultAssistDoubleTapAction;
    Action mDefaultEdgeLongSwipeAction;

    private boolean mHardwareKeys;

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.menu_navigation_settings;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Resources res = getResources();
        final ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();

        mHandler = new Handler();

        final boolean hasMenuKey = DeviceUtils.hasMenuKey(getActivity());
        final boolean hasAssistKey = DeviceUtils.hasAssistKey(getActivity());

        mDefaultHomeLongPressAction = Action.fromIntSafe(res.getInteger(org.lineageos.platform.internal.R.integer.config_longPressOnHomeBehavior));
        mDefaultHomeDoubleTapAction = Action.fromIntSafe(res.getInteger(org.lineageos.platform.internal.R.integer.config_doubleTapOnHomeBehavior));
        mDefaultBackLongPressAction = Action.fromIntSafe(res.getInteger(org.lineageos.platform.internal.R.integer.config_longPressOnBackBehavior));
        mDefaultBackDoubleTapAction = Action.fromIntSafe(res.getInteger(org.lineageos.platform.internal.R.integer.config_doubleTapOnBackBehavior));
        mDefaultAppSwitchShortPressAction = Action.APP_SWITCH;
        mDefaultAppSwitchLongPressAction = Action.fromIntSafe(res.getInteger(org.lineageos.platform.internal.R.integer.config_longPressOnAppSwitchBehavior));
        mDefaultAppSwitchDoubleTapAction = Action.fromIntSafe(res.getInteger(org.lineageos.platform.internal.R.integer.config_doubleTapOnAppSwitchBehavior));
        mDefaultMenuShortPressAction = Action.MENU;
        mDefaultMenuLongPressAction = Action.fromIntSafe(res.getInteger(org.lineageos.platform.internal.R.integer.config_longPressOnMenuBehavior));
        mDefaultMenuDoubleTapAction = Action.fromIntSafe(res.getInteger(org.lineageos.platform.internal.R.integer.config_doubleTapOnMenuBehavior));
        mDefaultAssistShortPressAction = Action.SEARCH;
        mDefaultAssistLongPressAction = Action.fromIntSafe(res.getInteger(org.lineageos.platform.internal.R.integer.config_longPressOnAssistBehavior));
        mDefaultAssistDoubleTapAction = Action.fromIntSafe(res.getInteger(org.lineageos.platform.internal.R.integer.config_doubleTapOnAssistBehavior));
        mDefaultEdgeLongSwipeAction = Action.NOTHING;

        Action homeLongPressAction = Action.fromSettings(resolver, LineageSettings.System.KEY_HOME_LONG_PRESS_ACTION, mDefaultHomeLongPressAction);
        Action homeDoubleTapAction = Action.fromSettings(resolver, LineageSettings.System.KEY_HOME_DOUBLE_TAP_ACTION, mDefaultHomeDoubleTapAction);
        Action backLongPressAction = Action.fromSettings(resolver, LineageSettings.System.KEY_BACK_LONG_PRESS_ACTION, mDefaultBackLongPressAction);
        Action backDoubleTapAction = Action.fromSettings(resolver, LineageSettings.System.KEY_BACK_DOUBLE_TAP_ACTION, mDefaultBackDoubleTapAction);
        Action appSwitchShortPressAction = Action.fromSettings(resolver, LineageSettings.System.KEY_APP_SWITCH_ACTION, mDefaultAppSwitchShortPressAction);
        Action appSwitchLongPressAction = Action.fromSettings(resolver, LineageSettings.System.KEY_APP_SWITCH_LONG_PRESS_ACTION, mDefaultAppSwitchLongPressAction);
        Action appSwitchDoubleTapAction = Action.fromSettings(resolver, LineageSettings.System.KEY_APP_SWITCH_DOUBLE_TAP_ACTION, mDefaultAppSwitchDoubleTapAction);
        Action menuShortPressAction = Action.fromSettings(resolver, LineageSettings.System.KEY_MENU_ACTION, mDefaultMenuShortPressAction);
        Action menuLongPressAction = Action.fromSettings(resolver, LineageSettings.System.KEY_MENU_LONG_PRESS_ACTION, mDefaultMenuLongPressAction);
        Action menuDoubleTapAction = Action.fromSettings(resolver, LineageSettings.System.KEY_MENU_DOUBLE_TAP_ACTION, mDefaultMenuDoubleTapAction);
        Action assistShortPressAction = Action.fromSettings(resolver, LineageSettings.System.KEY_ASSIST_ACTION, mDefaultAssistShortPressAction);
        Action assistLongPressAction = Action.fromSettings(resolver, LineageSettings.System.KEY_ASSIST_LONG_PRESS_ACTION, mDefaultAssistLongPressAction);
        Action assistDoubleTapAction = Action.fromSettings(resolver, LineageSettings.System.KEY_ASSIST_DOUBLE_TAP_ACTION, mDefaultAssistDoubleTapAction);

        Action edgeLongSwipeAction = Action.fromSettings(resolver, LineageSettings.System.KEY_EDGE_LONG_SWIPE_ACTION, Action.NOTHING);

        mNavigationBarOptionsCategory = findPreference(CATEGORY_NAVBAR_OPTIONS);
        mNavigationHomeKeyCategory = findPreference(CATEGORY_HOME_KEY);
        mNavigationBackKeyCategory = findPreference(CATEGORY_BACK_KEY);
        mNavigationAppSwitchKeyCategory = findPreference(CATEGORY_APP_SWITCH_KEY);
        mNavigationMenuKeyCategory = findPreference(CATEGORY_MENU_KEY);
        mNavigationAssistKeyCategory = findPreference(CATEGORY_ASSIST_KEY);

        mEnableNavigationBar = findPreference(KEY_NAVIGATION_BAR_ENABLE);
        mNavigationSystemType = findPreference(KEY_NAVIGATION_SYSTEM_TYPE);
        mNavigationArrowKeys = findPreference(KEY_NAVIGATION_ARROW_KEYS);
        mNavigationInvertLayout = findPreference(KEY_NAVIGATION_INVERT_LAYOUT);
        mEdgeLongSwipeAction = initList(KEY_EDGE_LONG_SWIPE_ACTION, edgeLongSwipeAction);

        mHomeLongPressAction = initList(KEY_HOME_LONG_PRESS_ACTION, homeLongPressAction);
        mHomeDoubleTapAction = initList(KEY_HOME_DOUBLE_TAP_ACTION, homeDoubleTapAction);
        mBackLongPressAction = initList(KEY_BACK_LONG_PRESS_ACTION, backLongPressAction);
        mBackDoubleTapAction = initList(KEY_BACK_DOUBLE_TAP_ACTION, backDoubleTapAction);
        mAppSwitchShortPressAction = initList(KEY_APP_SWITCH_PRESS_ACTION, appSwitchShortPressAction);
        mAppSwitchLongPressAction = initList(KEY_APP_SWITCH_LONG_PRESS_ACTION, appSwitchLongPressAction);
        mAppSwitchDoubleTapAction = initList(KEY_APP_SWITCH_DOUBLE_TAP_ACTION, appSwitchDoubleTapAction);
        mMenuShortPressAction = initList(KEY_MENU_PRESS_ACTION, menuShortPressAction);
        mMenuLongPressAction = initList(KEY_MENU_LONG_PRESS_ACTION, menuLongPressAction);
        mMenuDoubleTapAction = initList(KEY_MENU_DOUBLE_TAP_ACTION, menuDoubleTapAction);
        mAssistShortPressAction = initList(KEY_ASSIST_PRESS_ACTION, assistShortPressAction);
        mAssistLongPressAction = initList(KEY_ASSIST_LONG_PRESS_ACTION, assistLongPressAction);
        mAssistDoubleTapAction = initList(KEY_ASSIST_DOUBLE_TAP_ACTION, assistDoubleTapAction);

        mHomeLongPressCustomApp = findPreference(KEY_HOME_LONG_PRESS_CUSTOM_APP);
        mHomeDoubleTapCustomApp = findPreference(KEY_HOME_DOUBLE_TAP_CUSTOM_APP);
        mBackLongPressCustomApp = findPreference(KEY_BACK_LONG_PRESS_CUSTOM_APP);
        mBackDoubleTapCustomApp = findPreference(KEY_BACK_DOUBLE_TAP_CUSTOM_APP);
        mAppSwitchLongPressCustomApp = findPreference(KEY_APP_SWITCH_LONG_PRESS_CUSTOM_APP);
        mAppSwitchDoubleTapCustomApp = findPreference(KEY_APP_SWITCH_DOUBLE_TAP_CUSTOM_APP);

        final LineageHardwareManager hardware = LineageHardwareManager.getInstance(getActivity());

        // Hardware key disabler
        mHardwareKeys = isKeyDisablerSupported(getActivity());
        if (mHardwareKeys) {
            updateEnableNavigationBarOption();
        } else {
            // Remove enable navbar option if no hardware keys
            prefScreen.removePreference(mEnableNavigationBar);
        }

        if (!hasMenuKey) {
            prefScreen.removePreference(mNavigationMenuKeyCategory);
        }

        if (!hasAssistKey) {
            prefScreen.removePreference(mNavigationAssistKeyCategory);
        }

        // Override key actions on Go devices in order to hide any unsupported features
        if (ActivityManager.isLowRamDeviceStatic()) {
            String[] actionEntriesGo = res.getStringArray(R.array.hardware_keys_action_entries_go);
            String[] actionValuesGo = res.getStringArray(R.array.hardware_keys_action_values_go);

            setListGoEntries(mHomeLongPressAction, actionEntriesGo, actionValuesGo);
            setListGoEntries(mHomeDoubleTapAction, actionEntriesGo, actionValuesGo);
            setListGoEntries(mBackLongPressAction, actionEntriesGo, actionValuesGo);
            setListGoEntries(mBackDoubleTapAction, actionEntriesGo, actionValuesGo);
            setListGoEntries(mAppSwitchShortPressAction, actionEntriesGo, actionValuesGo);
            setListGoEntries(mAppSwitchLongPressAction, actionEntriesGo, actionValuesGo);
            setListGoEntries(mAppSwitchDoubleTapAction, actionEntriesGo, actionValuesGo);
            setListGoEntries(mMenuShortPressAction, actionEntriesGo, actionValuesGo);
            setListGoEntries(mMenuLongPressAction, actionEntriesGo, actionValuesGo);
            setListGoEntries(mMenuDoubleTapAction, actionEntriesGo, actionValuesGo);
            setListGoEntries(mAssistShortPressAction, actionEntriesGo, actionValuesGo);
            setListGoEntries(mAssistLongPressAction, actionEntriesGo, actionValuesGo);
            setListGoEntries(mAssistDoubleTapAction, actionEntriesGo, actionValuesGo);
            setListGoEntries(mEdgeLongSwipeAction, actionEntriesGo, actionValuesGo);
        }

        updatePreferences(mEnableNavigationBar.isChecked());
        updateCustomAppSummaries();
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
        updatePreferences(mEnableNavigationBar.isChecked());
        updateCustomAppSummaries();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.add(0, MENU_RESET, 0, R.string.navigation_reset_button_settings_title)
                .setAlphabeticShortcut('r')
                .setEnabled(true)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_RESET:
                resetAll();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void resetAll() {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.navigation_reset_button_settings_title)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setMessage(R.string.navigation_reset_button_settings_message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        resetButtonsToDefaults();
                        dialog.dismiss();

                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private ListPreference initList(String key, Action value) {
        return initList(key, value.ordinal());
    }

    private ListPreference initList(String key, int value) {
        ListPreference list = getPreferenceScreen().findPreference(key);
        if (list == null) return null;
        list.setValue(Integer.toString(value));
        list.setSummary(list.getEntry());
        list.setOnPreferenceChangeListener(this);
        return list;
    }

    private void handleListChange(ListPreference pref, Object newValue, String setting) {
        String value = (String) newValue;
        int index = pref.findIndexOfValue(value);
        pref.setSummary(pref.getEntries()[index]);
        LineageSettings.System.putInt(getContentResolver(), setting, Integer.valueOf(value));
    }

    private void setListGoEntries(ListPreference listPref, String[] entries, String[] values) {
        listPref.setEntries(entries);
        listPref.setEntryValues(values);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mHomeLongPressAction) {
            handleListChange((ListPreference) preference, newValue,
                    LineageSettings.System.KEY_HOME_LONG_PRESS_ACTION);
            updateCustomAppSummaries();
            return true;
        } else if (preference == mHomeDoubleTapAction) {
            handleListChange((ListPreference) preference, newValue,
                    LineageSettings.System.KEY_HOME_DOUBLE_TAP_ACTION);
            updateCustomAppSummaries();
            return true;
        } else if (preference == mBackLongPressAction) {
            handleListChange((ListPreference) preference, newValue,
                    LineageSettings.System.KEY_BACK_LONG_PRESS_ACTION);
            updateCustomAppSummaries();
            return true;
        } else if (preference == mBackDoubleTapAction) {
            handleListChange((ListPreference) preference, newValue,
                    LineageSettings.System.KEY_BACK_DOUBLE_TAP_ACTION);
            updateCustomAppSummaries();
            return true;
        } else if (preference == mAppSwitchShortPressAction) {
            handleListChange((ListPreference) preference, newValue,
                    LineageSettings.System.KEY_APP_SWITCH_ACTION);
            return true;
        } else if (preference == mAppSwitchLongPressAction) {
            handleListChange((ListPreference) preference, newValue,
                    LineageSettings.System.KEY_APP_SWITCH_LONG_PRESS_ACTION);
            updateCustomAppSummaries();
            return true;
        } else if (preference == mAppSwitchDoubleTapAction) {
            handleListChange((ListPreference) preference, newValue,
                    LineageSettings.System.KEY_APP_SWITCH_DOUBLE_TAP_ACTION);
            updateCustomAppSummaries();
            return true;
        } else if (preference == mMenuShortPressAction) {
            handleListChange((ListPreference) preference, newValue,
                    LineageSettings.System.KEY_MENU_ACTION);
            return true;
        } else if (preference == mMenuLongPressAction) {
            handleListChange((ListPreference) preference, newValue,
                    LineageSettings.System.KEY_MENU_LONG_PRESS_ACTION);
            return true;
        } else if (preference == mMenuDoubleTapAction) {
            handleListChange((ListPreference) preference, newValue,
                    LineageSettings.System.KEY_MENU_DOUBLE_TAP_ACTION);
            return true;
        } else if (preference == mAssistShortPressAction) {
            handleListChange((ListPreference) preference, newValue,
                    LineageSettings.System.KEY_ASSIST_ACTION);
            return true;
        } else if (preference == mAssistLongPressAction) {
            handleListChange((ListPreference) preference, newValue,
                    LineageSettings.System.KEY_ASSIST_LONG_PRESS_ACTION);
            return true;
        } else if (preference == mAssistDoubleTapAction) {
            handleListChange((ListPreference) preference, newValue,
                    LineageSettings.System.KEY_ASSIST_DOUBLE_TAP_ACTION);
            return true;
        } else if (preference == mEdgeLongSwipeAction) {
            handleListChange(mEdgeLongSwipeAction, newValue,
                    LineageSettings.System.KEY_EDGE_LONG_SWIPE_ACTION);
            return true;
        }
        return false;
    }

    private void resetButtonsToDefaults() {
        resetListPreference(mHomeLongPressAction, mDefaultHomeLongPressAction,
                LineageSettings.System.KEY_HOME_LONG_PRESS_ACTION);
        resetListPreference(mHomeDoubleTapAction, mDefaultHomeDoubleTapAction,
                LineageSettings.System.KEY_HOME_DOUBLE_TAP_ACTION);
        resetListPreference(mBackLongPressAction, mDefaultBackLongPressAction,
                LineageSettings.System.KEY_BACK_LONG_PRESS_ACTION);
        resetListPreference(mBackDoubleTapAction, mDefaultBackDoubleTapAction,
                LineageSettings.System.KEY_BACK_DOUBLE_TAP_ACTION);
        resetListPreference(mAppSwitchShortPressAction, mDefaultAppSwitchShortPressAction,
                LineageSettings.System.KEY_APP_SWITCH_ACTION);
        resetListPreference(mAppSwitchLongPressAction, mDefaultAppSwitchLongPressAction,
                LineageSettings.System.KEY_APP_SWITCH_LONG_PRESS_ACTION);
        resetListPreference(mAppSwitchDoubleTapAction, mDefaultAppSwitchDoubleTapAction,
                LineageSettings.System.KEY_APP_SWITCH_DOUBLE_TAP_ACTION);
        resetListPreference(mMenuShortPressAction, mDefaultMenuShortPressAction,
                LineageSettings.System.KEY_MENU_ACTION);
        resetListPreference(mMenuLongPressAction, mDefaultMenuLongPressAction,
                LineageSettings.System.KEY_MENU_LONG_PRESS_ACTION);
        resetListPreference(mMenuDoubleTapAction, mDefaultMenuDoubleTapAction,
                LineageSettings.System.KEY_MENU_DOUBLE_TAP_ACTION);
        resetListPreference(mAssistShortPressAction, mDefaultAssistShortPressAction,
                LineageSettings.System.KEY_ASSIST_ACTION);
        resetListPreference(mAssistLongPressAction, mDefaultAssistLongPressAction,
                LineageSettings.System.KEY_ASSIST_LONG_PRESS_ACTION);
        resetListPreference(mAssistDoubleTapAction, mDefaultAssistDoubleTapAction,
                LineageSettings.System.KEY_ASSIST_DOUBLE_TAP_ACTION);
        resetListPreference(mEdgeLongSwipeAction, mDefaultEdgeLongSwipeAction,
                LineageSettings.System.KEY_EDGE_LONG_SWIPE_ACTION);

        resetCustomAppSettings(
                LineageSettings.System.KEY_HOME_LONG_PRESS_CUSTOM_APP,
                LineageSettings.System.KEY_HOME_LONG_PRESS_CUSTOM_APP_FR_NAME,
                LineageSettings.System.KEY_HOME_LONG_PRESS_CUSTOM_ACTIVITY);
        resetCustomAppSettings(
                LineageSettings.System.KEY_HOME_DOUBLE_TAP_CUSTOM_APP,
                LineageSettings.System.KEY_HOME_DOUBLE_TAP_CUSTOM_APP_FR_NAME,
                LineageSettings.System.KEY_HOME_DOUBLE_TAP_CUSTOM_ACTIVITY);
        resetCustomAppSettings(
                LineageSettings.System.KEY_BACK_LONG_PRESS_CUSTOM_APP,
                LineageSettings.System.KEY_BACK_LONG_PRESS_CUSTOM_APP_FR_NAME,
                LineageSettings.System.KEY_BACK_LONG_PRESS_CUSTOM_ACTIVITY);
        resetCustomAppSettings(
                LineageSettings.System.KEY_BACK_DOUBLE_TAP_CUSTOM_APP,
                LineageSettings.System.KEY_BACK_DOUBLE_TAP_CUSTOM_APP_FR_NAME,
                LineageSettings.System.KEY_BACK_DOUBLE_TAP_CUSTOM_ACTIVITY);
        resetCustomAppSettings(
                LineageSettings.System.KEY_APP_SWITCH_LONG_PRESS_CUSTOM_APP,
                LineageSettings.System.KEY_APP_SWITCH_LONG_PRESS_CUSTOM_APP_FR_NAME,
                LineageSettings.System.KEY_APP_SWITCH_LONG_PRESS_CUSTOM_ACTIVITY);
        resetCustomAppSettings(
                LineageSettings.System.KEY_APP_SWITCH_DOUBLE_TAP_CUSTOM_APP,
                LineageSettings.System.KEY_APP_SWITCH_DOUBLE_TAP_CUSTOM_APP_FR_NAME,
                LineageSettings.System.KEY_APP_SWITCH_DOUBLE_TAP_CUSTOM_ACTIVITY);
        updateCustomAppSummaries();
    }

    private void resetListPreference(ListPreference pref, Action defaultAction, String setting) {
        String action = Integer.toString(defaultAction.ordinal());
        handleListChange(pref, action, setting);
        pref.setValue(action);
    }

    private void resetCustomAppSettings(String customAppPref, String customAppFrNamePref, String customActivityPref) {
        LineageSettings.System.putStringForUser(getActivity().getContentResolver(),
                customAppPref, "", UserHandle.USER_CURRENT);
        LineageSettings.System.putStringForUser(getActivity().getContentResolver(),
                customAppFrNamePref, "", UserHandle.USER_CURRENT);
        LineageSettings.System.putStringForUser(getActivity().getContentResolver(),
                customActivityPref, "NONE", UserHandle.USER_CURRENT);
    }

    private static void writeForceShowNavbarSetting(Context context, boolean enabled) {
        LineageSettings.System.putIntForUser(context.getContentResolver(),
                LineageSettings.System.FORCE_SHOW_NAVBAR, enabled ? 1 : 0, UserHandle.USER_CURRENT);
    }

    private void updateEnableNavigationBarOption() {
        boolean enabled = LineageSettings.System.getIntForUser(getActivity().getContentResolver(),
                LineageSettings.System.FORCE_SHOW_NAVBAR, 0, UserHandle.USER_CURRENT) != 0;

        mEnableNavigationBar.setChecked(enabled);
    }

    private void updatePreferences(boolean navbarEnabled) {
        final boolean hasNavbar = navbarEnabled || !mHardwareKeys;
        final boolean gestureNavBar = DeviceUtils.isEdgeToEdgeEnabled(getContext());
        final boolean twoButtonNavBar = DeviceUtils.isSwipeUpEnabled(getContext());
        final boolean legacyNavBar = !gestureNavBar && !twoButtonNavBar;

        mNavigationSystemType.setEnabled(hasNavbar);
        if (gestureNavBar) {
            updateNavigationSystemTypeSummary(R.string.edge_to_edge_navigation_title);
        } else if (twoButtonNavBar) {
            updateNavigationSystemTypeSummary(R.string.swipe_up_to_switch_apps_title);
        } else {
            updateNavigationSystemTypeSummary(R.string.legacy_navigation_title);
        }
        mNavigationArrowKeys.setEnabled(hasNavbar && !gestureNavBar);
        mNavigationInvertLayout.setEnabled(hasNavbar && !gestureNavBar);
        mEdgeLongSwipeAction.setEnabled(hasNavbar && gestureNavBar);

        mNavigationHomeKeyCategory.setEnabled(!hasNavbar || !gestureNavBar);
        mNavigationBackKeyCategory.setEnabled(!hasNavbar || !gestureNavBar);
        mNavigationAppSwitchKeyCategory.setEnabled(!hasNavbar || legacyNavBar);
    }

    private void updateCustomAppSummaries() {
        mHomeLongPressCustomApp.setVisible(isCustomAppAction(
                LineageSettings.System.KEY_HOME_LONG_PRESS_ACTION, mDefaultHomeLongPressAction));
        mHomeDoubleTapCustomApp.setVisible(isCustomAppAction(
                LineageSettings.System.KEY_HOME_DOUBLE_TAP_ACTION, mDefaultHomeDoubleTapAction));
        mBackLongPressCustomApp.setVisible(isCustomAppAction(
                LineageSettings.System.KEY_BACK_LONG_PRESS_ACTION, mDefaultBackLongPressAction));
        mBackDoubleTapCustomApp.setVisible(isCustomAppAction(
                LineageSettings.System.KEY_BACK_DOUBLE_TAP_ACTION, mDefaultBackDoubleTapAction));
        mAppSwitchLongPressCustomApp.setVisible(isCustomAppAction(
                LineageSettings.System.KEY_APP_SWITCH_LONG_PRESS_ACTION, mDefaultAppSwitchLongPressAction));
        mAppSwitchDoubleTapCustomApp.setVisible(isCustomAppAction(
                LineageSettings.System.KEY_APP_SWITCH_DOUBLE_TAP_ACTION, mDefaultAppSwitchDoubleTapAction));

        setCustomAppSummary(mHomeLongPressCustomApp, LineageSettings.System.KEY_HOME_LONG_PRESS_CUSTOM_APP_FR_NAME);
        setCustomAppSummary(mBackLongPressCustomApp, LineageSettings.System.KEY_BACK_LONG_PRESS_CUSTOM_APP_FR_NAME);
        setCustomAppSummary(mAppSwitchLongPressCustomApp, LineageSettings.System.KEY_APP_SWITCH_LONG_PRESS_CUSTOM_APP_FR_NAME);
        setCustomAppSummary(mHomeDoubleTapCustomApp, LineageSettings.System.KEY_HOME_DOUBLE_TAP_CUSTOM_APP_FR_NAME);
        setCustomAppSummary(mBackDoubleTapCustomApp, LineageSettings.System.KEY_BACK_DOUBLE_TAP_CUSTOM_APP_FR_NAME);
        setCustomAppSummary(mAppSwitchDoubleTapCustomApp, LineageSettings.System.KEY_APP_SWITCH_DOUBLE_TAP_CUSTOM_APP_FR_NAME);
    }

    private boolean isCustomAppAction(String key, Action def) {
        int action = LineageSettings.System.getIntForUser(getContentResolver(),
                key, def.ordinal(), UserHandle.USER_CURRENT);
        return (action == Action.CUSTOM_APP.ordinal());
    }

    private void setCustomAppSummary(Preference pref, String key) {
        String summary = LineageSettings.System.getStringForUser(getActivity().getContentResolver(),
                key, UserHandle.USER_CURRENT);
        if (TextUtils.isEmpty(summary)) {
            summary = getString(R.string.none);
        }
        pref.setSummary(summary);
    }

    private void updateNavigationSystemTypeSummary(int systemType) {
        if (mNavigationSystemType != null) {
            mNavigationSystemType.setSummary(systemType);
        }
    }

    private static boolean isKeyDisablerSupported(Context context) {
        final LineageHardwareManager hardware = LineageHardwareManager.getInstance(context);
        return hardware.isSupported(LineageHardwareManager.FEATURE_KEY_DISABLE);
    }

    public static void restoreKeyDisabler(Context context) {
        if (!isKeyDisablerSupported(context)) {
            return;
        }

        boolean enabled = LineageSettings.System.getIntForUser(context.getContentResolver(),
                LineageSettings.System.FORCE_SHOW_NAVBAR, 0, UserHandle.USER_CURRENT) != 0;

        writeForceShowNavbarSetting(context, enabled);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mEnableNavigationBar) {
            mEnableNavigationBar.setEnabled(false);
            writeForceShowNavbarSetting(getActivity(), mEnableNavigationBar.isChecked());
            updateEnableNavigationBarOption();
            updatePreferences(false);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mEnableNavigationBar.setEnabled(true);
                    updatePreferences(mEnableNavigationBar.isChecked());
                }
            }, 1000);
        }

        return super.onPreferenceTreeClick(preference);
    }

    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();
                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.menu_navigation_settings;
                    result.add(sir);

                    return result;
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);

                    if (!isKeyDisablerSupported(context)) {
                        keys.add(KEY_NAVIGATION_BAR_ENABLE);
                    }

                    if (!DeviceUtils.hasMenuKey(context)) {
                        keys.add(CATEGORY_MENU_KEY);
                        keys.add(KEY_MENU_PRESS_ACTION);
                        keys.add(KEY_MENU_LONG_PRESS_ACTION);
                        keys.add(KEY_MENU_DOUBLE_TAP_ACTION);
                    }

                    if (!DeviceUtils.hasAssistKey(context)) {
                        keys.add(CATEGORY_ASSIST_KEY);
                        keys.add(KEY_ASSIST_PRESS_ACTION);
                        keys.add(KEY_ASSIST_LONG_PRESS_ACTION);
                        keys.add(KEY_ASSIST_DOUBLE_TAP_ACTION);
                    }

                    return keys;
                }
            };
}

