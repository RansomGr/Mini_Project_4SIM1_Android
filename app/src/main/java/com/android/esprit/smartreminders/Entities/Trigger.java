package com.android.esprit.smartreminders.Entities;

import com.android.esprit.smartreminders.Exceptions.NotAValidStateOfTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Trigger implements Entity {
    protected int id;
    protected String name;
    protected int icon;
    public Trigger(String name,int icon){
        this.name=name;
        this.icon=icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    @Override
    public void FromJsonObject(JSONObject ja) throws JSONException, NotAValidStateOfTask {
        this.id=ja.getInt("id");
        this.name= ja.getString("name");
        this.icon=ja.getInt("icon");
    }

    @Override
    public JSONObject ToJsonObject() throws JSONException {
        return
                new JSONObject()
                        .put("id", this.id)
                        .put("name",this.name)
                        .put("icon", icon);
    }

    @Override
    public Map<String, String> ToPostMap() {
        Map<String, String> res = new HashMap<>();
        res.put("name", this.name);
        res.put("id", this.id +"");
        res.put("icon",this.icon+"");
        return res;
    }
}
