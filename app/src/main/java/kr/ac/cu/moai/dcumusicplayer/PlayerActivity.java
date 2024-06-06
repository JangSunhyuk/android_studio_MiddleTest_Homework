package kr.ac.cu.moai.dcumusicplayer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import java.io.IOException;
import java.util.Objects;

public class PlayerActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer;
    SeekBar seekbar;
    ImageView pausePlay;
    Intent intent;
    String mp3file;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        seekbar = findViewById(R.id.seek_bar);
        pausePlay = findViewById(R.id.pause_play);
        intent = getIntent();
        mp3file = intent.getStringExtra("mp3");

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        try (MediaMetadataRetriever retriever = new MediaMetadataRetriever()) {
            ImageView ivCover = findViewById(R.id.ivCover);
            retriever.setDataSource(mp3file);
            byte[] b = retriever.getEmbeddedPicture();
            Bitmap cover = BitmapFactory.decodeByteArray(b, 0, b.length);
            ivCover.setImageBitmap(cover);

            TextView tvTitle = findViewById(R.id.tvTitle);
            String title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            tvTitle.setText(title);

            TextView tvDuration = findViewById(R.id.tvDuration);
            tvDuration.setText(ListViewMP3Adapter.getDuration(retriever));

            TextView tvArtist = findViewById(R.id.tvArtist);
            String artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            tvArtist.setText(artist);

            mediaPlayer = new MediaPlayer();

            setResourceWithMusic();

            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(mediaPlayer!=null){
                        seekbar.setProgress(mediaPlayer.getCurrentPosition());

                        if(mediaPlayer.isPlaying()){
                            pausePlay.setImageResource(R.drawable.pause_image);
                        } else{
                            pausePlay.setImageResource(R.drawable.start_image);
                        }
                    }
                }
            });
            seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if(mediaPlayer!=null && fromUser){
                        mediaPlayer.seekTo(progress);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    void setResourceWithMusic(){
        pausePlay.setOnClickListener(v-> pausePlay());
        playMusic();
    }

    private void playMusic(){
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(mp3file);
            mediaPlayer.prepare();
            mediaPlayer.start();
            seekbar.setMax(mediaPlayer.getDuration());
            updateSeekBar();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateSeekBar() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            int currentPosition = mediaPlayer.getCurrentPosition();
            seekbar.setProgress(currentPosition);
            new Handler().postDelayed(this::updateSeekBar, 500);
        }
    }

    private void pausePlay(){
        if(mediaPlayer.isPlaying()){
            mediaPlayer.pause();
            pausePlay.setImageResource(R.drawable.start_image);
        } else {
            mediaPlayer.start();
            pausePlay.setImageResource(R.drawable.pause_image);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        if(mediaPlayer != null)
        {
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        finish();
        return super.onSupportNavigateUp();
    }
    protected void onDestroy()
    {
        if(mediaPlayer != null)
        {
            mediaPlayer.release();
        }
        super.onDestroy();
    }
}