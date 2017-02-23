package com.havrylyuk.tvapp.data.remote;

import com.havrylyuk.tvapp.model.TvCategory;
import com.havrylyuk.tvapp.model.TvChannel;
import com.havrylyuk.tvapp.model.TvProgram;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 *
 * Created by Igor Havrylyuk on 18.02.2017.
 */

public interface TvService {

    @GET("categories")
    Call<List<TvCategory>> getCategories();

    @GET("chanels")
    Call<List<TvChannel>> getChanels();

    @GET("programs/{timestamp}")
    Call<List<TvProgram>> getPrograms(@Path("timestamp") long timeStamp);
}
