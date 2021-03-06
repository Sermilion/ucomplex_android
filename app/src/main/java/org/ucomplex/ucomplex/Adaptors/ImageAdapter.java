package org.ucomplex.ucomplex.Adaptors;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.ucomplex.ucomplex.Activities.MessagesActivity;
import org.ucomplex.ucomplex.Activities.UsersActivity;
import org.ucomplex.ucomplex.Common;
import org.ucomplex.ucomplex.Model.Users.User;
import org.ucomplex.ucomplex.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Sermilion on 12/01/2016.
 */
public class ImageAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private DisplayImageOptions options;
    ArrayList<User> mItems;
    protected ImageLoader imageLoader;
    private int usersType;
    public Activity context;
    public boolean fromSearch = false;

    public void setmItems(ArrayList<User> mItems) {
        this.mItems = mItems;
    }

    public ArrayList<User> getmItems() {
        return mItems;
    }

    public void setFromSearch(boolean fromSearch) {
        this.fromSearch = fromSearch;
    }

    public ImageAdapter(ArrayList<User> items, Activity context, int usersType) {
        this.context = context;
        this.inflater = LayoutInflater.from(this.context);
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(null)
                .showImageForEmptyUri(null)
                .showImageOnFail(null)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
        this.mItems = items;
        imageLoader = ImageLoader.getInstance();
        imageLoader.init(ImageLoaderConfiguration.createDefault(context));
        this.usersType = usersType;

    }

    @Override
    public int getCount() {
        return mItems.size()>0?mItems.size():1;
    }
    @Override
    public boolean isEnabled(int position) {
        return mItems.size() != 0;
    }

    @Override
    public User getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (mItems.size()==0){
            if(!Common.isNetworkConnected(context)){
                convertView = inflater.inflate(R.layout.list_item_no_internet, parent, false);
            }else{
                convertView = inflater.inflate(R.layout.list_item_no_content, parent, false);
            }
            return convertView;
        }
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_users, null);
            viewHolder = new ViewHolder(convertView, position, this, usersType, mItems, context);
            viewHolder.btnMenu.setTag(position);
            if (convertView != null) {
                convertView.setTag(viewHolder);
            }

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.icon.setImageDrawable(getDrawable(position));
        if (mItems.get(position).getPhotoBitmap()==null && mItems.get(position).getPhoto()==1){
            imageLoader.displayImage("http://ucomplex.org/files/photos/" + mItems.get(position).getCode() + ".jpg", viewHolder.icon, options, new SimpleImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {

                }
                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                }
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    if(loadedImage!=null){
                        BitmapDrawable bitmapDrawable = ((BitmapDrawable) viewHolder.icon.getDrawable());
                        Bitmap bitmap = bitmapDrawable.getBitmap();
                        mItems.get(position).setPhotoBitmap(bitmap);
                        viewHolder.icon.setImageBitmap(bitmap);
                    }else{
                        viewHolder.icon.setImageDrawable(getDrawable(position));
                    }
                }
            }, new ImageLoadingProgressListener() {
                @Override
                public void onProgressUpdate(String imageUri, View view, int current, int total) {
                }
            });

        } else {
            Bitmap image = mItems.get(position).getPhotoBitmap();
            if(image!=null)
                viewHolder.icon.setImageBitmap(getItem(position).getPhotoBitmap());
            else {
                viewHolder.icon.setImageDrawable(getDrawable(position));
            }
        }

        User user = getItem(position);
        if(user.isFriendRequested()){
            if (convertView != null) {
                convertView.setBackgroundColor(Color.parseColor("#ecfbfe"));
            }
        }
        int type = user.getType();
        if(type!=-1 && !fromSearch){
            String typeStr = Common.getStringUserType(context, type);
            viewHolder.textView2.setText(typeStr);
        }
        viewHolder.textView1.setText(user.getName());

        return convertView;
    }

    public Drawable getDrawable(int position){
        final int colorsCount = 16;
        final int number = (getItem(position).getPerson() <= colorsCount) ? getItem(position).getPerson() : getItem(position).getPerson() % colorsCount;
        char firstLetter = getItem(position).getName().split("")[1].charAt(0);

        TextDrawable drawable = TextDrawable.builder().beginConfig()
                .width(60)
                .height(60)
                .endConfig()
                .buildRound(String.valueOf(firstLetter), Common.getColor(number));
        return drawable;
    }
}

class ViewHolder {
    ImageView icon;
    TextView textView1;
    TextView textView2;
    Button btnMenu;
    ArrayList<String> actionsArrayList = new ArrayList<>();
    ImageAdapter imageAdapter;
    Activity context;
    int pos;


