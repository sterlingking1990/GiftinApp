package com.giftinapp.merchant.model;

import androidx.annotation.Keep;

@Keep
public class GiftinACustomerPojo {
    public String email;
    public long gift_coin;
    public Boolean isRedeemed;
    public String referrer;
    public String firstName;
    public String giftorId;
    public long latestReward;
    public GiftinACustomerPojo(){}
}
