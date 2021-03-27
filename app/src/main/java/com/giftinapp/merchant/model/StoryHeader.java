package com.giftinapp.merchant.model;

import androidx.annotation.Keep;

import java.util.ArrayList;

@Keep
public class StoryHeader {
    public String merchantId;
    public ArrayList<StoryHeaderPojo> storyHeaderPojos;

    public StoryHeader(){}
}