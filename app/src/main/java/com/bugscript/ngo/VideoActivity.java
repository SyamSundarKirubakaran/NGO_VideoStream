package com.bugscript.ngo;

import android.annotation.SuppressLint;
import android.app.PictureInPictureParams;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Rational;
import android.view.Display;
import android.view.View;

import com.bugscript.ngo.Services.VideoDownloadService;
import com.bugscript.ngo.Utilities.DownloadUtil;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.offline.ProgressiveDownloadAction;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import static com.bugscript.ngo.Utilities.VideoURI.MP4_URI;

public class VideoActivity extends AppCompatActivity {

    private PlayerView playerView;
    private SimpleExoPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        playerView = findViewById(R.id.player_view);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    protected void onStart() {
        super.onStart();
        player = ExoPlayerFactory.newSimpleInstance(this, new DefaultTrackSelector());
        playerView.setPlayer(player);

        if(isNetworkAvailable()) {
            ProgressiveDownloadAction action = new ProgressiveDownloadAction(MP4_URI, false, null, null);
            VideoDownloadService.startWithAction(VideoActivity.this, VideoDownloadService.class, action, false);
        }

        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(
                this,
                Util.getUserAgent(this, getString(R.string.app_name)));

        CacheDataSourceFactory cacheDataSourceFactory = new CacheDataSourceFactory(
                DownloadUtil.getCache(this),
                dataSourceFactory,
                CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR);

        ExtractorMediaSource mediaSource = new ExtractorMediaSource.Factory(cacheDataSourceFactory)
                .createMediaSource(MP4_URI);
        player.prepare(mediaSource);
        player.setPlayWhenReady(true);
    }

    @Override
    protected void onStop() {
        playerView.setPlayer(null);
        player.release();
        player = null;
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUi();
    }

    @SuppressLint("InlinedApi")
    private void hideSystemUi() {
        playerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            Rational aspectRatio = new Rational(size.x, size.y);
            PictureInPictureParams.Builder mPictureInPicture = null;
            mPictureInPicture = new PictureInPictureParams.Builder();
            mPictureInPicture.setAspectRatio(aspectRatio).build();
            enterPictureInPictureMode(mPictureInPicture.build());
        }
    }
}
