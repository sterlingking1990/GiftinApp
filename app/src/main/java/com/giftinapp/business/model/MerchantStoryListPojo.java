package com.giftinapp.business.model;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

@Keep
 public class MerchantStoryListPojo implements Serializable {

    public String merchantStatusId = "";
    public String merchantOwnerId = "";
    public String merchantStatusImageLink = null;
    public String merchantStatusVideoLink = null;
    public String videoArtWork = null;
    public String storyTag = "";
    public String storyAudioLink = null;
    public String mediaDuration = "0";
    public Boolean seen;
    public ArrayList<String> viewers;
    public StatusReachAndWorthPojo statusReachAndWorthPojo;
    public Integer taskCount = 0;
    public SharableCondition sharableCondition = null;
    public String publishedAt = null;

    public String challengeType = "";

    public MerchantStoryListPojo (){}

}