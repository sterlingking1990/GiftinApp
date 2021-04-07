package com.giftinapp.merchant.model;

import androidx.annotation.Keep;

import java.util.ArrayList;

@Keep
public class MerchantStoryPojo {
    public String merchantId;
    public String storyOwner;
    public ArrayList<MerchantStoryListPojo> merchantStoryList;

    public MerchantStoryPojo(){}
}
