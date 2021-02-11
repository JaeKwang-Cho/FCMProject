package com.dorasima.fcmproject;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FCMService extends FirebaseMessagingService {
    public FCMService() {
    }
    // 토큰값이 새롭게 갱신되면 호출되는 메서드
    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        Log.d("test123", "new token: "+s);
    }
    // firebase 서버에서 메시지가 도착하면 호출되는 메서드
    // Android OS가 별도의 스레드를 발생 시켜서 호출하게 된다.
    // 핸들러를 이용해야 한다.
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // 도착한 메시지가 있다면
        if(remoteMessage.getNotification()!=null){
            // 메시지를 추출한다.
            final String message = remoteMessage.getNotification().getBody();

            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast toast = Toast.makeText(getBaseContext(),message,Toast.LENGTH_LONG);
                    toast.show();
                }
            });
        }
    }
}