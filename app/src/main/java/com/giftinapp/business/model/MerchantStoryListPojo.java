package com.giftinapp.business.model;

import androidx.annotation.Keep;

import java.util.ArrayList;

@Keep
 public class MerchantStoryListPojo {

    public String merchantStatusId = null;
    public String merchantStatusImageLink = null;
    public String storyTag = null;
    public Boolean seen = false;
    public ArrayList<String> viewers;

    public MerchantStoryListPojo(){}
}