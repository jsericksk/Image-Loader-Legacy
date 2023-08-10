package com.kproject.imageloader.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.kproject.imageloader.R;
import com.kproject.imageloader.adapters.ColorPickerAdapter;
import com.kproject.imageloader.utils.Constants;
import com.kproject.imageloader.utils.Utils;

public class ColorPickerAdapter extends RecyclerView.Adapter<ColorPickerAdapter.ItemHolder> {
    private int[] colorList;
    private int colorPickerOption;
    private LayoutInflater layoutInflater;
    private ItemClickListener clickListener;

    public ColorPickerAdapter(Context context, int[] colorList, int colorPickerOption) {
        this.layoutInflater = LayoutInflater.from(context);
        this.colorList = colorList;
        this.colorPickerOption = colorPickerOption;
    }

    @Override
    public ColorPickerAdapter.ItemHolder onCreateViewHolder(ViewGroup parent, int position) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_color_list_adapter, parent, false);
        ItemHolder item = new ItemHolder(view);
        return item;
    }

    @Override
    public void onBindViewHolder(ColorPickerAdapter.ItemHolder item, int position) {
        int colotResId = colorList[position];
        item.btColor.setBackgroundResource(colotResId);

        int colorSelected = 0;
        if (colorPickerOption == Constants.COLOR_PICKER_APP_THEME) {
            colorSelected = Integer.parseInt(Utils.getPreferenceValue(Constants.PREF_APP_THEME, Constants.THEME_DARK));
        } else if (colorPickerOption == Constants.COLOR_PICKER_BACKGROUND_COLOR) {
            colorSelected = Integer.parseInt(Utils.getPreferenceValue(Constants.PREF_BACKGROUND_COLOR, Constants.COLOR_GREY));
        }

        if (position == colorSelected) {
            item.itemView.setBackgroundColor(Color.parseColor("#50000000"));
        }
    }

    @Override
    public int getItemCount() {
        return colorList.length;
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }

    public class ItemHolder extends RecyclerView.ViewHolder {
        Button btColor;

        public ItemHolder(View view) {
            super(view);
            btColor = view.findViewById(R.id.btColorList_Color);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (clickListener != null) {
                        clickListener.onRecyclerViewItemClick(view, getLayoutPosition());
                    }
                }
            });
        }

    }

    public interface ItemClickListener {
        void onRecyclerViewItemClick(View view, int position);
    }

}
