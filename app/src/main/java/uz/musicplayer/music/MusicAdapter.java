package uz.musicplayer.music;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MyViewHolder> {

    private Context mContext;
    static ArrayList<MusicFiles> mFiles;

    MusicAdapter(Context mContext, ArrayList<MusicFiles> mFiles) {
        this.mContext = mContext;
        this.mFiles = mFiles;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.music_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        if (mFiles.get(position).getTitle() != null && mFiles.get(position).getArtist() != null ){
            holder.file_name.setText(mFiles.get(position).getTitle());
            holder.artist_name.setText(mFiles.get(position).getArtist());
        }
        //byte[] image = getAlbumToAdapter(Uri.parse(mFiles.get(position).getPath()));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Size size = new Size(200, 200);
            try {
                Uri contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        Long.parseLong(mFiles.get(position).getId()));
                Bitmap albumArt = mContext.getContentResolver().loadThumbnail(contentUri, size, null);
                if (albumArt != null) {
                    Glide.with(mContext).asBitmap()
                            .load(albumArt)
                            .into(holder.img_icon);
                } else {
                    Glide.with(mContext).asBitmap()
                            .load(R.drawable.programmity).into(holder.img_icon);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            byte[] image = getAlbumToAdapter(Uri.parse(mFiles.get(position).getPath()));
            if (image != null) {
                Glide.with(mContext).asBitmap()
                        .load(image)
                        .into(holder.img_icon);
            } else {
                Glide.with(mContext).asBitmap()
                        .load(R.drawable.programmity).into(holder.img_icon);
            }
        }
        //this is end of above commented line about part..

        holder.music_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, PlayerActivity.class);
                //  now sending position of item to retrieve the path of that file to open in the new activity..
                intent.putExtra("position", position);
                intent.putExtra("title", mFiles.get(position).getTitle());
                intent.putExtra("path", mFiles.get(position).getPath());
                mContext.startActivity(intent);
            }
        });
        holder.menuMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                PopupMenu popupMenu = new PopupMenu(mContext, v);
                popupMenu.getMenuInflater().inflate(R.menu.popup, popupMenu.getMenu());
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.delete:
                                Toast.makeText(mContext, "Delete Clicked!!", Toast.LENGTH_SHORT).show();
                                delete(position, v);
                                break;
                        }
                        return true;
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return mFiles.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView file_name,artist_name;
        ImageView img_icon, menuMore;
        RelativeLayout music_item;

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            file_name = itemView.findViewById(R.id.music_file_name);
            img_icon = itemView.findViewById(R.id.img_music);
            music_item = itemView.findViewById(R.id.music_item);
            menuMore = itemView.findViewById(R.id.menuMore);
            artist_name = itemView.findViewById(R.id.music_artist_name);
        }
    }

    private byte[] getAlbumToAdapter(Uri uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri.toString());
        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }

    public void delete(final int position, View view) {
        Uri contentUri = ContentUris.withAppendedId(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, Long.parseLong(mFiles.get(position).getId()));
        String filePath = mFiles.get(position).getPath();
        File file = new File(filePath);
        boolean deleted = file.delete();
        if (deleted) {
            mContext.getContentResolver().delete(contentUri, null, null);
            mFiles.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, mFiles.size());
            Snackbar.make(view, "File Deleted : ", Snackbar.LENGTH_LONG)
                    .show();
        }
        else
        {
            Snackbar.make(view, "Can't Delete File : ", Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    void updateList(ArrayList<MusicFiles> musicFiles)
    {
        mFiles = new ArrayList<>();
        mFiles.addAll(musicFiles);
        notifyDataSetChanged();
    }
}
