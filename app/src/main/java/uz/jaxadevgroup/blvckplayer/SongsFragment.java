package uz.jaxadevgroup.blvckplayer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;




/**
 * A simple {@link Fragment} subclass.
 */
public class SongsFragment extends Fragment {

    RecyclerView recyclerView;
    static MusicAdapter musicAdapter;
     View view;
    // private ArrayList<MusicFiles> musicFiles = new ArrayList<>();
    public SongsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_songs, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
       // musicFiles = getAllAudioFromDevice(getContext());
        if (MainActivity.musicFiles != null && !(MainActivity.musicFiles.size() < 1))
        {
            musicAdapter = new MusicAdapter(getContext(), MainActivity.musicFiles);
            recyclerView.setAdapter(musicAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext(),
                    RecyclerView.VERTICAL, false));
        }
        return view;
    }

    //    private void retrieve() {
//        musicFiles.clear();
//        DBAdapter dbAdapter = new DBAdapter(getContext());
//        dbAdapter.openDB();
//        //retrieving.
//        Cursor cursor = dbAdapter.getAllPdf();
//        //loop and add data to the arraylist..
//        try {
//            while (cursor.moveToNext())
//            {
//                int id = cursor.getInt(0);
//                String name = cursor.getString(1);
//                String position = cursor.getString(2);
//
//                MusicFiles pdfFile = new MusicFiles();
//                musicFiles.add(pdfFile);
//            }
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace();
//        }
//        finally {
//            cursor.close();
//        }
//        dbAdapter.closeDB();
//        //check if array list is empty
//        if (!(musicFiles.size() < 1))
//        {
//            musicAdapter = new MusicAdapter(getContext(), musicFiles);
//            recyclerView.setAdapter(musicAdapter);
//            recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
//        }
//    }

}
