/*
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
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import com.altair.settings.utils.SystemUtils;

import com.android.internal.logging.nano.MetricsProto;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class UserInterfaceSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "UserInterface";

    private static final String RECENTS_COMPONENT_TYPE = "recents_component";
    private static final String RECENTS_TYPE = "recents_layout_style";
    private static final String RECENTS_CLEAR_ALL_LOCATION = "recents_clear_all_location";

    private static final int RECENTS_COMPONENT_OREO = 1;

/*
    private ListPreference mRecentsComponentType;
    private ListPreference mRecentsClearAllLocation;
    private SwitchPreference mRecentsClearAll;
    private ListPreference mRecentsType;
    private PreferenceCategory mOreoRecentsCat;
*/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.userinterface_settings);

        final ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefSet = getPreferenceScreen();

        // SmartPixels
        boolean enableSmartPixels = getContext().getResources().
                getBoolean(com.android.internal.R.bool.config_enableSmartPixels);
        Preference SmartPixels = findPreference("smart_pixels");
        if (!enableSmartPixels) {
            prefSet.removePreference(SmartPixels);
        }

/*
        // recents component type
        mRecentsComponentType = findPreference(RECENTS_COMPONENT_TYPE);
        int type = Settings.System.getInt(resolver,
                Settings.System.RECENTS_COMPONENT, 0);
        mRecentsComponentType.setValue(String.valueOf(type));
        mRecentsComponentType.setSummary(mRecentsComponentType.getEntry());
        mRecentsComponentType.setOnPreferenceChangeListener(this);

        // clear all recents
        mRecentsClearAllLocation = findPreference(RECENTS_CLEAR_ALL_LOCATION);
        int location = Settings.System.getIntForUser(resolver,
                Settings.System.RECENTS_CLEAR_ALL_LOCATION, 3, UserHandle.USER_CURRENT);
        mRecentsClearAllLocation.setValue(String.valueOf(location));
        mRecentsClearAllLocation.setSummary(mRecentsClearAllLocation.getEntry());
        mRecentsClearAllLocation.setOnPreferenceChangeListener(this);

        // oreo recents type
        mRecentsType = findPreference(RECENTS_TYPE);
        int style = Settings.System.getIntForUser(resolver,
                Settings.System.RECENTS_LAYOUT_STYLE, 0, UserHandle.USER_CURRENT);
        mRecentsType.setValue(String.valueOf(style));
        mRecentsType.setSummary(mRecentsType.getEntry());
        mRecentsType.setOnPreferenceChangeListener(this);

        mOreoRecentsCat = findPreference("recents_ui_oreo_recents_category");
        mOreoRecentsCat.setEnabled(type == RECENTS_COMPONENT_OREO);
*/
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.ALTAIR;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
/*
        final String key = preference.getKey();
        if (preference == mRecentsComponentType) {
            int type = Integer.valueOf((String) objValue);
            int index = mRecentsComponentType.findIndexOfValue((String) objValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.RECENTS_COMPONENT, type);
            mRecentsComponentType.setSummary(mRecentsComponentType.getEntries()[index]);
            mOreoRecentsCat.setEnabled(type == RECENTS_COMPONENT_OREO);
            if (type == RECENTS_COMPONENT_OREO) { // Disable swipe up gesture, if oreo type selected
                Settings.Secure.putInt(getActivity().getContentResolver(),
                    Settings.Secure.SWIPE_UP_TO_SWITCH_APPS_ENABLED, 0);
            }
            SystemUtils.showSystemUiRestartDialog(getContext());
            return true;
        } else if (preference == mRecentsClearAllLocation) {
            int location = Integer.valueOf((String) objValue);
            int index = mRecentsClearAllLocation.findIndexOfValue((String) objValue);
            Settings.System.putIntForUser(getActivity().getContentResolver(),
                    Settings.System.RECENTS_CLEAR_ALL_LOCATION, location, UserHandle.USER_CURRENT);
            mRecentsClearAllLocation.setSummary(mRecentsClearAllLocation.getEntries()[index]);
            return true;
        } else if (preference == mRecentsType) {
            int style = Integer.valueOf((String) objValue);
            int index = mRecentsType.findIndexOfValue((String) objValue);
            Settings.System.putIntForUser(getActivity().getContentResolver(),
                    Settings.System.RECENTS_LAYOUT_STYLE, style, UserHandle.USER_CURRENT);
            mRecentsType.setSummary(mRecentsType.getEntries()[index]);
            SystemUtils.showSystemUiRestartDialog(getContext());
            return true;
        }
*/
        return true;
    }
}
