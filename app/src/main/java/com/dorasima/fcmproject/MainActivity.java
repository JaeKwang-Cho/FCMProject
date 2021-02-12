package com.dorasima.fcmproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseMessaging.getInstance().subscribeToTopic("dorasima_topic");

        getToken();
    }
    public void getToken(){
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w("test123",
                                  "Fetching FCM registration token failed",
                                  task.getException());
                            return;
                        }
                        // Get new FCM registration token
                        String token = task.getResult();
                        Log.d("test123", "token: "+ token);

                        SendTokenThread thread = new SendTokenThread(token);
                        thread.start();
                    }
                });
    }
    public void nextBtn(View view){
        Intent intent = new Intent(this, GoogleMapProject.class );
        startActivity(intent);
    }
    // 서버로 토큰을 보내는 스레드
    public class SendTokenThread extends Thread{
        private String token;

        SendTokenThread(String token){
            this.token = token;
        }

        @Override
        public void run() {
            super.run();
            try {
                OkHttpClient client = new OkHttpClient();
                Request.Builder builder = new Request.Builder();
                builder = builder.url("http://192.168.35.240:8080/FCMProject/add_token.jsp?token="+token);

                Request request =builder.build();
                Call call = client.newCall(request);
                call.execute();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}