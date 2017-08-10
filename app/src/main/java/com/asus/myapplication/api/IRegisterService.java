package com.asus.myapplication.api;

import com.asus.myapplication.model.RegisterBean;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by asus on 2017/7/15.
 */
public interface IRegisterService {
    @FormUrlEncoded
    @POST("RegisterDataServlet")
    Call<RegisterBean> createUser(@FieldMap Map<String ,String> params);
}
