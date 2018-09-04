package com.craiovadata.sightsandsounds.model;


import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Entry {

    private String country;
    private String img_title;
    private String img_description;
    private String music_title;
    private String music_description;
    //    private double lat;
//    private double lon;
    private int numRatings;
    private double avgRating;

    public Entry(String country, String img_title, String img_description, String music_title, String music_description, int numRatings, double avgRating) {
        this.country = country;
        this.img_title = img_title;
        this.img_description = img_description;
        this.music_title = music_title;
        this.music_description = music_description;
//        this.lat = lat;
//        this.lon = lon;
        this.numRatings = numRatings;
        this.avgRating = avgRating;
    }

    public Entry() {
    }

//    public double getLat() {
//        return lat;
//    }

//    public void setLat(double lat) {
//        this.lat = lat;
//    }
//
//    public double getLon() {
//        return lon;
//    }
//
//    public void setLon(double lon) {
//        this.lon = lon;
//    }

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

    public String getImg_description() {
        return img_description;
    }

    public void setImg_description(String img_description) {
        this.img_description = img_description;
    }

    public String getMusic_title() {
        return music_title;
    }

    public void setMusic_title(String music_title) {
        this.music_title = music_title;
    }

    public String getMusic_description() {
        return music_description;
    }

    public void setMusic_description(String music_description) {
        this.music_description = music_description;
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
