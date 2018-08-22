package com.craiovadata.sightsandsounds.model;

import com.google.firebase.firestore.IgnoreExtraProperties;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Restaurant POJO.
 */
@IgnoreExtraProperties
public class Entry {

    public static final String FIELD_CITY = "country";
    public static final String FIELD_CATEGORY = "img_title";
    public static final String FIELD_PRICE = "img_description";
    public static final String FIELD_POPULARITY = "numRatings";
    public static final String FIELD_AVG_RATING = "avgRating";

    private String id;
    private String country;
    private String img_title;
    private String photo;
    private String img_description;
    private int numRatings;
    private double avgRating;

    public Entry() {}

    public Entry(JSONObject object) throws JSONException {
        this.id = object.getString("id");
        this.country = object.getString("country");
        this.img_title = object.getJSONObject("img").getString("title");
        this.img_description = object.getJSONObject("img").getString("descr");
        this.numRatings = numRatings;
        this.avgRating = avgRating;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getImg_title() {
        return img_title;
    }

    public void setImg_title(String img_title) {
        this.img_title = img_title;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String  getImg_description() {
        return img_description;
    }

    public void setImg_description(String img_description) {
        this.img_description = img_description;
    }

    public int getNumRatings() {
        return numRatings;
    }

    public void setNumRatings(int numRatings) {
        this.numRatings = numRatings;
    }

    public double getAvgRating() {
        return avgRating;
    }

    public void setAvgRating(double avgRating) {
        this.avgRating = avgRating;
    }
}
