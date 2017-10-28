package com.android.thelightmarshmallow.myeye;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;

import java.util.Locale;

public class SplashActivity extends AppCompatActivity {
    private final int SPLASH_DISPLAY_LENGTH= 3000;
    private TextToSpeech textToSpeech;

    public void toSpeak(String s){
        textToSpeech.speak(s,TextToSpeech.QUEUE_FLUSH,null);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        textToSpeech=new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i!=TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.UK);
                }
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                toSpeak("Hello");
                toSpeak("welcome to my eye app");
                toSpeak("tap the mic button to let me know how can i help you");
                Intent intent= new Intent(SplashActivity.this,MainActivity.class);
                SplashActivity.this.startActivity(intent);
                SplashActivity.this.finish();
            }
        },SPLASH_DISPLAY_LENGTH);

    }
}
