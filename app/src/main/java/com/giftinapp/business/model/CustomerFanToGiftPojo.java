package com.giftinapp.business.model;


import androidx.annotation.Keep;

@Keep
public class CustomerFanToGiftPojo {
    public String CustomerToGiftEmail;
    public Integer CustomerToGiftRewardCoin;
    public String firstName;

    public  CustomerFanToGiftPojo(){}

    @Override
    public String toString() {
        return
                "first name-" + firstName +
                ", email-" + CustomerToGiftEmail +  ", amount-" + CustomerToGiftRewardCoin;
    }
}
