

package com.havrylyuk.tvapp.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.havrylyuk.tvapp.R;
import com.havrylyuk.tvapp.data.local.TvContract.ChannelEntry;
import com.havrylyuk.tvapp.fragment.ChannelFragment;
import com.havrylyuk.tvapp.util.ImageHelper;


/**
 * Simple Cursor Adapter
 * Created by Igor Havrylyuk on 20.02.2017.
 */

public class ChannelCursorAdapter extends RecyclerView.Adapter<ChannelCursorAdapter.ChannelViewHolder> {


    public interface OnFavoriteClickListener {
        void onFavoriteClick(long id, boolean value);
    }

    private OnFavoriteClickListener listener;

    private Cursor cursor;
    private Context context;
    private int currentPosition = RecyclerView.NO_POSITION;

    public ChannelCursorAdapter(Context context) {
        this.context = context;
    }

    public void setCursor(Cursor cursor) {
        this.cursor = cursor;
        notifyDataSetChanged();
    }
    public int getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
    }

    @Override
    public ChannelViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycle_item_channel, parent, false);
        return new ChannelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ChannelViewHolder holder, int position) {
        if (cursor != null) {
        cursor.moveToPosition(position);
        final long id = cursor.getLong(ChannelFragment.COL_ID);
        String fileName = cursor.getString(ChannelFragment.COL_IMAGE);
        if (!TextUtils.isEmpty(fileName)) {
            ImageHelper.load(fileName, holder.image);
        }
        holder.name.setText(cursor.getString(ChannelFragment.COL_NAME));
        holder.desc.setText(cursor.getString(ChannelFragment.COL_CAT));
        holder.url.setText(Html.fromHtml(cursor.getString(ChannelFragment.COL_URL)));
        holder.url.setMovementMethod(LinkMovementMethod.getInstance());
        holder.favorite.setTag(holder);
        final boolean isFavorite = cursor.getInt(ChannelFragment.COL_FAV) == 1;
        ImageHelper.load(isFavorite ? R.drawable.ic_heart : R.drawable.ic_heart_gray,
                holder.favorite);
        holder.favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notifyItemChanged(currentPosition);
                if (listener != null) {
                    listener.onFavoriteClick(id, isFavorite);
                }
            }
        });
        holder.view.setSelected(currentPosition == position);
        }
    }

    @Override
    public long getItemId(int position) {
        if (cursor != null && cursor.moveToPosition(position)) {
            return cursor.getLong(ChannelFragment.COL_ID);
        }
        return super.getItemId(position);
    }

    public void removeFavorite(int position) {
        if (cursor != null && cursor.moveToPosition(position)) {
            long id = cursor.getLong(ChannelFragment.COL_ID);
            ContentValues cv = new ContentValues();
            cv.put(ChannelEntry.COLUMN_CHANNEL_FAVORITE, 0);
            context.getContentResolver().update(ChannelEntry.CONTENT_URI,
                    cv,
                    ChannelEntry.TABLE_NAME + "." + ChannelEntry._ID + " = ?",
                    new String[]{String.valueOf(id)});
        }
    }
    public void setListener(@NonNull OnFavoriteClickListener listener) {
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        return cursor == null ? 0 : cursor.getCount();
    }


    public static class ChannelViewHolder extends RecyclerView.ViewHolder {

        public  View view;
        public  ImageView image;
        public  ImageView favorite;
        public  TextView name;
        public  TextView desc;
        public  TextView url;

        public ChannelViewHolder(View view) {
            super(view);
            this.view = view;
            image= (ImageView) view.findViewById(R.id.list_item_icon);
            favorite= (ImageView) view.findViewById(R.id.list_item_icon_fav);
            name = (TextView) view.findViewById(R.id.list_item_content_name);
            desc = (TextView) view.findViewById(R.id.list_item_sub_content_name);
            url = (TextView) view.findViewById(R.id.list_item_url);
        }
    }
}
