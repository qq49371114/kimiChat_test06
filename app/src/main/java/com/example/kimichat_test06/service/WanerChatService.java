package com.example.kimichat_test06.service;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class WanerChatService {

    // 哥哥，记得把这里换成你的AstrBot API地址哦！这步最关键啦！
    private static final String ASTRBOT_API_URL = "http://127.0.0.1:8080/api/v1/chat/completion";
    private final OkHttpClient client = new OkHttpClient();

    // 我们定义一个回调接口，这样MainActivity就能知道什么时候收到了回复
    public interface WanerCallback {
        void onSuccess(String reply);
        void onFailure(String errorMessage);
    }

    public void sendMessageToWaner(String userMessage, String userId, WanerCallback callback) {
        // 1. 准备要发送的JSON数据
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("message", userMessage);
            jsonObject.put("user_id", userId); // user_id 可以帮助婉儿记住不同的人
            jsonObject.put("stream", false); // 我们先用简单的非流式回复
        } catch (JSONException e) {
            callback.onFailure("创建请求失败: " + e.getMessage());
            return;
        }
        RequestBody body = RequestBody.create(jsonObject.toString(), JSON);

        // 2. 创建一个网络请求
        Request request = new Request.Builder()
                .url(ASTRBOT_API_URL)
                // 如果哥哥的AstrBot API需要token验证，在这里加上
                // .addHeader("Authorization", "Bearer YOUR_ASTRBOT_TOKEN")
                .post(body)
                .build();

        // 3. 异步发送请求
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // 网络请求失败了
                callback.onFailure("网络连接失败: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        // 4. 解析收到的回复
                        String responseBody = response.body().string();
                        JSONObject responseJson = new JSONObject(responseBody);
                        String wanerReply = responseJson.getString("message"); // 取出婉儿的回复
                        callback.onSuccess(wanerReply);
                    } catch (Exception e) {
                        callback.onFailure("解析回复失败: " + e.getMessage());
                    }
                } else {
                    callback.onFailure("服务器错误: " + response.code());
                }
            }
        });
    }
}
