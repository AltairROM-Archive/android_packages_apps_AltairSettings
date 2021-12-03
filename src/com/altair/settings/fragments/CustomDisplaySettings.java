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
import android.content.Context;
import android.hardware.display.AmbientDisplayConfiguration;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import com.altair.settings.utils.SystemUtils;
import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.display.AmbientDisplayAlwaysOnPreferenceController;
import com.android.settings.display.AmbientDisplayNotificationsPreferenceController;
import com.android.settings.gestures.DoubleTapScreenPreferenceController;
import com.android.settings.gestures.PickupGesturePreferenceController;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settingslib.search.SearchIndexable;
import com.lineage.support.preferences.GlobalSettingListPreference;

import java.util.ArrayList;
import java.util.List;

@SearchIndexable
public class CustomDisplaySettings extends DashboardFragment implements
        Preference.OnPreferenceChangeListener, Indexable {
    private static final String TAG = "CustomDisplaySettings";

    private static final String KEY_SMART_PIXELS = "smart_pixels";
    private static final String KEY_REFRESH_RATE_SETTING = "refresh_rate_setting";

    private static final String CATEGORY_MISCELLANEOUS = "miscellaneous";

    private AmbientDisplayConfiguration mConfig;

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.menu_display_settings;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();
        final PreferenceCategory miscCategory = prefScreen.findPreference(CATEGORY_MISCELLANEOUS);

        // Smart Pixels
        boolean enableSmartPixels = getContext().getResources().
                getBoolean(com.android.internal.R.bool.config_enableSmartPixels);
        Preference smartPixels = findPreference(KEY_SMART_PIXELS);
        if (!enableSmartPixels) {
            miscCategory.removePreference(smartPixels);
        }

        // Refresh rate
        GlobalSettingListPreference refreshRateSetting = prefScreen.findPreference(KEY_REFRESH_RATE_SETTING);
        boolean hasVariableRefreshRate =
            getContext().getResources().getBoolean(com.android.internal.R.bool.config_hasVariableRefreshRate);
        if (!hasVariableRefreshRate) {
            miscCategory.removePreference(refreshRateSetting);
        } else {
            int defVarRateSetting = getContext().getResources().getInteger(
                 com.android.internal.R.integer.config_defaultVariableRefreshRateSetting);
            int mVarRateSetting = Settings.Global.getInt(getContext().getContentResolver(),
                 Settings.Global.REFRESH_RATE_SETTING, defVarRateSetting);
            refreshRateSetting.setValue(String.valueOf(mVarRateSetting));
        }

        if (!enableSmartPixels && !hasVariableRefreshRate) {
            prefScreen.removePreference(miscCategory);
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
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        use(AmbientDisplayAlwaysOnPreferenceController.class).setConfig(getConfig(context));
        use(AmbientDisplayNotificationsPreferenceController.class).setConfig(getConfig(context));
        use(DoubleTapScreenPreferenceController.class).setConfig(getConfig(context));
        use(PickupGesturePreferenceController.class).setConfig(getConfig(context));
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        return true;
    }

    private AmbientDisplayConfiguration getConfig(Context context) {
        if (mConfig == null) {
            mConfig = new AmbientDisplayConfiguration(context);
        }
        return mConfig;
    }

    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();
                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.menu_display_settings;
                    result.add(sir);

                    return result;
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);

                    boolean enableSmartPixels = context.getResources().
                            getBoolean(com.android.internal.R.bool.config_enableSmartPixels);
                    if (!enableSmartPixels) {
                        keys.add(KEY_SMART_PIXELS);
                    }

                    boolean hasVariableRefreshRate =
                        context.getResources().getBoolean(com.android.internal.R.bool.config_hasVariableRefreshRate);
                    if (!hasVariableRefreshRate) {
                        keys.add(KEY_REFRESH_RATE_SETTING);
                    }

                    return keys;
                }
            };
}
