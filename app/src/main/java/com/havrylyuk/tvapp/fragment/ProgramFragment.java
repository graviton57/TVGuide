package com.havrylyuk.tvapp.fragment;

import android.app.MediaRouteButton;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.havrylyuk.tvapp.R;
import com.havrylyuk.tvapp.adapter.ProgramCursorAdapter;
import com.havrylyuk.tvapp.data.local.TvContract.ProgramEntry;


/**
 *
 * Created by Igor Havrylyuk on 19.02.2017.
 */

public class ProgramFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public String[] PROGRAM_COLUMNS = new String[]{
            ProgramEntry.TABLE_NAME+"."+ProgramEntry._ID,
            ProgramEntry.TABLE_NAME+"."+ProgramEntry.COLUMN_PROGRAM_TITLE,
            ProgramEntry.TABLE_NAME+"."+ProgramEntry.COLUMN_PROGRAM_DESCRIPTION,
            ProgramEntry.TABLE_NAME+"."+ProgramEntry.COLUMN_PROGRAM_DATE,
            ProgramEntry.TABLE_NAME+"."+ProgramEntry.COLUMN_PROGRAM_TIME,
            ProgramEntry.TABLE_NAME+"."+ ProgramEntry.COLUMN_PROGRAM_CHANEL_ID

    };
    public static final byte COL_ID = 0;
    public static final byte COL_TITLE = 1;
    public static final byte COL_DESC = 2;
    public static final int  COL_DATE = 3;
    public static final int  COL_TIME = 4;
    public static final int  COL_CHANNEL_ID = 5;

    public static final String EXTRA_PROGRAM_DATE = "com.havrylyuk.tvapp.EXTRA_PROGRAM_DATE";
    public static final String EXTRA_CHANNEL_ID = "com.havrylyuk.tvapp.EXTRA_CHANNEL_ID";

    private static final int INVALID_CHANNEL_ID = -1;

    private ProgramCursorAdapter mAdapter;
    private String date;
    private long channelId = INVALID_CHANNEL_ID;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView emptyListView;

    public static Fragment newInstance(long channelId, String picked_date) {
        ProgramFragment fragment = new ProgramFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_PROGRAM_DATE, picked_date);
        args.putLong(EXTRA_CHANNEL_ID, channelId);
        fragment.setArguments(args);
        return fragment;
    }

    public ProgramFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            date = getArguments().getString(EXTRA_PROGRAM_DATE);
            channelId = getArguments().getLong(EXTRA_CHANNEL_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.main_list, container, false);
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        setupRecyclerView(recyclerView);
        emptyListView = (TextView) rootView.findViewById(R.id.recycler_view_empty_content);
        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_to_refresh);
        initSwipeToRefresh();
        getActivity().getSupportLoaderManager().initLoader((int) channelId, Bundle.EMPTY, this);
        return rootView;
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        mAdapter = new ProgramCursorAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == channelId) {
            return new CursorLoader(
                    getActivity(),
                    ProgramEntry.CONTENT_URI,
                    PROGRAM_COLUMNS,
                    ProgramEntry.TABLE_NAME + "." + ProgramEntry.COLUMN_PROGRAM_DATE + " = ? AND " +
                            ProgramEntry.TABLE_NAME + "." + ProgramEntry.COLUMN_PROGRAM_CHANEL_ID + " = ? ",
                    new String[]{date, String.valueOf(channelId)},
                    ProgramEntry.TABLE_NAME + "." + ProgramEntry.COLUMN_PROGRAM_TIME);
        }
            return null;
       }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == channelId) {
            if (data != null) {
                mAdapter.setCursor(data);
                if (emptyListView != null) {
                    emptyListView.setVisibility(data.getCount() == 0 ? View.VISIBLE : View.GONE);
                }
            }
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == channelId) {
            mAdapter.setCursor(null);
        }
    }

    private void initSwipeToRefresh() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    getActivity().getSupportLoaderManager()
                            .restartLoader((int)channelId, Bundle.EMPTY, ProgramFragment.this);
                }
            });
            swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                    android.R.color.holo_green_light,
                    android.R.color.holo_orange_light,
                    android.R.color.holo_red_light);
        }

    }
}
