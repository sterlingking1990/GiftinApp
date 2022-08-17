package com.giftinapp.business

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.net.Uri
import android.util.Log
import android.util.SparseArray
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import co.paystack.android.PaystackSdk
import com.facebook.ads.internal.bridge.gms.AdvertisingId
import com.giftinapp.business.databinding.ActivityMerchantBinding
import com.giftinapp.business.utility.RemoteConfigUtil
import com.giftinapp.business.utility.SessionManager
import com.giftinapp.business.utility.base.BaseActivity
import com.google.android.gms.ads.MobileAds
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.squareup.picasso.Picasso
import com.synnapps.carouselview.CarouselView
import com.synnapps.carouselview.ViewListener
import dagger.hilt.android.AndroidEntryPoint
import smartdevelop.ir.eram.showcaseviewlib.GuideView
import smartdevelop.ir.eram.showcaseviewlib.config.DismissType
import javax.inject.Inject
import kotlin.properties.Delegates

@AndroidEntryPoint
open class MerchantActivity : BaseActivity<ActivityMerchantBinding>() {
    @Inject
    lateinit var firebaseRemoteConfig: FirebaseRemoteConfig

    private lateinit var navController: NavController
    private lateinit var binding: ActivityMerchantBinding
    var bottomNavigation: BottomNavigationView? = null
    var sessionManager: SessionManager? = null
    private var appUpdateManager: AppUpdateManager? = null
    private val mAuth = FirebaseAuth.getInstance()
    var remoteConfigUtil: RemoteConfigUtil? = null
    var btnExploreBrand: Button? = null
    var imageOne =
        "https://i0.wp.com/maboplus.com/wp-content/uploads/2019/08/1-91.jpg?resize=640,740&ssl=1"
    var imageTwo =
        "https://i0.wp.com/maboplus.com/wp-content/uploads/2019/08/1-91.jpg?resize=640,740&ssl=1"
    var imageThree =
        "https://i0.wp.com/maboplus.com/wp-content/uploads/2019/08/1-91.jpg?resize=640,740&ssl=1"
    private var carouselViewMerchant: CarouselView? = null
    private var holderListMerchant = SparseArray<MerchantReportsViewHolder>()
    private var numberOfCustomerGifted by Delegates.notNull<Int>()
    private var totalAmountGifted by Delegates.notNull<Long>()
    var totalWalletBalance: Long? = null
    private var t: ActionBarDrawerToggle? = null
    var counter = 0
    var following = 0
    private lateinit var navTextView: TextView


