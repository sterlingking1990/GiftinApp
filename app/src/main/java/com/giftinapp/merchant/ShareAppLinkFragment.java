package com.giftinapp.merchant;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;

import java.net.URISyntaxException;

public class ShareAppLinkFragment extends Fragment {
    private Button btnShareGiftinAppLink;
    private TextView tvGiftinAppLink;
    public DynamicLinkUtil dynamicLinkUtil;
    public SessionManager sessionManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_share_app_link, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        btnShareGiftinAppLink=view.findViewById(R.id.btnShareGiftinAppLink);
        tvGiftinAppLink=view.findViewById(R.id.tvGiftinAppLink);

        sessionManager=new SessionManager(requireContext());


        btnShareGiftinAppLink.setOnClickListener(v -> {
            shareAppLink();
        });
    }

    private void shareAppLink() {

        String link = "https://giftinapp.page.link/getgifts/?link=gifting.com/?invitedBy=" + sessionManager.getEmail();

        FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse(link))
                .setDomainUriPrefix("https://giftinapp.page.link")
                .setAndroidParameters(
                        new DynamicLink.AndroidParameters.Builder("com.giftinapp.merchant")
                                .build())
                .buildShortDynamicLink()
                .addOnSuccessListener(new OnSuccessListener<ShortDynamicLink>() {
                    @Override
                    public void onSuccess(ShortDynamicLink shortDynamicLink) {
                        Uri mInvitationUrl = shortDynamicLink.getShortLink();

                        // ...
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("text/plain");
                        intent.putExtra(Intent.EXTRA_TEXT, mInvitationUrl.toString());
                        startActivity(Intent.createChooser(intent, "Share GiftinApp With"));
                    }
                });
    }
}