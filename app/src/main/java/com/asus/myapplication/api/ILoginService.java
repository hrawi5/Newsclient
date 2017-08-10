package com.asus.myapplication.api;

import com.asus.myapplication.model.RegisterBean;
import com.asus.myapplication.util.BaseResp;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

/**
 * Created by asus on 2017/7/15.
 */
public interface ILoginService {
    @GET("LoginDataServlet")
    @Headers("Cache-Control: public, max-age=30")
    Call<BaseResp<RegisterBean>> userLogin(@Query("username") String username, @Query("password") String password);
}