    private var adViewListener = ViewListener { position ->
        val customView = layoutInflater.inflate(R.layout.single_item_carousel_ad_view, null)
        val labelTextView = customView.findViewById<View>(R.id.adDescription) as TextView
        val merchantBrandImageView = customView.findViewById<View>(R.id.adImageView) as ImageView
        when (position) {
            0 -> {
                val remoteConfigUtil = RemoteConfigUtil()
                imageOne = remoteConfigUtil.getCarouselOneImage()
                //labelTextView.setText(sampleTitles[position]);
                if (imageOne != "") {
                    Picasso.get().load(imageOne).into(merchantBrandImageView)
                }
                merchantBrandImageView.setOnClickListener {
                    Toast.makeText(
                        this@MerchantActivity,
                        "Hello",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            1 -> {
                val remoteConfigUtil = RemoteConfigUtil()
                imageTwo = remoteConfigUtil.getCarouselTwoImage()
                //labelTextView.setText(sampleTitles[position]);
                Picasso.get().load(imageTwo).into(merchantBrandImageView)
            }
            2 -> {
                val remoteConfigUtil = RemoteConfigUtil()
                imageThree = remoteConfigUtil.getCarouselThreeImage()
                //labelTextView.setText(sampleTitles[position]);
                Picasso.get().load(imageThree).into(merchantBrandImageView)
            }
        }
        binding.carouselView.indicatorGravity = Gravity.CENTER_HORIZONTAL or Gravity.TOP
        customView
    }
    var viewListener = ViewListener { position: Int ->
        @SuppressLint("InflateParams") val customView =
            layoutInflater.inflate(R.layout.single_item_merchant_carousel_report, null)
        val holder = MerchantReportsViewHolder()
        holder.reportValue = customView.findViewById(R.id.kpi_report_value)
        holder.reportName = customView.findViewById(R.id.kpi_report_name)
        holder.reportIcon = customView.findViewById(R.id.kpi_report_icon)
        when (position) {
            0 -> {
                numberOfCustomersGifted
                holder.reportName.text ="Total Influencers Rewarded"
                holder.reportIcon.setImageResource(R.drawable.happycustomer)
                val totalGiftCoinSum =
                    numberOfCustomerGifted.toLong()
                holder.reportValue.text = totalGiftCoinSum.toString()
                holderListMerchant.put(0, holder)
            }
            1 -> {
                walletBalance
                holder.reportName.text = "Wallet Balance"
                holder.reportValue.text = totalWalletBalance.toString()
                holder.reportIcon.setImageResource(R.drawable.gift_coin_icon)
                holderListMerchant.put(1, holder)
            }
            2 -> {
                numberOfFollowers
                holder.reportName.text = "Influencers following your Brand"
                holder.reportValue.text = sessionManager!!.getFollowingCount().toString()
                holder.reportIcon.setImageResource(R.drawable.influencer_following_brand_icon)
                holderListMerchant.put(2, holder)
            }
        }
        customView
    }

    private fun updateCounter(position: Int) {
        when (position) {
            0 -> {
                run {
                    walletBalance
                    val walletBalanceTotal =
                        if (totalWalletBalance == null) 0L else totalWalletBalance!!
                    holderListMerchant[1].reportValue.text = walletBalanceTotal.toString()
                }
            }
            1 -> {
                walletBalance
                val walletBalanceTotal =
                    if (totalWalletBalance == null) 0L else totalWalletBalance!!
                holderListMerchant[1].reportValue.text = walletBalanceTotal.toString()
            }
            2 -> {
                numberOfFollowers
                val numberOfFollowers = sessionManager!!.getFollowingCount()
                holderListMerchant[2].reportValue.text = numberOfFollowers.toString()
            }
        }
    }

    //brand impression count is the total number of brand stories that have gotten impressions i.e views, or likes or dms
    //usually gotten views
    private val brandImpressionCount: Unit
        get() {
            //brand impression count is the total number of brand stories that have gotten impressions i.e views, or likes or dms
            //usually gotten views
        }

    class MerchantReportsViewHolder {
        lateinit var reportValue: TextView
        lateinit var reportName: TextView
        lateinit var reportIcon: ImageView
    }
    // [END get_firestore_instance]

    // [START set_firestore_settings]
    private val walletBalance: Unit
        get() {
            val db = FirebaseFirestore.getInstance()
            // [END get_firestore_instance]

            // [START set_firestore_settings]
            val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
            db.firestoreSettings = settings
            db.collection("merchants").document(sessionManager!!.getEmail()!!)
                .collection("reward_wallet").document("deposit").get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        totalWalletBalance = 0L
                        val documentSnapshot = task.result
                        if (documentSnapshot.exists()) {
                            totalWalletBalance = documentSnapshot["merchant_wallet_amount"] as Long
                        }
                    } else {
                        totalWalletBalance = 0L
                    }
                }
        }

    private fun getTotalAmountGifted() {
        val db = FirebaseFirestore.getInstance()
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings
        db.collection("merchants").document(sessionManager!!.getEmail()!!)
            .collection("reward_statistics").document("customers").collection("customer_details")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    totalAmountGifted = 0L
                    for (queryDocumentSnapshot in task.result) {
                        val gift_coin_amount = queryDocumentSnapshot["gift_coin"] as Long
                        totalAmountGifted += gift_coin_amount
                    }
                } else {
                    totalAmountGifted = 0L
                }
            }
    }
    // [END get_firestore_instance]

    // [START set_firestore_settings]
    private val numberOfCustomersGifted: Unit
        get() {
            val db = FirebaseFirestore.getInstance()
            // [END get_firestore_instance]

            // [START set_firestore_settings]
            val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
            db.firestoreSettings = settings
            db.collection("merchants").document(sessionManager!!.getEmail()!!)
                .collection("reward_statistics").document("customers")
                .collection("customer_details").get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        numberOfCustomerGifted = 0
                        for (queryDocumentSnapshot in task.result) {
                            numberOfCustomerGifted += 1
                        }
                    } else {
                        numberOfCustomerGifted = 0
                    }
                }
        }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.merchant_menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (t!!.onOptionsItemSelected(item)) {
            return true
        }
        when (item.itemId) {
            R.id.merchant_refresh_page -> {
                val intent = Intent(this, MerchantActivity::class.java)
                startActivity(intent)
                return true
            }

            R.id.merchant_update_info -> {
                binding.carouselView.visibility = View.GONE
                navController.navigate(R.id.merchantInfoUpdate)
                return true
            }

            R.id.merchant_about_giftin -> {
                binding.carouselView.visibility = View.GONE
                navController.navigate(R.id.giftinAboutForMerchant)
                return true
            }

            R.id.merchant_exit -> {
                showMessageDialog(title = "Log out", message = "Do you want to continue to logout from Brandible?",
                    disMissable = false, hasNegativeBtn = true, negbtnText = "Cancel",posBtnText = "Yes", listener = {
                        mAuth.signOut()
                        sessionManager!!.clearData()
                        startActivity(Intent(this@MerchantActivity, SignUpActivity::class.java))
                        finish()
                    }
                )
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun selectDrawerItem(menuitem: MenuItem) {
//        if(menuitem.getItemId()==R.id.navigation_gift_customer_fan){
//            carouselViewMerchant.setVisibility(View.GONE);
//            GiftACustomerFragment giftACustomer = new GiftACustomerFragment();
//            openFragment(giftACustomer);
//        }

        when(menuitem.itemId){
            R.id.navigation_wallet_info->{
                binding.carouselView.visibility = View.GONE
                navController.navigate(R.id.walletInfo)
            }
            R.id.navigation_set_reward_deal->{
                binding.carouselView.visibility = View.GONE
                navController.navigate(R.id.setRewardDeal)
            }
            R.id.navigation_view_reward_deal->{
                binding.carouselView.visibility = View.GONE
                navController.navigate(R.id.merchantStoryList2)
            }
            R.id.navigation_merchant_follow_brands->{
                binding.carouselView.visibility = View.GONE
                navController.navigate(R.id.brandPreferenceFragment2)
            }
        }
        binding.merchantNavDrawerLayout.close()
    }

    override fun onBackPressed() {
        if ( binding.merchantNavDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.merchantNavDrawerLayout.closeDrawer(GravityCompat.START)
        } else {
            try {
                if (sessionManager!!.getCurrentFragment() != "CustomerRewardStoriesFragment") {
                    startActivity(Intent(this@MerchantActivity, MerchantActivity::class.java))
                    super.onBackPressed()
                }
            } catch (e: Exception) {
                //mAuth.signOut();
                //sessionManager.clearData();
//                startActivity(new Intent(MerchantActivity.this, SignUpActivity.class));
//                finish();
                //super.onBackPressed()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data != null) {
            Log.d("Data", data.toString())
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
    // [END get_firestore_instance]

    // [START set_firestore_settings]
    private val numberOfFollowers: Unit
        get() {
            val db = FirebaseFirestore.getInstance()
            // [END get_firestore_instance]

            // [START set_firestore_settings]
            val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
            db.firestoreSettings = settings
            db.collection("merchants").document(sessionManager!!.getEmail()!!)
                .collection("followers").get()
                .addOnCompleteListener { followersTask ->
                    if (followersTask.isSuccessful) {
                        val followersQuerry = followersTask.result
                        if (followersQuerry != null) {
                            following = followersQuerry.size()
                            sessionManager!!.setFollowingCount(following)
                            navTextView.text = resources.getString(
                                R.string.brand_name_and_status,
                               mAuth.currentUser?.email,
                                following.toString()
                            )
                        }
                    }
                }
        }


    private fun checkAppUpdate(){
        firebaseRemoteConfig.fetchAndActivate().addOnCompleteListener {
            if (it.isSuccessful) {
                var stringValue = ""

                stringValue = remoteConfigUtil?.getUpdateVersion()?.asString().toString()
                val updateMessage = remoteConfigUtil?.getUpdateMessage()
                val isForced = remoteConfigUtil?.getForceUpdate()?.asBoolean()
                Log.d("Version", stringValue)
                Log.d("Config", BuildConfig.VERSION_CODE.toString())
                if (BuildConfig.VERSION_CODE.toString() != stringValue) {
                    if (isForced == true) {
                        showMessage(false, false, message = updateMessage.toString(),null){
                            openPlayStore()
                        }
                    }else{
                        if (isForced != null) {
                            showMessage(true,true, updateMessage.toString(),"Cancel"){
                                openPlayStore()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun openPlayStore() {
        val appPackageName = packageName

        try {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=$appPackageName")
                )
            )
        } catch (anfe: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
                )
            )
        }
    }

    companion object {
        private const val MY_REQUEST_CODE = 103
    }

    override fun getActivityBinding(inflater: LayoutInflater): ActivityMerchantBinding {
        binding = ActivityMerchantBinding.inflate(layoutInflater)

        val navHostFrag = supportFragmentManager.findFragmentById(R.id.merchant_act_nav_host_fragment) as NavHostFragment
        navController = navHostFrag.findNavController()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.merchantNavView.setupWithNavController(navController)

        MobileAds.initialize(this) { initializationStatus ->
            val statusMap =
                initializationStatus.adapterStatusMap
            for (adapterClass in statusMap.keys) {
                val status = statusMap[adapterClass]
                Log.d(
                    "MyApp", String.format(
                        "Adapter name: %s, Description: %s, Latency: %d",
                        adapterClass, status!!.description, status.latency
                    )
                )
            }
        }
        checkAppUpdate()

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        PaystackSdk.initialize(applicationContext)
        // Returns an intent object that you use to check for an update.
        sessionManager = SessionManager(applicationContext)
        numberOfFollowers
        t = ActionBarDrawerToggle(
            this,
            binding.merchantNavDrawerLayout,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )

        binding.merchantNavDrawerLayout.addDrawerListener(t!!)
        t!!.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.merchantNavView.setNavigationItemSelectedListener { item: MenuItem ->
            selectDrawerItem(item)
            true
        }
        val headerView = binding.merchantNavView.getHeaderView(0)
        navTextView = headerView.findViewById(R.id.nav_header_textView)
        val navImageView = headerView.findViewById<ImageView>(R.id.nav_header_imageView)
        //        Picasso.get().load(R.drawable.ic_brandible_icon).into(navImageView);
        navTextView.text = resources.getString(
            R.string.brand_name_and_status,
            mAuth.currentUser?.email,
            following.toString()
        )
        binding.carouselView.pageCount = 3
        //binding.carouselView.setViewListener(viewListener);
        binding.carouselView.setViewListener(adViewListener)
        binding.carouselView.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                //updateCounter(position);
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
        //btnExploreBrand = findViewById(R.id.btnExploreBrand)
        remoteConfigUtil = RemoteConfigUtil()
        //binding.btnExploreBrand.setOnClickListener(View.OnClickListener { openWebView(remoteConfigUtil!!.getBrandLink()) })

        return binding
    }
}