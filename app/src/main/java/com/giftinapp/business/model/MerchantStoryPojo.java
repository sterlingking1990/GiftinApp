package com.giftinapp.business.model;

import androidx.annotation.Keep;

import java.io.Serializable;
import java.util.ArrayList;

@Keep
public class MerchantStoryPojo implements Serializable {
    public String merchantId;
    public String storyOwner;
    public ArrayList<MerchantStoryListPojo> merchantStoryList;

    public MerchantStoryPojo(){}
}
