package com.kproject.imageloader.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import com.kproject.imageloader.R;
import com.kproject.imageloader.adapters.ImageListAdapter;
import com.kproject.imageloader.models.Image;
import com.kproject.imageloader.utils.Constants;
import com.kproject.imageloader.utils.Utils;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;

public class ImageListAdapter extends RecyclerView.Adapter<ImageListAdapter.ItemHolder> {
	private LayoutInflater layoutInflater;
	private ItemClickListener clickListener;
	private List<Image> imageList;
	private ArrayList<Image> selectedItems = new ArrayList<>();
	private boolean useGridLayout;
	private boolean isActionMode = false;
	
	public ImageListAdapter(Context context, List<Image> imageList) {
		this.layoutInflater = LayoutInflater.from(context);
		this.imageList = imageList;
	}

	@Override
	public ImageListAdapter.ItemHolder onCreateViewHolder(ViewGroup parent, int position) {
		useGridLayout = Utils.getPreferenceValue(Constants.PREF_USE_GRID_LAYOUT, true);
		View view = null;
		if (useGridLayout) {
			view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_grid_image_list_adapter, parent, false);
		} else {
			view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_image_list_adapter, parent, false);
		}
		return new ItemHolder(view);
	}

	@Override
	public void onBindViewHolder(final ImageListAdapter.ItemHolder item, int position) {
		final String imageUrl = imageList.get(position).getImageUrl();
		String imageName = imageList.get(position).getImageName();
		if (!useGridLayout) {
			item.tvImageName.setText(imageName);
		}
		if (imageUrl != null && !imageUrl.isEmpty()) {
			Picasso.with(layoutInflater.getContext()).load(imageUrl)
			.fit().centerCrop().placeholder(R.drawable.loading_image)
			.error(R.drawable.error_loading_image)
			.into(item.ivImage);
		}
		
		if (isActionMode) {
			item.cbItemSelected.setVisibility(View.VISIBLE);
		} else {
			item.cbItemSelected.setVisibility(View.GONE);
		}
		
		if (selectedItems.contains(imageList.get(position))) {
			item.ivImage.setColorFilter(Color.parseColor("#60000000"));
			item.cbItemSelected.setChecked(true);
			item.cbItemSelected.setBackgroundColor(Color.parseColor("#AA212121"));
		} else {
			item.ivImage.setColorFilter(Color.TRANSPARENT);
			item.cbItemSelected.setChecked(false);
			item.cbItemSelected.setBackgroundColor(Color.TRANSPARENT);
		}
		
		if (Utils.getThemeSelected().equals(Constants.THEME_DARK)) {
			if (!useGridLayout) {
				item.cvCardView.setBackgroundColor(Color.parseColor("#404040"));
				item.tvImageName.setTextColor(Color.parseColor("#CDCDCD"));
			}
		}
	}

	@Override
	public int getItemCount() {
		return imageList.size();
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getItemViewType(int position) {
		return position;
	}
	
	public void addSelectedItem(Image image, int position) {
		selectedItems.add(image);
		notifyDataSetChanged();
	}

	public void removeSelectedItem(Image image, int position) {
		selectedItems.remove(image);
		notifyDataSetChanged();
	}
	
	public void selectAllItems() {
		selectedItems.clear();
		selectedItems.addAll(imageList);
		notifyDataSetChanged();
	}

	public void clearSelectedItems() {
		this.selectedItems.clear();
		notifyDataSetChanged();
	}

	public boolean containsSelectedItem(Image image) {
		return selectedItems.contains(image);
	}

	public void isActionMode(boolean isActionMode) {
		this.isActionMode = isActionMode;
	}

	public List<Image> getImageList() {
		return this.imageList;
	}

	public ArrayList<Image> getSelectedItems() {
		return this.selectedItems;
	}
	
	public void setClickListener(ItemClickListener itemClickListener) {
		this.clickListener = itemClickListener;
	}

	public class ItemHolder extends RecyclerView.ViewHolder {
		CardView cvCardView;
		ImageView ivImage;
		TextView tvImageName;
		CheckBox cbItemSelected;
		
		public ItemHolder(View view) {
			super(view);
			cvCardView = view.findViewById(R.id.cvImageList_CardView);
			ivImage = view.findViewById(R.id.ivImageList_Image);
			tvImageName = view.findViewById(R.id.tvImageList_ImageName);
			cbItemSelected = view.findViewById(R.id.cbImageList_ItemSelected);
			
			itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					if (clickListener != null) {
						clickListener.onRecyclerViewItemClick(view, getLayoutPosition());
					}
				}
			});
			itemView.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View view) {
					if (clickListener != null) {
						clickListener.onRecyclerViewLongItemClick(view, getLayoutPosition());
					}
					return true;
				}
			});
		}

	}

	public interface ItemClickListener {
		void onRecyclerViewItemClick(View view, int position);
		void onRecyclerViewLongItemClick(View view, int position);
	}

}
