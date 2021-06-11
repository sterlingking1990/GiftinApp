package com.giftinapp.business.utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ExpandableListDataPump {
    public static HashMap<String, List<String>> getData() {
        HashMap<String, List<String>> expandableListDetail = new HashMap<String, List<String>>();

        List<String> brandibleIntroHead = new ArrayList<String>();
        brandibleIntroHead.add("Brandible is a brand promotion app where Influencers are kings because they are the enablers and drivers for brands. " +
                " Influencers are giving the opportunity to to grow and earn while helping brands gain visibility in the market space ");

        List<String> beingBrandibleInfluencer = new ArrayList<String>();
        beingBrandibleInfluencer.add("Being an enviable Brandible Influencer means to not see brandible as a get rich quick platform but as a platform to distinguish yourself and shine out as a brand mover and enabler. " +
                "An Influencer in Brandible is ready to engage with brands calling for promotion of their products across social media. " + " She is passionate and wants to really be seen as a unique Influencer " +
                " She is not about the money but her fulfilment comes from helping businesses and brands get a voice or visibility as fast as possible");


        List<String> chargingBrandsInBrandible = new ArrayList<String>();
        chargingBrandsInBrandible.add(" When a brand has posted her interest to be promoted by you the Influencer, you are to engage via whatsApp with the brand, carry out the task successfully and to the best of your expertise " +
                "After which the brand is said to pay what you both have bargained for during conversation. " + " Charging brands should be in consideration that they are small and yet thriving so you are willing to give them that helping hand to do so as fast as possible ");


        List<String> paymentChannel = new ArrayList<String>();
        paymentChannel.add("You should request the brand to pay you via the app by providing the brand your Brandible Influencer Id which you can always set from the Update Info menu. " +
                "This is also important as it helps us to track your performance which we will use in the future to recommend you to brands seeking Influencers with some levels of performance");

        List<String> rewardTypesInBrandible = new ArrayList<String>();
        rewardTypesInBrandible.add("you are rewarded when you are among the target number set by the brand to view her status. " + " You also receive reward from the brand after carrying out activity which leads to the brands visibility for a particular promotional status");

        List<String> pointsInBrandible = new ArrayList<String>();
        pointsInBrandible.add("Brandible measures your participation within the app to gather you points which translates also to levels such as pioneer, artic etc. This in future will be a driver for brands to decide which Influencer to work with. "+
                "Hence, you are to be pro-active and be willing to do your best as an Influencer who has a driving goal for brands visibility");

        List<String> receivingPayments = new ArrayList<String>();
        receivingPayments.add("As soon as you are rewarded from the completion of a status promotional task, once you are paid from the platform by the brand, make sure the brand rates you from his own Rate an Influencer menu " +
                "Rating you goes a long way to helping you improve and get you a place in the future during recommendation. " +
                " When your rewards is above #3000, you will see a cashout menu which when triggered will inform Brandible about your interest to withdrew your reward and then follow up with you on that process conveniently");

        List<String> taskToExpectFromABrand = new ArrayList<String>();
        taskToExpectFromABrand.add("Some brands might require you to test out their products and make a video about how it feels for you. " +
        "Other brands might require you to just share their products or service banner across your social media platform " +
                "You might also be asked to write about the brand or make a video which centers on certain product or service the brand offers. " +
                "In all, you are to see yourself as an Influencer willing and driven by the goal of helping the brand get visibility and as fast as possible");


        expandableListDetail.put("Brandible For Influencer", brandibleIntroHead);
        expandableListDetail.put("Being an enviable Brandible Influencer", beingBrandibleInfluencer);
        expandableListDetail.put("Charging Brands on Promotional Tasks", chargingBrandsInBrandible);
        expandableListDetail.put("What appropriate channel should I request to be paid into", paymentChannel);
        expandableListDetail.put("Reward types in Brandible", rewardTypesInBrandible);
        expandableListDetail.put("How Influencers are awarded points and levels", pointsInBrandible);
        expandableListDetail.put("Receiving Payments and rating", receivingPayments);
        expandableListDetail.put("What kind of task can i expect from a brand", taskToExpectFromABrand);
        return expandableListDetail;
    }
}
