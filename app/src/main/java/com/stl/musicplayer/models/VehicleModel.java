package com.stl.musicplayer.models;

/**
 * Created by office on 29-Jun-16.
 */
public class VehicleModel {
    private String vehicle_name ;
    private String vehicle_no ;

    public VehicleModel(String vehicle_name, String vehicle_no) {
        this.vehicle_name = vehicle_name;
        this.vehicle_no = vehicle_no;
    }

    @Override
    public String toString() {
        return vehicle_no;
    }

    public String getName() {
        return vehicle_name;
    }

    public void setName(String vehicle_name) {
        this.vehicle_name = vehicle_name;
    }

    public String getNumber() {
        return vehicle_no;
    }

    public void setNumber(String vehicle_no) {
        this.vehicle_no = vehicle_no;
    }
}
