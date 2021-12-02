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

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import com.android.internal.util.custom.ThemeUtils;

import com.android.settings.R;

import com.android.settingslib.Utils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ThemeDialogFragment extends DialogFragment {

    private AlertDialog mDialog;

    private static ThemeUtils mThemeUtils;

    private String mThemeCategory;
    private List<String> mThemePackages;
    private List<String> mThemeLabels;
    private List<Integer> mThemeColors;
    private List<ShapeDrawable> mThemeShapes;


    public ThemeDialogFragment() {
    }

    public static ThemeDialogFragment newInstance(String title, String category) {
        ThemeDialogFragment fragment = new ThemeDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("category", category);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Context context = getContext();
        final FragmentActivity activity = requireActivity();
        final Bundle args = getArguments();

        // Create new instance of ThemeUtils class and get theme data
        mThemeUtils = new ThemeUtils(context);
        mThemeCategory = args.getString("category");
        mThemePackages = mThemeUtils.getOverlayPackagesForCategory(mThemeCategory);
        mThemeLabels = mThemeUtils.getLabels(mThemeCategory);
        mThemeColors = mThemeUtils.getColors();
        mThemeShapes = mThemeUtils.getShapeDrawables();

        // Set up dialog's list view
        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.theme_dialog_listview, null);
        final ListView list = view.findViewById(R.id.listview);
        CustomListViewAdapter adapter = new CustomListViewAdapter(getContext());
        list.setAdapter(adapter);
        list.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String overlayPackage = (String) parent.getAdapter().getItem(position);
                mThemeUtils.setOverlayEnabled(mThemeCategory, overlayPackage);
                listener.onThemeOverlayChanged(ThemeDialogFragment.this);
                mDialog.dismiss();
            }
        });

        // Create dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(args.getString("title"));
        builder.setView(view);
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Dismiss the window while doing nothing
                    }
                });
        mDialog = builder.create();

        return mDialog;
    }

    /*
     *  The activity that creates an instance of this dialog fragment must
     *  implement this interface in order to receive event callbacks.
     *  The method passes the DialogFragment in case the host activity
     *  needs to query it.
     */

    public interface ThemeDialogListener {
        public void onThemeOverlayChanged(DialogFragment dialog);
    }

    ThemeDialogListener listener;

    public void setThemeDialogListener(ThemeDialogListener listener) {
        this.listener = listener;
    }

    /*
     *  This BaseAdapter extension controls the rows shown in the ListView.
     */

    public class CustomListViewAdapter extends BaseAdapter {

        Context context;

        public CustomListViewAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getViewTypeCount() {
            return getCount();
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public int getCount() {
            if (mThemePackages != null) {
                return mThemePackages.size();
            }
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return mThemePackages.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        // Holder class for ListView item text and image (used in getView() below)
        private class ViewHolder {
            ImageView image;
            TextView text;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                holder = new ViewHolder();
                LayoutInflater inflater = LayoutInflater.from(context);
                convertView = inflater.inflate(R.layout.theme_list_item, null, true);
                holder.text = (TextView) convertView.findViewById(R.id.text);
                holder.image = (ImageView) convertView.findViewById(R.id.image);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            if (mThemeCategory.equals(ThemeUtils.ACCENT_KEY) || mThemeCategory.equals(ThemeUtils.ICON_SHAPE_KEY)) {

                if (mThemeCategory.equals(ThemeUtils.ACCENT_KEY)) {
                    // If accent color, show round image using color at current position
                    holder.image.setBackgroundResource(R.drawable.theme_circle_shape);
                    holder.image.setBackgroundTintList(ColorStateList.valueOf(mThemeColors.get(position)));
                } else {
                    // If icon shape, show icon shape at current position using default accent color
                    //holder.image.setBackgroundDrawable(mThemeShapes.get(position));
                    holder.image.setBackground(mThemeShapes.get(position));
                    final int color = Utils.getColorAttrDefaultColor(getContext(), android.R.attr.colorAccent);
                    holder.image.setBackgroundTintList(ColorStateList.valueOf(color));
                }

                // If item is currently selected package, show a checkmark on the image
                final String overlayPackage = mThemePackages.get(position);
                String currentPackageName = mThemeUtils.getOverlayInfos(mThemeCategory)
                                                        .stream()
                                                        .filter(info -> info.isEnabled())
                                                        .map(info -> info.packageName)
                                                        .findFirst()
                                                        .orElse("Default");
                final boolean isDefault = "Default".equals(currentPackageName) && "android".equals(overlayPackage);
                holder.image.setImageResource(
                        (overlayPackage.equals(currentPackageName) || isDefault) ? R.drawable.ic_checkmark_24dp : -1);

                holder.text.setText(mThemeLabels.get(position));
            }

            return convertView;
        }
    }
}
