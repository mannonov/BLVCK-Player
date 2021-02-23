package uz.jaxadevgroup.blvckplayer;

import android.Manifest;
import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;


import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
    static ArrayList<MusicFiles> musicFiles = new ArrayList<>();
    static ArrayList<MusicFiles> albumFiles = new ArrayList<>();
    public static final int REQUET_PERMISSION = 1;
    static boolean shuffleBoolean = false, repeatBoolean = false;
    static SearchView searchView;
    String MY_PREFS_THEME = "THEME";
    String MY_PREFS_NAME = "SortOrder";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme();
        setContentView(R.layout.activity_main);
        permission();

        //  retrieve();
//        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
//        fragmentTransaction.add(R.id.frag_player, new NowPlayingBottomFragment());
//        fragmentTransaction.commit();
    }

    private void permission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUET_PERMISSION);
        } else {
            musicFiles = getAllAudioFromDevice(MainActivity.this);
            initViewPager();
        }
    }

    public ArrayList<MusicFiles> getAllAudioFromDevice(final Context context) {

        String MY_PREFS_NAME = "SortOrder";
        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        String order = prefs.getString("sorting", "sortByName");
        final ArrayList<MusicFiles> tempAudioList = new ArrayList<>();
        ArrayList<String> duplicates = new ArrayList<>();
        albumFiles.clear();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String sortOrder = null;
        switch (order) {
            case "sortByName":
                sortOrder = MediaStore.MediaColumns.DISPLAY_NAME + " ASC";
                break;
            case "sortByDate":
                sortOrder = MediaStore.MediaColumns.DATE_ADDED + " ASC";
                break;
            case "sortBySize":
                sortOrder = MediaStore.MediaColumns.SIZE + " DESC";
                break;
        }
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.COMPOSER,
                MediaStore.Audio.Media.TRACK,
                MediaStore.Audio.Media.YEAR,
                MediaStore.Audio.Media.DATE_ADDED,
                MediaStore.Audio.Media.DATE_MODIFIED,
                MediaStore.Audio.Media.ALBUM_ID
        };
        Cursor c = context.getContentResolver().query(uri, projection, null, null, sortOrder);
        if (c != null) {
            int index = 0;
            while (c.moveToNext()) {

                MusicFiles audioModel = new MusicFiles();
                String id = c.getString(0);
                String artist = c.getString(1);
                String title = c.getString(2);
                String path = c.getString(3);
                String displayname = c.getString(4);
                String duration = c.getString(5);
                String album = c.getString(6);
                String size = c.getString(7);
                String composer = c.getString(8);
                String track = c.getString(9);
                String year = c.getString(10);
                String dateadded = c.getString(11);
                String datemodified = c.getString(12);
                String albumid = c.getString(13);
                Uri contentUri = ContentUris.withAppendedId(
                        uri, Long.parseLong(id));

                audioModel.setId(id);
                audioModel.setArtist(artist);
                audioModel.setTitle(title);
                audioModel.setPath(path);
                audioModel.setFilename(displayname);
                audioModel.setDuration(duration);
                audioModel.setAlbum(album);
                audioModel.setSize(size);
                audioModel.setComposer(composer);
                audioModel.setTrack(track);
                audioModel.setYear(year);
                audioModel.setDateAdded(dateadded);
                audioModel.setLastModified(datemodified);
                audioModel.setAlbumid(albumid);
                if (!duplicates.contains(album)) {
                    albumFiles.add(index, audioModel);
                    duplicates.add(album);
                    index++;
                }
                tempAudioList.add(audioModel);
            }
            c.close();
        }

        return tempAudioList;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUET_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                musicFiles = getAllAudioFromDevice(MainActivity.this);
                initViewPager();
            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUET_PERMISSION);
            }
        }
    }

    private void initViewPager() {
        ViewPager viewPager = findViewById(R.id.viewpager);
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        //   tabLayout.setSelectedTabIndicator(R.drawable.tab_background);

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragments(new SongsFragment(), "Songs");
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    public static class ViewPagerAdapter extends FragmentPagerAdapter {
        private ArrayList<Fragment> fragments;
        private ArrayList<String> titles;

        ViewPagerAdapter(@NonNull FragmentManager fm) {
            super(fm);
            this.fragments = new ArrayList<>();
            this.titles = new ArrayList<>();

        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        void addFragments(Fragment fragment, String title) {
            fragments.add(fragment);
            titles.add(title);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem menuItem = menu.findItem(R.id.search);
        searchView = (SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(this);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        String userInput = newText.toLowerCase();
        ArrayList<MusicFiles> myFiles = new ArrayList<>();
        for (MusicFiles song : musicFiles) {
            if (song.getTitle().toLowerCase().contains(userInput)) {
                myFiles.add(song);
            }
        }
        SongsFragment.musicAdapter.updateList(myFiles);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        SharedPreferences.Editor editorTheme = getSharedPreferences(MY_PREFS_THEME, MODE_PRIVATE).edit();
        switch (item.getItemId()) {
            case R.id.sortByName:
                editor.putString("sorting", "sortByName");
                editor.apply();
                this.recreate();
                Toast.makeText(MainActivity.this, "Name Sorted Clicked!!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.sortByDate:
                editor.putString("sorting", "sortByDate");
                editor.apply();
                this.recreate();
                Toast.makeText(MainActivity.this, "Date Sorted Clicked!!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.sortBySize:
                editor.putString("sorting", "sortBySize");
                editor.apply();
                this.recreate();
                Toast.makeText(MainActivity.this, "Size Sorted Clicked!!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.lightTheme:
                editorTheme.putString("theme", "light");
                editorTheme.apply();
                this.recreate();
                Toast.makeText(MainActivity.this, "Light Clicked!!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.darkTheme:
                editorTheme.putString("theme", "dark");
                editorTheme.apply();
                this.recreate();
                Toast.makeText(MainActivity.this, "Dark Clicked!!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.coolTheme:
                editorTheme.putString("theme", "cool");
                editorTheme.apply();
                this.recreate();
                Toast.makeText(MainActivity.this, "Cool Clicked!!", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setTheme() {
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
