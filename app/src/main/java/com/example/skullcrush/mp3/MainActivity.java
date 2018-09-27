package com.example.skullcrush.mp3;

import android.app.ProgressDialog;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import dyanamitechetan.vusikview.VusikView;

public class MainActivity extends AppCompatActivity implements MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnCompletionListener {

    private ImageButton imageButton;
    private SeekBar seekBar;
    private TextView textView;
    private VusikView vusikView;
    private MediaPlayer mediaPlayer;
    private int MediaFileLength;
    private int RealTimeLength;
    final Handler handler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        vusikView = findViewById(R.id.MusicView);
        imageButton = findViewById(R.id.btn_play_pause);
        textView = findViewById(R.id.Timer);
        seekBar = findViewById(R.id.seekbar);

        seekBar.setMax(99);
        seekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(mediaPlayer.isPlaying())
                {
                    SeekBar seekBar = (SeekBar)v;
                    int playPosition = (MediaFileLength/100)*seekBar.getProgress();
                    mediaPlayer.seekTo(playPosition);
                }
                return false;
            }
        });

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);


                AsyncTask<String,String,String> asyncTask = new AsyncTask<String, String, String>(){
                    @Override
                    protected void onPreExecute() {
                       progressDialog.setMessage("Please Wait .... ");
                       progressDialog.show();
                    }

                    @Override
                    protected String doInBackground(String... strings) {

                        try {
                            mediaPlayer.setDataSource(strings[0]);
                            mediaPlayer.prepare();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return "";
                    }

                    @Override
                    protected void onPostExecute(String s) {
                        MediaFileLength = mediaPlayer.getDuration();
                        RealTimeLength = MediaFileLength;
                        if(!mediaPlayer.isPlaying()){
                            mediaPlayer.start();
                            imageButton.setImageResource(R.drawable.ic_pause);
                        }
                        else {
                            mediaPlayer.pause();
                            imageButton.setImageResource(R.drawable.ic_play);
                        }

                        updateSeekBar();
                        progressDialog.dismiss();
                    }
                };

                asyncTask.execute("http://mic.duytan.edu.vn:86/ncs.mp3");
                vusikView.start();
            }


        });

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnCompletionListener(this);

    }

    private void updateSeekBar() {
        seekBar.setProgress((int)((float)mediaPlayer.getCurrentPosition()/MediaFileLength)*1000);
        if(mediaPlayer.isPlaying())
        {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {

                    updateSeekBar();
                    RealTimeLength-=100;
                    textView.setText(String.format("%d:%d",TimeUnit.MILLISECONDS.toMinutes(RealTimeLength),
                            TimeUnit.MILLISECONDS.toSeconds(RealTimeLength) - TimeUnit.MILLISECONDS.toSeconds(TimeUnit.MILLISECONDS.toMinutes(RealTimeLength))));                }
            };
        }
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

        seekBar.setSecondaryProgress(percent);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

        imageButton.setImageResource(R.drawable.ic_play);
        vusikView.startNotesFall();
    }
}
