package org.ucomplex.ucomplex.Model.Calendar;

import android.graphics.Color;
import android.graphics.drawable.Drawable;

import com.amulyakhare.textdrawable.TextDrawable;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;


/**
 * Created by Sermilion on 23/12/2015.
 */
public class CalendarDayDecorator implements DayViewDecorator {

    private String color;
    private String year;
    private String month;
    private HashSet<CalendarDay> dates;
    String[] colors = {"#51cde7","#fecd71","#9ece2b","#d18ec0","#fe7877","#8ea3d1","#c3ccd3"};
    //"Занятие",
    //"Аттестация",
    //"Экзамен",
    //"Индивидуальное занятие"

    public CalendarDayDecorator(UCCalendar calendar, int type ) {
        year = calendar.getYear();
        month = calendar.getMonth();
        this.color = colors[type];
        if(type<4){
            dates = changedDaysToCalendarDays(calendar.getChangedDays(),type);
        }else if(type==4){
            dates = eventsToCalendarDays(calendar.getEvents());
        }else if(type==5){
            dates = timetableDaysToCalendarDays(calendar.getTimetable().getEntries());
        }else if(type==6){
            dates = new HashSet<>();
            Calendar cal = Calendar.getInstance();
            int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
            String dayOfMonthStr = String.valueOf(dayOfMonth);
            dates.add(CalendarDay.from(Integer.parseInt(year),Integer.parseInt(month), dayOfMonth));
        }

    }

    public CalendarDayDecorator(ArrayList<ChangedDay> days, String year, String month, int type ) {
        this.year = year;
        this.month = month;
        this.color = colors[type];
        dates = changedDaysToCalendarDays(days,type);


    }

    public CalendarDayDecorator(){

    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        if(dates.contains(day)) {
            return true;
        }
        return false;
    }


    @Override
    public void decorate(DayViewFacade view) {
        TextDrawable drawable = TextDrawable.builder().beginConfig()
                .width(20)
                .height(20)
                .endConfig()
                .buildRound(null, Color.parseColor(color));

        view.setBackgroundDrawable(drawable);

    }



    private HashSet<CalendarDay> changedDaysToCalendarDays(List changedDays, int type){
        ArrayList<ChangedDay> list = (ArrayList<ChangedDay>) changedDays;
        HashSet<CalendarDay> dates = new HashSet<>();
        for(ChangedDay day: list){
            int dayInt = day.getDay();
            CalendarDay calendarDay = CalendarDay.from(Integer.parseInt(year), Integer.parseInt(month)-1,dayInt);
            for(Lesson lesson: day.getLessons()){
                if(lesson.getType()==type){
                    dates.add(calendarDay);
                }
            }
        }
        return dates;
    }

    private HashSet<CalendarDay> eventsToCalendarDays(List events){
        HashSet<CalendarDay> dates = new HashSet<>();
        ArrayList<CalendarEvent> list = (ArrayList<CalendarEvent>) events;
        for(CalendarEvent event: list){
            String date = event.getStart();
            String[] dateList = date.split("-");
            int year = Integer.parseInt(dateList[0]);
            int month = dateList[1].charAt(0) == '0' ? Character.getNumericValue(dateList[1].charAt(1)) : Integer.valueOf(dateList[1]);
            int day = dateList[2].charAt(0)== '0' ? Character.getNumericValue(dateList[2].charAt(1)) : Integer.valueOf(dateList[2]);
            CalendarDay calendarDay = CalendarDay.from(year, month-1,day);
            dates.add(calendarDay);
        }
        return dates;
    }


    private HashSet<CalendarDay> timetableDaysToCalendarDays(List timetableDays){
        HashSet<CalendarDay> dates = new HashSet<>();
        ArrayList<HashMap<String, String>> list = (ArrayList<HashMap<String, String>>) timetableDays;
        for(HashMap<String, String> day : list){
            CalendarDay calendarDay = CalendarDay.from(Integer.parseInt(year), Integer.parseInt(month)-1, Integer.parseInt(day.get("day")));
            dates.add(calendarDay);
        }
        return dates;
    }
}