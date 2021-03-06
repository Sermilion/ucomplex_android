package org.ucomplex.ucomplex.Adaptors;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import org.ucomplex.ucomplex.Common;
import org.ucomplex.ucomplex.Fragments.CalendarFragment;
import org.ucomplex.ucomplex.Interfaces.OnTaskCompleteListener;
import org.ucomplex.ucomplex.Model.Calendar.ChangedDay;
import org.ucomplex.ucomplex.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Sermilion on 23/04/16.
 */
public class TeacherAddProtocolAdapter extends ArrayAdapter<Integer> {

    private LayoutInflater inflater;
    private static final int TYPE_NAME = 0;
    private static final int TYPE_ADD = 1;
    private int subjId;
    ArrayList<Integer> numbersOfLessons = new ArrayList<>();
    Context context;
    CalendarFragment calendarFragment;
    String dayMonthYear;
    ChangedDay changedDay;

    public TeacherAddProtocolAdapter(Context context, int subjId, ArrayList<Integer> numbersOfLessons,
                                     String dayMonthYear, CalendarFragment calendarFragment,
                                     ChangedDay changedDay) {
        super(context, -1, numbersOfLessons);
        this.calendarFragment = calendarFragment;
        this.context = context;
        this.subjId = subjId;
        this.numbersOfLessons = numbersOfLessons;
        this.numbersOfLessons.add(0);
        this.dayMonthYear = dayMonthYear;
        this.changedDay = changedDay;
    }

    @Override
    public int getItemViewType(int position) {
        if (getItem(position) == 0) {
            return TYPE_ADD;
        } else {
            return TYPE_NAME;
        }
    }

    @Override
    public Integer getItem(int position) {
        return this.numbersOfLessons.get(position);
    }

    @Override
    public int getCount() {
        return numbersOfLessons.size();
    }

    private View createHolder(int viewType, int position) {
        ViewHolder viewHolder = new ViewHolder(context);
        View convertView = null;
        if (viewType == TYPE_ADD) {
            convertView = inflater.inflate(R.layout.list_item_calendar_day_lessons_popup_add, null);
            viewHolder.nameTextView = (TextView) convertView.findViewById(R.id.list_calendar_day_popup_add);
            viewHolder.holderId = TYPE_ADD;

        } else if (viewType == TYPE_NAME) {
            convertView = inflater.inflate(R.layout.list_item_calendar_day_lessons_popup, null);
            viewHolder.nameTextView = (TextView) convertView.findViewById(R.id.list_calendar_day_popup_lesson);
            Button button = (Button) convertView.findViewById(R.id.list_calendar_day_popup_lesson_button);
            viewHolder.setButton(button, position);
            viewHolder.holderId = TYPE_NAME;
        }
        if (convertView != null) {
            convertView.setTag(viewHolder);
        }
        return convertView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        inflater = LayoutInflater.from(getContext());
        int viewType = getItemViewType(position);
        if (convertView == null) {
            convertView = createHolder(viewType, position);
            viewHolder = (ViewHolder) convertView.getTag();
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            if (viewHolder.holderId != viewType) {
                convertView = createHolder(viewType, position);
                viewHolder = (ViewHolder) convertView.getTag();
            }
        }
        if(viewHolder.holderId == TYPE_NAME){
            viewHolder.nameTextView.setText("Занятие "+(position+1));
        }else{
            viewHolder.nameTextView.setText("+ Добавить занятие на "+dayMonthYear);
        }
        return convertView;
    }

    public class ViewHolder {
        int holderId;
        TextView nameTextView;
        Button menuBotton;
        ArrayList<String> actionsArrayList = new ArrayList<>();
        Context context;

        void setButton(Button button, final int position) {
            menuBotton = button;
            menuBotton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    AlertDialog.Builder build = new AlertDialog.Builder(context);
                    build.setItems(actionsArrayList.toArray(new String[actionsArrayList.size()]), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 0:
                                    new AsyncTask<String, Void, String>() {
                                        private OnTaskCompleteListener listener = calendarFragment;
                                        @Override
                                        protected String doInBackground(String... params) {
                                            HashMap<String, String> postParams = new HashMap<>();
                                            postParams.put("delete", String.valueOf("1"));
                                            postParams.put("subjId", String.valueOf(subjId));
                                            postParams.put("time", dayMonthYear);
                                            postParams.put("hourNumber", String.valueOf(position));
                                            String url = "https://ucomplex.org/teacher/ajax/oneday?mobile=1";
                                            return Common.httpPost(url, Common.getLoginDataFromPref(context), postParams);
                                        }

                                        @Override
                                        protected void onPostExecute(String s) {
                                            super.onPostExecute(s);
                                            if(s!=null){
                                                changedDay.getLessons().remove(position);
                                                numbersOfLessons.remove(position);
                                                TeacherAddProtocolAdapter.this.notifyDataSetChanged();
                                                listener.onTaskComplete(this, "protocolDeleteSuccess", changedDay);
                                            }else{
                                                listener.onTaskComplete(this, "protocolDeleteFail", changedDay);
                                            }
                                        }
                                    }.execute();


                                    break;
                                case 1:

                                    break;
                            }
                        }
                    });
                    AlertDialog alert = build.create();
//                                    alert.getWindow().setBackgroundDrawableResource(android.R.color.background_light);
                    alert.show();
                }
            });
        }

        public ViewHolder(final Context context) {
            actionsArrayList.add("Удалить");
            this.context = context;
        }

    }

}
