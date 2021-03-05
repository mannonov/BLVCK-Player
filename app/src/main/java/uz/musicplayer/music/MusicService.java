package uz.musicplayer.music;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;



import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import static uz.musicplayer.music.App.ACTION_NEXT;
import static uz.musicplayer.music.App.ACTION_PLAY;
import static uz.musicplayer.music.App.ACTION_PREVIOUS;
import static uz.musicplayer.music.App.CHANNEL_ID_2;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener {
    public static final String CLICK_PLAY = "actionplay";
    public static final String CLICK_NEXT = "actionnext";
    public static final String CLICK_PREVIOUS = "actionprevious";
    public static final String CLICK_DELETE = "actiondelete";
    MediaPlayer mediaPlayer = new MediaPlayer();
    private final IBinder mBinder = new MyBinder();
    ArrayList<MusicFiles> musicFiles = new ArrayList<>();
    static Uri uri;
    int position;
    MyServiceCallback myServiceCallback;
    Playable playable;
    MediaSessionCompat mediaSession;

    @Override
    public void onCreate() {
        super.onCreate();
        mediaSession = new MediaSessionCompat(getBaseContext(), "BlvckPlayer");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    void playSong(int startPosition) {
        musicFiles = PlayerActivity.listsong;
        position = startPosition;
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            if (musicFiles != null) {
                createMediaPlayer(position);
                mediaPlayer.start();
            }
        } else {
            createMediaPlayer(position);
            mediaPlayer.start();
        }
        onCompleted();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (playable != null) {
            playable.nextBtnClicked();
            sendChannel2(R.drawable.ic_baseline_pause_24);
            if (mediaPlayer != null) {
                createMediaPlayer(position);
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(this);
            }
        }
    }

    public class MyBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int myPosition = intent.getIntExtra("servicePosition", -1);
        if (myPosition != -1) {
            playSong(myPosition);
        }
        String action;
        action = intent.getStringExtra("actionname");
        if (action != null) {
            switch (action) {
                case CLICK_PLAY:
                    if (playable != null)
                        playable.pausePlayBtnClicked();
                    break;
                case CLICK_NEXT:
                    if (playable != null)
                        playable.nextBtnClicked();
                    break;
                case CLICK_PREVIOUS:
                    if (playable != null)
                        playable.previousBtnClicked();
                    break;
                case CLICK_DELETE:
                    if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                        playable.pausePlayBtnClicked();
                    }
                    stopForeground(true);
                    stopSelf();
                    break;
            }
        }
        return START_STICKY;
    }

    int getFileDuration() {
        if (mediaPlayer != null)
            return mediaPlayer.getDuration();
        return 0;
    }

    int getFileCurrentPosition() {
        if (mediaPlayer != null)
            return mediaPlayer.getCurrentPosition();
        return 0;
    }

    void seekToPosition(int myPosition) {
        if (mediaPlayer != null)
            mediaPlayer.seekTo(myPosition);
    }

    boolean isPlaying() {
        if (mediaPlayer != null)
            return mediaPlayer.isPlaying();
        return true;
    }

    void pause() {
        if (mediaPlayer != null)
            mediaPlayer.pause();
    }

    void start() {
        if (mediaPlayer != null)
            mediaPlayer.start();
    }

    void stop() {
        if (mediaPlayer != null)
            mediaPlayer.stop();
    }

    void release() {
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.release();
        }
    }

    MusicFiles getObjectOfMusicFile() {
        return musicFiles.get(position);
    }

    Uri getUriOfMusicFile() {
        return uri;
    }

    int getPositionOfMusicFile() {
        if (mediaPlayer.isPlaying())
            return position;
        return 0;
    }

    void createMediaPlayer(int positionInner) {
        position = positionInner;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Uri contentUri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    Long.parseLong(musicFiles.get(position).getId()));
            String ex = contentUri + "";
            uri = Uri.fromFile(getContentFile(this, contentUri));
        } else {
            uri = Uri.parse(musicFiles.get(position).getPath());
        }
        mediaPlayer = MediaPlayer.create(getBaseContext(), uri);
    }

    void onCompleted() {
        if (mediaPlayer != null)
            mediaPlayer.setOnCompletionListener(this);
    }

    private static int getRandom(int size) {
        Random random = new Random();
        return random.nextInt((size) + 1);
    }

    public void setCallbacks(MyServiceCallback callbacks, Playable playableCallback) {
        myServiceCallback = callbacks;
        playable = playableCallback;
    }

    public void sendChannel2(int playbutton) {
        Intent intent = new Intent(this, PlayerActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this,
                0, intent, 0);
        Intent broadcastIntent = new Intent(this, NotificationReciever.class);
        PendingIntent actionIntent = PendingIntent.getBroadcast(this, 0,
                broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Bitmap picture;
            picture = BitmapFactory.decodeResource(getResources(), R.drawable.programmity);
        PendingIntent pendingIntentPrevious;
        int drw_previous;
        Intent intentPrevious = new Intent(this, NotificationReciever.class)
                .setAction(ACTION_PREVIOUS);
        pendingIntentPrevious = PendingIntent.getBroadcast(this, 0,
                intentPrevious, PendingIntent.FLAG_UPDATE_CURRENT);
        drw_previous = R.drawable.ic_skip_previous;

        Intent intentPlay = new Intent(this, NotificationReciever.class)
                .setAction(ACTION_PLAY);
        PendingIntent pendingIntentPlay = PendingIntent.getBroadcast(this, 0,
                intentPlay, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pendingIntentNext;
        int drw_next;
        Intent intentNext = new Intent(this, NotificationReciever.class)
                .setAction(ACTION_NEXT);
        pendingIntentNext = PendingIntent.getBroadcast(this, 0,
                intentNext, PendingIntent.FLAG_UPDATE_CURRENT);
        drw_next = R.drawable.ic_skip_next;
        PendingIntent pendingIntentDelete;
        int drw_delete;
        Intent intentDelete = new Intent(this, NotificationReciever.class)
                .setAction(CLICK_DELETE);
        pendingIntentDelete = PendingIntent.getBroadcast(this, 0,
                intentDelete, PendingIntent.FLAG_UPDATE_CURRENT);
        drw_delete = R.drawable.ic_close;
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID_2)
                .setSmallIcon(R.drawable.programmity)
                .setLargeIcon(picture)
                .setContentTitle(musicFiles.get(position).getTitle())
                .setContentText(musicFiles.get(position).getArtist())
                .setLargeIcon(picture)
                .addAction(drw_previous, "Previous", pendingIntentPrevious)
                .addAction(playbutton, "Play", pendingIntentPlay)
                .addAction(drw_next, "Next", pendingIntentNext)
                .addAction(drw_delete, "Delete", pendingIntentDelete)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(1, 2, 3)
                        .setMediaSession(mediaSession.getSessionToken()))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(contentIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOnlyAlertOnce(true)
                .setSound(null, AudioManager.STREAM_SYSTEM)
                .build();
        startForeground(2, notification);
        NotificationManager mNotificationManager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);
        mNotificationManager.cancelAll();
    }
    private static File getContentFile(Context context, Uri uri) {
        if (uri == null) return null;
        FileInputStream input = null;
        FileOutputStream output = null;
        String filePath = new File(context.getCacheDir(), "temp").getAbsolutePath();
        try {
            ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
            if (pfd == null) return null;
            FileDescriptor fd = pfd.getFileDescriptor();
            input = new FileInputStream(fd);
            output = new FileOutputStream(filePath);
            int read;
            byte[] bytes = new byte[4096];
            while ((read = input.read(bytes)) != -1) {
                output.write(bytes, 0, read);
            }
            return new File(filePath);
        } catch (IOException ignored) {
        } finally {
            try {
                if (input != null)
                    input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (output != null)
                    output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
//            closeQuietly(input);
//            closeQuietly(output);
        }
        return null;
    }
}