    public ViewHolder(View itemView, final int position1, final ImageAdapter adapter, final int usersType, final ArrayList<User> mItems, final Activity context) {
        imageAdapter = adapter;
        this.textView1 = (TextView) itemView.findViewById(R.id.list_users_item_textview1);
        this.textView2 = (TextView) itemView.findViewById(R.id.list_users_item_textview2);
        this.icon      = (ImageView) itemView.findViewById(R.id.list_users_item_image);
        this.btnMenu        = (Button) itemView.findViewById(R.id.list_users_item_menu_button);
        this.context = context;
        this.btnMenu.setTag(position1);

        this.btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View parentRow = (View) v.getParent();
                ListView listView = (ListView) parentRow.getParent();
                final int position = listView.getPositionForView(parentRow);
                actionsArrayList.clear();
                switch (usersType){
                    case 0:
                        actionsArrayList.add("Написать сообщение");
                        actionsArrayList.add("Добавить в друзья");
                        actionsArrayList.add("Заблокировать");
                        break;
                    case 2:
                        actionsArrayList.add("Написать сообщение");
                        actionsArrayList.add("Добавить в друзья");
                        actionsArrayList.add("Заблокировать");
                        break;
                    case 3:
                        actionsArrayList.add("Написать сообщение");
                        actionsArrayList.add("Добавить в друзья");
                        actionsArrayList.add("Заблокировать");
                        break;
                    case 1:
                        actionsArrayList.add("Написать сообщение");
                        if(mItems.get(position).isFriendRequested()){
                            actionsArrayList.add("Принять заявку");
                            actionsArrayList.add("Отклонить заявку");
                        }else{
                            actionsArrayList.add("Удалить из друзей");
                        }
                        break;
                    case 4:
                        actionsArrayList.add("Удалить из списка");
                        break;
                    default:

                }
                AlertDialog.Builder build = new AlertDialog.Builder(context);
                build.setItems(actionsArrayList.toArray(new String[actionsArrayList.size()]), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final HashMap<String, String> params = new HashMap<>();
                        switch (which) {
                            case 0:
                                if (usersType == 0 || usersType == 1 || usersType == 2 || usersType == 3) {
                                    Intent intent = new Intent(context, MessagesActivity.class);
                                    String companion = String.valueOf(mItems.get(position).getPerson());
                                    String name = String.valueOf(mItems.get(position).getName());
                                    intent.putExtra("companion", companion);
                                    intent.putExtra("name", name);
                                    intent.putExtra("profileImage", mItems.get(position).getPhotoBitmap());
                                    context.startActivity(intent);
                                }else if (usersType == 4) {
                                    params.put("user", String.valueOf(mItems.get(position).getPerson()));
                                    HandleMenuPress handleMenuPress1 = new HandleMenuPress();
                                    handleMenuPress1.execute("http://you.com.ru/user/blacklist/delete", params);
                                    Toast.makeText(context, "Пользователь удален из черного списка.", Toast.LENGTH_SHORT).show();
                                    mItems.remove(position);
                                    adapter.notifyDataSetChanged();
                                }
                                break;
                            case 1:
                                if (usersType !=1 && usersType !=4) {
                                    params.put("user", String.valueOf(mItems.get(position).getPerson()));
                                    HandleMenuPress handleMenuPress1 = new HandleMenuPress();
                                    handleMenuPress1.execute("http://you.com.ru/user/friends/add", params);
                                    Toast.makeText(context, "Заявка на дружбу отправлена.", Toast.LENGTH_SHORT).show();
                                    break;
                                } else if (usersType == 1) {
                                    params.put("user", String.valueOf(mItems.get(position).getPerson()));
                                    if (mItems.get(position).isFriendRequested()) {
                                        HandleMenuPress handleMenuPress = new HandleMenuPress();
                                        handleMenuPress.execute("http://you.com.ru/user/friends/accept", params);
                                        mItems.get(position).setFriendRequested(false);
                                        Toast.makeText(context, mItems.get(position).getName() + " теперь ваш друг.", Toast.LENGTH_SHORT).show();
                                        break;
                                    } else {
                                        HandleMenuPress handleMenuPress = new HandleMenuPress();
                                        handleMenuPress.execute("http://you.com.ru/user/friends/delete", params);
                                        Toast.makeText(context, "Пользователь удален из друзей.", Toast.LENGTH_SHORT).show();
                                        mItems.remove(position);
                                        adapter.notifyDataSetChanged();
                                        break;
                                    }
                                }
                            case 2:
                                if(usersType==1) {
                                    params.put("user", String.valueOf(mItems.get(position).getPerson()));
                                    HandleMenuPress handleMenuPress = new HandleMenuPress();
                                    handleMenuPress.execute("https://ucomplex.org/user/friends/reject", params);
                                    Toast.makeText(context, "Заявка на дружбу отклонена.", Toast.LENGTH_SHORT).show();
                                    mItems.remove(position);
                                    adapter.notifyDataSetChanged();
                                    break;
                                }else if(usersType==0 || usersType==2 || usersType==3) {
                                    params.put("user", String.valueOf(mItems.get(position).getPerson()));
                                    HandleMenuPress handleMenuPress = new HandleMenuPress();
                                    handleMenuPress.execute("http://you.com.ru/user/blacklist/add", params);
                                    Common.userListChanged = 4;
                                    Toast.makeText(context, "Пользователь "+ mItems.get(position).getName()+" добавлен в черный список.", Toast.LENGTH_SHORT).show();
                                    break;
                                }
                            case 3:
                                params.put("user", String.valueOf(mItems.get(position).getPerson()));
                                if(usersType==1) {
                                    HandleMenuPress handleMenuPress = new HandleMenuPress();
                                    handleMenuPress.execute("http://you.com.ru/user/blacklist/add", params);
                                    Toast.makeText(context, "Пользователь "+ mItems.get(position).getName()+" добавлен в черный список.", Toast.LENGTH_SHORT).show();
                                    break;
                                }
                        }
                    }
                }).create().show();
            }
        });
    }

    class HandleMenuPress extends AsyncTask<Object, Void, Void> {
        @Override
        protected Void doInBackground(Object... params) {
            Common.httpPost((String) params[0],Common.getLoginDataFromPref(context), (HashMap<String, String>) params[1]);
            return null;
        }
    }
}

