package uz.jaxadevgroup.blvckplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentUris;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Size;
import android.widget.ImageView;
import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.ArrayList;

public class AlbumDetails extends AppCompatActivity {

    RecyclerView recyclerView;
    ImageView albumPhoto;
    AlbumDetailsAdapter albumDetailsAdapter;
    String albumName;
    ArrayList<MusicFiles> albumSongs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme();
        setContentView(R.layout.activity_album_details);
        recyclerView = findViewById(R.id.recyclerView);
        albumPhoto = findViewById(R.id.albumPhoto);
        albumName = getIntent().getStringExtra("albumId");
        int j = 0;
        for (int i = 0; i < MusicAdapter.mFiles.size(); i++) {
            assert albumName != null;
            if (albumName.equals(MusicAdapter.mFiles.get(i).getAlbumid())) {
                albumSongs.add(j, MusicAdapter.mFiles.get(i));
                j++;
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Size size = new Size(200, 200);
            try {
                Uri contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        Long.parseLong(albumSongs.get(0).getId()));
                Bitmap albumArt = this.getContentResolver().loadThumbnail(contentUri, size, null);
                if (albumArt != null) {
                    Glide.with(this).asBitmap()
                            .load(albumArt)
                            .into(albumPhoto);
                } else {
                    Glide.with(this).asBitmap()
                            .load(R.drawable.programmity).into(albumPhoto);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            byte[] image = getAlbumToAdapter(albumSongs.get(0).getPath());
            if (image != null) {
                Glide.with(this).asBitmap()
                        .load(image)
                        .into(albumPhoto);
            } else {
                Glide.with(this).asBitmap()
                        .load(R.drawable.programmity).into(albumPhoto);
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (!(albumSongs.size() < 1))
        {
            albumDetailsAdapter = new AlbumDetailsAdapter(this, albumSongs);
            recyclerView.setAdapter(albumDetailsAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        }
    }
    private byte[] getAlbumToAdapter(String uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }

    private void setTheme() {
        String MY_PREFS_THEME = "THEME";
        SharedPreferences preferences = getSharedPreferences(MY_PREFS_THEME, MODE_PRIVATE);
        if (preferences.getString("theme", "dark").equals("dark")) {
            setTheme(R.style.DarkTheme);
        } else if (preferences.getString("theme", "dark").equals("cool")) {
            setTheme(R.style.CoolTheme);
        } else {
            setTheme(R.style.LightTheme);
        }
    }
}