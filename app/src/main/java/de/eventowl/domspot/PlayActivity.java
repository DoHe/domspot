package de.eventowl.domspot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Connectivity;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Metadata;
import com.spotify.sdk.android.player.PlaybackState;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;
import com.squareup.picasso.Picasso;

public class PlayActivity extends AppCompatActivity
    implements Player.NotificationCallback, ConnectionStateCallback {

    private static final String CLIENT_ID = "e367fb9ef0654179b3a7e7d575e2a936";
    private static final String TAG = "DomSpotPlay";

    private SpotifyPlayer mPlayer;
    private BroadcastReceiver mNetworkStateReceiver;
    private String mToken;
    private PlaybackState mCurrentPlaybackState;
    private Metadata mMetadata;

    private final Player.OperationCallback mOperationCallback = new Player.OperationCallback() {
        @Override
        public void onSuccess() {
            Log.i(TAG, "OK!");
        }

        @Override
        public void onError(Error error) {
            Log.i(TAG, "ERROR:" + error);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        Intent intent = getIntent();
        mToken = intent.getStringExtra(MainActivity.TOKEN);

        if (mPlayer == null) {
            Config playerConfig = new Config(getApplicationContext(), mToken, CLIENT_ID);
            mPlayer = Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {
                @Override
                public void onInitialized(SpotifyPlayer player) {
                    Log.i(TAG, "-- Player initialized --");
                    mPlayer.setConnectivityStatus(mOperationCallback, getNetworkConnectivity(PlayActivity.this));
                    mPlayer.addNotificationCallback(PlayActivity.this);
                    mPlayer.addConnectionStateCallback(PlayActivity.this);
                    updateView();
                }

                @Override
                public void onError(Throwable error) {
                    Log.i(TAG, "Error in initialization: " + error.getMessage());
                }
            });
        } else {
            mPlayer.login(mToken);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        mNetworkStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (mPlayer != null) {
                    Connectivity connectivity = getNetworkConnectivity(getBaseContext());
                    Log.i(TAG, "Network state changed: " + connectivity.toString());
                    mPlayer.setConnectivityStatus(mOperationCallback, connectivity);
                }
            }
        };

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mNetworkStateReceiver, filter);

        if (mPlayer != null) {
            mPlayer.addNotificationCallback(PlayActivity.this);
            mPlayer.addConnectionStateCallback(PlayActivity.this);
        }
    }

    private Connectivity getNetworkConnectivity(Context context) {
        ConnectivityManager connectivityManager;
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            return Connectivity.fromNetworkType(activeNetwork.getType());
        } else {
            return Connectivity.OFFLINE;
        }
    }

    private void updateView() {
        Metadata metdata = mPlayer.getMetadata();
        Log.i(TAG, "Meta: " + metdata);
        if (metdata != null && metdata.currentTrack != null) {
            ImageView coverView = (ImageView)findViewById(R.id.coverView);
            Picasso.with(this).load(metdata.currentTrack.albumCoverWebUrl).into(coverView);
        }
    }

    @Override
    public void onLoggedIn() {
        Log.i(TAG, "Login complete");
        mPlayer.playUri(mOperationCallback, "spotify:track:6KywfgRqvgvfJc3JRwaZdZ", 0, 0);
        updateView();
    }

    @Override
    public void onLoggedOut() {
        Log.i(TAG, "Logout complete");
        updateView();
    }

    public void onLoginFailed(Error error) {
        Log.i(TAG, "Login error "+ error);
    }

    @Override
    public void onTemporaryError() {
        Log.i(TAG, "Temporary error occurred");
    }

    @Override
    public void onPlaybackEvent(PlayerEvent event) {
        Log.i(TAG, "Event: " + event);
        mCurrentPlaybackState = mPlayer.getPlaybackState();
        mMetadata = mPlayer.getMetadata();
        Log.i(TAG, "Player state: " + mCurrentPlaybackState);
        Log.i(TAG, "Metadata: " + mMetadata);
        updateView();
    }

    @Override
    public void onConnectionMessage(final String message) {
        Log.i(TAG, "Incoming connection message: " + message);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mNetworkStateReceiver);

        if (mPlayer != null) {
            mPlayer.removeNotificationCallback(PlayActivity.this);
            mPlayer.removeConnectionStateCallback(PlayActivity.this);
        }
    }

    @Override
    protected void onDestroy() {
        Spotify.destroyPlayer(this);
        super.onDestroy();
    }

    @Override
    public void onPlaybackError(Error error) {
        Log.i(TAG, "Err: " + error);
    }
}
