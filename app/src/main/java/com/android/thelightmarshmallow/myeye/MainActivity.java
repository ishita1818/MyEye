package com.android.thelightmarshmallow.myeye;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.util.Map;

import ai.api.AIListener;
import ai.api.android.GsonFactory;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.model.Result;
import ai.api.ui.AIDialog;

public class MainActivity extends AppCompatActivity implements AIDialog.AIDialogListener {


    private AudioManager audioManager;
    private ImageView imageView;
    private MediaPlayer mMediaplayer;
    private AIService aiService;
    private AIDialog aiDialog;
    private Gson gson= GsonFactory.getGson();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        releseMediaPlayer();
        int result = audioManager.requestAudioFocus(mOnAudioFocusChangeListener,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {

            mMediaplayer = MediaPlayer.create(MainActivity.this, R.raw.heyhowcan);
            mMediaplayer.start();
            mMediaplayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaplayer) {
                    releseMediaPlayer();
                }
            });
        }
        final AIConfiguration config = new AIConfiguration("54dfcfaad0c1426c8b2429edcefacb08",
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);

        aiDialog= new AIDialog(this, config);
        aiDialog.setResultsListener(this);
        aiService = AIService.getService(this, config);
        aiService.setListener(new AIListener() {
            @Override
            public void onResult(ai.api.model.AIResponse response) {
                Result result = response.getResult();

                // Get parameters
                String parameterString = "";
                if (result.getParameters() != null && !result.getParameters().isEmpty()) {
                    for (final Map.Entry<String, JsonElement> entry : result.getParameters().entrySet()) {
                        parameterString += "(" + entry.getKey() + ", " + entry.getValue() + ") ";
                    }
                }

                // Show results in TextView.
                Log.v("Mainactivity", "Query:" + result.getResolvedQuery() +
                        "\nAction: " + result.getAction() +
                        "\nParameters: " + parameterString);
            }

            @Override
            public void onError(ai.api.model.AIError error) {
                Log.e("mainactivity", error.toString());
            }

            @Override
            public void onAudioLevel(float level) {
            }

            @Override
            public void onListeningStarted() {
            }

            @Override
            public void onListeningCanceled() {
            }

            @Override
            public void onListeningFinished() {
            }
        });

        imageView = findViewById(R.id.mainActivityImageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                aiDialog.showAndListen();
                //aiService.startListening();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (aiService != null) {
            aiService.resume();
        }
    }
    private AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener(){
        @Override
        public void onAudioFocusChange(int FocusChange) {

            if(FocusChange==AudioManager.AUDIOFOCUS_LOSS_TRANSIENT||FocusChange==AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK){
                mMediaplayer.pause();
                mMediaplayer.seekTo(0);

            }
            else if(FocusChange==AudioManager.AUDIOFOCUS_GAIN){
                mMediaplayer.start();
            }
            else if(FocusChange==AudioManager.AUDIOFOCUS_LOSS){
                releseMediaPlayer();
            }
        }
    };
    private void releseMediaPlayer(){
        if(mMediaplayer!=null){
            mMediaplayer.release();

            mMediaplayer=null;
            audioManager.abandonAudioFocus(mOnAudioFocusChangeListener);}
    }

    @Override
    public void onResult(final AIResponse result) {
        //Log.v("MAinActivity","result");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.v("json response",gson.toJson(result));
            }
        });
    }

    @Override
    public void onError(AIError error) {

    }

    @Override
    public void onCancelled() {

    }
}
