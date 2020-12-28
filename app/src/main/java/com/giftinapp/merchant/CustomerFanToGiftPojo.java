package com.giftinapp.merchant;


import androidx.annotation.Keep;

@Keep
public class CustomerFanToGiftPojo {
    public String CustomerToGiftEmail;
    public Integer CustomerToGiftRewardCoin;

    public  CustomerFanToGiftPojo(){}

    @Override
    public String toString() {
        return
                "Customer Email-" + CustomerToGiftEmail +
                ", Customer Reward Coin-" + CustomerToGiftRewardCoin;
    }
}
