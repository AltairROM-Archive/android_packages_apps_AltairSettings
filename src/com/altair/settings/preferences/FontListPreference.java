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

package com.altair.settings.preferences;

import static android.os.UserHandle.USER_SYSTEM;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;

import androidx.preference.ListPreference;

import com.android.internal.util.custom.ThemeUtils;

import com.android.settings.R;

import java.util.ArrayList;
import java.util.List;

public class FontListPreference extends ListPreference {
    private int mClickedDialogEntryIndex;

    private ThemeUtils mThemeUtils;
    private List<Typeface> mFonts;

    public FontListPreference(Context context) {
        this(context, null);
    }

    public FontListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
            mThemeUtils = new ThemeUtils(context);
            mFonts = mThemeUtils.getFonts();
    }

    @Override
    protected void onClick() {
        ArrayAdapter<CharSequence> mAdapter = new ArrayAdapter<CharSequence>(getContext(), R.layout.font_list_item, getEntries()) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                CheckedTextView view = (CheckedTextView) convertView;
                if (view == null) {
                    view = (CheckedTextView) View.inflate(getContext(), R.layout.font_list_item, null);
                }
                view.setText(getEntries()[position]);
                view.setTypeface(mFonts.get(position));
                return view;
            }
        };

        mClickedDialogEntryIndex = findIndexOfValue(getValue());

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setTitle(FontListPreference.this.getTitle())
                .setCancelable(true)
                .setSingleChoiceItems(mAdapter, mClickedDialogEntryIndex, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mClickedDialogEntryIndex = which;
                        String value = getEntryValues()[mClickedDialogEntryIndex].toString();
                        if (FontListPreference.this.callChangeListener(value)) {
                            FontListPreference.this.setValue(value);
                        }
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(android.R.string.cancel, (dialog, which) -> {
                    dialog.cancel();
                });
        builder.show();
    }
}
