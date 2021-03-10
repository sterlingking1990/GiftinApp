package com.giftinapp.merchant.business;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MerchantExpandableListDataPump {
    public static HashMap<String, List<String>> getData() {
        HashMap<String, List<String>> expandableListDetail = new HashMap<String, List<String>>();

        List<String> giftinAppIntroHead = new ArrayList<String>();
        giftinAppIntroHead.add("As a Business, you gifts when you buy certain products. Depending on the " +
                "terms and condition of your business, you reward loyal customers with some amount of money. GiftinApp makes it " +
                "easy by allowing you to reward your customers with any amount you can afford while still letting them know you care so much");

        List<String> availableGiftsHead = new ArrayList<String>();
        availableGiftsHead.add("GiftinApp Inc., is a gifting company that delivers care and hospitality to your customer " +
                "through gifts; these gifts are chosen by your customers but curated by GiftinApp Inc., with the help of experts who understands the inVogue " +
                "things your customers might love as a gift when you continue to reward them as they patronise your businesses");


        List<String> walletFunding = new ArrayList<String>();
        walletFunding.add("To gifts customers, you need to have fund in your GiftinApp wallet, this is easy as you can fund with amount from 1000 above. Payment is processed in partnership with " +
                "the reputable payment company Paystack to ensure secure and flexible payment");


        List<String> makingCustomersHappy = new ArrayList<String>();
        makingCustomersHappy.add("Every little amount you gift your customers help them reach their gift mark and makes them happy!. GiftinApp is sure you value your customers as much as we do ");

        List<String> redeemingGiftsHead = new ArrayList<String>();
        redeemingGiftsHead.add("When customers gift mark is reached, GiftinApp reaches out to your customer and makes sure they get the treat they deserve" +
                " for being an amazing customer. We let them know you contributed to their happiness in a memorable way ");



        List<String> publicisingYou = new ArrayList<String>();
        publicisingYou.add("Businesses that use GiftinApp are trusted businesses that understands the value of her customer. All users of GiftinApp as a customer always sees all businesses that reward any of her customer");


        expandableListDetail.put("GiftinApp For Business", giftinAppIntroHead);
        expandableListDetail.put("Available Gifts with GiftinApp", availableGiftsHead);
        expandableListDetail.put("Before you can Gift your Customers", walletFunding);
        expandableListDetail.put("What makes your customers Happy", makingCustomersHappy);
        expandableListDetail.put("How we treat your customers", redeemingGiftsHead);
        expandableListDetail.put("Does Customers Know Businesses That are Rewarding Her Customers", publicisingYou);
        return expandableListDetail;
    }
}
