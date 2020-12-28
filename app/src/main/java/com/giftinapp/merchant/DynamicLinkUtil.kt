package com.giftinapp.merchant

import android.net.Uri
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks

object DynamicLinkUtil {


        // [START ddl_generate_content_link]
        fun generateContentLink(): Uri {
            val baseUrl = Uri.parse("https://example.com")
            val domain = "https://giftinapp.page.link"

            val link = FirebaseDynamicLinks.getInstance()
                    .createDynamicLink()
                    .setLink(baseUrl)
                    .setDomainUriPrefix(domain)
                    .setAndroidParameters(DynamicLink.AndroidParameters.Builder("com.giftinapp.merchant").build())
                    .buildDynamicLink()

            return link.uri
        }
        // [END ddl_generate_content_link]
}