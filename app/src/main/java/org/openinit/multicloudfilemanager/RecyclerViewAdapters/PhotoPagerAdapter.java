package org.openinit.multicloudfilemanager.RecyclerViewAdapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.github.chrisbanes.photoview.PhotoView;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.openinit.multicloudfilemanager.Items.FileItem;
import org.openinit.multicloudfilemanager.Items.RemoteItem;
import org.openinit.multicloudfilemanager.R;
import org.openinit.multicloudfilemanager.Rclone;

import io.github.x0b.safdav.SafAccessProvider;
import io.github.x0b.safdav.file.FileAccessError;

public class PhotoPagerAdapter extends RecyclerView.Adapter<PhotoPagerAdapter.PhotoViewHolder> {

    private final Context context;
    private final List<FileItem> photoList;
    private final String thumbnailServerAuth;
    private final int thumbnailServerPort;
    private OnPhotoClickListener photoClickListener;

    public interface OnPhotoClickListener {
        void onPhotoTap();
    }

    public PhotoPagerAdapter(Context context, List<FileItem> photoList, String thumbnailServerAuth, int thumbnailServerPort) {
        this.context = context;
        this.photoList = photoList;
        this.thumbnailServerAuth = thumbnailServerAuth;
        this.thumbnailServerPort = thumbnailServerPort;
    }

    public void setOnPhotoClickListener(OnPhotoClickListener listener) {
        this.photoClickListener = listener;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_photo_view, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        FileItem item = photoList.get(position);
        holder.progressBar.setVisibility(View.VISIBLE);

        String itemPath = item.getPath();
        if (itemPath != null && itemPath.startsWith("/")) {
            itemPath = itemPath.substring(1);
        }

        RemoteItem remote = item.getRemote();
        if (remote != null && remote.getType() == RemoteItem.LOCAL) {
            File localFile = getLocalFile(item, context);
            Glide.with(context)
                    .load(localFile)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            holder.progressBar.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            holder.progressBar.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(holder.photoView);
        } else if (remote != null && remote.getType() == RemoteItem.SAFW) {
            try {
                Uri uri = SafAccessProvider.getDirectServer(context).getDocumentUri('/' + itemPath);
                Glide.with(context)
                        .load(uri)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                holder.progressBar.setVisibility(View.GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                holder.progressBar.setVisibility(View.GONE);
                                return false;
                            }
                        })
                        .into(holder.photoView);
            } catch (FileAccessError e) {
                holder.progressBar.setVisibility(View.GONE);
                holder.photoView.setImageResource(R.drawable.ic_file);
            }
        } else if (thumbnailServerAuth != null && thumbnailServerPort > 0 && remote != null) {
            String url = "http://127.0.0.1:" + thumbnailServerPort + "/" + thumbnailServerAuth + '/' + remote.getName() + '/' + itemPath;
            Glide.with(context)
                    .load(url)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            holder.progressBar.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            holder.progressBar.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(holder.photoView);
        } else {
            holder.progressBar.setVisibility(View.GONE);
            holder.photoView.setImageResource(R.drawable.ic_file);
        }

        holder.photoView.setOnViewTapListener((view, x, y) -> {
            if (photoClickListener != null) {
                photoClickListener.onPhotoTap();
            }
        });
    }

    @Override
    public void onViewRecycled(@NonNull PhotoViewHolder holder) {
        super.onViewRecycled(holder);
        Glide.with(context).clear(holder.photoView);
    }

    private File getLocalFile(FileItem item, Context context) {
        String itemPath = item.getPath();
        if (itemPath == null) {
            return new File("");
        }

        // 1. Direct file path check
        File directFile = new File(item.getPath());
        if (directFile.exists()) {
            return directFile;
        }

        String cleanPath = itemPath.startsWith("/") ? itemPath.substring(1) : itemPath;

        // 2. Environment.getExternalStorageDirectory() (/storage/emulated/0)
        File extStorage = android.os.Environment.getExternalStorageDirectory();
        if (extStorage != null) {
            File extFile = new File(extStorage, cleanPath);
            if (extFile.exists()) {
                return extFile;
            }
        }

        // 3. Rclone local remote prefix
        RemoteItem remote = item.getRemote();
        if (remote != null) {
            String prefix = Rclone.getLocalRemotePathPrefix(remote, context);
            if (prefix != null && !prefix.isEmpty()) {
                String fullPath = prefix.endsWith("/") ? prefix + cleanPath : prefix + "/" + cleanPath;
                File f = new File(fullPath);
                if (f.exists()) {
                    return f;
                }
            }
        }
        return directFile;
    }

    @Override
    public int getItemCount() {
        return photoList != null ? photoList.size() : 0;
    }

    private static class PersistentGlideUrl extends GlideUrl {

        public PersistentGlideUrl(String url) {
            super(url);
        }

        @Override
        public String getCacheKey() {
            try {
                URL url = super.toURL();
                String path = url.getPath();
                return path.substring(path.indexOf('/', 1));
            } catch (MalformedURLException e) {
                return super.getCacheKey();
            }
        }
    }

    static class PhotoViewHolder extends RecyclerView.ViewHolder {
        PhotoView photoView;
        ProgressBar progressBar;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            photoView = itemView.findViewById(R.id.photo_view);
            progressBar = itemView.findViewById(R.id.photo_loading_progress);
        }
    }
}
