package org.ucomplex.ucomplex.Fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ListFragment;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import org.ucomplex.ucomplex.Activities.ProfileActivity;
import org.ucomplex.ucomplex.Activities.Tasks.FetchUsersTask;
import org.ucomplex.ucomplex.Adaptors.ImageAdapter;
import org.ucomplex.ucomplex.Common;
import org.ucomplex.ucomplex.Model.Users.User;

import java.io.File;
import java.util.ArrayList;

import static org.ucomplex.ucomplex.Common.MY_PERMISSIONS_REQUEST_WRITE_STORAGE;


public class UsersFragment extends ListFragment {


    ArrayList<User> mItems = new ArrayList<>();
    int usersType;
    ImageAdapter imageAdapter;
    Button btnLoadExtra;
    ArrayList<User> loadedUsers;
    int lastPos;
    Activity activity;
    User user;
    FetchUsersTask loadMoreTask;
    private int position;


    public void setActivity(Activity activity) {
        this.activity = activity;
        user = Common.getUserDataFromPref(activity);
    }

    public UsersFragment() {

    }

    public void setUsersType(int usersType) {
        this.usersType = usersType;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
//        outState.putSerializable("mItems", mItems);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        this.position = position;
        if(ContextCompat.checkSelfPermission(this.getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED){
            prepareProfileIntent();
        }else{
            ActivityCompat.requestPermissions(this.getActivity(),
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_STORAGE);
        }

    }

    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if (visible) {
            if (usersType == Common.userListChanged) {
                fetchUsers();
                Common.userListChanged = -1;
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        btnLoadExtra = new Button(getContext());
        btnLoadExtra.setFocusable(false);
        btnLoadExtra.setText("Загрузить еще...");
        if (usersType > 1) {
            btnLoadExtra.setVisibility(View.INVISIBLE);
        }
        if (savedInstanceState != null) {
            mItems = (ArrayList<User>) savedInstanceState.getSerializable("mItems");
            if (mItems != null && mItems.size() > 0 && mItems.size() < 20) {
                btnLoadExtra.setVisibility(View.INVISIBLE);
            }
        }
        if ((mItems != null ? mItems.size() : 0) == 0) {
            fetchUsers();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (usersType == Common.userListChanged) {
            fetchUsers();
            Common.userListChanged = -1;
        }
    }

    private void fetchUsers() {
        new FetchUsersTask(getActivity()) {
            @Override
            protected void onPostExecute(ArrayList<User> users) {
                super.onPostExecute(users);
                mItems = users;
                setListAdapter(new ImageAdapter(mItems, activity, usersType));
                if (mItems != null) {
                    if (mItems.size() < 20) {
                        btnLoadExtra.setVisibility(View.GONE);
                    }
                    if (mItems.size() > 1) {
                        if (mItems.get(0).getName().equals(user.getName())) {
                            mItems.remove(0);
                        } else if (mItems.get(1).getName().equals(user.getName())) {
                            mItems.remove(1);
                        }
                    }
                    imageAdapter.getmItems().clear();
                    imageAdapter.setmItems(mItems);
                    imageAdapter.notifyDataSetChanged();
                }
                if (users != null && users.size() > 0 && users.size() < 20) {
                    btnLoadExtra.setVisibility(View.INVISIBLE);
                }
            }
        }.execute(usersType);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getListView().setDivider(null);
        imageAdapter = new ImageAdapter(mItems, getActivity(), usersType);

        if (usersType != 3) {
            btnLoadExtra.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    if (loadMoreTask == null) {
                        loadMoreTask = (FetchUsersTask) new FetchUsersTask(getActivity()) {
                            @Override
                            protected void onPostExecute(ArrayList<User> users) {
                                super.onPostExecute(users);
                                lastPos = mItems.size();
                                loadedUsers = users;
                                if (loadedUsers.size() <= 20) {
                                    btnLoadExtra.setVisibility(View.GONE);
                                }
                                mItems.addAll(loadedUsers);
                                setListAdapter(new ImageAdapter(mItems, getActivity(), usersType));
                                getListView().setSelection(lastPos - 1);
                                if (users.size() > 0 && users.size() < 20) {
                                    btnLoadExtra.setVisibility(View.INVISIBLE);
                                }
                                loadMoreTask = null;
                            }
                        }.execute(usersType, mItems.size() - 1);
                    }
                }
            });
        }
        getListView().addFooterView(btnLoadExtra);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    prepareProfileIntent();
                } else {
                    Toast.makeText(getActivity(), "Вы не разрешили доступ к пямяти.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void prepareProfileIntent() {
        if (Common.isNetworkConnected(activity)) {
            User user = mItems.get(position);
            Intent intent = new Intent(getContext(), ProfileActivity.class);
            Bundle extras = new Bundle();
            extras.putInt("type", user.getType());
            extras.putString("person", String.valueOf(user.getPerson()));
            if (user.getPhotoBitmap() != null) {
                File bitmapFile = Common.storeImage(user.getPhotoBitmap(), activity.getApplication());
                intent.putExtra("bitmap", bitmapFile);
            }
            extras.putString("hasPhoto", String.valueOf(user.getPhoto()));
            extras.putString("code", user.getCode());
            intent.putExtras(extras);
            startActivity(intent);
        } else {
            Toast.makeText(activity, "Проверте интернет соединение.", Toast.LENGTH_LONG).show();
        }
    }
}
