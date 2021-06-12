package com.giftinapp.business.business;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MerchantExpandableListDataPump {
    public static HashMap<String, List<String>> getData() {
        HashMap<String, List<String>> expandableListDetail = new HashMap<>();

        List<String> giftinAppIntroHead = new ArrayList<>();
        giftinAppIntroHead.add("Brandible gives you as a brand the opportunity to grow and become visible to the market as fast as possible by leveraging on the influencers ability to perform brand assigned tasks " +
                "which can help your brand to scale and become visible in the market. " +
                "example of this brand tasks can be to ask influencer to share your product status on the platforms he is available at, or " +
                "make a video about any of your product or you can give the influencer your product to try out and tell people about it. " +
                "These and more you can think of. " +
                "When this task has been performed, you can then pay and rate the Influencer within the app");

        List<String> requestingInfluencerToPerformBrandTask = new ArrayList<>();
        requestingInfluencerToPerformBrandTask.add("Requesting an Influencer to perform brand task is as simple as uploading a status about it. " +
                "The Set Deal Menu provides a checkbox to use promotional banner which indicate your interest that you want an Influencer to engage with. " +
                "The Influencer sees your Interest on status and pull up chat with you right in your whatsApp if you had set your phone number right else he chats with Brandible agent which then tries to reach to you" );


        List<String> settingProductsAsStatus = new ArrayList<>();
        settingProductsAsStatus.add("The Set Deal Menu enables you to add status of products you will like influencers on the platform to see. " +
                "However with as little as #2 per status view per influencer, you can decide how many Influencers to target with default being 50.");


        List<String> decidingAnInfluencerForMyBrand = new ArrayList<>();
        decidingAnInfluencerForMyBrand.add("For now, you can work with the Influencers signed into the app, "+
                "but subsequently we will be exposing Influencers rating, points and level so that you can use those to make decision of an Influencer to work with");

        List<String> payingAnInfluencer = new ArrayList<>();
        payingAnInfluencer.add("Because our goal and mission is to help small and medium business get visibility as fast and as cheap as possible, " +
                "Our Influencers also know that, they work with you based on how much you can afford and the task at hand. " +
                "They understand they are not to overprice on task related to your brand promotion for any certain status" );



        List<String> mediumForPayment = new ArrayList<>();
        mediumForPayment.add("Brandible provides you with wallet system from paystack payment systems. With this, you can fund your wallet once and then pay one or more Influencers at once from the Reward an Influencer Menu without having to do several bank transactions outside the app. "+
                "After paying an Influencer for a particular status task been completed, you are expected to rate the influencer for that particular status task been completed. " +
                "Note also that paying from the platform is not compulsory but it saves your from making several bank transactions when you have to pay more influencers." );

        List<String> importantOfRatingInfluencer = new ArrayList<>();
        importantOfRatingInfluencer.add("When you rate an Influencer, it helps the Influencer to know if he has done well or he needs to do better next time. It also helps Brandible to suggest better Influencer to you in the future");


        List<String> differenceFromOthers = new ArrayList<>();
        differenceFromOthers.add("Brandible is unique in the sense that it draws several users to first view your status because you promise a certain worth on each of your status as is been viewed, "
                + " More so, you are focused on promoting your brand hence, Influencers on Brandible know that statuses are filled with products waiting to be promoted. " +
                " Brandible opens you up to serious and numerous passionate Influencers who have one purpose, to scale your brand and they are proud of seeing your status, engaging in conversation with you via whatsApp and pushing your brand outside the market as fast and as cheap as possible");

        List<String> deductingBalance = new ArrayList<>();
        deductingBalance.add("When you upload status by setting the status worth which will be paid to any Influencer who visits that particular status, and the status reach or target for the number of influencers that worth will be paid, " +
                "Brandible denotes this as status budget however it is not deducted until Influencers begin viewing your status. However, the total of the status worth and reach is computed and locked from your wallet as a status budget. " +
                "When Influencers begin viewing your status are they paid the worth from the status budget. You can decide to delete your status at anytime to stop the payment to influencers and stop reaching them with your status. " +
                "When you want to reward an Influencer, your status budget is excluded from the wallet balance at this time, you can choose to redeem it by deleting your status which then update your wallet accordingly");


        expandableListDetail.put("Brandible For Brands", giftinAppIntroHead);
        expandableListDetail.put("Requesting an Influencer to Perform Brand task", requestingInfluencerToPerformBrandTask);
        expandableListDetail.put("How do I set Products I sell as Status", settingProductsAsStatus);
        expandableListDetail.put("What Metrics do I use in Deciding the Influencer to work with", decidingAnInfluencerForMyBrand);
        expandableListDetail.put("How much is Ideal to pay an Influencer after a task completion", payingAnInfluencer);
        expandableListDetail.put("What Medium is appropriate to pay an Influencer", mediumForPayment);
        expandableListDetail.put("Why is rating an Influencer for a status task so important", importantOfRatingInfluencer);
        expandableListDetail.put("What Makes Brandible different from Other Platform", differenceFromOthers);
        expandableListDetail.put("How my wallet balance is deducted", deductingBalance);

        return expandableListDetail;
    }
}
