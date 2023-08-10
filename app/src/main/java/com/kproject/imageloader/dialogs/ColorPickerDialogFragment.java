package com.kproject.imageloader.dialogs;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import com.kproject.imageloader.R;
import com.kproject.imageloader.adapters.ColorPickerAdapter;
import com.kproject.imageloader.application.MyApplication;
import com.kproject.imageloader.utils.Constants;
import com.kproject.imageloader.utils.Utils;

public class ColorPickerDialogFragment extends DialogFragment implements ColorPickerAdapter.ItemClickListener {
    private RecyclerView rvColorList;
    private ColorPickerAdapter adpColorListAdapter;
    private int option;

    private static final int[] appThemeColorList =
            {
                    R.drawable.red_color, R.drawable.green_color, R.drawable.blue_color,
                    R.drawable.grey_color, R.drawable.pink_color, R.drawable.purple_color
            };

    private static final int[] backgroundColorList =
            {
                    R.drawable.red_color, R.drawable.green_color, R.drawable.blue_color,
                    R.drawable.black_color, R.drawable.white_color, R.drawable.grey_color,
                    R.drawable.pink_color, R.drawable.purple_color
            };

    public ColorPickerDialogFragment() {
    }

    public static ColorPickerDialogFragment newInstance(int colorPickerOption) {
        Bundle bundle = new Bundle();
        bundle.putInt("colorPickerOption", colorPickerOption);
        ColorPickerDialogFragment dialog = new ColorPickerDialogFragment();
        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        option = getArguments().getInt("colorPickerOption");
        int[] colorList = option == Constants.COLOR_PICKER_APP_THEME ? appThemeColorList : backgroundColorList;
        int colorPickerOption = option == Constants.COLOR_PICKER_APP_THEME ? Constants.COLOR_PICKER_APP_THEME : Constants.COLOR_PICKER_BACKGROUND_COLOR;

        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity(), Utils.setThemeForDialog());
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_color_picker, null);
        rvColorList = view.findViewById(R.id.rvColorPicker_ColorList);
        adpColorListAdapter = new ColorPickerAdapter(getActivity(), colorList, colorPickerOption);
        adpColorListAdapter.setClickListener(this);
        rvColorList.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        rvColorList.setAdapter(adpColorListAdapter);
        dialog.setView(view);
        return dialog.create();
    }

    @Override
    public void onRecyclerViewItemClick(View view, int position) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
        if (option == Constants.COLOR_PICKER_APP_THEME) {
            prefs.edit().putString(Constants.PREF_APP_THEME, String.valueOf(position)).commit();
        } else if (option == Constants.COLOR_PICKER_BACKGROUND_COLOR) {
            prefs.edit().putString(Constants.PREF_BACKGROUND_COLOR, String.valueOf(position)).commit();
        }
        this.dismiss();
    }

}
