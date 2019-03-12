package com.example.luwenjie0312.model.util;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @Auther:
 * @Date: 2019/3/12 08:58
 * @Description:
 */
public class HttpUtil<T> {

    private final OkHttpClient okHttpClient;
    private CallBackData callBackData;

    private HttpUtil() {
        //创建okhttp对象
        okHttpClient = new OkHttpClient.Builder()
                .addNetworkInterceptor(getAppInterceptor())
                .readTimeout(5, TimeUnit.SECONDS)
                .connectTimeout(5, TimeUnit.SECONDS)
                .build();
    }

    //拦截器
    private static Interceptor getAppInterceptor() {
        Interceptor interceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                Log.i("aaa", "--------" + "拦截前");
                Response response = chain.proceed(request);
                Log.i("aaa", "--------" + "拦截后");
                return response;
            }
        };
        return interceptor;
    }

    //单例模式
    public static HttpUtil getInstance() {
        return getHttpUtilInstance.httpUtil;
    }

    private static class getHttpUtilInstance {
        //创建HttpUtil对象
        public static HttpUtil httpUtil = new HttpUtil();
    }

    //get请求
    public void getData(String url, final Class<T> tClass, CallBackData callBackData) {
        this.callBackData = callBackData;
        //创建请求对象
        Request request = new Request
                .Builder()
                .url(url)
                .get()
                .build();
        //将request封装成call请求对象
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Message message = handler.obtainMessage();
                message.what = 0;
                message.obj = e.getMessage();
                handler.sendMessage(message);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                Gson gson = new Gson();
                T t = gson.fromJson(string, tClass);
                Message message = handler.obtainMessage();
                message.what = 1;
                message.obj = t;
                handler.sendMessage(message);
            }
        });
    }

    //post请求
    public void postData(String url, final Class<T> tClass, HashMap<String, String> hashMap, CallBackData callBackData) {
        this.callBackData = callBackData;
        FormBody.Builder builder = new FormBody.Builder();
        for (Map.Entry<String, String> stringStringEntry : hashMap.entrySet()) {
            String key = stringStringEntry.getKey();
            String value = stringStringEntry.getValue();
            builder.add(key, value);
        }
        FormBody build = builder.build();
        //创建请求对象
        Request request = new Request.Builder()
                .url(url)
                .post(build)
                .build();
        //将request封装成call请求对象
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Message message = handler.obtainMessage();
                message.what = 0;
                message.obj = e.getMessage();
                handler.sendMessage(message);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                Gson gson = new Gson();
                T t = gson.fromJson(string, tClass);
                Message message = handler.obtainMessage();
                message.what = 1;
                message.obj = t;
                handler.sendMessage(message);
            }
        });
    }

    //接口
    public interface CallBackData<D> {
        void onResponse(D d);

        void onFail(String msg);
    }

    //handler
    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    String s = (String) msg.obj;
                    callBackData.onFail(s);
                    break;
                case 1:
                    T t = (T) msg.obj;
                    callBackData.onResponse(t);
                    break;
            }
        }
    };

}
