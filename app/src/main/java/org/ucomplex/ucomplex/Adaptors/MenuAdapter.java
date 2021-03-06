package org.ucomplex.ucomplex.Adaptors;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;

import org.ucomplex.ucomplex.Activities.CalendarActivity;
import org.ucomplex.ucomplex.Activities.EventsActivity;
import org.ucomplex.ucomplex.Activities.LoginActivity;
import org.ucomplex.ucomplex.Activities.MessagesListActivity;
import org.ucomplex.ucomplex.Activities.MyFilesActivity;
import org.ucomplex.ucomplex.Activities.ProfileActivity;
import org.ucomplex.ucomplex.Activities.SettingsActivity2;
import org.ucomplex.ucomplex.Activities.SubjectsActivity;
import org.ucomplex.ucomplex.Activities.UsersActivity;
import org.ucomplex.ucomplex.Common;
import org.ucomplex.ucomplex.Model.Users.User;
import org.ucomplex.ucomplex.R;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by hp1 on 28-12-2014.
 */
public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_ITEM_MSG = 2;
    private static final int TYPE_ITEM_USR = 3;
    private String mNavTitles[];
    private int mIcons[];
    private String name;
    private String role;
    private int msgCount = 0;
    private boolean newFriend;
    private EventsActivity context;
    private static Bitmap profileBitmap;
    private User user;

    public void setNewFriend(boolean newFriend) {
        this.newFriend = newFriend;
    }

    public boolean isNewFriend() {
        return newFriend;
    }

    public static Bitmap getProfileBitmap() {
        return profileBitmap;
    }

    public void setMsgCount(int msgCount) {
        this.msgCount = msgCount;
    }

    public int getMsgCount() {
        return msgCount;
    }

    public void setProfileBitmap(Bitmap profileBitmap) {
        MenuAdapter.profileBitmap = profileBitmap;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        int Holderid;
        TextView roleTextView;
        TextView textView;
        ImageView imageView;
        ImageView msgCountImageView;
        de.hdodenhof.circleimageview.CircleImageView profile;
        TextView Name;
        Context contxt;
        Typeface custom_font;

        public ViewHolder(View itemView, int ViewType, Context c) {
            super(itemView);
            contxt = c;
            custom_font = Typeface.createFromAsset(contxt.getAssets(), "fonts/Roboto-Bold.ttf");
            itemView.setClickable(true);
            itemView.setOnClickListener(this);

            if (ViewType == TYPE_ITEM) {
                textView = (TextView) itemView.findViewById(R.id.rowText);
                textView.setTypeface(custom_font);
                imageView = (ImageView) itemView.findViewById(R.id.rowIcon);
                Holderid = 1;
            } else if (ViewType == TYPE_HEADER) {
                Name = (TextView) itemView.findViewById(R.id.name);
                Name.setTypeface(custom_font);
                roleTextView = (TextView) itemView.findViewById(R.id.role);
                profile = (de.hdodenhof.circleimageview.CircleImageView) itemView.findViewById(R.id.circleView);
                profileBitmap = Common.decodePhotoPref(context, "profilePhoto");
                if (profileBitmap != null) {
                    this.profile.setImageBitmap(profileBitmap);
                }
                Holderid = 0;
            } else if (ViewType == TYPE_ITEM_MSG) {
                textView = (TextView) itemView.findViewById(R.id.rowText);
                textView.setTypeface(custom_font);
                imageView = (ImageView) itemView.findViewById(R.id.rowIcon);
                msgCountImageView = (de.hdodenhof.circleimageview.CircleImageView) itemView.findViewById(R.id.rowMsgCount);
                Holderid = 2;
            }else if (ViewType == TYPE_ITEM_USR) {
                textView = (TextView) itemView.findViewById(R.id.rowText);
                textView.setTypeface(custom_font);
                imageView = (ImageView) itemView.findViewById(R.id.rowIcon);
                msgCountImageView = (de.hdodenhof.circleimageview.CircleImageView) itemView.findViewById(R.id.rowMsgCount);
                Holderid = 3;
            }
        }

        @Override
        public void onClick(View v) {
            if (getAdapterPosition() == 0) {
                goToProfile();
            }if (getAdapterPosition() == 1) {
                Intent intent = new Intent(contxt, EventsActivity.class);

                contxt.startActivity(intent);
            } else if (getAdapterPosition() == 2) {
                Intent intent;
                if(Common.USER_TYPE ==0){
                    intent = new Intent(contxt, UsersActivity.class);
                }else{
                    intent = new Intent(contxt, SubjectsActivity.class);
                }
                contxt.startActivity(intent);
            } else if (getAdapterPosition() == 3) {
                Intent intent;
                if(Common.USER_TYPE ==0){
                    intent = new Intent(contxt, MessagesListActivity.class);
                }else{
                    intent = new Intent(contxt, MyFilesActivity.class);
                }
                contxt.startActivity(intent);
            }
            else if (getAdapterPosition() == 4) {
                Intent intent;
                if(Common.USER_TYPE ==0){
                    intent = new Intent(contxt, SettingsActivity2.class);
                }else{
                    intent = new Intent(contxt, UsersActivity.class);
                }
                contxt.startActivity(intent);
            } else if (getAdapterPosition() == 5) {
                Intent intent = new Intent();
                if(Common.USER_TYPE ==0){
                    this.contxt.stopService(EventsActivity.i);
                    this.logout();
                }else{
                    intent = new Intent(contxt, MessagesListActivity.class);
                    Common.newMesg = 0;
                    contxt.startActivity(intent);
                }

            }
            else if (getAdapterPosition() == 6) {
                Intent intent = new Intent(contxt, CalendarActivity.class);
                intent.putExtra("fromMenu", "1");
                contxt.startActivity(intent);
            } else if (getAdapterPosition() == 7) {

                try {
                    Intent intent = new Intent(contxt, SettingsActivity2.class);
                    //Write file
                    String filename = "bitmap.png";
                    FileOutputStream stream = contxt.openFileOutput(filename, Context.MODE_PRIVATE);
                    try {
                        profileBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    } catch (Exception ignored) {}
                    //Cleanup
                    stream.close();
                    if (profileBitmap != null) {
//                        profileBitmap.recycle();
                    }
                    //Pop intent
                    intent.putExtra("image", filename);
                    contxt.startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (getAdapterPosition() == 8) {
                this.contxt.stopService(EventsActivity.i);
                this.logout();
            }
        }

        private void goToProfile() {
            if(Common.isNetworkConnected(context)){
                User user = Common.getUserDataFromPref(context);
                Intent intent = new Intent(contxt, ProfileActivity.class);
                intent.putExtra("person", String.valueOf(user.getPerson()));
                if (profileBitmap != null) {
                    File bitmapFile = Common.storeImage(profileBitmap, (Application) context.getApplicationContext());
                    intent.putExtra("bitmap", bitmapFile);
                }
                intent.putExtra("hasPhoto", String.valueOf(user.getPhoto()));
                intent.putExtra("code",user.getCode());
                intent.putExtra("type",user.getType());
                contxt.startActivity(intent);
            }else{
                Toast.makeText(context, "Проверьте интернет соединение.",
                        Toast.LENGTH_SHORT).show();
            }
        }

        private void logout() {
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(contxt).edit();
            editor.clear();
            editor.apply();
            Intent intent = new Intent(contxt, LoginActivity.class);
            contxt.startActivity(intent);
        }
    }

    public MenuAdapter(String Titles[], int Icons[], User user, EventsActivity passedContext) {
        mNavTitles = Titles;
        mIcons = Icons;
        String tempName = user.getName();
        if(tempName!=null){
            if (tempName.split(" ").length > 1) {
                name = tempName.split(" ")[1];
            } else {
                name = tempName;
            }
            profileBitmap = user.getPhotoBitmap();
            this.user = user;
        }
        switch (Common.USER_TYPE){
            case 4: role = "Студент"; break;
            case 3: role = "Преподаватель"; break;
            default:role = "Сотрудник"; break;
        }
        this.context = passedContext;
    }

    @Override
    public MenuAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_menu, parent, false);
            return new ViewHolder(v, viewType, context);
        } else if (viewType == TYPE_HEADER) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_menu_header, parent, false);
            return new ViewHolder(v, viewType, context);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_menu_message, parent, false);
            return new ViewHolder(v, viewType, context);
        }
    }

    @Override
    public void onBindViewHolder(MenuAdapter.ViewHolder holder, int position) {
        if (holder.Holderid == 1) {
            holder.textView.setText(mNavTitles[position - 1]);
            holder.imageView.setImageResource(mIcons[position - 1]);
        } else if (holder.Holderid == 0) {
             if (profileBitmap != null) {
                holder.profile.setImageBitmap(profileBitmap);
            } else {
                 final int colorsCount = 16;
                 TextDrawable drawable;
                 if(user!=null){
                     final int number = (user.getPerson() <= colorsCount) ? user.getPerson() : user.getPerson() % colorsCount;
                     char firstLetter = user.getName().split(" ")[1].charAt(0);
                     drawable = TextDrawable.builder().beginConfig()
                             .width(604)
                             .height(604)
                             .endConfig()
                             .buildRect(String.valueOf(firstLetter), Common.getColor(number));
                 }else{
                     drawable = TextDrawable.builder().beginConfig()
                             .width(604)
                             .height(604)
                             .endConfig()
                             .buildRect(String.valueOf(" "), Common.getColor(1));
                 }
                 holder.profile.setImageDrawable(drawable);
            }
            holder.Name.setText(name);
            holder.roleTextView.setText(role);

        } else if (holder.Holderid == 2) {
            holder.textView.setText(mNavTitles[position - 1]);
            holder.imageView.setImageResource(mIcons[position - 1]);
            if (msgCount > 0 && holder.msgCountImageView != null) {
                TextDrawable drawable = TextDrawable.builder().beginConfig()
                        .width(604)
                        .height(604)
                        .endConfig()
                        .buildRect(String.valueOf(msgCount), Color.parseColor("#20bcfa"));
                holder.msgCountImageView.setImageDrawable(drawable);
            }
        }else if (holder.Holderid == 3) {
            holder.textView.setText(mNavTitles[position - 1]);
            holder.imageView.setImageResource(mIcons[position - 1]);
            if (newFriend && holder.msgCountImageView != null) {
                Typeface iconFont  = Typeface.createFromAsset(context.getAssets(), "fonts/fontawesome-webfont.ttf");
                TextDrawable drawable1 = TextDrawable.builder().beginConfig()
                        .width(604)
                        .height(604)
                        .useFont(iconFont)
                        .endConfig()
                        .buildRect("\uF007", Color.parseColor("#20bcfa"));
                holder.msgCountImageView.setImageDrawable(drawable1);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mNavTitles.length + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position)) {
            int i = TYPE_HEADER;
            return TYPE_HEADER;
        }else if (mNavTitles[position - 1].equals("Сообщения"))
            return TYPE_ITEM_MSG;
        else if (mNavTitles[position - 1].equals("Пользователи"))
            return TYPE_ITEM_USR;
        else
            return TYPE_ITEM;
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }


}