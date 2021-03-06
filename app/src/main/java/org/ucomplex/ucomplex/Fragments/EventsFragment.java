package org.ucomplex.ucomplex.Fragments;

import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.ucomplex.ucomplex.Activities.CourseActivity;
import org.ucomplex.ucomplex.Activities.Tasks.FetchUserEventsTask;
import org.ucomplex.ucomplex.Common;
import org.ucomplex.ucomplex.Model.EventRowItem;
import org.ucomplex.ucomplex.R;

import java.util.ArrayList;

/**
 * @author Sermilion
 */
public class EventsFragment extends ListFragment {

    private ArrayList<EventRowItem> eventItems = null;
    ImageAdapter imageAdapter;
    Button btnLoadExtra;
    Context context;
    int userType;

    public void setUserType(int userType) {
        this.userType = userType;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public EventsFragment() {
        System.out.println();
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getListView().setDivider(null);
        getListView().setAdapter(imageAdapter);
        if ((eventItems != null ? eventItems.size() : 0) == 0) {
            getListView().setClickable(false);
        } else {
            getListView().setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (eventItems.get(position).getType() != 2) {

                    } else {
                        if (eventItems.size() > 0) {
                            Intent intent = new Intent(getActivity(), CourseActivity.class);
                            intent.putExtra("gcourse", eventItems.get(position).getParams().getGcourse());
                            intent.putExtra("type", eventItems.get(position).getType());
                            intent.putExtra("bitmap", eventItems.get(position).getEventImageBitmap());
                            intent.putExtra("courseName", eventItems.get(position).getParams().getCourseName());
                            startActivity(intent);
                        }
                    }
                }
            });
        }

        btnLoadExtra = new Button(getActivity());
        btnLoadExtra.setFocusable(false);
        btnLoadExtra.setText("Загрузить еще...");
        btnLoadExtra.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (Common.isNetworkConnected(context)) {
                    new FetchUserEventsTask(getActivity()) {
                        @Override
                        protected void onPostExecute(ArrayList<EventRowItem> items) {
                            super.onPostExecute(items);
                            if (items != null) {
                                eventItems.addAll(items);
                                imageAdapter.notifyDataSetChanged();

                            } else {
                                btnLoadExtra.setVisibility(View.GONE);
                            }
                            if (items != null) {
                                if (items.size() < 10) {
                                    btnLoadExtra.setVisibility(View.GONE);
                                }
                            }
                        }
                    }.execute(userType, eventItems.size());
                } else {
                    Toast.makeText(getActivity(), "Проверьте интернет соединение.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (eventItems != null) {
            if (eventItems.size() < 10) {
                btnLoadExtra.setVisibility(View.GONE);
            }
            getListView().addFooterView(btnLoadExtra);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        Bundle extras = getArguments();
        this.eventItems = (ArrayList<EventRowItem>) extras.getSerializable("eventItems");
        View rootView = inflater.inflate(R.layout.fragment_events, container, false);
        imageAdapter = new ImageAdapter(getActivity());
        return rootView;
    }


    private class ImageAdapter extends BaseAdapter {

        private LayoutInflater inflater;
        private DisplayImageOptions options;
        protected ImageLoader imageLoader;
        Context context;

        ImageAdapter(Context context) {
            this.context = context;
            inflater = LayoutInflater.from(context);
            options = new DisplayImageOptions.Builder()
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .considerExifParams(true)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .build();
            imageLoader = ImageLoader.getInstance();
            imageLoader.init(ImageLoaderConfiguration.createDefault(context));
        }

        @Override
        public int getCount() {
            return eventItems != null && eventItems.size()>0 ? eventItems.size() : 1;
        }

        @Override
        public boolean isEnabled(int position) {
            return eventItems != null && eventItems.size() != 0;
        }

        @Override
        public Object getItem(int position) {
            return eventItems != null ? eventItems.get(position) : null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (eventItems.size()==0){
                if(!Common.isNetworkConnected(context)){
                    convertView = inflater.inflate(R.layout.list_item_no_internet, parent, false);
                }else{
                    convertView = inflater.inflate(R.layout.list_item_no_content, parent, false);
                }
                return convertView;
            }
            ViewHolder viewHolder = new ViewHolder();
            if (view == null) {
                if (eventItems == null) {
                    if (!Common.isNetworkConnected(context)) {
                        view = inflater.inflate(R.layout.list_item_no_internet, parent, false);
                    } else {
                        view = inflater.inflate(R.layout.list_item_no_content, parent, false);
                    }
                    return view;
                } else {
                    view = inflater.inflate(R.layout.list_item_events, parent, false);
                    viewHolder.eventsImageView = (ImageView) view.findViewById(R.id.list_events_item_image);
                    viewHolder.eventTextView = (TextView) view.findViewById(R.id.list_events_item_text);
                    viewHolder.eventTextView.setTypeface(Common.getTypeFace(context, "Roboto-Regular.ttf"));
                    viewHolder.eventTime = (TextView) view.findViewById(R.id.list_events_item_date);
                    viewHolder.eventTime.setTypeface(Common.getTypeFace(context, "Roboto-Regular.ttf"));
                    viewHolder.eventPersonName = (TextView) view.findViewById(R.id.list_events_item_name);
                    viewHolder.eventPersonName.setTypeface(Common.getTypeFace(context, "Roboto-Regular.ttf"));
                    view.setTag(viewHolder);
                }

            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            if (eventItems.get(position).getEventImageBitmap() == null && eventItems.get(position).getParams().getPhoto() == 1) {
                final ViewHolder finalViewHolder = viewHolder;
                imageLoader.displayImage("http://ucomplex.org/files/photos/" + eventItems.get(position).getParams().getCode() + ".jpg", viewHolder.eventsImageView, options, new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        BitmapDrawable bitmapDrawable = ((BitmapDrawable) finalViewHolder.eventsImageView.getDrawable());
                        Bitmap bitmap = bitmapDrawable.getBitmap();
                        eventItems.get(position).setEventImageBitmap(bitmap);
                        finalViewHolder.eventsImageView.setImageBitmap(bitmap);
                    }
                }, new ImageLoadingProgressListener() {
                    @Override
                    public void onProgressUpdate(String imageUri, View view, int current, int total) {
                    }
                });

            } else {
                Bitmap image = eventItems.get(position).getEventImageBitmap();
                if (image != null)
                    viewHolder.eventsImageView.setImageBitmap(eventItems.get(position).getEventImageBitmap());
                else
                    viewHolder.eventsImageView.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_ucomplex));
            }
            viewHolder.eventTextView.setText(eventItems.get(position).getEventText());
            viewHolder.eventTime.setText(eventItems.get(position).getTime());
            if (eventItems.get(position).getParams().getName() == null) {
                viewHolder.eventPersonName.setText("UComplex");
            } else {
                viewHolder.eventPersonName.setText(eventItems.get(position).getParams().getName());
            }

            return view;
        }
    }

    static class ViewHolder {
        ImageView eventsImageView;
        TextView eventTextView;
        TextView eventTime;
        TextView eventPersonName;
    }


}

