package org.ucomplex.ucomplex.Activities;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.javatuples.Pair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ucomplex.ucomplex.Activities.Tasks.UploadPhotoTask;
import org.ucomplex.ucomplex.Adaptors.TeacherRatingAdapter;
import org.ucomplex.ucomplex.Adaptors.ViewPagerAdapter;
import org.ucomplex.ucomplex.Common;
import org.ucomplex.ucomplex.Fragments.ProfileStatisticsFragment;
import org.ucomplex.ucomplex.Fragments.SettingsOneFragment;
import org.ucomplex.ucomplex.Fragments.SettingsTwoFragment;
import org.ucomplex.ucomplex.Fragments.TeacherInfoFragment;
import org.ucomplex.ucomplex.Fragments.TeacherProfileFragment;
import org.ucomplex.ucomplex.Fragments.TeacherRatingFragment;
import org.ucomplex.ucomplex.Model.TeacherInfo;
import org.ucomplex.ucomplex.Model.TeacherTimetableCourses;
import org.ucomplex.ucomplex.Model.Votes;
import org.ucomplex.ucomplex.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class TeacherProfileStatisticsActivity extends AppCompatActivity {

    private LinearLayout linlaHeaderProgress;
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private TeacherInfo teacherInfo;
    ViewPagerAdapter adapter;
    ViewPager mViewPager;
    int role = -1;
    int id = -1;
    int type;

    public static ImageButton doneButton;

    private TeacherInfoFragment teacherStatisticsFragment;
//    private TeacherRatingFragment teacherRatingFragment;
    private TeacherProfileFragment teacherProfileFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }
        setContentView(R.layout.activity_teacher_info);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Профиль");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        doneButton = (ImageButton) findViewById(R.id.settings_done);

        doneButton.setOnClickListener(new View.OnClickListener() {
                                          @Override
                                          public void onClick(View v) {
                                              if (TeacherRatingAdapter.voted) {
                                                  if (Common.isNetworkConnected(TeacherProfileStatisticsActivity.this)) {
                                                      new AsyncTask<Void, Void, Void>() {
                                                          @Override
                                                          protected Void doInBackground(Void... params) {
                                                              String urlString = "https://ucomplex.org/student/ajax/set_teacher_vote?mobile=1";
                                                              HashMap<String, String> httpParams = new HashMap<>();
                                                              httpParams.put("teacher", String.valueOf(teacherInfo.getId()));
                                                              for (int i = 0; i < 10; i++) {
//                                                                  Votes vote = teacherRatingFragment.getListAdapter().getmItems().get(i);
//                                                                  if (vote.getChecked() > 0) {
//                                                                      httpParams.put("qs[" + (i + 1) + "]", String.valueOf(vote.getChecked()));
//                                                                  }
                                                              }
                                                              String jsonData = Common.httpPost(urlString, Common.getLoginDataFromPref(TeacherProfileStatisticsActivity.this), httpParams);
                                                              Toast.makeText(TeacherProfileStatisticsActivity.this, "Оценка сохранена.", Toast.LENGTH_LONG).show();
                                                              return null;
                                                          }
                                                      }.execute();
                                                  } else {
                                                      Toast.makeText(TeacherProfileStatisticsActivity.this, "Проверьте интернет соединение.", Toast.LENGTH_LONG).show();
                                                  }
                                              } else {
                                                  Toast.makeText(TeacherProfileStatisticsActivity.this, "Нету изменений.", Toast.LENGTH_LONG).show();
                                              }
                                          }
                                      }

        );

        linlaHeaderProgress = (LinearLayout)

                findViewById(R.id.linlaHeaderProgress);

        mViewPager = (ViewPager)

                findViewById(R.id.viewpager);

        adapter = new

                ViewPagerAdapter(getSupportFragmentManager()

        );
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener()

                                           {
                                               @Override
                                               public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                                               }

                                               @Override
                                               public void onPageSelected(int position) {
                                                   if (position == 1) {

                                                   } else {

                                                   }
                                               }

                                               @Override
                                               public void onPageScrollStateChanged(int state) {
                                               }
                                           }

        );
        role = Integer.parseInt(getIntent().getExtras().getString("role"));
        id = getIntent().getExtras().getInt("id");
        type = Integer.valueOf(getIntent().getExtras().getString("type"));
        toolbar.setTitle(getIntent().getExtras().getString("name"));
        FetchTeachersInfoTask fetchTeachersInfoTask = new FetchTeachersInfoTask();
        fetchTeachersInfoTask.execute(role);
        tabLayout = (TabLayout)findViewById(R.id.tabs);
        tabLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupViewPager(ViewPager viewPager) {

        teacherProfileFragment = new TeacherProfileFragment();
        teacherProfileFragment.setmContext(this);
        teacherProfileFragment.setmTeacherRating(teacherInfo);

        teacherStatisticsFragment = new TeacherInfoFragment();
        teacherStatisticsFragment.setTeacherInfo(teacherInfo);

//        teacherRatingFragment = new TeacherRatingFragment();
//        teacherRatingFragment.setmContext(this);
//        teacherRatingFragment.setTeacher(id);

        adapter = new ViewPagerAdapter(getSupportFragmentManager());

        adapter.addFragment(teacherProfileFragment, "Профиль");
        adapter.addFragment(teacherStatisticsFragment, "Личная информация");
//        adapter.addFragment(teacherRatingFragment, "Рейтинг");
        viewPager.setAdapter(adapter);
        tabLayout.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        tabLayout.setupWithViewPager(viewPager);
    }

    private class FetchTeachersInfoTask extends AsyncTask<Integer, Void, TeacherInfo> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            linlaHeaderProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected TeacherInfo doInBackground(Integer... params) {
            String urlString = "https://ucomplex.org/user/page/" + params[0] + "?json";
            String jsonData = Common.httpPost(urlString, Common.getLoginDataFromPref(TeacherProfileStatisticsActivity.this));
            return getTeacherInfo(jsonData);
        }

        @Override
        protected void onPostExecute(TeacherInfo aVoid) {
            super.onPostExecute(aVoid);
            teacherInfo = aVoid;
            if (teacherInfo.getType() != 0) {
                setupViewPager(mViewPager);

            } else {
                adapter = new ViewPagerAdapter(getSupportFragmentManager());

                ProfileStatisticsFragment profileStatisticsFragment = new ProfileStatisticsFragment();
                ArrayList<Pair<String, String>> items = new ArrayList<>();
                long lastOnlineMilliseconds = teacherInfo.getOnline();
                long s = lastOnlineMilliseconds * 1000;
                String lastOnline = "";
                if (lastOnlineMilliseconds > 0) {
                    Date date = new Date(s);
                    Locale locale = new Locale("ru", "RU");
                    SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", locale);
                    lastOnline = sdfDate.format(date);
                }
                int theType = -1;
                if (teacherInfo.getType() == 0 && type > 0) {
                    theType = type;
                } else {
                    theType = teacherInfo.getType();
                }
                items.add(new Pair<>(Common.getStringUserType(TeacherProfileStatisticsActivity.this, theType), lastOnline));
                profileStatisticsFragment.setStatisticItems(items);
                if (teacherInfo.getClosed() == 1) {
                    profileStatisticsFragment.setClosed(true);
                } else {
                    profileStatisticsFragment.setClosed(false);
                }

                adapter.addFragment(profileStatisticsFragment, "Профиль");
                mViewPager.setAdapter(adapter);
                tabLayout.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                tabLayout.setupWithViewPager(mViewPager);
            }

            linlaHeaderProgress.setVisibility(View.GONE);
        }

        private TeacherInfo getTeacherInfo(String jsonData) {
            JSONObject filesJson = null;
            TeacherInfo teacherInfo = new TeacherInfo();
            try {
                filesJson = new JSONObject(jsonData);
                teacherInfo.setId(filesJson.getInt("id"));
                teacherInfo.setName(filesJson.getString("name"));
                teacherInfo.setType(filesJson.getInt("type"));
                teacherInfo.setOnline(filesJson.getInt("online"));
                try {
                    teacherInfo.setCourses(filesJson.getString("courses"));
                } catch (JSONException ignored) {
                }
                teacherInfo.setClosed(filesJson.getInt("closed"));
                teacherInfo.setAlias(filesJson.getString("alias"));
                teacherInfo.setAgent(filesJson.getInt("agent"));
                try {
                    String[] deps = filesJson.getString("department").split(",");
                    for (String dep : deps) {
                        teacherInfo.addDepartment(Integer.valueOf(dep));
                    }
                    teacherInfo.setUpqualification(filesJson.getString("upqualification"));
                    teacherInfo.setRank(filesJson.getInt("rank"));
                    teacherInfo.setDegree(filesJson.getInt("degree"));
                    teacherInfo.setBio(filesJson.getString("bio"));
                    teacherInfo.setPlan(filesJson.getInt("plan"));
                    teacherInfo.setFact(filesJson.getInt("fact"));
                    try {
                        teacherInfo.setActivity(filesJson.getDouble("activity"));
                    } catch (JSONException ignored) {
                    }
                    try {
                        teacherInfo.setDepartmentName(filesJson.getString("department_name"));
                    } catch (JSONException ignored) {
                    }
                    try {
                        teacherInfo.setFacultyName(filesJson.getString("faculty_name"));
                    } catch (JSONException ignored) {
                    }

                    JSONArray timetableCoursesJson = filesJson.getJSONArray("timetable_courses");
                    ArrayList<TeacherTimetableCourses> teacherTimetableCoursesArrayList = new ArrayList<>();
                    TeacherTimetableCourses teacherTimetableCourses;

                    for (int i = 0; i < timetableCoursesJson.length(); i++) {
                        JSONObject timeTableCourse = timetableCoursesJson.getJSONObject(i);
                        teacherTimetableCourses = new TeacherTimetableCourses();
                        teacherTimetableCourses.setId(timeTableCourse.getInt("id"));
                        teacherTimetableCourses.setName(timeTableCourse.getString("name"));
                        teacherTimetableCourses.setDescription(timeTableCourse.getString("description"));
                        teacherTimetableCourses.setCat(timeTableCourse.getInt("cat"));
                        teacherTimetableCourses.setType(timeTableCourse.getInt("type"));
                        teacherTimetableCourses.setDepartment(timeTableCourse.getInt("department"));
                        teacherTimetableCourses.setClient(timeTableCourse.getInt("client"));
                        teacherTimetableCoursesArrayList.add(teacherTimetableCourses);
                    }
                    teacherInfo.setTeacherTimetableCourses(teacherTimetableCoursesArrayList);
                } catch (JSONException ignored) {
                    teacherInfo.setType(0);
                }
                return teacherInfo;

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return new TeacherInfo();
        }
    }

}
