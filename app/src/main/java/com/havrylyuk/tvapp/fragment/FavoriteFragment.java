package com.havrylyuk.tvapp.fragment;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.havrylyuk.tvapp.R;
import com.havrylyuk.tvapp.activity.MainActivity;
import com.havrylyuk.tvapp.adapter.ChannelCursorAdapter;
import com.havrylyuk.tvapp.data.local.TvContract.ChannelEntry;


/**
 *
 * Created by Igor Havrylyuk on 19.02.2017.
 */

public class FavoriteFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String FAVORITES_FRAGMENT_TAG = "com.havrylyuk.tvapp.FAVORITES_FRAGMENT_TAG";

    private static final int FAVORITE_LOADER = 1003;
    private ChannelCursorAdapter mAdapter;
    private TextView emptyListView;

    public FavoriteFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.content_list, container, false);
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        emptyListView = (TextView) rootView.findViewById(R.id.recycler_view_empty_content);
        setupRecyclerView(recyclerView);
        ((MainActivity)getActivity()).updateChannelView(getString(R.string.item_preferred));
        ((MainActivity)getActivity()).setChannelLogo(R.drawable.img_favorites);
        getActivity().getSupportLoaderManager().initLoader(FAVORITE_LOADER, Bundle.EMPTY, this);
        return rootView;
    }

    private void setupRecyclerView(@NonNull final RecyclerView recyclerView) {
        mAdapter = new ChannelCursorAdapter(getActivity());
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(mAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter.setListener(new ChannelCursorAdapter.OnFavoriteClickListener() {
            @Override
            public void onFavoriteClick(long id, boolean value) {
                ((MainActivity) getActivity()).onChangeFavoriteState(id, value);
            }
        });
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback =
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                    @Override
                    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                          RecyclerView.ViewHolder target) {
                        return false;
                    }
                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                        int position = viewHolder.getAdapterPosition();
                        removeFromFavorite(mAdapter.getItemId(position));
                        if (mAdapter.getCurrentPosition() > position) {
                            mAdapter.setCurrentPosition(mAdapter.getCurrentPosition() - 1);
                        }
                    }
                };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void removeFromFavorite(long id ) {
        ContentValues cv = new ContentValues();
        cv.put(ChannelEntry.COLUMN_CHANNEL_FAVORITE, 0);
        getActivity().getContentResolver().update(ChannelEntry.CONTENT_URI, cv,
                ChannelEntry.TABLE_NAME + "." + ChannelEntry._ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == FAVORITE_LOADER) {
            return new CursorLoader(
                    getActivity(),
                    ChannelEntry.CONTENT_URI,
                    ChannelFragment.CHANNEL_COLUMNS,
                    ChannelEntry.TABLE_NAME + "." + ChannelEntry.COLUMN_CHANNEL_FAVORITE + " = ?",
                    new String[]{"1"},
                    null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == FAVORITE_LOADER) {
            if (data != null && isAdded()) {
                if (emptyListView != null ) {
                    emptyListView.setVisibility(data.getCount() == 0 ? View.VISIBLE : View.GONE);
                    emptyListView.setText(getString(R.string.empty_favorites_list));
                }
                mAdapter.setCursor(data);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == FAVORITE_LOADER) {
            mAdapter.setCursor(null);
        }
    }
}
