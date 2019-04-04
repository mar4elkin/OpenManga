package org.nv95.openmanga.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.nv95.openmanga.R;
import org.nv95.openmanga.adapters.NewChaptersAdapter;
import org.nv95.openmanga.items.MangaInfo;
import org.nv95.openmanga.lists.MangaList;
import org.nv95.openmanga.providers.FavouritesProvider;
import org.nv95.openmanga.providers.NewChaptersProvider;
import org.nv95.openmanga.utils.AnimUtils;
import org.nv95.openmanga.utils.FileLogger;
import org.nv95.openmanga.utils.NetworkUtils;
import org.nv95.openmanga.utils.WeakAsyncTask;

import java.io.IOException;
import java.util.Map;

/**
 * Created by nv95 on 17.04.16.
 */
public class NewChaptersActivity extends BaseAppActivity {

    //views
    private RecyclerView mRecyclerView;
    private TextView mTextViewHolder;
    private ProgressBar mProgressBar;
    //utils
    private final MangaList mList = new MangaList();
    private final NewChaptersAdapter mAdapter = new NewChaptersAdapter(mList);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_updates);
        setSupportActionBar(R.id.toolbar);
        enableHomeAsUp();

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mTextViewHolder = (TextView) findViewById(R.id.textView_holder);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter);
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.START | ItemTouchHelper.END) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                final int pos = viewHolder.getAdapterPosition();
                final MangaInfo o = mList.get(pos);
                NewChaptersProvider.getInstance(NewChaptersActivity.this)
                        .markAsViewed(o.hashCode());
                mList.remove(pos);
                mAdapter.notifyItemRemoved(pos);
                mTextViewHolder.setVisibility(mList.size() == 0 ? View.VISIBLE : View.GONE);
            }
        }).attachToRecyclerView(mRecyclerView);

        if (NetworkUtils.checkConnection(this)) {
            new LoadTask(this).attach(this).start();
        } else {
            mTextViewHolder.setText(R.string.no_network_connection);
            AnimUtils.crossfade(mProgressBar, mTextViewHolder);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.updates, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_checkall:
                new AlertDialog.Builder(this)
                        .setMessage(R.string.mark_all_viewed_confirm)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                NewChaptersProvider.getInstance(NewChaptersActivity.this)
                                        .markAllAsViewed();
                                mList.clear();
                                mAdapter.notifyDataSetChanged();
                                mTextViewHolder.setVisibility(View.VISIBLE);
                            }
                        }).create().show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private static class LoadTask extends WeakAsyncTask<NewChaptersActivity, Void,Void,MangaList> {

        LoadTask(NewChaptersActivity object) {
            super(object);
        }

        @Override
        protected void onPreExecute(@NonNull NewChaptersActivity object) {
            object.mProgressBar.setVisibility(View.VISIBLE);
        }


        @Override
        protected MangaList doInBackground(Void... params) {
            try {
                final FavouritesProvider favs = FavouritesProvider.getInstance(getObject());
                final NewChaptersProvider news = NewChaptersProvider.getInstance(getObject());
                news.checkForNewChapters();
                MangaList mangas = favs.getList(0, 0, 0);
                Map<Integer, Integer> updates =  news.getLastUpdates();
                Integer t;
                final MangaList res = new MangaList();
                for (MangaInfo o:mangas) {
                    t = updates.get(o.hashCode());
                    if (t != null && t != 0) {
                        o.extra = "+" + t;
                        res.add(o);
                    }
                }
                return res;
            } catch (IOException e) {
                FileLogger.getInstance().report("CHUPD", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(@NonNull NewChaptersActivity activity, MangaList mangaInfos) {
            if (mangaInfos == null) {
                AnimUtils.crossfade(activity.mProgressBar, activity.mTextViewHolder);
                Toast.makeText(activity, R.string.error, Toast.LENGTH_SHORT).show();
            } else {
                if (mangaInfos.isEmpty()) {
                    AnimUtils.crossfade(activity.mProgressBar, activity.mTextViewHolder);
                } else {
                    activity.mList.clear();
                    activity.mList.addAll(mangaInfos);
                    activity.mAdapter.notifyDataSetChanged();
                    AnimUtils.crossfade(activity.mProgressBar, activity.mRecyclerView);
                }
            }
        }
    }
}
