package com.example.lithy.popularmovies;

import org.json.JSONException;
import org.json.JSONObject;

public class Review {

    private String id , author , content;

    public Review() { }

    public Review(JSONObject trailer) throws JSONException {
        this.id = trailer.getString("id");
        this.author = trailer.getString("author");
        this.content = trailer.getString("content");
    }

    public String getId() { return id; }

    public String getAuthor() { return author; }

    public String getContent() { return content; }
}
