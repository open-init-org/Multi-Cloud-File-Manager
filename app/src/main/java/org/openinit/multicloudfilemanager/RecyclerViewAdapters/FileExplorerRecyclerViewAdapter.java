package org.openinit.multicloudfilemanager.RecyclerViewAdapters;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.graphics.drawable.Drawable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.openinit.multicloudfilemanager.Items.FileItem;
import org.openinit.multicloudfilemanager.Items.RemoteItem;
import org.openinit.multicloudfilemanager.R;
import org.openinit.multicloudfilemanager.Rclone;
import org.openinit.multicloudfilemanager.util.FLog;
import io.github.x0b.safdav.SafAccessProvider;
import io.github.x0b.safdav.file.FileAccessError;

public class FileExplorerRecyclerViewAdapter extends RecyclerView.Adapter<FileExplorerRecyclerViewAdapter.ViewHolder> {

    public static final int VIEW_TYPE_LIST = 0;
    public static final int VIEW_TYPE_GRID = 1;

    private int viewMode = VIEW_TYPE_LIST;
    private static final String TAG = "FileExplorerRVA";
    private List<FileItem> files;
    private View emptyView;
    private View noSearchResultsView;
    private OnClickListener listener;
    private boolean isInSelectMode;
    private List<FileItem> selectedItems;
    private boolean isInMoveMode;
    private boolean isInSearchMode;
    private boolean canSelect;
    private boolean showThumbnails;
    private boolean optionsDisabled;
    private boolean wrapFileNames;
    private Context context;
    private long sizeLimit;

    public interface OnClickListener {
        void onFileClicked(FileItem fileItem);
        void onDirectoryClicked(FileItem fileItem, int position);
        void onFilesSelected();
        void onFileDeselected();
        void onFileOptionsClicked(View view, FileItem fileItem);
        String[] getThumbnailServerParams();
    }

    public void setViewMode(int viewMode) {
        this.viewMode = viewMode;
        notifyDataSetChanged();
    }

    public int getViewMode() {
        return viewMode;
    }

    @Override
    public int getItemViewType(int position) {
        return viewMode;
    }

    public FileExplorerRecyclerViewAdapter(Context context, View emptyView, View noSearchResultsView, OnClickListener listener) {
        files = new ArrayList<>();
        this.context = context;
        this.emptyView = emptyView;
        this.noSearchResultsView = noSearchResultsView;
        this.listener = listener;
        isInSelectMode = false;
        selectedItems = new ArrayList<>();
        isInMoveMode = false;
        isInSearchMode = false;
        canSelect = true;
        wrapFileNames = true;
        optionsDisabled = false;
        sizeLimit = PreferenceManager.getDefaultSharedPreferences(context)
                .getLong(context.getString(R.string.pref_key_thumbnail_size_limit),
                        context.getResources().getInteger(R.integer.default_thumbnail_size_limit));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId = (viewType == VIEW_TYPE_GRID)
                ? R.layout.fragment_file_explorer_grid_item
                : R.layout.fragment_file_explorer_item;
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final FileItem item = files.get(position);

        holder.fileItem = item;
        if (item.isDir()) {
            holder.dirIcon.setVisibility(View.VISIBLE);
            holder.fileIcon.setVisibility(View.GONE);
            holder.fileSize.setVisibility(View.GONE);
            holder.interpunct.setVisibility(View.GONE);
        } else {
            holder.fileIcon.setVisibility(View.VISIBLE);
            holder.dirIcon.setVisibility(View.GONE);
            if (viewMode == VIEW_TYPE_GRID) {
                // In gallery mode, hide file size to keep the bottom bar compact
                holder.fileSize.setVisibility(View.GONE);
                holder.interpunct.setVisibility(View.GONE);
            } else {
                holder.fileSize.setText(item.getHumanReadableSize());
                holder.fileSize.setVisibility(View.VISIBLE);
                holder.interpunct.setVisibility(View.VISIBLE);
            }
        }

        if (holder.thumbnailLoadingProgress != null) {
            holder.thumbnailLoadingProgress.setVisibility(View.GONE);
        }

        boolean shouldShowThumbnail = (showThumbnails || viewMode == VIEW_TYPE_GRID);
        if (shouldShowThumbnail && !item.isDir()) {
            File localFile = getLocalFile(item, context);
            RemoteItem remote = item.getRemote();
            String mimeType = item.getMimeType();
            boolean isMedia = (mimeType != null && (mimeType.startsWith("image/") || mimeType.startsWith("video/"))) || localFile.exists();

            if (isMedia && item.getSize() <= sizeLimit) {
                holder.fileIcon.setImageTintList(null);
                holder.fileIcon.clearColorFilter();
                if (holder.thumbnailLoadingProgress != null) {
                    holder.thumbnailLoadingProgress.setVisibility(View.VISIBLE);
                }

                RequestOptions glideOption = new RequestOptions()
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL);

                RequestListener<Drawable> listenerCallback = new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        if (holder.thumbnailLoadingProgress != null) {
                            holder.thumbnailLoadingProgress.setVisibility(View.GONE);
                        }
                        applyDefaultFileIcon(holder);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        if (holder.thumbnailLoadingProgress != null) {
                            holder.thumbnailLoadingProgress.setVisibility(View.GONE);
                        }
                        return false;
                    }
                };

