package org.ucomplex.ucomplex.Adaptors;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;

import org.javatuples.Quartet;
import org.ucomplex.ucomplex.Common;
import org.ucomplex.ucomplex.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sermilion on 08/12/2015.
 */
public class CourseCalendarBeltAdapter extends ArrayAdapter<Quartet<Integer, String, String, Integer>> {

    String[] colors = {"#51cde7", "#fecd71", "#9ece2b", "#d18ec0"};
    List<Quartet<Integer, String, String, Integer>> mItems = new ArrayList<>();
    Context mContext;
    LayoutInflater inflater;

    public void addAllFeedItems(List<Quartet<Integer, String, String, Integer>> feedItems) {
        this.mItems.addAll(feedItems);
    }

    public CourseCalendarBeltAdapter(Context context, List<Quartet<Integer, String, String, Integer>> items) {
        super(context, R.layout.list_item_course_calendar_belt, items);
        mItems = items;
        mContext = context;
    }

    public void changeItems(ArrayList<Quartet<Integer, String, String, Integer>> feedItems){
        this.mItems.clear();
        this.mItems.addAll(feedItems);
        notifyDataSetChanged();
    }

    @Override
    public Quartet<Integer, String, String, Integer> getItem(int position) {
        return this.mItems.get(position);
    }

    public List<Quartet<Integer, String, String, Integer>> getmItems() {
        return mItems;
    }

    @Override
    public boolean isEnabled(int position) {
        return mItems.size() != 0;
    }

    @Override
    public int getCount() {
        if(mItems==null){
            mItems = new ArrayList<>();
        }
        return mItems.size()>0?mItems.size():1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        inflater = LayoutInflater.from(getContext());
        if (mItems.size()==0){
            if(!Common.isNetworkConnected(getContext())){
                convertView = inflater.inflate(R.layout.list_item_no_internet, null, false);
            }else{
                convertView = inflater.inflate(R.layout.list_item_no_content, null, false);
            }
            return convertView;
        }
        if(convertView!=null && mItems.size()>0){
            convertView = null;
        }
        Quartet<Integer, String, String, Integer> feedItem = getItem(position);
        if (convertView == null) {
            viewHolder = new ViewHolder();
            if(feedItem.getValue3()==-2){
                convertView = inflater.inflate(R.layout.list_item_calendar_calendar_belt, parent, false);
                viewHolder.teacherNametextView = (TextView) convertView.findViewById(R.id.course_calendar_belt_listview_item_textview1);
                viewHolder.markImageView = (ImageView) convertView.findViewById(R.id.course_calendar_belt_listview_item_image);
            }else{
                convertView = inflater.inflate(R.layout.list_item_course_calendar_belt, parent, false);
                viewHolder.teacherNametextView = (TextView) convertView.findViewById(R.id.course_calendar_belt_listview_item_textview1);
                viewHolder.dateTextView2 = (TextView) convertView.findViewById(R.id.course_calendar_belt_listview_item_textview2);
                viewHolder.markImageView = (ImageView) convertView.findViewById(R.id.course_calendar_belt_listview_item_image);
                viewHolder.timeIconttextView = (TextView) convertView.findViewById(R.id.course_calendar_belt_listview_time_icon);
            }
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.teacherNametextView.setText(feedItem.getValue1());
        if(!feedItem.getValue2().equals("-1")){
            if(feedItem.getValue3()!=-2){
                viewHolder.dateTextView2.setText(Common.makeDate(feedItem.getValue2()));
            }
        }
        String hex;
        if(feedItem.getValue3()==-2){
            hex = feedItem.getValue2();
        }else{
            hex = this.colors[feedItem.getValue3()]; // green
        }
        long thisCol = Long.decode(hex) + 4278190080L;
        int classColor = (int) thisCol;
        TextDrawable drawable;
        if (feedItem.getValue0() == 0) {
            Typeface custom_font = Typeface.createFromAsset(getContext().getAssets(), "fonts/fontawesome-webfont.ttf");
            drawable = TextDrawable.builder().beginConfig().useFont(custom_font).textColor(classColor).endConfig()
                    .buildRound(String.valueOf(getLetter(feedItem.getValue0())), Color.WHITE);

        } else {
            drawable = TextDrawable.builder()
                    .buildRound(String.valueOf(getLetter(feedItem.getValue0())), classColor);
        }
        viewHolder.markImageView.setImageDrawable(drawable);
        if(feedItem.getValue3()!=-2){
            viewHolder.timeIconttextView.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/fontawesome-webfont.ttf"));
            viewHolder.timeIconttextView.setText("\uF017");
        }
        return convertView;
    }

    private static String getLetter(int mark) {
        if (mark == -1) {
            return "н";
        } else if (mark == 0) {
            return "\uF00C";
        } else if (mark == -3) {
            return "б";
        } else {
            return String.valueOf(mark);
        }
    }

    public static class ViewHolder {
        TextView teacherNametextView;
        TextView dateTextView2;
        ImageView markImageView;
        TextView timeIconttextView;

    }
}
