package com.kproject.imageloader.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.kproject.imageloader.R;
import com.kproject.imageloader.adapters.ManagerAdapter;
import com.kproject.imageloader.models.Manager;
import com.kproject.imageloader.utils.Constants;
import com.kproject.imageloader.utils.Utils;
import java.util.List;

public class ManagerAdapter extends RecyclerView.Adapter<ManagerAdapter.ItemHolder> {
	private List<Manager> folderList;
	private LayoutInflater layoutInflater;
	private ItemClickListener clickListener;

	public ManagerAdapter(Context context, List<Manager> folderList) {
		this.layoutInflater = LayoutInflater.from(context);
		this.folderList = folderList;
	}

	@Override
	public ManagerAdapter.ItemHolder onCreateViewHolder(ViewGroup parent, int position) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_manager_adapter, parent, false);
		ItemHolder item = new ItemHolder(view);
		return item;
	}

	@Override
	public void onBindViewHolder(ManagerAdapter.ItemHolder item, int position) {
		String name = folderList.get(position).getFolderName();
		item.tvItemName.setText(name);

		if (folderList.get(position).getFolderName().endsWith("...")) {
			item.ivImage.setImageResource(R.drawable.back_icon);
		} else {
			item.ivImage.setImageResource(R.drawable.folder_icon);
		}
		
		if (Utils.getThemeSelected().equals(Constants.THEME_DARK)) {
			item.tvItemName.setTextColor(Color.parseColor("#CCCCCC"));
		}
	}

	@Override
	public int getItemCount() {
		return folderList.size();
	}

	public void setClickListener(ItemClickListener itemClickListener) {
		this.clickListener = itemClickListener;
	}

	// Classe responsável pela View
	public class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
		TextView tvItemName;
		ImageView ivImage;

		public ItemHolder(View view) {
			super(view);
			tvItemName = view.findViewById(R.id.tvManagerAdapter_FolderName);
			ivImage = view.findViewById(R.id.ivManagerAdapter_ImageIcon);
			itemView.setOnClickListener(this);
		}

		@Override
		public void onClick(View view) {
			if (clickListener != null) {
				clickListener.onRecyclerViewItemClick(view, getLayoutPosition());
			}
		}

	}

	public interface ItemClickListener {
		void onRecyclerViewItemClick(View view, int position);
	}

}
