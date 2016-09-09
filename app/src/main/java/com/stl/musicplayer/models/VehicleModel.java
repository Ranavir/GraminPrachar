package com.stl.musicplayer.models;

/**
 * Created by office on 29-Jun-16.
 */
public class VehicleModel {
    private String vehicle_name ;
    private String vehicle_no ;
    private int sitting_capacity ;
    private String start_time ;
    private String end_time ;

    public VehicleModel() {
    }

    public VehicleModel(String vehicle_name, String vehicle_no) {
        this.vehicle_name = vehicle_name;
        this.vehicle_no = vehicle_no;
    }

    public VehicleModel(String vehicle_name, String vehicle_no,String start_time,String end_time,int sitting_capacity) {
        this.end_time = end_time;
        this.sitting_capacity = sitting_capacity;
        this.start_time = start_time;
        this.vehicle_name = vehicle_name;
        this.vehicle_no = vehicle_no;
    }

    @Override
    public String toString() {
        return vehicle_no;
    }



    public String getEnd_time() {
        return end_time;
    }

    public void setEnd_time(String end_time) {
        this.end_time = end_time;
    }

    public int getSitting_capacity() {
        return sitting_capacity;
    }

    public void setSitting_capacity(int sitting_capacity) {
        this.sitting_capacity = sitting_capacity;
    }

    public String getStart_time() {
        return start_time;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    public String getVehicle_name() {
        return vehicle_name;
    }

    public void setVehicle_name(String vehicle_name) {
        this.vehicle_name = vehicle_name;
    }

    public String getVehicle_no() {
        return vehicle_no;
    }

    public void setVehicle_no(String vehicle_no) {
        this.vehicle_no = vehicle_no;
    }
}
