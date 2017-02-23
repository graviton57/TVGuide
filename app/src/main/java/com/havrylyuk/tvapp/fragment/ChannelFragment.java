package com.havrylyuk.tvapp.fragment;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.havrylyuk.tvapp.R;
import com.havrylyuk.tvapp.activity.MainActivity;
import com.havrylyuk.tvapp.adapter.ChannelCursorAdapter;
import com.havrylyuk.tvapp.data.local.TvContract.ChannelEntry;
import com.havrylyuk.tvapp.data.local.TvContract.CategoryEntry;
import com.havrylyuk.tvapp.util.PreferencesHelper;
import com.havrylyuk.tvapp.util.Utility;


/**
 *
 * Created by Igor Havrylyuk on 19.02.2017.
 */

public class ChannelFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String CHANNELS_FRAGMENT_TAG = "com.havrylyuk.tvapp.CHANNELS_FRAGMENT_TAG";

    public final static String[] CHANNEL_COLUMNS = new String[]{
            ChannelEntry.TABLE_NAME+"."+ChannelEntry._ID,
            ChannelEntry.TABLE_NAME+"."+ChannelEntry.COLUMN_CHANNEL_ID,
            ChannelEntry.TABLE_NAME+"."+ChannelEntry.COLUMN_CHANNEL_NAME,
            CategoryEntry.TABLE_NAME + "." + CategoryEntry.COLUMN_CATEGORY_TITLE,
            ChannelEntry.TABLE_NAME+"."+ChannelEntry.COLUMN_CHANNEL_PICTURE,
            ChannelEntry.TABLE_NAME+"."+ChannelEntry.COLUMN_CHANNEL_FAVORITE,
            ChannelEntry.TABLE_NAME+"."+ChannelEntry.COLUMN_CHANNEL_URL

    };
    public static final byte COL_ID = 0;
    public static final byte COL_CH_ID = 1;
    public static final byte COL_NAME = 2;
    public static final int  COL_CAT = 3;
    public static final int  COL_IMAGE = 4;
    public static final int  COL_FAV = 5;
    public static final int  COL_URL = 6;

    public interface OnChangeFavoriteListener {
        void onChangeFavoriteState(long id, boolean value);
    }
    public static final String EXTRA_CATEGORY_ID = "com.havrylyuk.tvapp.EXTRA_CATEGORY_ID";

    private static final int CHANNELS_LOADER = 1002;

    private ChannelCursorAdapter mAdapter;
    private String category;
    private String sortBy;
    private OnChangeFavoriteListener listener;

    public ChannelFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnChangeFavoriteListener) {
            listener = (OnChangeFavoriteListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnChangeFavoriteListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.content_list, container, false);
        sortBy = PreferencesHelper.getInstance()
                .getChannelSortType(getString(R.string.pref_sort_channel_key));
        if (getArguments() != null) {
            category = String.valueOf(getArguments().getLong(EXTRA_CATEGORY_ID));
        }
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        setupRecyclerView(recyclerView);
        ((MainActivity)getActivity()).updateChannelView(getString(R.string.item_channels));
        ((MainActivity)getActivity()).setChannelLogo(R.drawable.img_channel);
        getActivity().getSupportLoaderManager()
                .restartLoader(CHANNELS_LOADER, Bundle.EMPTY, this);
        return rootView;
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        mAdapter = new ChannelCursorAdapter(getActivity());
        mAdapter.setListener(new ChannelCursorAdapter.OnFavoriteClickListener() {
            @Override
            public void onFavoriteClick(long id, boolean value) {
                if (listener != null) {
                    listener.onChangeFavoriteState(id, value);
                }
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == CHANNELS_LOADER) {
            return new CursorLoader(
                    getActivity(),
                    ChannelEntry.CONTENT_URI,
                    CHANNEL_COLUMNS,
                    TextUtils.isEmpty(category) ? null
                            : ChannelEntry.TABLE_NAME + "."
                            + ChannelEntry.COLUMN_CHANNEL_CATEGORY_ID + " = ?",
                    TextUtils.isEmpty(category) ? null :
                            new String[]{String.valueOf(category)},
                    Utility.getPrefSortChannelOrder(sortBy));
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == CHANNELS_LOADER) {
            if (data != null) mAdapter.setCursor(data);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == CHANNELS_LOADER) {
            mAdapter.setCursor(null);
        }
    }

    public void sortChannels(String value) {
        sortBy = value;
        getActivity().getSupportLoaderManager()
                .restartLoader(CHANNELS_LOADER, Bundle.EMPTY, this);
    }

}
