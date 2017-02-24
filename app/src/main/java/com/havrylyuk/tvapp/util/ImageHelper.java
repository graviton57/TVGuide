package com.havrylyuk.tvapp.util;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.havrylyuk.tvapp.R;
import com.squareup.picasso.Picasso;

import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 * Created by Igor Havrylyuk on 19.02.2017.
 */


public class ImageHelper {

    public static void load(@NonNull String url, ImageView imageView) {
        String resultUrl = validateImageUrl(url);
        try {
            resultUrl = new URL(Uri.encode(url)).toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        Picasso.with(imageView.getContext())
                .load(resultUrl)
                .error(R.drawable.ic_placeholder)
                .into(imageView);
    }

    public static void load( int id, ImageView imageView) {
        Picasso.with(imageView.getContext())
                .load(id)
                .error(R.drawable.ic_placeholder)
                .into(imageView);
    }

    private static String validateImageUrl(String checkedUrl) {
        return checkedUrl.startsWith("http://") ? checkedUrl : checkedUrl.substring(checkedUrl.indexOf("http://"));

    }
}
