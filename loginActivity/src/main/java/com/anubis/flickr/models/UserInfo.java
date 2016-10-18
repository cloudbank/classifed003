package com.anubis.flickr.models;

/**
 * Created by sabine on 10/17/16.
 */
public class UserInfo   {
    //hybrid wrapper for rxjava zip call

    private Who who;
    private Photos friends;

    public UserInfo(Who who, Photos friends) {
        this.who = who;
        this.friends = friends;
    }

    public Who getWho() {
        return who;
    }

    public void setWho(Who who) {
        this.who = who;
    }

    public Photos getFriends() {
        return friends;
    }

    public void setFriends(Photos friends) {
        this.friends = friends;
    }
}