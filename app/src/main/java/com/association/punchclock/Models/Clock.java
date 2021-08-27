package com.association.punchclock.Models;

public class Clock {
    public int id;
    public int state;
    public int workid;
    public String image;
    public Clock(int id, int state, int workid, String image){
        this.id = id;
        this.state = state;
        this.workid = workid;
        this.image = image;
    }

    public void setImage(String image) {
        this.image = image;
    }
    public String getStrId(){
        return String.valueOf(id);
    }
    public String getStrState(){
        return String.valueOf(state);
    }
    public String getStrWorkerid(){
        return String.valueOf(workid);
    }
}
