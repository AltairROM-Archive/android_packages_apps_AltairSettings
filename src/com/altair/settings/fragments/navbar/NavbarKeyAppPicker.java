/*
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

package com.altair.settings.fragments.navbar;

import android.content.pm.ActivityInfo;
import android.provider.Settings;
import android.view.View;
import android.widget.ListView;

import com.lineage.support.apppicker.AppPicker;

public class NavbarKeyAppPicker extends AppPicker {

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        if (!mIsActivitiesList) {
            // we are in the Apps list
            String packageName = applist.get(position).packageName;
            String friendlyAppString = (String) applist.get(position).loadLabel(packageManager);
            setPackage(packageName, friendlyAppString);
            setPackageActivity(null);
        } else if (mIsActivitiesList) {
            // we are in the Activities list
            setPackageActivity(mActivitiesList.get(position));
        }

        mIsActivitiesList = false;
        finish();
    }

    @Override
    protected void onLongClick(int position) {
        if (mIsActivitiesList) return;
        String packageName = applist.get(position).packageName;
        String friendlyAppString = (String) applist.get(position).loadLabel(packageManager);
        // always set CUSTOM_APP so we can fallback if something goes wrong with
        // packageManager.getPackageInfo
        setPackage(packageName, friendlyAppString);
        setPackageActivity(null);
        showActivitiesDialog(packageName);
    }

    protected void setPackage(String packageName, String friendlyAppString) {
    }

    protected void setPackageActivity(ActivityInfo ai) {
    }
}
