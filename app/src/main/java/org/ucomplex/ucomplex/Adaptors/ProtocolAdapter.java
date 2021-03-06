package org.ucomplex.ucomplex.Adaptors;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.javatuples.Triplet;
import org.ucomplex.ucomplex.Common;
import org.ucomplex.ucomplex.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sermilion on 24/04/16.
 */
public class ProtocolAdapter extends ArrayAdapter<Triplet<Integer, String, String>> {

    //id, mark, name
    private ArrayList<Triplet<Integer, String, String>> mItems;
    private Context mContext;
    private LayoutInflater inflater;

    public ProtocolAdapter(Context context, ArrayList<Triplet<Integer, String, String>> items) {
        super(context, -1, items);
        mItems = items;
        mContext = context;
    }

    public void setItemAt(Triplet<Integer, String, String> item, int position){
        mItems.set(position, item);
    }

    public List<Triplet<Integer, String, String>> getmItems() {
        return mItems;
    }

    public void setmItems(ArrayList<Triplet<Integer, String, String>> mItems) {
        this.mItems = mItems;
    }

    @Override
    public int getCount() {
        return mItems.size() > 0 ? mItems.size() : 1;
    }

    @Override
    public boolean isEnabled(int position) {
        return mItems.size() != 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        inflater = LayoutInflater.from(getContext());
        if (mItems.size() == 0) {
            if (!Common.isNetworkConnected(getContext())) {
                convertView = inflater.inflate(R.layout.list_item_no_internet, null, false);
            } else {
                convertView = inflater.inflate(R.layout.list_item_no_content, null, false);
            }
            return convertView;
        } else {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.list_item_calendar_day_protocol_add, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.mNameTextView = (TextView) convertView.findViewById(R.id.list_calendar_day_name);
                viewHolder.mMarkTextView = (TextView) convertView.findViewById(R.id.list_calendar_day_mark);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.mNameTextView.setText((position + 1) + ". " + getItem(position).getValue2());
            int mark = Integer.valueOf(getItem(position).getValue1());
            if(mark == 0){
                viewHolder.mMarkTextView.setText("");
            }else if(mark == -3){
                viewHolder.mMarkTextView.setText("б");
            }else if(mark == -1){
                viewHolder.mMarkTextView.setText("н");
            }else{
                viewHolder.mMarkTextView.setText(String.valueOf(getItem(position).getValue1()));
            }
//            viewHolder.mMarkTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//                public void onFocusChange(View v, boolean hasFocus) {
//                    if (!hasFocus){
//                        final int position = v.getId();
//                        final EditText Caption = (EditText) v;
//                        mItems.get(position).setAt1(Integer.valueOf(Caption.getText().toString()));
//                    }
//                }
//            });
        }
        return convertView;
    }

    private static class ViewHolder {
        TextView mNameTextView;
        TextView mMarkTextView;
    }


}
