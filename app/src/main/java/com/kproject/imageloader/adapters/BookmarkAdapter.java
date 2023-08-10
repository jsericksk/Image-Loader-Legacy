package com.kproject.imageloader.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.kproject.imageloader.R;
import com.kproject.imageloader.adapters.BookmarkAdapter;
import com.kproject.imageloader.models.Bookmark;
import com.kproject.imageloader.utils.Constants;
import com.kproject.imageloader.utils.Utils;

import java.util.List;

public class BookmarkAdapter extends RecyclerView.Adapter<BookmarkAdapter.ItemHolder> {
    private List<Bookmark> bookmarkList;
    private LayoutInflater layoutInflater;
    private ItemClickListener clickListener;

    public BookmarkAdapter(Context context, List<Bookmark> bookmarkList) {
        this.layoutInflater = LayoutInflater.from(context);
        this.bookmarkList = bookmarkList;
    }

    @Override
    public BookmarkAdapter.ItemHolder onCreateViewHolder(ViewGroup parent, int position) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_bookmark_adapter, parent, false);
        ItemHolder item = new ItemHolder(view);
        return item;
    }

    @Override
    public void onBindViewHolder(BookmarkAdapter.ItemHolder item, int position) {
        String title = bookmarkList.get(position).getTitle();
        String pageUrl = bookmarkList.get(position).getUrl();
        item.tvTitle.setText(title);
        item.tvPageUrl.setText(pageUrl);

        if (Utils.getThemeSelected().equals(Constants.THEME_DARK)) {
            item.cvCardView.setBackgroundColor(Color.parseColor("#404040"));
            item.tvTitle.setTextColor(Color.parseColor("#CDCDCD"));
            item.tvPageUrl.setTextColor(Color.parseColor("#CDCDCD"));
        }
    }

    @Override
    public int getItemCount() {
        return bookmarkList.size();
    }

    public List<Bookmark> getAllBookmarks() {
        return bookmarkList;
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }

    public class ItemHolder extends RecyclerView.ViewHolder {
        CardView cvCardView;
        TextView tvTitle;
        TextView tvPageUrl;

        public ItemHolder(View view) {
            super(view);
            cvCardView = view.findViewById(R.id.cvBookmarkAdapter_CardView);
            tvTitle = view.findViewById(R.id.tvBookmarkAdapter_Title);
            tvPageUrl = view.findViewById(R.id.tvBookmarkAdapter_PageUrl);
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
