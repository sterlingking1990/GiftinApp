package com.giftinapp.business.model;

import androidx.annotation.Keep;

@Keep
public class RewardPojo {
    public String email;
    public long gift_coin;
    public Boolean isRedeemed;
    public String referrer;
    public String firstName;

    public RewardPojo(){}
}
