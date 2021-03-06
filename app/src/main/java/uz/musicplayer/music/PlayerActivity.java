package uz.musicplayer.music;

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.Glide;

import net.alhazmy13.mediapicker.Video.VideoPicker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.alterac.blurkit.BlurLayout;

import static uz.musicplayer.music.AlbumDetailsAdapter.musicFilesAlbums;
import static uz.musicplayer.music.MainActivity.repeatBoolean;
import static uz.musicplayer.music.MainActivity.shuffleBoolean;
import static uz.musicplayer.music.MusicAdapter.mFiles;

public class PlayerActivity extends AppCompatActivity implements ServiceConnection, MyServiceCallback, Playable {
    TextView song_name, artist;
    TextView duration_played;
    TextView duration_total;
    ImageView next, previous, back_button, menuBtn, coverMusic, shuffleBtn, repeateBtn;
    ImageView pause_play;
    VideoView videoView;
    SeekBar seekBar;
    BlurLayout blurLayout;
    int position = -1;
    Thread pausePlay, nextBtn, previousBtn;
    static ArrayList<MusicFiles> listsong = new ArrayList<>();
    private Handler mHandler = new Handler();
    Animation animBlink;
    static Uri uri1;
    MusicService musicService;
    boolean bounded = false;
    Intent intent;
    public static SharedPreferences sharedPreferences;
    String pathG;
    String pathV;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen();
        setContentView(R.layout.activity_player);
        getSupportActionBar().hide();
        initViews();
        getIntentMethod();
        back_button.setOnClickListener(v -> onBackPressed());
        blurLayout = findViewById(R.id.img_blur_backround);

        sharedPreferences = getSharedPreferences(getString(R.string.pref_key), MODE_PRIVATE);
        pathV = sharedPreferences.getString(getString(R.string.path), null);
        if (pathV != null) {
            videoView.setVideoPath(pathV);
        } else {
            pathG = "android.resource://" + getPackageName() + "/"
                    + R.raw.gg;
            Uri u = Uri.parse(pathG);
            videoView.setVideoURI(u);
        }
        videoView.start();

        menuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new VideoPicker.Builder(PlayerActivity.this)
                        .mode(VideoPicker.Mode.GALLERY)
                        .directory(VideoPicker.Directory.DEFAULT)
                        .extension(VideoPicker.Extension.MP4)
                        .enableDebuggingMode(true)
                        .build();
            }
        });
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setVolume(0f, 0f);
                mp.setLooping(true);
                float videoRatio = mp.getVideoWidth() / (float) mp.getVideoHeight();
                float screenRatio = videoView.getWidth() / (float) videoView.getHeight();
                float scaleX = videoRatio / screenRatio;
                if (scaleX >= 1f) {
                    videoView.setScaleX(scaleX);
                } else {
                    videoView.setScaleY(1f / scaleX);
                }
            }
        });
        shuffleBtn.setOnClickListener(v -> {
            if (shuffleBoolean) {
                shuffleBoolean = false;
                shuffleBtn.setImageResource(R.drawable.ic_shuffle_on);
            } else {
                shuffleBtn.setImageResource(R.drawable.ic_shuffle_of);
                shuffleBoolean = true;
            }
        });
        repeateBtn.setOnClickListener(v -> {
            if (repeatBoolean) {
                repeatBoolean = false;
                repeateBtn.setImageResource(R.drawable.ic_repeate_on);
            } else {
                repeatBoolean = true;
                repeateBtn.setImageResource(R.drawable.ic_repeate_of);
            }
        });


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        videoView.stopPlayback();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == VideoPicker.VIDEO_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> mPaths = data.getStringArrayListExtra(VideoPicker.EXTRA_VIDEO_PATH);
            //Your Code
            sharedPreferences = getSharedPreferences(getString(R.string.pref_key), MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(getString(R.string.path), mPaths.get(0));
            editor.apply();
            Log.d("Hello", "onActivityResult: " + mPaths.get(0));
            videoView.setVideoPath(mPaths.get(0));
            videoView.start();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        bindService(intent, this, Context.BIND_AUTO_CREATE);
        videoView.resume();
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (musicService != null && fromUser) {
                    musicService.seekToPosition(progress * 1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (musicService != null) {
                    int mCurrentPosition = musicService.getFileCurrentPosition() / 1000;
                    seekBar.setProgress(mCurrentPosition);
                    duration_played.setText(formattedTime(mCurrentPosition));
                    if (!musicService.isPlaying()) {
                        duration_played.setVisibility(View.VISIBLE);
                        // start the animation
                        duration_played.startAnimation(animBlink);
                    } else {
                        duration_played.setVisibility(View.VISIBLE);
                        // start the animation
                        duration_played.clearAnimation();
                    }
                }
                mHandler.postDelayed(this, 1000);
            }
        });
        animBlink = AnimationUtils.loadAnimation(this,
                R.anim.blink);
        playPauseBtnThread();
        nextBtnThread();
        previousBtnThread();

    }

    private String formattedTime(int mCurrentPosition) {

        String totalout = "";
        String totaloutNew = "";
        String seconds = String.valueOf((mCurrentPosition % 60));
        String minutes = String.valueOf(mCurrentPosition / 60);
        totalout = minutes + ":" + seconds;
        totaloutNew = minutes + ":" + "0" + seconds;
        if (seconds.length() == 1) {
            return totaloutNew;
        } else {
            return totalout;
        }
    }

    private void playPauseBtnThread() {
        pausePlay = new Thread() {
            @Override
            public void run() {
                pause_play.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pausePlayBtnClicked();
                    }
                });
            }
        };


        pausePlay.start();
    }

    private void nextBtnThread() {
        nextBtn = new Thread() {
            @Override
            public void run() {
                next.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        nextBtnClicked();
                    }
                });
            }
        };

        nextBtn.start();
    }

    private static int getRandom(int size) {
        Random random = new Random();
        return random.nextInt((size) + 1);
    }

    private void previousBtnThread() {
        previousBtn = new Thread() {
            @Override
            public void run() {
                previous.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        previousBtnClicked();
                    }
                });
            }
        };
        previousBtn.start();
    }

    private void getIntentMethod() {
        position = getIntent().getIntExtra("position", -1);
        if (position != -1) {
            String sender = getIntent().getStringExtra("sender");
            if (sender != null && sender.equals("albumDetails")) {
                listsong = musicFilesAlbums;
            } else {
                listsong = mFiles;
            }
            if (listsong != null) {
                pause_play.setImageResource(R.drawable.ic_pause);
                if (shuffleBoolean) {
                    shuffleBtn.setImageResource(R.drawable.ic_shuffle_of);
                }
                if (repeatBoolean) {
                    repeateBtn.setImageResource(R.drawable.ic_repeate_of);
                    uri1 = Uri.parse(listsong.get(position).getPath());
                }
            }
        }
        intent = new Intent(this, MusicService.class);
        intent.putExtra("servicePosition", position);
        startService(intent);
    }

    private void initViews() {
        menuBtn = findViewById(R.id.menu_btn);
        videoView = findViewById(R.id.video_view);
        song_name = findViewById(R.id.song_name);
        artist = findViewById(R.id.artist);
        next = findViewById(R.id.id_next);
        previous = findViewById(R.id.id_prev);
        pause_play = findViewById(R.id.play_pause);
        seekBar = findViewById(R.id.seekBar);
        back_button = findViewById(R.id.back_Btn);
        duration_played = findViewById(R.id.duration_played);
        duration_total = findViewById(R.id.total_duration);
        coverMusic = findViewById(R.id.image_cover);
        shuffleBtn = findViewById(R.id.img_shuffle);
        repeateBtn = findViewById(R.id.img_repeate);


    }

    private void setFullScreen() {

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

    }


    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        MusicService.MyBinder b = (MusicService.MyBinder) binder;
        musicService = b.getService();
        bounded = true;
        musicService.setCallbacks(PlayerActivity.this, PlayerActivity.this);
        if (musicService != null) {
            seekBar.setMax(musicService.getFileDuration() / 1000);
            song_name.setText(musicService.getObjectOfMusicFile().getTitle());
            artist.setText(musicService.getObjectOfMusicFile().getArtist());
            musicService.sendChannel2(R.drawable.ic_baseline_pause_24);
            metaDataMethod(musicService.getUriOfMusicFile());
            musicService.onCompleted();
            if (musicService.isPlaying()) {
                pause_play.setImageResource(R.drawable.ic_pause);
            } else {
                pause_play.setImageResource(R.drawable.ic_play);
            }
            position = musicService.getPositionOfMusicFile();
        }
        //Toast.makeText(PlayerActivity.this, "Connected", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        musicService = null;
    }

    @Override
    public void metaDataMethod(Uri uri) {
        String totalout;
        String totaloutNew;
        // get mp3 info
        // convert duration to minute:seconds
        String duration =
                musicService.getObjectOfMusicFile().getDuration();
        long dur = Long.parseLong(duration);
        String seconds = String.valueOf((dur % 60000) / 1000);

        String minutes = String.valueOf(dur / 60000);
        totalout = minutes + ":" + seconds;
        totaloutNew = minutes + ":" + "0" + seconds;
        if (seconds.length() == 1) {
            duration_total.setText(totaloutNew);
        } else {
            duration_total.setText(totalout);
        }
        byte[] art = null;
        Bitmap image = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Size size = new Size(200, 200);
            try {
                Uri contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        Long.parseLong(mFiles.get(position).getId()));
                image = getApplicationContext().getContentResolver().loadThumbnail(contentUri, size, null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(uri.toString());
            art = retriever.getEmbeddedPicture();
            retriever.release();
            if (art != null)
                image = BitmapFactory.decodeByteArray(art, 0, art.length);
            else
                image = BitmapFactory.decodeResource(getResources(), R.drawable.programmity);
        }


        if (art != null) {
            Glide.with(PlayerActivity.this).asBitmap()
                    .load(art).into(coverMusic);
            //image = BitmapFactory.decodeByteArray(art, 0, art.length);
            //setting image with animation.
            image = BitmapFactory.decodeResource(getResources(), R.drawable.programmity);
            Palette.from(image).generate(palette -> {
                Palette.Swatch dominantSwach = null;
                if (palette != null) {
                    dominantSwach = palette.getDarkMutedSwatch();
                }
                if (dominantSwach != null) {

                }
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(this);
        videoView.suspend();
        bounded = false;
        Log.e("Paused", bounded + "");
        Log.e("Paused", "passed");
    }

    @Override
    public void pausePlayBtnClicked() {
        seekBar.setMax(musicService.getFileDuration());
        if (musicService.isPlaying()) {
            pause_play.setImageResource(R.drawable.ic_play);
            musicService.pause();
            seekBar.setMax(musicService.getFileDuration() / 1000);
            musicService.sendChannel2(R.drawable.ic_baseline_play_arrow_24);
            PlayerActivity.this.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (musicService != null) {
                        int mCurrentPosition = musicService.getFileCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    mHandler.postDelayed(this, 200);
                }
            });
        } else {
            pause_play.setImageResource(R.drawable.ic_pause);
            musicService.start();
            seekBar.setMax(musicService.getFileDuration() / 1000);
            musicService.sendChannel2(R.drawable.ic_baseline_pause_24);
            PlayerActivity.this.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (musicService != null) {
                        int mCurrentPosition = musicService.getFileCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    mHandler.postDelayed(this, 200);
                }
            });
        }
    }

    @Override
    public void nextBtnClicked() {
        if (musicService.isPlaying()) {
            musicService.stop();
            musicService.release();
            if (shuffleBoolean && !repeatBoolean) {
                position = getRandom(listsong.size() - 1);
            } else if (!shuffleBoolean && !repeatBoolean) {
                position = ((position + 1) % listsong.size());
            }
            uri1 = Uri.parse(listsong.get(position).getPath());
            Log.e("Uri -> ", uri1.toString());
            musicService.createMediaPlayer(position);
            //MediaMetaData to set album art
            if (bounded) {
                metaDataMethod(uri1);
                song_name.setText(listsong.get(position).getTitle());
                artist.setText(listsong.get(position).getArtist());
                pause_play.setImageResource(R.drawable.ic_pause);
                seekBar.setMax(musicService.getFileDuration() / 1000);
                PlayerActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (musicService != null) {
                            int mCurrentPosition = musicService.getFileCurrentPosition() / 1000;
                            seekBar.setProgress(mCurrentPosition);
                        }
                        mHandler.postDelayed(this, 1000);
                    }
                });
            }
            musicService.onCompleted();
            musicService.sendChannel2(R.drawable.ic_baseline_pause_24);
            musicService.start();
        } else {
            musicService.stop();
            musicService.release();
            if (shuffleBoolean && !repeatBoolean) {
                position = getRandom(listsong.size() - 1);
            } else if (!shuffleBoolean && !repeatBoolean) {
                position = ((position + 1) % listsong.size());
            }
            uri1 = Uri.parse(listsong.get(position).getPath());
            musicService.createMediaPlayer(position);
            musicService.onCompleted();
            if (bounded) {
                song_name.setText(listsong.get(position).getTitle());
                artist.setText(listsong.get(position).getArtist());
                pause_play.setImageResource(R.drawable.ic_play);
                seekBar.setMax(musicService.getFileDuration() / 1000);
                PlayerActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        if (musicService != null) {
                            int mCurrentPosition = musicService.getFileCurrentPosition() / 1000;
                            seekBar.setProgress(mCurrentPosition);
                        }
                        mHandler.postDelayed(this, 1000);
                    }
                });
                metaDataMethod(uri1);
            }
            musicService.sendChannel2(R.drawable.ic_baseline_play_arrow_24);
        }
    }

    @Override
    public void previousBtnClicked() {
        if (musicService.isPlaying()) {
            musicService.stop();
            musicService.release();
            if (shuffleBoolean && !repeatBoolean) {
                position = getRandom(listsong.size());
            } else if (!shuffleBoolean && !repeatBoolean) {
                position = ((position - 1) < 0 ? (listsong.size() - 1) : (position - 1));
            }
            uri1 = Uri.parse(listsong.get(position).getPath());
            musicService.createMediaPlayer(position);
            musicService.onCompleted();
            song_name.setText(listsong.get(position).getTitle());
            artist.setText(listsong.get(position).getArtist());
            seekBar.setMax(musicService.getFileDuration() / 1000);
            if (bounded) {
                PlayerActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        if (musicService != null) {
                            int mCurrentPosition = musicService.getFileCurrentPosition() / 1000;
                            seekBar.setProgress(mCurrentPosition);
                        }
                        mHandler.postDelayed(this, 200);
                    }
                });
                metaDataMethod(uri1);
                pause_play.setImageResource(R.drawable.ic_pause);
            }
            musicService.sendChannel2(R.drawable.ic_baseline_pause_24);
            musicService.start();
        } else {
            musicService.stop();
            musicService.release();
            if (shuffleBoolean && !repeatBoolean) {
                position = getRandom(listsong.size());
            } else if (!shuffleBoolean && !repeatBoolean) {
                position = ((position - 1) < 0 ? (listsong.size() - 1) : (position - 1));
            }
            uri1 = Uri.parse(listsong.get(position).getPath());
            musicService.createMediaPlayer(position);
            musicService.onCompleted();
            if (bounded) {
                song_name.setText(listsong.get(position).getTitle());
                artist.setText(listsong.get(position).getArtist());
                seekBar.setMax(musicService.getFileDuration() / 1000);
                PlayerActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        if (musicService != null) {
                            int mCurrentPosition = musicService.getFileCurrentPosition() / 1000;
                            seekBar.setProgress(mCurrentPosition);
                        }
                        mHandler.postDelayed(this, 200);
                    }
                });
                metaDataMethod(uri1);
                pause_play.setImageResource(R.drawable.ic_play);
            }
            musicService.sendChannel2(R.drawable.ic_baseline_play_arrow_24);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

}
