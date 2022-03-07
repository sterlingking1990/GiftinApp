package com.giftinapp.business.model;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

@Keep
 public class MerchantStoryListPojo implements Serializable {

    public String merchantStatusId = "";
    public String merchantStatusImageLink = "";
    public String storyTag = "";
    public String storyAudioLink = "";
    public Boolean seen;
    public ArrayList<String> viewers;
    public StatusReachAndWorthPojo statusReachAndWorthPojo;

    public MerchantStoryListPojo(){}

}