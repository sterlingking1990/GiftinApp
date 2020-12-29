package com.giftinapp.merchant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ExpandableListDataPump {
    public static HashMap<String, List<String>> getData() {
        HashMap<String, List<String>> expandableListDetail = new HashMap<String, List<String>>();

        List<String> giftinAppIntroHead = new ArrayList<String>();
        giftinAppIntroHead.add("As a customer, you get gifts when you buy certain products. Depending on the " +
                "terms and condition of the business, you are rewarded with some amount of money. Businesses use GiftinApp to" +
                "reward you when you continually buy from them");

        List<String> availableGiftsHead = new ArrayList<String>();
        availableGiftsHead.add("GiftinApp Inc., is a gifting company that delivers care and hospitality to customers of various business " +
                "through gifts; these gifts are choosen by customers but curated by GiftinApp Inc. with the help of experts who understands the inVogue " +
                "things you might love as a gift when you continue to buy from businesses");


        List<String> giftCartHead = new ArrayList<String>();
        giftCartHead.add("To add gifts to cart is just a single click on the gift in the gifts list; and to remove gifts added to cart is a" +
                "single click on the gifts in your cart");


        List<String> giftStatusHead = new ArrayList<String>();
        giftStatusHead.add("Each time you are gifted from any business using GiftinApp, your gift cart reflects the progress you have made to " +
                "reaching the gift mark");

        List<String> redeemingGiftsHead = new ArrayList<String>();
        redeemingGiftsHead.add("When your gifts mark is reached, GiftinApp reaches out to you and makes sure you get the treat you deserve" +
                "for being an amazing customer. If you feel you were not reached out; which does not happen, you should contact GiftinApp on 08060456301");



        List<String> giftingMerchantHead = new ArrayList<String>();
        giftingMerchantHead.add("Businesses that use GiftinApp are rewarding businesses that understands the value of her customer. They are verified and profiled");


        expandableListDetail.put("GiftinApp For Customer", giftinAppIntroHead);
        expandableListDetail.put("Available Gifts with GiftinApp", availableGiftsHead);
        expandableListDetail.put("Adding Gifts to Cart", giftCartHead);
        expandableListDetail.put("Checking Gift Status", giftStatusHead);
        expandableListDetail.put("Redeeming your Gifts", redeemingGiftsHead);
        expandableListDetail.put("Buying from Gifting Merchants", giftingMerchantHead);
        return expandableListDetail;
    }
}
