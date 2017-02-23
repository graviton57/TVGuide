package com.havrylyuk.tvapp.util;

import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.havrylyuk.tvapp.R;
import com.squareup.picasso.Picasso;

/**
 *
 * Created by Igor Havrylyuk on 19.02.2017.
 */


public class ImageHelper {

    public static void load(@NonNull String url, ImageView imageView) {
        Picasso.with(imageView.getContext())
                .load(url)
                .error(R.drawable.ic_placeholder)
                .into(imageView);
    }

    public static void load( int id, ImageView imageView) {
        Picasso.with(imageView.getContext())
                .load(id)
                .error(R.drawable.ic_placeholder)
                .into(imageView);
    }
}
