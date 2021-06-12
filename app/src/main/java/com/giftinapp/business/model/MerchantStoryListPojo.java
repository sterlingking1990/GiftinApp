package com.giftinapp.business.model;

import androidx.annotation.Keep;

import java.util.ArrayList;

@Keep
 public class MerchantStoryListPojo {

    public String merchantStatusId;
    public String merchantStatusImageLink;
    public String storyTag;
    public Boolean seen;
    public ArrayList<String> viewers;
    public StatusReachAndWorthPojo statusReachAndWorthPojo;

    public MerchantStoryListPojo(){}
}