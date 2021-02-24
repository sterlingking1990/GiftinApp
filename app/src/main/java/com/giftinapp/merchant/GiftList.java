package com.giftinapp.merchant;

import androidx.annotation.Keep;

import java.util.ArrayList;

@Keep
public class GiftList{
    public String gift_name;
    public Long gift_cost;
    public String gift_url;
    public ArrayList<String> category;
    public Business business;
    public GiftList(){}
}
