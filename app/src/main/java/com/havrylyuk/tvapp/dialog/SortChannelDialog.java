package com.havrylyuk.tvapp.dialog;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioButton;

import com.havrylyuk.tvapp.R;


/**
 *
 * Created by igor on 19.02.2017.
 */
public class SortChannelDialog extends DialogFragment {

    public static final String SORT_DIALOG_TAG = "com.havrylyuk.tvapp.SORT_DIALOG";

    public interface onSortApplyListener {
        void changeSortType(int position , boolean isDesc);
    }

    private onSortApplyListener mListener;

    public SortChannelDialog() {
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        try {
            mListener = (onSortApplyListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement SortChannelDialog");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        @SuppressLint("InflateParams")
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_sort, null);
        final RadioButton radioDesc  =(RadioButton) view.findViewById(R.id.radio_btn_desc);
        ListView sortList  =(ListView) view.findViewById(R.id.sort_list_view);
        ArrayAdapter<CharSequence> sortAdapter = ArrayAdapter.createFromResource(getActivity(),
                        R.array.channel_sort_names, android.R.layout.simple_list_item_1);
        sortList.setAdapter(sortAdapter);
        sortList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                mListener.changeSortType( position, radioDesc.isChecked());
                                dismiss();
                              }
                        });
        builder.setView(view);
        builder.setTitle(R.string.dialog_sort_title);
        return builder.create();
    }


}
