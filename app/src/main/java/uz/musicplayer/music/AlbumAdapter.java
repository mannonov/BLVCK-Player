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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.ArrayList;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.MyViewHolder> {
    private Context mContext;
    private ArrayList<MusicFiles> musicFiles;
    View view;

    AlbumAdapter(Context mContext, ArrayList<MusicFiles> musicFiles) {
        this.mContext = mContext;
        this.musicFiles = musicFiles;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(mContext).inflate(R.layout.album_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Size size = new Size(200, 200);
            try {
                Uri contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        Long.parseLong(musicFiles.get(position).getId()));
                Bitmap albumArt = mContext.getContentResolver().loadThumbnail(contentUri, size, null);
                if (albumArt != null) {
                    Glide.with(mContext).asBitmap()
                            .load(albumArt)
                            .into(holder.album_image);
                } else {
                    Glide.with(mContext).asBitmap()
                            .load(R.drawable.programmity).into(holder.album_image);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            byte[] image = getAlbumToAdapter(musicFiles.get(position).getPath());
            if (image != null) {
                Glide.with(mContext).asBitmap()
                        .load(image)
                        .into(holder.album_image);
            } else {
                Glide.with(mContext).asBitmap()
                        .load(R.drawable.programmity).into(holder.album_image);
            }
        }
        holder.album_name.setText(musicFiles.get(position).getAlbum());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, AlbumDetails.class);
                intent.putExtra("albumId", musicFiles.get(position).getAlbumid());
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return musicFiles.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView album_image;
        TextView album_name;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            album_image = itemView.findViewById(R.id.album_image);
            album_name = itemView.findViewById(R.id.album_name);
        }
    }

    private byte[] getAlbumToAdapter(String uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }
}
