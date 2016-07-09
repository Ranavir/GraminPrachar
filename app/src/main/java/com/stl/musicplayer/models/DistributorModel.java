package com.stl.musicplayer.models;

/**
 * Created by office on 29-Jun-16.
 */
public class DistributorModel {
    private String distributor_code ;
    private String name ;
    private String mobile_no ;

    public DistributorModel(String distributor_code, String mobile_no, String name) {
        this.distributor_code = distributor_code;
        this.mobile_no = mobile_no;
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public String getDistributor_code() {
        return distributor_code;
    }

    public void setDistributor_code(String distributor_code) {
        this.distributor_code = distributor_code;
    }

    public String getMobile_no() {
        return mobile_no;
    }

    public void setMobile_no(String mobile_no) {
        this.mobile_no = mobile_no;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
