package com.potholecop.androidapp.pojo;

public class Location {

    private double latitude;
    private double longitude;

    public Location() {

    }

    public Location(double latitude, double longitude) {

        setLatitude(latitude);
        setLongitude(longitude);
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
}
