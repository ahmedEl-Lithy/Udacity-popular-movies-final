package com.example.lithy.popularmovies;

import org.json.JSONException;
import org.json.JSONObject;

public class Trailer {

    private String id , name , key , type , site;

    public Trailer() {}

    public Trailer(JSONObject trailer) throws JSONException {
        this.id = trailer.getString("id");
        this.name = trailer.getString("name");
        this.key = trailer.getString("key");
        this.type = trailer.getString("type");
        this.site = trailer.getString("site");
    }

    public String getId() {
        return id;
    }

    public String getKey() { return key; }

    public String getName() { return name; }

    public String getSite() { return site; }

    public String getType() { return type; }
}
