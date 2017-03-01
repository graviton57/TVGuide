

package com.havrylyuk.tvapp.adapter;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.havrylyuk.tvapp.R;
import com.havrylyuk.tvapp.fragment.CategoryFragment;
import com.havrylyuk.tvapp.util.ImageHelper;


/**
 * TvCategory Cursor Adapter
 * Created by Igor Havrylyuk on 20.02.2017.
 */

public class CategoryCursorAdapter extends RecyclerView.Adapter<CategoryCursorAdapter.CategoryViewHolder> {

    public interface OnItemSelectedListener {
        void onItemSelected(long id, CategoryViewHolder view);
    }

    private OnItemSelectedListener listener;
    private Cursor cursor;
    private int currentPosition = RecyclerView.NO_POSITION;

    public CategoryCursorAdapter() {
    }

    public void setCursor(Cursor cursor) {
        this.cursor = cursor;
        notifyDataSetChanged();
    }

    @Override
    public CategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycle_item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final CategoryViewHolder holder, int position) {
        if (cursor != null) {
            cursor.moveToPosition(position);
        holder.image.setImageDrawable(null);
        final long id = cursor.getLong(CategoryFragment.COL_CAT_ID);
        String fileName = cursor.getString(CategoryFragment.COL_IMAGE);
        if (!TextUtils.isEmpty(fileName)) {
            ImageHelper.load(fileName, holder.image);
        }
        holder.name.setText(cursor.getString(CategoryFragment.COL_TITLE));
        holder.desc.setVisibility(View.GONE);
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notifyItemChanged(currentPosition);
                currentPosition = holder.getAdapterPosition();
                if (listener != null) {
                    listener.onItemSelected(id, holder);
                }
            }
        });
        holder.view.setSelected(currentPosition == position);
        }
    }


    public void setListener(@NonNull OnItemSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        return cursor == null ? 0 : cursor.getCount();
    }


    public static class CategoryViewHolder extends RecyclerView.ViewHolder {

        public  View view;
        public  ImageView image;
        public  TextView name;
        public  TextView desc;

        public CategoryViewHolder(View view) {
            super(view);
            this.view = view;
            image= (ImageView) view.findViewById(R.id.list_item_icon);
            name = (TextView) view.findViewById(R.id.list_item_name);
            desc = (TextView) view.findViewById(R.id.list_item_subname);
        }
    }
}
