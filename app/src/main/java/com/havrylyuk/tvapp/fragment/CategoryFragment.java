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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.havrylyuk.tvapp.R;
import com.havrylyuk.tvapp.activity.MainActivity;
import com.havrylyuk.tvapp.adapter.CategoryCursorAdapter;
import com.havrylyuk.tvapp.data.local.TvContract.CategoryEntry;


/**
 *
 * Created by Igor Havrylyuk on 19.02.2017.
 */

public class CategoryFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String CATEGORIES_FRAGMENT_TAG = "com.havrylyuk.tvapp.CATEGORIES_FRAGMENT_TAG";
    public String[] CATEGORIES_COLUMNS = new String[]{
            CategoryEntry.TABLE_NAME+"."+CategoryEntry._ID,
            CategoryEntry.TABLE_NAME+"."+CategoryEntry.COLUMN_CATEGORY_ID,
            CategoryEntry.TABLE_NAME+"."+CategoryEntry.COLUMN_CATEGORY_TITLE,
            CategoryEntry.TABLE_NAME+"."+CategoryEntry.COLUMN_CATEGORY_PICTURE

    };
    public static final byte COL_ID = 0;
    public static final byte COL_CAT_ID = 1;
    public static final int  COL_TITLE = 2;
    public static final int  COL_IMAGE = 3;

    private OnSelectCategoryListener listener;

    public interface OnSelectCategoryListener {
        void onCategoryClick(long id);
    }

    private static final int CATEGORIES_LOADER = 1001;
    private CategoryCursorAdapter mAdapter;

    public CategoryFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSelectCategoryListener) {
            listener = (OnSelectCategoryListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnSelectCategoryListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.content_list, container, false);
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        ((MainActivity)getActivity()).updateChannelView(getString(R.string.item_categories));
        ((MainActivity)getActivity()).setChannelLogo(R.drawable.img_categories);
        setupRecyclerView(recyclerView);
        getActivity().getSupportLoaderManager().initLoader(CATEGORIES_LOADER, Bundle.EMPTY, this);
        return rootView;
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        mAdapter = new CategoryCursorAdapter();
        mAdapter.setListener(new CategoryCursorAdapter.OnItemSelectedListener() {
            @Override
            public void onItemSelected(long id, CategoryCursorAdapter.CategoryViewHolder view) {
                if (listener!=null){
                    listener.onCategoryClick(id);
                }
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(mAdapter);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == CATEGORIES_LOADER) {
            return new CursorLoader(
                    getActivity(),
                    CategoryEntry.CONTENT_URI,
                    CATEGORIES_COLUMNS,
                    null,
                    null,
                    CategoryEntry.TABLE_NAME + "." + CategoryEntry.COLUMN_CATEGORY_TITLE);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == CATEGORIES_LOADER) {
            if (data != null) mAdapter.setCursor(data);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == CATEGORIES_LOADER) {
            mAdapter.setCursor(null);
        }

    }
}
