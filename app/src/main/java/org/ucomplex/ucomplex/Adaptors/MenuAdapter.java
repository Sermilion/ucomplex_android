package org.ucomplex.ucomplex.Adaptors;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;

import org.ucomplex.ucomplex.Activities.CalendarActivity;
import org.ucomplex.ucomplex.Activities.EventsActivity;
import org.ucomplex.ucomplex.Activities.LibraryActivity;
import org.ucomplex.ucomplex.Activities.LoginActivity;
import org.ucomplex.ucomplex.Activities.MessagesListActivity;
import org.ucomplex.ucomplex.Activities.MyFilesActivity;
import org.ucomplex.ucomplex.Activities.ReferenceActivity;
import org.ucomplex.ucomplex.Activities.SettingsActivity;
import org.ucomplex.ucomplex.Activities.SubjectsActivity;
import org.ucomplex.ucomplex.Activities.UsersActivity;
import org.ucomplex.ucomplex.Activities.WebViewActivity;
import org.ucomplex.ucomplex.Common;
import org.ucomplex.ucomplex.Model.Users.User;
import org.ucomplex.ucomplex.R;

import java.io.FileOutputStream;

/**
 * Created by hp1 on 28-12-2014.
 */
public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.ViewHolder>  {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private String mNavTitles[];
    private int mIcons[];
    private String name;
    private String email;
    private Context context;
    private static Bitmap profileBitmap;
    private User user;

    public  void setProfileBitmap(Bitmap profileBitmap) {
        MenuAdapter.profileBitmap = profileBitmap;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        int Holderid;

        TextView textView;
        ImageView imageView;
        ImageView profile;
        TextView Name;
        TextView email;
        Context contxt;

        public ViewHolder(View itemView,int ViewType,Context c) {
            super(itemView);
            contxt = c;
            itemView.setClickable(true);
            itemView.setOnClickListener(this);

            if(ViewType == TYPE_ITEM) {
                textView = (TextView) itemView.findViewById(R.id.rowText);
                imageView = (ImageView) itemView.findViewById(R.id.rowIcon);
                Holderid = 1;
            }
            else{
                Name = (TextView) itemView.findViewById(R.id.name);
                email = (TextView) itemView.findViewById(R.id.email);
                profile = (ImageView) itemView.findViewById(R.id.circleView);
                if(profileBitmap!=null){
                    this.profile.setImageBitmap(profileBitmap);
                }
                Holderid = 0;
            }
        }

        @Override
        public void onClick(View v) {

            if(getAdapterPosition()==1){
                Intent intent = new Intent(contxt, EventsActivity.class);
                contxt.startActivity(intent);
            }else if(getAdapterPosition()==2){
                Intent intent = new Intent(contxt, WebViewActivity.class);
                contxt.startActivity(intent);
            }else if(getAdapterPosition()==3){
                Intent intent = new Intent(contxt, SubjectsActivity.class);
                contxt.startActivity(intent);
            }else if(getAdapterPosition()==4){
                Intent intent = new Intent(contxt, MyFilesActivity.class);
                contxt.startActivity(intent);
            }else if(getAdapterPosition()==5){
                Intent intent = new Intent(contxt, ReferenceActivity.class);
                contxt.startActivity(intent);
            }else if(getAdapterPosition()==6){
                Intent intent = new Intent(contxt, UsersActivity.class);
                contxt.startActivity(intent);
            }else if(getAdapterPosition()==7){
                Intent intent = new Intent(contxt, MessagesListActivity.class);
                contxt.startActivity(intent);
            }else if(getAdapterPosition()==8){
                Intent intent = new Intent(contxt, LibraryActivity.class);
                contxt.startActivity(intent);
            }else if(getAdapterPosition()==9){
                Intent intent = new Intent(contxt, CalendarActivity.class);
                contxt.startActivity(intent);
            }else if(getAdapterPosition()==10){

                try {
                    Intent intent = new Intent(contxt, SettingsActivity.class);
                    //Write file
                    String filename = "bitmap.png";
                    FileOutputStream stream = contxt.openFileOutput(filename, Context.MODE_PRIVATE);
                    try {
                        profileBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //Cleanup
                    stream.close();
                    if(profileBitmap!=null){
                        profileBitmap.recycle();
                    }
                    //Pop intent
                    intent.putExtra("image", filename);
                    contxt.startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }else if(getAdapterPosition()==11){
                this.logout();
            }
        }

        private void logout(){
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(contxt).edit();
            editor.clear();
            editor.apply();
            Intent intent = new Intent(contxt, LoginActivity.class);
            contxt.startActivity(intent);
        }
    }

    public MenuAdapter(String Titles[], int Icons[], User user, Context passedContext){
        mNavTitles = Titles;
        mIcons = Icons;
        name = user.getName();
        email = user.getEmail();
        this.context = passedContext;
        profileBitmap = user.getPhotoBitmap();
        this.user = user;
    }

    @Override
    public MenuAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_menu,parent,false);
            return new ViewHolder(v,viewType,context);
        } else if (viewType == TYPE_HEADER) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_menu_header,parent,false);
            return new ViewHolder(v,viewType,context);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(MenuAdapter.ViewHolder holder, int position) {
        if(holder.Holderid ==1) {
            holder.textView.setText(mNavTitles[position - 1]);
            holder.imageView.setImageResource(mIcons[position -1]);
        }else{
            if(profileBitmap!=null){
                holder.profile.setImageBitmap(profileBitmap);
            }else{
                final int colorsCount = 16;
                final int number = (user.getPerson() <= colorsCount) ? user.getPerson() : user.getPerson() % colorsCount;
                char  firstLetter = user.getName().split(" ")[1].charAt(0);
                TextDrawable drawable = TextDrawable.builder().beginConfig()
                        .width(604)
                        .height(604)
                        .endConfig()
                        .buildRect(String.valueOf(firstLetter), Common.getColor(number));
                holder.profile.setImageDrawable(drawable);
            }
            holder.Name.setText(name);
        }
    }

    @Override
    public int getItemCount() {
        return mNavTitles.length+1;
    }
    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position))
            return TYPE_HEADER;
        return TYPE_ITEM;
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }

}