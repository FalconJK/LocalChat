package com.falconjk.LocalChat;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

// ChatService.java
public class ChatService {
    private static String TAG = "ChatService";
    private static final int PORT = 53317;
    private final WebServer webServer;
    private final OkHttpClient httpClient;
    private final ChatListener listener;
    private final Gson gson;

    public interface ChatListener {
        void onMessageReceived(Message message);
    }

    public ChatService(ChatListener listener) {
        this.listener = listener;
        this.httpClient = new OkHttpClient();
        this.webServer = new WebServer(PORT);
        this.gson = new Gson();
    }

    public void start() {
        try {
            webServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        webServer.stop();
    }

    public void sendMessage(String targetIp, Message message) {
        String url = String.format("http://%s:%d/chat/message", targetIp, PORT);

        RequestBody body = RequestBody.create(
                gson.toJson(message),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) {
                response.close();
            }
        });
    }

    private class WebServer extends NanoHTTPD {
        public WebServer(int port) {
            super(port);
        }

        @Override
        public Response serve(IHTTPSession session) {
            if (session.getUri().equals("/chat/message") &&
                    session.getMethod() == Method.POST) {
                try {
                    Map<String, String> files = new HashMap<>();
                    session.parseBody(files);
                    String messageJson = files.get("postData");
                    Log.d(TAG, files.toString());

                    Message message = gson.fromJson(messageJson, Message.class);
                    message.setType(Message.MessageType.RECEIVED);

                    // 通知主線程收到新消息
                    new Handler(Looper.getMainLooper()).post(() -> {
                        listener.onMessageReceived(message);
                    });

                    return newFixedLengthResponse(Response.Status.OK, "application/json", "{\"status\":\"ok\"}");
                } catch (Exception e) {
                    e.printStackTrace();
                    return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "Error");
                }
            }
            return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Not Found");
        }
    }
}
