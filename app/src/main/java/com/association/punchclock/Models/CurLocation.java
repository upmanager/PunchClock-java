package com.association.punchclock.Models;

public class CurLocation {
    private double latitude ;
    private double longitude ;
    private String country;
    private String area;
    private String postal_code;
    private String city;
    public void setArea(String area) {
        this.area = area;
    }
    public void setCity(String city) {
        this.city = city;
    }
    public void setCountry(String country) {
        this.country = country;
    }
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    public void setPostal_code(String postal_code) {
        this.postal_code = postal_code;
    }
    public double getLatitude() {
        if(latitude > 0) return latitude;
        return 0;
    }
    public double getLongitude() {
        return longitude;
    }
    public String getArea() {
        return area;
    }
    public String getCity() {
        return city;
    }
    public String getCountry() {
        return country;
    }
    public String getPostal_code() {
        return postal_code;
    }
}