                if (localFile.exists()) {
                    Glide.with(context)
                            .load(localFile)
                            .apply(glideOption)
                            .listener(listenerCallback)
                            .into(holder.fileIcon);
                } else if (remote != null && remote.getType() == RemoteItem.SAFW) {
                    bindSafFile(holder, item, glideOption, listenerCallback);
                } else if (listener != null) {
                    String[] serverParams = listener.getThumbnailServerParams();
                    if (serverParams != null && serverParams.length >= 2 && serverParams[0] != null && serverParams[1] != null) {
                        String hiddenPath = serverParams[0];
                        int serverPort = Integer.parseInt(serverParams[1]);
                        String itemPath = item.getPath();
                        if (itemPath != null && itemPath.startsWith("/")) {
                            itemPath = itemPath.substring(1);
                        }
                        String url = "http://127.0.0.1:" + serverPort + "/" + hiddenPath + '/' + itemPath;
                        Glide.with(context)
                                .load(url)
                                .apply(glideOption)
                                .listener(listenerCallback)
                                .into(holder.fileIcon);
                    } else {
                        if (holder.thumbnailLoadingProgress != null) {
                            holder.thumbnailLoadingProgress.setVisibility(View.GONE);
                        }
                        applyDefaultFileIcon(holder);
                    }
                } else {
                    if (holder.thumbnailLoadingProgress != null) {
                        holder.thumbnailLoadingProgress.setVisibility(View.GONE);
                    }
                    applyDefaultFileIcon(holder);
                }

            } else {
                applyDefaultFileIcon(holder);
            }
        } else {
            applyDefaultFileIcon(holder);
        }

        RemoteItem itemRemote = item.getRemote();
        if (!itemRemote.isDirectoryModifiedTimeSupported() && item.isDir()) {
            holder.fileModTime.setVisibility(View.GONE);
        } else {
            holder.fileModTime.setVisibility(View.VISIBLE);
            holder.fileModTime.setText(item.getHumanReadableModTime());
        }
        
        holder.fileName.setText(item.getName());

        if (isInSelectMode) {
            if (selectedItems.contains(item)) {
                holder.view.setBackgroundColor(getSelectionBackgroundColor());
            } else {
                holder.view.setBackgroundColor(Color.TRANSPARENT);
            }
        } else {
            holder.view.setBackgroundColor(Color.TRANSPARENT);
        }

        if (isInMoveMode) {
            if (item.isDir()) {
                holder.view.setAlpha(1f);
            } else {
                holder.view.setAlpha(.5f);
            }
        } else if (holder.view.getAlpha() == .5f) {
            holder.view.setAlpha(1f);
        }

        if ((isInSelectMode || isInMoveMode) && !optionsDisabled) {
            holder.fileOptions.setVisibility(View.INVISIBLE);
        } else if (optionsDisabled) {
            holder.fileOptions.setVisibility(View.GONE);
        } else {
            holder.fileOptions.setVisibility(View.VISIBLE);
            holder.fileOptions.setOnClickListener(v -> listener.onFileOptionsClicked(v, item));
        }

        if (wrapFileNames) {
            holder.fileName.setEllipsize(TextUtils.TruncateAt.MIDDLE);
            holder.fileName.setSingleLine(true);
        } else {
            holder.fileName.setEllipsize(null);
            holder.fileName.setSingleLine(false);
        }

        holder.view.setOnClickListener(view -> {
            if (isInSelectMode) {
                onLongClickAction(item, holder);
            } else {
                onClickAction(item, holder.getAdapterPosition());
            }
        });

        holder.view.setOnLongClickListener(view -> {
            if (!isInMoveMode && canSelect) {
                onLongClickAction(item, holder);
            }
            return true;
        });

        if (viewMode == VIEW_TYPE_LIST && holder.icons != null) {
            holder.icons.setOnClickListener(v -> {
                if (!isInMoveMode && canSelect) {
                    onLongClickAction(item, holder);
                }
            });
        } else if (holder.icons != null) {
            holder.icons.setOnClickListener(null);
            holder.icons.setClickable(false);
        }
    }

    private void bindSafFile(@NonNull ViewHolder holder, FileItem item, RequestOptions glideOption, RequestListener<Drawable> listenerCallback) {
        try {
            String itemPath = item.getPath();
            if (itemPath != null && itemPath.startsWith("/")) {
                itemPath = itemPath.substring(1);
            }
            Uri contentUri = SafAccessProvider.getDirectServer(context).getDocumentUri('/' + itemPath);
            Glide
                    .with(context)
                    .load(contentUri)
                    .apply(glideOption)
                    .listener(listenerCallback)
                    .into(holder.fileIcon);
        } catch (FileAccessError e) {
            FLog.e(TAG, "onBindViewHolder: SAF error", e);
            if (holder.thumbnailLoadingProgress != null) {
                holder.thumbnailLoadingProgress.setVisibility(View.GONE);
            }
            applyDefaultFileIcon(holder);
        }
    }

    private void applyDefaultFileIcon(@NonNull ViewHolder holder) {
        holder.fileIcon.setImageResource(R.drawable.ic_file);
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnSurface, typedValue, true);
        holder.fileIcon.setColorFilter(typedValue.data);
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

    @Override
    public int getItemCount() {
        if (files == null) {
            return 0;
        } else {
            return files.size();
        }
    }

    public void disableFileOptions() {
        optionsDisabled = true;
    }

    public void showThumbnails(boolean showThumbnails) {
        this.showThumbnails = showThumbnails;
    }

    public List<FileItem> getCurrentContent() {
        return new ArrayList<>(files);
    }

    public void clear() {
        if (files == null) {
            return;
        }
        int count = files.size();
        files.clear();
        isInSelectMode = false;
        if (!selectedItems.isEmpty()) {
            selectedItems.clear();
            listener.onFileDeselected();
        }
        notifyItemRangeRemoved(0, count);
    }

    public void newData(List<FileItem> data) {
        this.clear();
        files = new ArrayList<>(data);
        isInSelectMode = false;
        if (files.isEmpty()) {
            showEmptyState(true);
        } else {
            showEmptyState(false);
        }
        if (isInMoveMode) {
            notifyDataSetChanged();
        } else {
            notifyItemRangeInserted(0, files.size());
        }
    }

    public void updateData(List<FileItem> data) {
        if (data.isEmpty()) {
            int count = files.size();
            files.clear();
            notifyItemRangeRemoved(0, count);
            showEmptyState(true);
            return;
        }
        showEmptyState(false);
        List<FileItem> newData = new ArrayList<>(data);
        List<FileItem> diff = new ArrayList<>(files);

        diff.removeAll(newData);
        for (FileItem fileItem : diff) {
            int index = files.indexOf(fileItem);
            files.remove(index);
            if (selectedItems.contains(fileItem)) {
                selectedItems.remove(fileItem);
                isInSelectMode = !selectedItems.isEmpty();
                listener.onFileDeselected();
            }
            notifyItemRemoved(index);
        }

        diff = new ArrayList<>(data);
        diff.removeAll(files);
        for (FileItem fileItem : diff) {
            int index = newData.indexOf(fileItem);
            files.add(index, fileItem);
            notifyItemInserted(index);
        }
    }

    public void updateSortedData(List<FileItem> data) {
        if (files != null) {
            int count = files.size();
            files.clear();
            notifyItemRangeRemoved(0, count);
        }
        files = new ArrayList<>(data);
        if (files.isEmpty()) {
            showEmptyState(true);
        } else {
            showEmptyState(false);
        }
        if (isInMoveMode) {
            notifyDataSetChanged();
        } else {
            notifyItemRangeInserted(0, files.size());
        }
    }

    public void refreshData() {
        notifyDataSetChanged();
    }

    public void setMoveMode(Boolean mode) {
        isInMoveMode = mode;
    }

    public void setSearchMode(Boolean mode) {
        isInSearchMode = mode;
    }

    public void setSelectedItems(List<FileItem> selectedItems) {
        this.selectedItems = new ArrayList<>(selectedItems);
        this.isInSelectMode = true;
        notifyDataSetChanged();
    }

    public Boolean isInSelectMode() {
        return isInSelectMode;
    }

    public List<FileItem> getSelectedItems() {
        return new ArrayList<>(selectedItems);
    }

    public int getNumberOfSelectedItems() {
        return selectedItems.size();
    }

    public Boolean isInMoveMode() {
        return isInMoveMode;
    }

    public void setWrapFileNames(boolean wrapFileNames) {
        this.wrapFileNames = wrapFileNames;
        refreshData();
    }

    private void showEmptyState(Boolean show) {
        if (isInSearchMode) {
            if (show) {
                noSearchResultsView.setVisibility(View.VISIBLE);
            } else {
                noSearchResultsView.setVisibility(View.INVISIBLE);
            }
        } else {
            if (show) {
                emptyView.setVisibility(View.VISIBLE);
            } else {
                emptyView.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void onClickAction(FileItem item, int position) {
        if (item.isDir() && null != listener) {
            listener.onDirectoryClicked(item, position);
        } else if (!item.isDir() && !isInMoveMode && null != listener) {
            listener.onFileClicked(item);
        }
    }

    public void toggleSelectAll() {
        if (null == files) {
            return;
        }
        if (selectedItems.size() == files.size()) {
            isInSelectMode = false;
            selectedItems.clear();
            listener.onFileDeselected();
        } else {
            isInSelectMode = true;
            selectedItems.clear();
            selectedItems.addAll(files);
            listener.onFilesSelected();
        }
        notifyDataSetChanged();
    }

    public void selectAll() {
        if (files != null) {
            isInSelectMode = true;
            selectedItems.clear();
            selectedItems.addAll(files);
            listener.onFilesSelected();
        }
        notifyDataSetChanged();
    }

    public void cancelSelection() {
        isInSelectMode = false;
        selectedItems.clear();
        listener.onFileDeselected();
        notifyDataSetChanged();
    }

    public void setCanSelect(Boolean canSelect) {
        this.canSelect = canSelect;
    }

    private int getSelectionBackgroundColor() {
        return context.getColor(R.color.selectedItem);
    }

    private void onLongClickAction(FileItem item, ViewHolder holder) {
        if (selectedItems.contains(item)) {
            selectedItems.remove(item);
            holder.view.setBackgroundColor(Color.TRANSPARENT);
            if (selectedItems.size() == 0) {
                isInSelectMode = false;
                listener.onFileDeselected();
            }
            listener.onFileDeselected();
        } else {
            selectedItems.add(item);
            isInSelectMode = true;
            holder.view.setBackgroundColor(getSelectionBackgroundColor());
            listener.onFilesSelected();
        }
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public final View view;
        public final View icons;
        public final ImageView fileIcon;
        public final ImageView dirIcon;
        public final TextView fileName;
        public final TextView fileModTime;
        public final TextView fileSize;
        public final TextView interpunct;
        public final ImageButton fileOptions;
        public final ProgressBar thumbnailLoadingProgress;
        public FileItem fileItem;

        ViewHolder(View itemView) {
            super(itemView);
            this.view = itemView;
            this.icons = view.findViewById(R.id.icons);
            this.fileIcon = view.findViewById(R.id.file_icon);
            this.dirIcon = view.findViewById(R.id.dir_icon);
            this.fileName = view.findViewById(R.id.file_name);
            this.fileModTime = view.findViewById(R.id.file_modtime);
            this.fileSize = view.findViewById(R.id.file_size);
            this.fileOptions = view.findViewById(R.id.file_options);
            this.interpunct = view.findViewById(R.id.interpunct);
            this.thumbnailLoadingProgress = view.findViewById(R.id.thumbnail_loading_progress);
        }
    }
}
