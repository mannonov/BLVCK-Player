package uz.jaxadevgroup.blvckplayer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;




/**
 * A simple {@link Fragment} subclass.
 */
public class NowPlayingBottomFragment extends Fragment {

    public NowPlayingBottomFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_now_playing_bottom, container, false);
    }
}
