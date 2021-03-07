package uz.musicplayer.music;

import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

import static uz.musicplayer.music.MainActivity.ARTIST_TO_FRAG;
import static uz.musicplayer.music.MainActivity.PATH_TO_FRAG;
import static uz.musicplayer.music.MainActivity.SHOW_MUSIC_PLAYER;
import static uz.musicplayer.music.MainActivity.SONG_TO_FRAG;


/**
 * A simple {@link Fragment} subclass.
 */
public class NowPlayingBottomFragment extends Fragment {

    ImageView coverArt,playPause,next;
    TextView artistName,musicName;
    View view;

    public NowPlayingBottomFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_now_playing_bottom,container,false);
        coverArt = view.findViewById(R.id.bottom_album_art);
        playPause = view.findViewById(R.id.pause_fragment);
        next = view.findViewById(R.id.skip_forward);
        artistName = view.findViewById(R.id.artis_name_mini_player);
        musicName = view.findViewById(R.id.song_name_mini_player);

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (SHOW_MUSIC_PLAYER){
            if (PATH_TO_FRAG != null) {
                byte[] art = getAlbumArt(PATH_TO_FRAG);
                if (art != null){
                    Glide.with(getContext()).load(art)
                            .into(coverArt);
                }else {
                    Glide.with(getContext()).load(R.drawable.programmity)
                            .into(coverArt);
                }


                artistName.setText(ARTIST_TO_FRAG);
                musicName.setText(SONG_TO_FRAG);

            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (SHOW_MUSIC_PLAYER){
            if (PATH_TO_FRAG != null) {
                byte[] art = getAlbumArt(PATH_TO_FRAG);
                if (art != null){
                    Glide.with(getContext()).load(art)
                            .into(coverArt);
                }else {
                    Glide.with(getContext()).load(R.drawable.programmity)
                            .into(coverArt);
                }


                artistName.setText(ARTIST_TO_FRAG);
                musicName.setText(SONG_TO_FRAG);

            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (SHOW_MUSIC_PLAYER){
            if (PATH_TO_FRAG != null) {
                byte[] art = getAlbumArt(PATH_TO_FRAG);
                if (art != null){
                    Glide.with(getContext()).load(art)
                            .into(coverArt);
                }else {
                    Glide.with(getContext()).load(R.drawable.programmity)
                            .into(coverArt);
                }


                artistName.setText(ARTIST_TO_FRAG);
                musicName.setText(SONG_TO_FRAG);

            }
        }
    }

    private byte[] getAlbumArt(String uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }
}
