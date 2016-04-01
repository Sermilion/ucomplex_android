package org.ucomplex.ucomplex.Activities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import org.ucomplex.ucomplex.Activities.Tasks.FetchUserEventsTask;
import org.ucomplex.ucomplex.Activities.Tasks.LoginTask;
import org.ucomplex.ucomplex.Adaptors.MenuAdapter;
import org.ucomplex.ucomplex.Common;
import org.ucomplex.ucomplex.Fragments.EventsFragment;
import org.ucomplex.ucomplex.Interfaces.OnTaskCompleteListener;
import org.ucomplex.ucomplex.Model.EventRowItem;
import org.ucomplex.ucomplex.Model.Users.User;
import org.ucomplex.ucomplex.MyService;
import org.ucomplex.ucomplex.R;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class EventsActivity extends AppCompatActivity implements OnTaskCompleteListener, LoginTask.AsyncResponse {


    ArrayList eventsArray = null;
    FetchUserEventsTask mEventsTask = null;
    User user;
    public static Intent i;

    final String[] TITLES = {"События", "Дисциплины", "Материалы", "Пользователи", "Сообщения", "Календарь", "Настройки", "Выход"};
    final int[] ICONS = {R.drawable.ic_menu_events,
            R.drawable.ic_menu_subjects,
            R.drawable.ic_menu_materials,
            R.drawable.ic_menu_users,
            R.drawable.ic_menu_messages,
            R.drawable.ic_menu_timetable,
            R.drawable.ic_menu_settings,
            R.drawable.ic_menu_exit};

    RecyclerView mRecyclerView;                           // Declaring RecyclerView
    MenuAdapter mAdapter;                        // Declaring Adapter For Recycler View
    LinearLayoutManager mLayoutManager;            // Declaring Layout Manager as a linear layout manager
    DrawerLayout Drawer;                                  // Declaring DrawerLayout
    ActionBarDrawerToggle mDrawerToggle;                  // Declaring Action Bar Drawer Toggle
    LinearLayout linlaHeaderProgress;
    MediaPlayer mAlert;
    boolean refreshed;

    public Intent getI() {
        return i;
    }

    public EventsActivity() {

    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putSerializable("eventsArray", eventsArray);
    }

    private void refresh() {
        linlaHeaderProgress.setVisibility(View.VISIBLE);
        Common.fetchMyNews(EventsActivity.this);
        if (Common.newMesg > 0) {
            mAdapter.setMsgCount(Common.newMesg);
            mAdapter.notifyDataSetChanged();
        }
        user = Common.getUserDataFromPref(this);
        if(user!=null && user.getPerson()!=0) {
            LoginTask loginTask = new LoginTask(user.getLogin(), user.getPass(), EventsActivity.this);
            loginTask.delegate = this;
            loginTask.execute();

            new FetchUserEventsTask(this) {
                @Override
                protected void onPostExecute(ArrayList<EventRowItem> items) {
                    super.onPostExecute(items);
                    eventsArray = items;
                    EventsFragment fragment = new EventsFragment();
                    fragment.setContext(EventsActivity.this);
                    fragment.setUserType(user.getType());
                    Bundle data = new Bundle();
                    data.putSerializable("eventItems", eventsArray);
                    fragment.setArguments(data);
                    getFragmentManager().beginTransaction()
                            .replace(R.id.container, fragment)
                            .commit();
                    linlaHeaderProgress.setVisibility(View.GONE);
                }
            }.execute(user.getType());
            refreshed = true;
        }else{
            EventsFragment fragment = new EventsFragment();
            fragment.setContext(EventsActivity.this);
            fragment.setUserType(user.getType());
            Bundle data = new Bundle();
            data.putSerializable("eventItems", eventsArray);
            fragment.setArguments(data);
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment)
                    .commitAllowingStateLoss();
            linlaHeaderProgress.setVisibility(View.GONE);
        }
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            int messageCount = bundle.getInt("newMessage");
            boolean newFriend = bundle.getBoolean("newFriend");
            if (!mAdapter.isNewFriend() && newFriend) {
                mAdapter.setNewFriend(newFriend);
                mAdapter.notifyItemChanged(4);
                mAlert.start();
            } else if (mAdapter.isNewFriend() && !newFriend) {
                mAdapter.setNewFriend(newFriend);
                mAdapter.notifyItemChanged(4);
            }
            if (messageCount > 0 && messageCount != mAdapter.getMsgCount()) {
                mAdapter.setMsgCount(messageCount);
                mAdapter.notifyItemChanged(5);
                if (mAlert != null) {
                    mAlert.start();
                }
            } else if (messageCount == 0 && mAdapter.getMsgCount() > 0) {
                mAdapter.setMsgCount(messageCount);
                mAdapter.notifyItemChanged(5);
            }
        }
    };

    @Override
    protected void onPause() {
        unregisterReceiver(receiver);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction("org.ucomplex.newMessageMenuBroadcast");
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);
        linlaHeaderProgress = (LinearLayout) findViewById(R.id.linlaHeaderProgress);
        refresh();
        mAlert = MediaPlayer.create(this, R.raw.alert);
        Common.fetchMyNews(EventsActivity.this);
        i = new Intent(EventsActivity.this, MyService.class);
        EventsActivity.this.startService(i);
        if ((savedInstanceState != null)
                && (savedInstanceState.getSerializable("eventsArray") != null)) {
            eventsArray = (ArrayList) savedInstanceState.getSerializable("eventsArray");
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        toolbar.setTitle("События");
        setSupportActionBar(toolbar);
        user = Common.getUserDataFromPref(this);
        Bitmap bmp;
        if (Common.hasKeyPref(this, "profilePhoto")) {
            bmp = Common.decodePhotoPref(this, "profilePhoto");
            user.setPhotoBitmap(bmp);
        }
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        mRecyclerView = (RecyclerView) findViewById(R.id.RecyclerView);
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new MenuAdapter(TITLES, ICONS, user, this);
        mRecyclerView.setAdapter(mAdapter);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        Drawer = (DrawerLayout) findViewById(R.id.DrawerLayout);
        mDrawerToggle = new ActionBarDrawerToggle(this, Drawer, toolbar, R.string.openDrawer, R.string.closeDrawer) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        Drawer.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
    }

    @Override
    public void onBackPressed() {
        if (this.Drawer.isDrawerOpen(GravityCompat.START)) {
            this.Drawer.closeDrawer(GravityCompat.START);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            refresh();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTaskComplete(AsyncTask task, Object... o) {
        try {
            eventsArray = (ArrayList) task.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        EventsFragment fragment = new EventsFragment();
        fragment.setContext(EventsActivity.this);
        fragment.setUserType(user.getType());
        Bundle data = new Bundle();
        data.putSerializable("eventItems", eventsArray);
        fragment.setArguments(data);
        getFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
        linlaHeaderProgress.setVisibility(View.GONE);
    }


    @Override
    public void processFinish(User output, Bitmap bitmap) {
        if (output != null) {
            if(output.getPerson()!=0){
                Common.setUserDataToPref(this, output);
            }
            if(MenuAdapter.getProfileBitmap()==null){
                mAdapter.setProfileBitmap(bitmap);
            }
            if (bitmap != null) {
                Common.encodePhotoPref(this, bitmap, "profileBitmap");
                user.setPhotoBitmap(bitmap);
            }
            mAdapter.notifyDataSetChanged();
            linlaHeaderProgress.setVisibility(View.VISIBLE);

        }
    }


    @Override
    public void canceled(boolean canceled) {

    }
}
