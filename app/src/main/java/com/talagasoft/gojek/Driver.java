package com.talagasoft.gojek;

/**
 * Created by compaq on 03/13/2016.
 */
public class Driver {
    private long id;
    private String name;
    private double lat;
    private double lng;
    private String phone;
    private int rate;

    public Driver(){}
    public Driver(long id, String name, double lat, double lng, String phone, int rate){
        super();
        this.id=id;
        this.name=name;
        this.lat=lat;
        this.lng=lng;
        this.phone=phone;
        this.rate=rate;
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }
}
