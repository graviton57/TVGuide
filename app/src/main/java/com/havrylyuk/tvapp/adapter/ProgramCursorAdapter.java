

package com.havrylyuk.tvapp.adapter;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.havrylyuk.tvapp.R;
import com.havrylyuk.tvapp.fragment.ProgramFragment;


/**
 * Simple  TvProgram Cursor Adapter
 * Created by Igor Havrylyuk on 20.02.2017.
 */

public class ProgramCursorAdapter extends RecyclerView.Adapter<ProgramCursorAdapter.ProgramViewHolder> {

    private Cursor cursor;


    public ProgramCursorAdapter() {
    }

    public void setCursor(Cursor cursor) {
        this.cursor = cursor;
        notifyDataSetChanged();
    }

    @Override
    public ProgramViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycle_item_program, parent, false);
        return new ProgramViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ProgramViewHolder holder, int position) {
        if (cursor != null) {
            cursor.moveToPosition(position);
            holder.time.setText(cursor.getString(ProgramFragment.COL_TIME));
            holder.name.setText(cursor.getString(ProgramFragment.COL_TITLE));
            holder.desc.setText(cursor.getString(ProgramFragment.COL_DESC));
        }
    }

    @Override
    public int getItemCount() {
        return cursor == null ? 0 : cursor.getCount();
    }


    public static class ProgramViewHolder extends RecyclerView.ViewHolder {

        public  View view;
        public  TextView time;
        public  TextView name;
        public  TextView desc;

        public ProgramViewHolder(View view) {
            super(view);
            this.view = view;
            time = (TextView) view.findViewById(R.id.list_item_content_time);
            name = (TextView) view.findViewById(R.id.list_item_content_name);
            desc = (TextView) view.findViewById(R.id.list_item_sub_content_name);
        }
    }
}
