package com.android.esprit.smartreminders.Entities;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.android.esprit.smartreminders.Enums.DayOfTheWeek;
import com.android.esprit.smartreminders.Enums.StateOfTask;
import com.android.esprit.smartreminders.Exceptions.NotAValidStateOfTask;
import com.android.esprit.smartreminders.appcommons.App;
import com.android.esprit.smartreminders.sessions.Session;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public abstract class AbstractEventOrTask implements Entity {
    protected int id;
    protected String title;
    protected StateOfTask state;
    protected String description;
    protected Set<DayOfTheWeek> days;
    protected User owner;

    public AbstractEventOrTask() {
    }

    public AbstractEventOrTask(StateOfTask state, String description, Set<DayOfTheWeek> days, User owner) {
        this.state = state;
        this.description = description;
        this.days = days;
        this.owner = owner;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDays(Set<DayOfTheWeek> days) {
        this.days = days;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public StateOfTask getState() {
        return state;
    }

    public void setState(StateOfTask state) {
        this.state = state;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<DayOfTheWeek> getDays() {
        return days;
    }

    public void setDay(Set<DayOfTheWeek> days) {
        this.days = days;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractEventOrTask that = (AbstractEventOrTask) o;
        return state == that.state &&
                Objects.equals(description, that.description) &&
                days == that.days;
    }

    @Override
    public int hashCode() {

        return Objects.hash(state, description, days);
    }

    @Override
    public String toString() {
        return "state=" +
                state +
                "," +
                "Description=" +
                description +
                ", " +
                "Day of Week=" +
                days +
                "," +
                "title=" +
                title +
                "," +
                "owner=" +
                owner;
    }

    @Override
    public void FromJsonObject(JSONObject ja) throws JSONException, NotAValidStateOfTask {
        this.id = ja.getInt("id");
       // System.out.println("id of AbstractEvent of task="+id);
        this.state = StateOfTask.fromString(ja.getString("state"));
        this.description = ja.getString("description");
        this.title = ja.getString("title");
        User u = new User();


        this.owner = u;
        Set<DayOfTheWeek> days = new HashSet<>();
        String stringdays=ja.get("days").toString();
        String res= stringdays.replaceAll("\\:",":");
        System.out.println(res);
        JSONArray jsa = new JSONArray(res);
        for (int i = 0; i < jsa.length(); i++) {
            days.add(DayOfTheWeek.valueOf(((JSONObject) jsa.get(i)).get("day").toString()));
        }
        this.days=new HashSet<>();
        this.days.addAll(days);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public JSONObject ToJsonObject() throws JSONException {
        JSONArray days = new JSONArray();
        this.days.forEach((d) -> {
            try {
                days.put(new JSONObject().put("day", d.toString()));
            } catch (JSONException j) {
                Log.d("Entity :EventOrTask", "ToJsonObject: " + j.getMessage());
            }
        });
        return
                new JSONObject()
                        .put("id", this.id)
                        .put("description", this.description)
                        .put("title", this.title)
                        .put("state", this.state.toString())
                        .put("days", days)
                        .put("user", this.owner.getId());
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public Map<String, String> ToPostMap() {
        Map<String, String> res = new HashMap<>();
        res.put("description", this.description);
        res.put("id", this.id + "");
        res.put("title", this.title);
        res.put("state", this.state.toString());
        res.put("user",this.owner.getId()+"");
        JSONArray days = new JSONArray();
        this.days.forEach((d) -> {
            try {
                days.put(new JSONObject().put("day", d.toString()));
            } catch (JSONException j) {
                Log.d("Entity :EventOrTask", "ToJsonObject: " + j.getMessage());
            }
        });
        res.put("days", days.toString());
        return res;
    }

    public boolean isMemberOfToday() {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        return days.contains(DayOfTheWeek.DayOfWeekForID(day));
    }
}
