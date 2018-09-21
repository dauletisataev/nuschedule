package com.example.admin.nuschedule.room_model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;
import java.util.HashMap;

import static com.example.admin.nuschedule.other.Constants.dayOfWeek;

/**
 * Created by Lenovo on 07.02.2018.
 */
@Entity(tableName = "lesson")
public class LessonModel {

    @PrimaryKey(autoGenerate = true)
    private long id;
    private long color;
    private String startTime;
    private String title, endTime, instructor, room, type, description;
    private String day;
    private Boolean repeat, hasTask;
    private long date;




    public LessonModel(){

    }
    public LessonModel(String tite, String startTime, String endTime, String instructor, String room, String day, long color, String type, String description) {
        this.title = tite;
        this.startTime = startTime;
        this.endTime = endTime;
        this.instructor = instructor;
        this.room = room;
        this.day = day;
        this.color = color;
        this.type = type;
        this.description = description;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getInstructor() {
        return instructor;
    }

    public void setInstructor(String instructor) {
        this.instructor = instructor;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getRepeat() {
        return repeat;
    }

    public void setRepeat(Boolean repeat) {
        this.repeat = repeat;
    }

    public Boolean getHasTask() {
        return hasTask;
    }

    public void setHasTask(Boolean hasTask) {
        this.hasTask = hasTask;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String toString(){
        return "{"+id+";"+startTime+"; "+title+"; "+room+"; "+type+"; "+color+"}";
    }

    public long getColor() {
        return color;
    }

    public void setColor(long color) {
        this.color = color;
    }
    public int getDayIndex(){
        return Arrays.asList(dayOfWeek).indexOf(day);
    }
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof LessonModel)) return false;
        LessonModel o = (LessonModel) obj;
        return o.getId() == this.getId();
    }

}