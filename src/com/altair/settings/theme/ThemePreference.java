/*
 * Copyright (C) 2018-2019 The Dirty Unicorns Project
 * Copyright (C) 2021 Altair ROM Project
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

package com.altair.settings.theme;

import static android.os.UserHandle.USER_SYSTEM;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.altair.settings.theme.ThemeDialogFragment;

import com.android.internal.util.custom.ThemeUtils;

import com.android.settings.R;

import com.android.settingslib.Utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ThemePreference extends Preference
        implements ThemeDialogFragment.ThemeDialogListener {

    private ThemeUtils mThemeUtils;
    private String mThemeCategory;
    private List<String> mThemeLabels;
    private List<String> mThemePackages;

    public ThemePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ThemePreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        setWidgetLayoutResource(R.layout.theme_preference_image);
        setIconSpaceReserved(true);
        setPersistent(true);

        mThemeUtils = new ThemeUtils(context);
        mThemeCategory = getKey();
        mThemeLabels = mThemeUtils.getLabels(mThemeCategory);
        mThemePackages = mThemeUtils.getOverlayPackagesForCategory(mThemeCategory);

        updateSummary();
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        ImageView preview = (ImageView) holder.findViewById(R.id.image);
        if ((preview != null) &&
                (mThemeCategory.equals(ThemeUtils.ACCENT_KEY) || mThemeCategory.equals(ThemeUtils.ICON_SHAPE_KEY))) {
            final int color = Utils.getColorAttrDefaultColor(getContext(), android.R.attr.colorAccent);
            if (mThemeCategory.equals(ThemeUtils.ACCENT_KEY)) {
                preview.setBackgroundResource(R.drawable.theme_circle_shape);
            } else {
                preview.setBackgroundDrawable(mThemeUtils.createShapeDrawable("default"));
            }
            preview.setBackgroundTintList(ColorStateList.valueOf(color));
        }
    }

    @Override
    protected void onClick() {
        ThemeDialogFragment dialog =
                ThemeDialogFragment.newInstance(getTitle().toString(), mThemeCategory);
        dialog.setThemeDialogListener(this);
        dialog.show(getActivity().getSupportFragmentManager(), "ThemeDialogFragment");
    }

    public FragmentActivity getActivity() {
        Context context = getContext();
        if (context instanceof FragmentActivity) {
            return (FragmentActivity) context;
        } else if (context instanceof ContextWrapper) {
            Context baseContext = ((ContextWrapper) context).getBaseContext();
            if (baseContext instanceof FragmentActivity) {
                return (FragmentActivity) baseContext;
            }
        }
        throw new IllegalStateException("Error getting activity from context");
    }

    @Override
    public void onThemeOverlayChanged(DialogFragment dialog) {
        updateSummary();
    }

    public void updateSummary() {
        String currentPackageName = getEnabledOverlay();
        setSummary("Default".equals(currentPackageName) ? "Default"
                : mThemeLabels.get(mThemePackages.indexOf(currentPackageName)));
    }

    public String getEnabledOverlay() {
        return mThemeUtils.getOverlayInfos(mThemeCategory)
                .stream()
                .filter(info -> info.isEnabled())
                .map(info -> info.packageName)
                .findFirst()
                .orElse("Default");
    }
}
