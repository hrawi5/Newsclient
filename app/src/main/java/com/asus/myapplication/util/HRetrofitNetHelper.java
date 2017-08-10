package com.asus.myapplication.util;

import android.content.Context;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by asus on 2017/7/15.
 */
public class HRetrofitNetHelper implements HttpLoggingInterceptor.Logger,Interceptor {
    public static HRetrofitNetHelper mInstance;
    private final Cache cache;
    public Retrofit mRetrofit;
    public OkHttpClient mOkHttpClient;
    public HttpLoggingInterceptor mHttpLogInterceptor;
    private BasicParamsInterceptor mBaseParamsInterceptor;
    private Interceptor  mUrlInterceptor;
    private Context mContext;
    public Gson mGson;
    public static final String BASE_URL = "http://192.168.1.108:8080/GoachWeb/";
    private Action1<String> onNextAction;
    private HRetrofitNetHelper(Context context){
        this.mContext = context ;
        createSubscriberByAction();
        mGson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .create();
        mHttpLogInterceptor = new HttpLoggingInterceptor(this);
        mHttpLogInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        Map<String,String> tempParams = getBaseParams(context);
        mBaseParamsInterceptor = new BasicParamsInterceptor.Builder()
                .addParamsMap(tempParams)
                .build();
        mUrlInterceptor = this;
        File cacheFile = new File(context.getCacheDir(), "HttpCache");
        Log.d("zgx","cacheFile====="+cacheFile.getAbsolutePath());
        cache = new Cache(cacheFile, 1024 * 1024 * 100); //100Mb
        mOkHttpClient = new OkHttpClient.Builder()
                .connectTimeout(12, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .addInterceptor(mHttpLogInterceptor)
                .addInterceptor(mBaseParamsInterceptor)
                .addInterceptor(mUrlInterceptor)
                .cache(cache)
                .build();
        mRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(mGson))
                .client(mOkHttpClient)
                .build();
    }
    public static HRetrofitNetHelper getInstance(Context context){
        if(mInstance==null){
            synchronized (HRetrofitNetHelper.class){
                if(mInstance==null){
                    mInstance =  new HRetrofitNetHelper(context);
                }
            }
        }
        return mInstance;
    }
    public <T> T getAPIService(Class<T> service) {
        return mRetrofit.create(service);
    }
    public <D> void enqueueCall(Call<BaseResp<D>> call,final RetrofitCallBack<D> retrofitCallBack){
        call.enqueue(new Callback<BaseResp<D>>() {
            @Override
            public void onResponse(Call<BaseResp<D>> call, Response<BaseResp<D>> response) {
                BaseResp<D> resp = response.body() ;
                if (resp == null) {
                    Toast.makeText(mContext, "暂时没有最新数据!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (resp.getResultCode() == 2000 || resp.getResultCode() == 2001 || resp.getResultCode() == 2002) {
                   Toast.makeText(mContext,"code====="+resp.getResultCode(),Toast.LENGTH_SHORT).show();
                }
                if (resp.getResultCode() == 200) {
                    if(retrofitCallBack!=null)
                    retrofitCallBack.onSuccess(resp);
                } else {

                    if(retrofitCallBack!=null)
                        retrofitCallBack.onFailure(resp.getErrMsg());
                }
            }

            @Override
            public void onFailure(Call<BaseResp<D>> call, Throwable t) {
             //   ToastMaker.makeToast(mContext, "网络错误，请重试！", Toast.LENGTH_SHORT);
                if(retrofitCallBack!=null){
                    retrofitCallBack.onFailure(t.toString());
                }
            }
        });
    }
    @Override
    public void log(String message) {
        Log.d("zgx","OkHttp: " + message);
    }
    public Map<String,String> getBaseParams(Context context){
        Map<String,String> params = new HashMap<>();
        params.put("userId", "324353");
        params.put("sessionToken", "434334");
        params.put("q_version", "1.1");
        params.put("device_id", "android7.0");
        params.put("device_os", "android");
        params.put("device_type", "android");
        params.put("device_osversion", "android");
        params.put("req_timestamp", System.currentTimeMillis() + "");
        params.put("app_name","forums");
        String sign = makeSign(params);
        params.put("sign", sign);
        return params ;
    }
    public String makeSign(Map<String, String> params) {
        final String signSalt = "fe#%d8ec93a1159a2a3";
        TreeMap<String, Object> sorted = new TreeMap<String, Object>();
        for (Map.Entry<String, String> kv : params.entrySet()) {
            sorted.put(kv.getKey(), kv.getValue());
        }
        StringBuilder sb = new StringBuilder(signSalt);
        for (String key : sorted.keySet()) {
            if (!"sign".equals(key) && !key.startsWith("file_")) {
                sb.append(key).append(sorted.get(key));
            }
        }
        sb.append(signSalt);
        return MD5.md5(sb.toString()).toUpperCase();
    }
    @Override
    public okhttp3.Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        //缓存
        if(NetUtil.checkNetwork(mContext)==NetUtil.NO_NETWORK){
            request = request.newBuilder()
                    .cacheControl(CacheControl.FORCE_CACHE)
                    .build();
            Log.d("zgx","no network");
        }

        okhttp3.Response response = chain.proceed(request);
        String requestUrl = response.request().url().uri().getPath();
        if(!TextUtils.isEmpty(requestUrl)){
            if(requestUrl.contains("LoginDataServlet")) {
                if (Looper.myLooper() == null) {
                    Looper.prepare();
                }
                createObservable("正在登陆，请稍后...");
            }
        }
        //缓存响应
        if(NetUtil.checkNetwork(mContext)!=NetUtil.NO_NETWORK){

            String cacheControl = request.cacheControl().toString();
            Log.d("zgx","cacheControl====="+cacheControl);
            return response.newBuilder()
                    .header("Cache-Control", cacheControl)
                    .removeHeader("Pragma")
                    .build();
        }else{
            return response.newBuilder()
                    .header("Cache-Control", "public, only-if-cached, max-stale=120")
                    .removeHeader("Pragma")
                    .build();
        }
    }
    public interface RetrofitCallBack<D>{
        void onSuccess(BaseResp<D> baseResp);
        void onFailure(String error);
    }
    private void createSubscriberByAction() {
        onNextAction = new Action1<String>() {
            @Override
            public void call(String s) {
                Log.d("zgx","s=========="+s);
                Toast.makeText(mContext,s, Toast.LENGTH_SHORT).show();
            }
        };
    }
    private void createObservable(String msg){
        Observable.just(msg).map(new Func1<String, String>() {
            @Override
            public String call(String s) {
                return s;
            }
        })
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(onNextAction);
    }
    public Cache getCache(){
        return cache;
    }
    public void clearCache() throws IOException {
        cache.delete();
    }
}
