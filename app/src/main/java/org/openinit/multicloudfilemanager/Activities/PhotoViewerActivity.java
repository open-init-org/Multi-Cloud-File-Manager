package org.openinit.multicloudfilemanager.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

import org.openinit.multicloudfilemanager.Dialogs.FilePropertiesDialog;
import org.openinit.multicloudfilemanager.Items.FileItem;
import org.openinit.multicloudfilemanager.Items.RemoteItem;
import org.openinit.multicloudfilemanager.R;
import org.openinit.multicloudfilemanager.RecyclerViewAdapters.PhotoPagerAdapter;
import es.dmoral.toasty.Toasty;

public class PhotoViewerActivity extends AppCompatActivity {

    public static final String EXTRA_PHOTOS = "org.openinit.multicloudfilemanager.PhotoViewerActivity.EXTRA_PHOTOS";
    public static final String EXTRA_INITIAL_POSITION = "org.openinit.multicloudfilemanager.PhotoViewerActivity.EXTRA_INITIAL_POSITION";
    public static final String EXTRA_THUMBNAIL_AUTH = "org.openinit.multicloudfilemanager.PhotoViewerActivity.EXTRA_THUMBNAIL_AUTH";
    public static final String EXTRA_THUMBNAIL_PORT = "org.openinit.multicloudfilemanager.PhotoViewerActivity.EXTRA_THUMBNAIL_PORT";

    private ViewPager2 viewPager;
    private Toolbar toolbar;
    private View topOverlay;
    private View bottomOverlay;
    private List<FileItem> photoList;
    private int currentPosition;
    private String thumbnailServerAuth;
    private int thumbnailServerPort;
    private boolean isOverlayVisible = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_viewer);

        if (getIntent() != null) {
            photoList = getIntent().getParcelableArrayListExtra(EXTRA_PHOTOS);
            currentPosition = getIntent().getIntExtra(EXTRA_INITIAL_POSITION, 0);
            thumbnailServerAuth = getIntent().getStringExtra(EXTRA_THUMBNAIL_AUTH);
            thumbnailServerPort = getIntent().getIntExtra(EXTRA_THUMBNAIL_PORT, 0);
        }

        if (photoList == null || photoList.isEmpty()) {
            finish();
            return;
        }

        topOverlay = findViewById(R.id.top_overlay);
        bottomOverlay = findViewById(R.id.bottom_overlay);
        toolbar = findViewById(R.id.toolbar);
        viewPager = findViewById(R.id.view_pager);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        PhotoPagerAdapter adapter = new PhotoPagerAdapter(this, photoList, thumbnailServerAuth, thumbnailServerPort);
        adapter.setOnPhotoClickListener(this::toggleOverlay);
        viewPager.setAdapter(adapter);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentPosition = position;
                updateTitle();
            }
        });

        if (currentPosition >= 0 && currentPosition < photoList.size()) {
            viewPager.setCurrentItem(currentPosition, false);
            updateTitle();
        }

        setupActionButtons();
    }

    private void updateTitle() {
        if (photoList == null || photoList.isEmpty() || currentPosition < 0 || currentPosition >= photoList.size()) {
            return;
        }
        FileItem currentItem = photoList.get(currentPosition);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(currentItem.getName());
            getSupportActionBar().setSubtitle(getString(R.string.photo_counter, currentPosition + 1, photoList.size()));
        }
    }

    private void toggleOverlay() {
        isOverlayVisible = !isOverlayVisible;
        if (isOverlayVisible) {
            topOverlay.setVisibility(View.VISIBLE);
            bottomOverlay.setVisibility(View.VISIBLE);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            topOverlay.setVisibility(View.GONE);
            bottomOverlay.setVisibility(View.GONE);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    private void setupActionButtons() {
        findViewById(R.id.btn_download).setOnClickListener(v -> downloadCurrentPhoto());
        findViewById(R.id.btn_share).setOnClickListener(v -> shareCurrentPhoto());
        findViewById(R.id.btn_info).setOnClickListener(v -> showPhotoInfo());
        findViewById(R.id.btn_delete).setOnClickListener(v -> deleteCurrentPhoto());
    }

    private FileItem getCurrentItem() {
        if (photoList != null && currentPosition >= 0 && currentPosition < photoList.size()) {
            return photoList.get(currentPosition);
        }
        return null;
    }

    private void downloadCurrentPhoto() {
        FileItem item = getCurrentItem();
        if (item == null) return;
        Toasty.info(this, getString(R.string.download) + ": " + item.getName(), Toast.LENGTH_SHORT, true).show();
    }

    private void shareCurrentPhoto() {
        FileItem item = getCurrentItem();
        if (item == null) return;
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType(item.getMimeType());
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, item.getName());
        shareIntent.putExtra(Intent.EXTRA_TEXT, item.getName());
        startActivity(Intent.createChooser(shareIntent, getString(R.string.link)));
    }

    private void showPhotoInfo() {
        FileItem item = getCurrentItem();
        if (item == null) return;

        FilePropertiesDialog filePropertiesDialog = new FilePropertiesDialog()
                .setFile(item)
                .setRemote(item.getRemote());
        filePropertiesDialog.withHashCalculations(false);
        filePropertiesDialog.show(getSupportFragmentManager(), "file properties");
    }

    private void deleteCurrentPhoto() {
        FileItem item = getCurrentItem();
        if (item == null) return;

        new MaterialAlertDialogBuilder(this, R.style.RoundedCornersDialog)
                .setTitle(R.string.delete)
                .setMessage(item.getName())
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    String fullPath = item.getPath();
                    String dirPath = fullPath;
                    if (dirPath != null && dirPath.contains("/")) {
                        dirPath = dirPath.substring(0, dirPath.lastIndexOf('/'));
                    } else {
                        dirPath = "//" + item.getRemote().getName();
                    }
                    if (!dirPath.startsWith("//")) {
                        dirPath = "//" + item.getRemote().getName() + "/" + dirPath;
                    }
                    org.openinit.multicloudfilemanager.workmanager.EphemeralTaskManager.Companion.queueDelete(
                            this, item.getRemote(), item, dirPath
                    );
                    Toasty.info(this, getString(R.string.delete) + ": " + item.getName(), Toast.LENGTH_SHORT, true).show();

                    photoList.remove(currentPosition);
                    if (photoList.isEmpty()) {
                        finish();
                    } else {
                        if (currentPosition >= photoList.size()) {
                            currentPosition = photoList.size() - 1;
                        }
                        if (viewPager.getAdapter() != null) {
                            viewPager.getAdapter().notifyDataSetChanged();
                        }
                        updateTitle();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
