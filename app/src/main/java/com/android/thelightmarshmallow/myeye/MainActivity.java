package com.android.thelightmarshmallow.myeye;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.android.GsonFactory;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.ui.AIDialog;

public class MainActivity extends AppCompatActivity implements AIDialog.AIDialogListener {

    private String item_id=null;
    private final String upc_token="328B2717F671E5BCB599FDE1518454F2";
    private AudioManager audioManager;
    private ImageView imageView;
    private MediaPlayer mMediaplayer;
    private AIService aiService;
    private AIDialog aiDialog;
    private Gson gson= GsonFactory.getGson();
    private TextToSpeech mytts;
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


      /*  aiService = AIService.getService(this, config);
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
        });*/

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
        //BarCodedetection();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.v("json response",gson.toJson(result));
                //Toast.makeText(MainActivity.this,gson.toJson(result),Toast.LENGTH_SHORT).show();
                //now we'll have to define further actions on the basis of the type of action
                if(result.getResult().getAction().toString().equals("camera")){
                    Toast.makeText(MainActivity.this,"inside runOnUi",Toast.LENGTH_SHORT).show();
                    BarCodedetection();
                }

            }
        });
    }

    private void BarCodedetection() {
        Intent intent= new Intent(MainActivity.this,BarCodeScannerActivity.class);
        startActivityForResult(intent,0);

    }

    @Override
    public void onError(AIError error) {


    }

    @Override
    public void onCancelled() {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==0){
            if(requestCode== CommonStatusCodes.SUCCESS){
                if(data!=null){
                    Barcode barcode = data.getParcelableExtra("barcode");
                    Toast.makeText(this,barcode.displayValue,Toast.LENGTH_SHORT).show();
                    item_id=barcode.displayValue;
                    //Toast.makeText(this,,Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(this,"failure",Toast.LENGTH_SHORT).show();
                }
            }

        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }

    }

    private String generateItemdetails() {
        StringBuilder ab= new StringBuilder();
        URL url=null;
        String s=null;
        ab.append("https://api.upcdatabase.org/product/");
        ab.append(item_id+"/");
        ab.append(upc_token);
        HttpURLConnection urlConnection=null;
        try {
            url = new URL(ab.toString());
            urlConnection = (HttpURLConnection) url.openConnection();
            InputStreamReader isr= new InputStreamReader(urlConnection.getInputStream());
            BufferedReader bufferedReader= new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String buffer= " ";
            while(buffer!=null){
                sb.append(buffer);
                buffer= bufferedReader.readLine();
            }
            s=sb.toString();
        } catch (IOException e) {
            Log.e("MainActivity","url");
        }
        finally {
            urlConnection.disconnect();
        }
        return s;
    }

    private void speakwords(String speech){

    }
}
