/*
 * Copyright (C) 2013 The CyanogenMod Project
 * Copyright (C) 2017 The LineageOS Project
 * Copyright (C) 2019-2021 Altair ROM Project
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
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;

import androidx.preference.SwitchPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;

import com.altair.settings.fragments.ButtonBacklightBrightness;
import com.altair.settings.utils.DeviceUtils;
import com.altair.settings.utils.TelephonyUtils;

import com.android.internal.logging.nano.MetricsProto;

import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;

import com.android.settingslib.search.SearchIndexable;

import com.lineage.support.preferences.CustomSeekBarPreference;

import java.util.Arrays;
import java.util.List;

import lineageos.hardware.LineageHardwareManager;
import lineageos.providers.LineageSettings;

import static org.lineageos.internal.util.DeviceKeysConstants.*;

@SearchIndexable
public class CustomButtonSettings extends DashboardFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "CustomButtonSettings";

    private static final String KEY_BUTTON_BACKLIGHT = "navkeys_button_backlight";
    private static final String KEY_BACK_WAKE_SCREEN = "back_wake_screen";
    private static final String KEY_CAMERA_LAUNCH = "camera_launch";
    private static final String KEY_CAMERA_SLEEP_ON_RELEASE = "camera_sleep_on_release";
    private static final String KEY_CAMERA_WAKE_SCREEN = "camera_wake_screen";
    private static final String KEY_HOME_WAKE_SCREEN = "home_wake_screen";
    private static final String KEY_MENU_WAKE_SCREEN = "menu_wake_screen";
    private static final String KEY_ASSIST_WAKE_SCREEN = "assist_wake_screen";
    private static final String KEY_APP_SWITCH_WAKE_SCREEN = "app_switch_wake_screen";
    private static final String KEY_VOLUME_KEY_CURSOR_CONTROL = "volume_key_cursor_control";
    private static final String KEY_SWAP_VOLUME_BUTTONS = "swap_volume_buttons";
    private static final String KEY_VOLUME_PANEL_ON_LEFT = "volume_panel_on_left";
    private static final String KEY_VOLUME_WAKE_SCREEN = "volume_wake_screen";
    private static final String KEY_VOLUME_ANSWER_CALL = "volume_answer_call";
    private static final String KEY_POWER_END_CALL = "power_end_call";
    private static final String KEY_HOME_ANSWER_CALL = "home_answer_call";
    private static final String KEY_VOLUME_MUSIC_CONTROLS = "volbtn_music_controls";
    private static final String KEY_TORCH_LONG_PRESS_POWER_GESTURE =
            "torch_long_press_power_gesture";
    private static final String KEY_TORCH_LONG_PRESS_POWER_TIMEOUT =
            "torch_long_press_power_timeout";
    private static final String KEY_CLICK_PARTIAL_SCREENSHOT =
            "click_partial_screenshot";

    private static final String CATEGORY_WAKE_SCREEN = "wake_screen";
    private static final String CATEGORY_POWER = "power_key";
    private static final String CATEGORY_HOME = "home_key";
    private static final String CATEGORY_VOLUME = "volume_keys";
    private static final String CATEGORY_CAMERA = "camera_key";
    private static final String CATEGORY_EXTRAS = "extras_category";

    private SwitchPreference mHomeWakeScreen;
    private SwitchPreference mBackWakeScreen;
    private SwitchPreference mAppSwitchWakeScreen;
    private SwitchPreference mMenuWakeScreen;
    private SwitchPreference mAssistWakeScreen;
    private SwitchPreference mCameraWakeScreen;
    private SwitchPreference mCameraSleepOnRelease;
    private SwitchPreference mVolumeWakeScreen;
    private SwitchPreference mCameraLaunch;
    private ListPreference mVolumeKeyCursorControl;
    private SwitchPreference mVolumeMusicControls;
    private SwitchPreference mSwapVolumeButtons;
    private SwitchPreference mVolumePanelOnLeft;
    private SwitchPreference mPowerEndCall;
    private SwitchPreference mHomeAnswerCall;
    private SwitchPreference mTorchLongPressPowerGesture;
    private ListPreference mTorchLongPressPowerTimeout;
    private ButtonBacklightBrightness mBacklight;

    private Handler mHandler;
    private ContentResolver mResolver;

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.menu_button_settings;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mResolver = getActivity().getContentResolver();

        final Resources res = getResources();
        final ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();

        final boolean hasPowerKey = DeviceUtils.hasPowerKey();
        final boolean hasHomeKey = DeviceUtils.hasHomeKey(getActivity());
        final boolean hasBackKey = DeviceUtils.hasBackKey(getActivity());
        final boolean hasMenuKey = DeviceUtils.hasMenuKey(getActivity());
        final boolean hasAssistKey = DeviceUtils.hasAssistKey(getActivity());
        final boolean hasAppSwitchKey = DeviceUtils.hasAppSwitchKey(getActivity());
        final boolean hasCameraKey = DeviceUtils.hasCameraKey(getActivity());
        final boolean hasVolumeKeys = DeviceUtils.hasVolumeKeys(getActivity());

        final boolean showHomeWake = DeviceUtils.canWakeUsingHomeKey(getActivity());
        final boolean showBackWake = DeviceUtils.canWakeUsingBackKey(getActivity());
        final boolean showMenuWake = DeviceUtils.canWakeUsingMenuKey(getActivity());
        final boolean showAssistWake = DeviceUtils.canWakeUsingAssistKey(getActivity());
        final boolean showAppSwitchWake = DeviceUtils.canWakeUsingAppSwitchKey(getActivity());
        final boolean showCameraWake = DeviceUtils.canWakeUsingCameraKey(getActivity());
        final boolean showVolumeWake = DeviceUtils.canWakeUsingVolumeKeys(getActivity());

        final PreferenceCategory wakeScreenCategory = prefScreen.findPreference(CATEGORY_WAKE_SCREEN);
        final PreferenceCategory powerCategory = prefScreen.findPreference(CATEGORY_POWER);
        final PreferenceCategory homeCategory = prefScreen.findPreference(CATEGORY_HOME);
        final PreferenceCategory volumeCategory = prefScreen.findPreference(CATEGORY_VOLUME);
        final PreferenceCategory cameraCategory = prefScreen.findPreference(CATEGORY_CAMERA);
        final PreferenceCategory extrasCategory = prefScreen.findPreference(CATEGORY_EXTRAS);

        // Power button ends calls.
        mPowerEndCall = findPreference(KEY_POWER_END_CALL);

        // Long press power while display is off to activate torchlight
        mTorchLongPressPowerGesture = findPreference(KEY_TORCH_LONG_PRESS_POWER_GESTURE);
        final int torchLongPressPowerTimeout = LineageSettings.System.getInt(mResolver,
                LineageSettings.System.TORCH_LONG_PRESS_POWER_TIMEOUT, 0);
        mTorchLongPressPowerTimeout = initList(KEY_TORCH_LONG_PRESS_POWER_TIMEOUT,
                torchLongPressPowerTimeout);

        // Home button answers calls.
        mHomeAnswerCall = findPreference(KEY_HOME_ANSWER_CALL);

        mHandler = new Handler();

        final LineageHardwareManager hardware = LineageHardwareManager.getInstance(getActivity());

        boolean navKeysEnabled = LineageSettings.System.getIntForUser(getActivity().getContentResolver(),
                LineageSettings.System.FORCE_SHOW_NAVBAR, 0, UserHandle.USER_CURRENT) != 0;

        if (hasPowerKey) {
            if (!TelephonyUtils.isVoiceCapable(getActivity())) {
                powerCategory.removePreference(mPowerEndCall);
                mPowerEndCall = null;
            }
            if (!DeviceUtils.deviceSupportsFlashLight(getActivity())) {
                powerCategory.removePreference(mTorchLongPressPowerGesture);
                powerCategory.removePreference(mTorchLongPressPowerTimeout);
            }
        } else {
            prefScreen.removePreference(powerCategory);
        }

        if (!hasHomeKey || !TelephonyUtils.isVoiceCapable(getActivity())) {
            prefScreen.removePreference(homeCategory);
            mHomeAnswerCall = null;
        }

        mCameraWakeScreen = findPreference(KEY_CAMERA_WAKE_SCREEN);

        if (hasCameraKey) {
            mCameraSleepOnRelease = findPreference(KEY_CAMERA_SLEEP_ON_RELEASE);
            mCameraLaunch = findPreference(KEY_CAMERA_LAUNCH);

            // Only show 'Camera sleep on release' if the device has a focus key
            if (res.getBoolean(org.lineageos.platform.internal.R.bool.config_singleStageCameraKey)) {
                prefScreen.removePreference(mCameraSleepOnRelease);
            }
        } else {
            prefScreen.removePreference(cameraCategory);
        }

        if (hasVolumeKeys) {
            if (!TelephonyUtils.isVoiceCapable(getActivity())) {
                volumeCategory.removePreference(findPreference(KEY_VOLUME_ANSWER_CALL));
            }

            int cursorControlAction = Settings.System.getInt(mResolver,
                    Settings.System.VOLUME_KEY_CURSOR_CONTROL, 0);
            mVolumeKeyCursorControl = initList(KEY_VOLUME_KEY_CURSOR_CONTROL,
                    cursorControlAction);

            int swapVolumeKeys = LineageSettings.System.getInt(getContentResolver(),
                    LineageSettings.System.SWAP_VOLUME_KEYS_ON_ROTATION, 0);
            mSwapVolumeButtons = (SwitchPreference)
                    prefScreen.findPreference(KEY_SWAP_VOLUME_BUTTONS);
            if (mSwapVolumeButtons != null) {
                mSwapVolumeButtons.setChecked(swapVolumeKeys > 0);
            }

            final boolean volumePanelOnLeft = LineageSettings.Secure.getIntForUser(
                    getContentResolver(), LineageSettings.Secure.VOLUME_PANEL_ON_LEFT, 0,
                    UserHandle.USER_CURRENT) != 0;
            mVolumePanelOnLeft = (SwitchPreference)
                    prefScreen.findPreference(KEY_VOLUME_PANEL_ON_LEFT);
            if (mVolumePanelOnLeft != null) {
                mVolumePanelOnLeft.setChecked(volumePanelOnLeft);
            }
        } else {
            prefScreen.removePreference(volumeCategory);
            prefScreen.removePreference(mVolumeWakeScreen);
            extrasCategory.removePreference(findPreference(KEY_CLICK_PARTIAL_SCREENSHOT));
        }

        mBacklight = findPreference(KEY_BUTTON_BACKLIGHT);
        if (!DeviceUtils.hasButtonBacklightSupport(getActivity())
                && !DeviceUtils.hasKeyboardBacklightSupport(getActivity())) {
            prefScreen.removePreference(mBacklight);
            mBacklight = null;
        } else if (isKeyDisablerSupported(getActivity())) {
            //mBacklight.setEnabled(!(Settings.System.getIntForUser(resolver,
            //        Settings.System.HARDWARE_KEYS_DISABLE, 0,
            //        UserHandle.USER_CURRENT) == 1));
            mBacklight.setEnabled(!(
                LineageSettings.System.getIntForUser(getActivity().getContentResolver(),
                                LineageSettings.System.FORCE_SHOW_NAVBAR, 0, UserHandle.USER_CURRENT) != 0)
            );
        }
        boolean enabled = LineageSettings.System.getIntForUser(getActivity().getContentResolver(),
                LineageSettings.System.FORCE_SHOW_NAVBAR, 0, UserHandle.USER_CURRENT) != 0;

        if (mCameraWakeScreen != null) {
            if (mCameraSleepOnRelease != null && !res.getBoolean(
                    org.lineageos.platform.internal.R.bool.config_singleStageCameraKey)) {
                mCameraSleepOnRelease.setDependency(KEY_CAMERA_WAKE_SCREEN);
            }
        }

        mVolumeWakeScreen = findPreference(KEY_VOLUME_WAKE_SCREEN);
        mVolumeMusicControls = findPreference(KEY_VOLUME_MUSIC_CONTROLS);

        if (mVolumeWakeScreen != null) {
            if (mVolumeMusicControls != null) {
                mVolumeMusicControls.setDependency(KEY_VOLUME_WAKE_SCREEN);
                mVolumeWakeScreen.setDisableDependentsState(true);
            }
        }

        int wakeKeyTotal = 7;

        if (!hasHomeKey || !showHomeWake) {
            wakeScreenCategory.removePreference(findPreference(KEY_HOME_WAKE_SCREEN));
            wakeKeyTotal -= 1;
        }
        if (!hasBackKey || !showBackWake) {
            wakeScreenCategory.removePreference(findPreference(KEY_BACK_WAKE_SCREEN));
            wakeKeyTotal -= 1;
        }
        if (!hasAppSwitchKey || !showAppSwitchWake) {
            wakeScreenCategory.removePreference(findPreference(KEY_APP_SWITCH_WAKE_SCREEN));
            wakeKeyTotal -= 1;
        }
        if (!hasMenuKey || !showMenuWake) {
            wakeScreenCategory.removePreference(findPreference(KEY_MENU_WAKE_SCREEN));
            wakeKeyTotal -= 1;
        }
        if (!hasAssistKey || !showAssistWake) {
            wakeScreenCategory.removePreference(findPreference(KEY_ASSIST_WAKE_SCREEN));
            wakeKeyTotal -= 1;
        }
        if (!hasCameraKey || !showCameraWake) {
            wakeScreenCategory.removePreference(findPreference(KEY_CAMERA_WAKE_SCREEN));
            wakeKeyTotal -= 1;
        }
        if (!hasVolumeKeys || !showVolumeWake) {
            wakeScreenCategory.removePreference(findPreference(KEY_VOLUME_WAKE_SCREEN));
            wakeKeyTotal -= 1;
        }

        if (wakeKeyTotal == 0) {
            prefScreen.removePreference(wakeScreenCategory);
        }
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

        // Power button ends calls.
        if (mPowerEndCall != null) {
            final int incallPowerBehavior = Settings.Secure.getInt(getContentResolver(),
                    Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR,
                    Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR_DEFAULT);
            final boolean powerButtonEndsCall =
                    (incallPowerBehavior == Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR_HANGUP);
            mPowerEndCall.setChecked(powerButtonEndsCall);
        }

        // Home button answers calls.
        if (mHomeAnswerCall != null) {
            final int incallHomeBehavior = LineageSettings.Secure.getInt(getContentResolver(),
                    LineageSettings.Secure.RING_HOME_BUTTON_BEHAVIOR,
                    LineageSettings.Secure.RING_HOME_BUTTON_BEHAVIOR_DEFAULT);
            final boolean homeButtonAnswersCall =
                (incallHomeBehavior == LineageSettings.Secure.RING_HOME_BUTTON_BEHAVIOR_ANSWER);
            mHomeAnswerCall.setChecked(homeButtonAnswersCall);
        }
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

    private void handleSystemListChange(ListPreference pref, Object newValue, String setting) {
        String value = (String) newValue;
        int index = pref.findIndexOfValue(value);
        pref.setSummary(pref.getEntries()[index]);
        Settings.System.putInt(getContentResolver(), setting, Integer.valueOf(value));
    }

    private void setListGoEntries(ListPreference listPref, String[] entries, String[] values) {
        listPref.setEntries(entries);
        listPref.setEntryValues(values);
    }

    private static boolean isKeyDisablerSupported(Context context) {
        final LineageHardwareManager hardware = LineageHardwareManager.getInstance(context);
        return hardware.isSupported(LineageHardwareManager.FEATURE_KEY_DISABLE);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mVolumeKeyCursorControl) {
            handleSystemListChange(mVolumeKeyCursorControl, newValue,
                    Settings.System.VOLUME_KEY_CURSOR_CONTROL);
            return true;
        } else if (preference == mTorchLongPressPowerTimeout) {
            handleListChange(mTorchLongPressPowerTimeout, newValue,
                    LineageSettings.System.TORCH_LONG_PRESS_POWER_TIMEOUT);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mSwapVolumeButtons) {
            int value;

            if (mSwapVolumeButtons.isChecked()) {
                /* The native inputflinger service uses the same logic of:
                 *   1 - the volume rocker is on one the sides, relative to the natural
                 *       orientation of the display (true for all phones and most tablets)
                 *   2 - the volume rocker is on the top or bottom, relative to the
                 *       natural orientation of the display (true for some tablets)
                 */
                value = getResources().getInteger(
                        R.integer.config_volumeRockerVsDisplayOrientation);
            } else {
                /* Disable the re-orient functionality */
                value = 0;
            }
            LineageSettings.System.putInt(getActivity().getContentResolver(),
                    LineageSettings.System.SWAP_VOLUME_KEYS_ON_ROTATION, value);
        } else if (preference == mVolumePanelOnLeft) {
            LineageSettings.Secure.putIntForUser(getActivity().getContentResolver(),
                    LineageSettings.Secure.VOLUME_PANEL_ON_LEFT,
                    mVolumePanelOnLeft.isChecked() ? 1 : 0, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mPowerEndCall) {
            handleTogglePowerButtonEndsCallPreferenceClick();
            return true;
        } else if (preference == mHomeAnswerCall) {
            handleToggleHomeButtonAnswersCallPreferenceClick();
            return true;
        }

        return super.onPreferenceTreeClick(preference);
    }

    private void handleTogglePowerButtonEndsCallPreferenceClick() {
        Settings.Secure.putInt(getContentResolver(),
                Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR, (mPowerEndCall.isChecked()
                        ? Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR_HANGUP
                        : Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR_SCREEN_OFF));
    }

    private void handleToggleHomeButtonAnswersCallPreferenceClick() {
        LineageSettings.Secure.putInt(getContentResolver(),
                LineageSettings.Secure.RING_HOME_BUTTON_BEHAVIOR, (mHomeAnswerCall.isChecked()
                        ? LineageSettings.Secure.RING_HOME_BUTTON_BEHAVIOR_ANSWER
                        : LineageSettings.Secure.RING_HOME_BUTTON_BEHAVIOR_DO_NOTHING));
    }

    public static void reset(Context mContext) {
        ContentResolver resolver = mContext.getContentResolver();
        ButtonBacklightBrightness.reset(mContext);
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(
                        Context context, boolean enabled) {
                    final SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.menu_button_settings;
                    return Arrays.asList(sir);
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);

                    if (!TelephonyUtils.isVoiceCapable(context)) {
                        keys.add(KEY_POWER_END_CALL);
                        keys.add(KEY_HOME_ANSWER_CALL);
                        keys.add(KEY_VOLUME_ANSWER_CALL);
                    }

                    if (!DeviceUtils.hasHomeKey(context)) {
                        keys.add(CATEGORY_HOME);
                        keys.add(KEY_HOME_ANSWER_CALL);
                        keys.add(KEY_HOME_WAKE_SCREEN);
                    } else if (!DeviceUtils.canWakeUsingHomeKey(context)) {
                        keys.add(KEY_HOME_WAKE_SCREEN);
                    }

                    if (!DeviceUtils.hasCameraKey(context)) {
                        keys.add(CATEGORY_CAMERA);
                        keys.add(KEY_CAMERA_LAUNCH);
                        keys.add(KEY_CAMERA_SLEEP_ON_RELEASE);
                        keys.add(KEY_CAMERA_WAKE_SCREEN);
                    } else if (!DeviceUtils.canWakeUsingCameraKey(context)) {
                        keys.add(KEY_CAMERA_WAKE_SCREEN);
                    }

                    if (!DeviceUtils.hasVolumeKeys(context)) {
                        keys.add(CATEGORY_VOLUME);
                        keys.add(KEY_SWAP_VOLUME_BUTTONS);
                        keys.add(KEY_VOLUME_ANSWER_CALL);
                        keys.add(KEY_VOLUME_KEY_CURSOR_CONTROL);
                        keys.add(KEY_VOLUME_MUSIC_CONTROLS);
                        keys.add(KEY_VOLUME_PANEL_ON_LEFT);
                        keys.add(KEY_VOLUME_WAKE_SCREEN);
                        keys.add(KEY_CLICK_PARTIAL_SCREENSHOT);
                    } else if (!DeviceUtils.canWakeUsingVolumeKeys(context)) {
                        keys.add(KEY_VOLUME_WAKE_SCREEN);
                    }

                    if (!DeviceUtils.deviceSupportsFlashLight(context)) {
                        keys.add(KEY_TORCH_LONG_PRESS_POWER_GESTURE);
                        keys.add(KEY_TORCH_LONG_PRESS_POWER_TIMEOUT);
                    }

                    if (!DeviceUtils.hasButtonBacklightSupport(context)
                            && !DeviceUtils.hasKeyboardBacklightSupport(context)) {
                        keys.add(KEY_BUTTON_BACKLIGHT);
                    }

                    return keys;
                }
            };
}

