package com.havrylyuk.tvapp.util;

import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.havrylyuk.tvapp.R;
import com.squareup.picasso.Picasso;

import java.net.IDN;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;

/**
 *
 * Created by Igor Havrylyuk on 19.02.2017.
 */


public class ImageHelper {

    public static void load(@NonNull String url, ImageView imageView) {
        Picasso.with(imageView.getContext())
                .load(validateUrlIfNeeded(url))
                .error(R.drawable.ic_placeholder)
                .into(imageView);
    }

    public static void load( int id, ImageView imageView) {
        Picasso.with(imageView.getContext())
                .load(id)
                .error(R.drawable.ic_placeholder)
                .into(imageView);
    }

    // http://xn--80aagu7abdhcr.xn--p1ai ->  http://артпаровоз.рф
    private static String convertUrlToPunycode(String url) {
        try {
            URL u = new URL(url);
            URI p = new URI(u.getProtocol(), null, IDN.toASCII(u.getHost()),
                    u.getPort(), u.getPath(), u.getQuery(), u.getRef());
            return p.toString();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * fix url if needed
     * example -> Мhttp://lion-tv.com/uploads/posts/2016-06/1467086005_muztv.png
     */
    private static String validateUrlIfNeeded(String url) {
        if (!Charset.forName("US-ASCII").newEncoder().canEncode(url)) {
            if (url.toLowerCase().startsWith("http://") || url.toLowerCase().startsWith("https://")) {
                url = convertUrlToPunycode(url);
            } else {
                url = convertUrlToPunycode(url.substring(url.indexOf("http://")));
            }
        }
        return url;
    }

}
