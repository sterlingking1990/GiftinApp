package com.giftinapp.business.model;

import androidx.annotation.Keep;

@Keep
public class MyCartPojo{
    public String gift_name;
    public Integer gift_cost;
    public String gift_url;
    public Integer gift_track;
    public Boolean redeemable;
    public MyCartPojo(){}
}
