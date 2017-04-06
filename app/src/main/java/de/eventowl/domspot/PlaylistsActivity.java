package de.eventowl.domspot;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.Collections;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyCallback;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import retrofit.client.Response;

public class PlaylistsActivity extends AppCompatActivity {
    private static final String TAG = "DomSpotPlaylists";

    ArrayAdapter mAdapter;
    ListView mListView;
    ArrayList<String> mPlaylistNames = new ArrayList();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlists);
        mListView = (ListView)findViewById(R.id.list);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "Playlist: " + mAdapter.getItem(position).toString());
            }
        });
        ProgressBar progressBar = new ProgressBar(this);
        progressBar.setIndeterminate(true);
        mListView.setEmptyView(progressBar);

        ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
        root.addView(progressBar);

        mAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, mPlaylistNames);
        mListView.setAdapter(mAdapter);

        Intent intent = getIntent();
        String token = intent.getStringExtra(MainActivity.TOKEN);

        SpotifyApi api = new SpotifyApi();
        api.setAccessToken(token);
        SpotifyService spotify = api.getService();

        spotify.getMyPlaylists(new SpotifyCallback<Pager<PlaylistSimple>>() {
            @Override
            public void success(Pager<PlaylistSimple> playlistPager, Response response) {
                for (int i=0; i<playlistPager.items.size(); i++) {
                    Log.i(TAG, playlistPager.items.get(i).name);
                    mPlaylistNames.add(playlistPager.items.get(i).name);
                    mAdapter.notifyDataSetChanged();
                }

            }

            @Override
            public void failure(SpotifyError error) {
                Log.i(TAG, error.toString());
                Log.i(TAG, "Failure :(");
            }
        });

    }
}
