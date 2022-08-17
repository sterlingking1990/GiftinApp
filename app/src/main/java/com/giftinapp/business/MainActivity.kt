package com.giftinapp.business

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.net.Uri
import android.os.Build
import android.util.Log
import android.util.SparseArray
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout.DrawerListener
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.facebook.ads.internal.bridge.gms.AdvertisingId
import com.giftinapp.business.customer.AboutFragment
import com.giftinapp.business.customer.CashoutFragment
import com.giftinapp.business.customer.MerchantStoryList
import com.giftinapp.business.customer.SettingsFragment
import com.giftinapp.business.databinding.ActivityMainBinding
import com.giftinapp.business.model.FirebaseConfig
import com.giftinapp.business.model.ReferralRewardPojo
import com.giftinapp.business.utility.RemoteConfigUtil
import com.giftinapp.business.utility.SessionManager
import com.giftinapp.business.utility.StorySession
import com.giftinapp.business.utility.base.BaseActivity
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.tasks.Task
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
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.squareup.picasso.Picasso
import com.synnapps.carouselview.ImageListener
import com.synnapps.carouselview.ViewListener
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject
import java.util.*
import javax.inject.Inject
import kotlin.properties.Delegates

var imageOne =
    "https://i0.wp.com/maboplus.com/wp-content/uploads/2019/08/1-91.jpg?resize=640,740&ssl=1"
var imageTwo =
    "https://i0.wp.com/maboplus.com/wp-content/uploads/2019/08/1-91.jpg?resize=640,740&ssl=1"
var imageThree =
    "https://i0.wp.com/maboplus.com/wp-content/uploads/2019/08/1-91.jpg?resize=640,740&ssl=1"

@AndroidEntryPoint
open class MainActivity : BaseActivity<ActivityMainBinding>() {

    @Inject
    lateinit var firebaseRemoteConfig: FirebaseRemoteConfig

    private lateinit var navController: NavController
    private lateinit var binding: ActivityMainBinding
    var sessionManager: SessionManager? = null
    var storySession: StorySession? = null
    var builder: AlertDialog.Builder? = null
    private lateinit var navTextView: TextView
    var ivRating: ImageView? = null
    var remoteConfigUtil: RemoteConfigUtil? = null
    private var holderList = SparseArray<ReportsViewHolder>()
    protected var imageList = SparseArray<ImageView>()
    var totalGiftCoin = 0L
    private var latestAmountRedeemed: Long? = null
    var totalRatingForAllStatus by Delegates.notNull<Long>()
    var counter = 0
    var following = 0
    var posi: Int? = null
    var mauth: FirebaseAuth? = null
    private var t: ActionBarDrawerToggle? = null

    var rewardToBrcBase = 2

    var imageListener = ImageListener { position: Int, imageView: ImageView? ->
        when (position) {
            0 -> {
                val remoteConfigUtil = RemoteConfigUtil()
                val imageOne = remoteConfigUtil.getCarouselOneImage()
                Log.d("AmHere", imageOne)
                Picasso.get().load(imageOne).into(imageView)
            }
            1 -> {
                val remoteConfigUtil = RemoteConfigUtil()
                val imageTwo = remoteConfigUtil.getCarouselTwoImage()
                Log.d("AmHere", imageTwo)
                Picasso.get().load(imageTwo).into(imageView)
            }
            2 -> {
                val remoteConfigUtil = RemoteConfigUtil()
                val imageThree = remoteConfigUtil.getCarouselThreeImage()
                Log.d("AmHere", imageThree)
                Picasso.get().load(imageThree).into(imageView)
            }
        }
    }
    var viewListener = ViewListener { position: Int ->
        @SuppressLint("InflateParams") val customView =
            layoutInflater.inflate(R.layout.single_item_customer_carousel_report, null)
        val holder = ReportsViewHolder()
        holder.reportValue = customView.findViewById(R.id.kpi_report_value)
        holder.reportName = customView.findViewById(R.id.kpi_report_name)
        holder.reportIcon = customView.findViewById(R.id.kpi_report_icon)
        when (position) {
            0 -> {
                getTotalGiftCoin()
                holder.reportName.text = "Total Reward"
                holder.reportIcon.setImageResource(R.drawable.gift_coin_icon)
                val totalGiftCoinSum = if (totalGiftCoin == 0L) 0L else totalGiftCoin
                holder.reportValue.text = totalGiftCoinSum.toString()
                holderList.put(0, holder)
            }
            1 -> {
                holder.reportName.text = "Latest Redeemed Reward Worth"
                val latestAmount = if (latestAmountRedeemed == null) 0L else latestAmountRedeemed!!
                holder.reportValue.text = latestAmount.toString()
                holder.reportIcon.setImageResource(R.drawable.gift)
                holderList.put(1, holder)
            }
            2 -> {
                holder.reportName.text = "Influencer Point"
                val influencerPoint =
                    if (totalRatingForAllStatus == null) 0L else totalRatingForAllStatus!!
                holder.reportValue.text = influencerPoint.toString()
                holder.reportIcon.setImageResource(R.drawable.influencer_point_icon)
                holderList.put(2, holder)
            }
        }
        customView
    }

    private fun selectDrawerItem(menuitem: MenuItem) {
//        if (menuitem.getItemId() == R.id.navigation_gifting_history) {
//            carouselView.setVisibility(View.GONE);
//            btnExploreBrand.setVisibility(View.GONE);
//            MyGiftHistoryFragment myGiftHistoryFragment = new MyGiftHistoryFragment();
//            openFragment(myGiftHistoryFragment);
//        }
//        if (menuitem.getItemId() == R.id.navigation_gifting_merchant) {
//            carouselView.setVisibility(View.GONE);
//            GiftingMerchantFragment giftingMerchantFragment = new GiftingMerchantFragment();
//            openFragment(giftingMerchantFragment);
//        }
        if (menuitem.itemId == R.id.navigation_view_reward_deal) {
            binding.carouselView.visibility = View.GONE
            val merchantStoryList = MerchantStoryList()
           navController.navigate(R.id.merchantStoryList)
        }

//        if (menuitem.getItemId() == R.id.navigation_view_activity_rating) {
//            carouselView.setVisibility(View.GONE);
//            InfluencerActivityRatingFragment influencerActivityRatingFragment = new InfluencerActivityRatingFragment();
//            openFragment(influencerActivityRatingFragment);
//        }
        if (menuitem.itemId == R.id.navigation_view_brand_preference) {
            binding.carouselView.visibility = View.GONE
            navController.navigate(R.id.brandPreferenceFragment)
        }
        if (menuitem.itemId == R.id.navigation_referral_deal) {
            binding.carouselView.visibility = View.GONE
            navController.navigate(R.id.myReferralDealFragment)
        }
        binding.drawerLayout.close()
    }

    private fun updateCounter(position: Int) {
        when (position) {
            0 -> {
                getTotalGiftCoin()
                val totalGiftCoinSum = if (totalGiftCoin == 0L) 0L else totalGiftCoin
                holderList[0].reportValue.text = totalGiftCoinSum.toString()
            }
            1 -> {
                val latestAmount = if (latestAmountRedeemed == null) 0L else latestAmountRedeemed!!
                holderList[1].reportValue.text = latestAmount.toString()
            }
            2 -> {
                val influencerPoint =
                    if (totalRatingForAllStatus == null) 0L else totalRatingForAllStatus!!
                holderList[2].reportValue.text = influencerPoint.toString()
            }
        }
    }

    private fun updateImage(position: Int) {
        when (position) {
            0 -> {
                getTotalGiftCoin()
                val totalGiftCoinSum = if (totalGiftCoin == 0L) 0L else totalGiftCoin
                holderList[0].reportValue.text = totalGiftCoinSum.toString()
            }
            1 -> {
                val latestAmount = if (latestAmountRedeemed == null) 0L else latestAmountRedeemed!!
                holderList[1].reportValue.text = latestAmount.toString()
            }
            2 -> {
                val influencerPoint =
                    if (totalRatingForAllStatus == null) 0L else totalRatingForAllStatus!!
                holderList[2].reportValue.text = influencerPoint.toString()
            }
        }
    }

    override fun onPointerCaptureChanged(hasCapture: Boolean) {}
    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            try {
                if (sessionManager!!.getCurrentFragment() != "CustomerRewardStoriesFragment") {
                    startActivity(Intent(this@MainActivity, MainActivity::class.java))
                    super.onBackPressed()
                }
            } catch (e: Exception) {
               super.onBackPressed()
            }
        }
    }

    class ReportsViewHolder {
        lateinit var reportValue: TextView
        lateinit var reportName: TextView
        lateinit var reportIcon: ImageView
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.customer_menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (t!!.onOptionsItemSelected(item)) {
            return true
        }
        when(item.itemId){
            R.id.customer_refresh_page -> {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                return true
            }

            R.id.update_info -> {
                binding.carouselView.visibility = View.GONE
                navController.navigate(R.id.settingsFragment)
                return true
            }

            R.id.about_giftin -> {
                binding.carouselView.visibility = View.GONE
                navController.navigate(R.id.aboutFragment)
                return true
            }

            R.id.referwin -> {
                binding.carouselView.visibility = View.VISIBLE
                shareAppLink()
                return true
            }

            R.id.cash_out -> {
                binding.carouselView.visibility = View.GONE
                navController.navigate(R.id.cashoutFragment)
                return true
            }

            R.id.exit -> {
                showMessageDialog(title = "Log out", message = "Will you like to logout from Brandible?",
                    hasNegativeBtn = true, negbtnText = "No", posBtnText = "Yes", listener = {
                        mauth!!.signOut()
                        sessionManager!!.clearData()
                        storySession!!.clearData()
                        startActivity(Intent(this@MainActivity, SignUpActivity::class.java))
                        finish()
                    }

                )
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun getTotalGiftCoin() {
        //get the total gift coin for this user
        val db = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings
        db.collection("users").document(
                sessionManager?.getEmail().toString()
        ).collection("rewards").get()
            .addOnCompleteListener { task: Task<QuerySnapshot> ->
                if (task.isSuccessful) {
                    totalGiftCoin = 0L
                    for (queryDocumentSnapshot in task.result) {
                        val giftCoin = queryDocumentSnapshot.getDouble("gift_coin")
                        totalGiftCoin += giftCoin!!.toLong()
                    }

                    var divCoin = 1
                        try{
                            divCoin = ((totalGiftCoin/rewardToBrcBase).toInt())
                        }catch (e:Exception){

                        }
                    navTextView.text = resources.getString(
                        R.string.influenca_name_and_status,
                        mauth!!.currentUser?.email ?: "",
                        sessionManager!!.getFollowingCount().toString(),
                        divCoin.toString()
                    )
                } else {
                    totalGiftCoin = 0L
                }
            }
    }

    fun getLatestAmountRedeemed() {
        val db = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings
        db.collection("users").document("giftinappinc@gmail.com").collection("customers_redeemed")
            .document(
                    sessionManager?.getEmail().toString()
            ).get()
            .addOnCompleteListener { task: Task<DocumentSnapshot> ->
                latestAmountRedeemed = if (task.isSuccessful) {
                    try {
                        val documentSnapshot = task.result
                        documentSnapshot["gift_coin"] as Long
                    } catch (e: Exception) {
                        0L
                    }
                } else {
                    0L
                }
            }
    }

    private val influencerPoints: Unit
        get() {
            val db = FirebaseFirestore.getInstance()
            val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
            db.firestoreSettings = settings
            db.collection("influenca_activity_track").document(
                    sessionManager?.getEmail().toString()
            ).collection("status_rating").get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val queryDocumentSnapshots = task.result
                        for (eachDoc in queryDocumentSnapshots.documents) {
                            if (eachDoc["rating"] != null) {
                                Log.d("rating", eachDoc["rating"].toString())
                                totalRatingForAllStatus += eachDoc["rating"] as Long
                            }
                        }
                    }
                }
        }

    private fun computeInfluencerRankBasedOnActivity() {
        val db = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings
        db.collection("influenca_activity_track").document(
                sessionManager?.getEmail().toString()
        ).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val documentSnapshot = task.result
                    if (documentSnapshot.getBoolean("influencer_first_saw_a_brands_post") != null) {
                        val influencerFirstSawPost =
                            documentSnapshot.getBoolean("influencer_first_saw_a_brands_post")
                        if (influencerFirstSawPost!!) {
                            //navTextView.setText(getResources().getString(R.string.influenca_name_and_status, Objects.requireNonNull(mauth.getCurrentUser()).getEmail(), "pioneer"));
                        }
                    }
                }
            }
    }

    private fun shareAppLink() {
        try {
            val link =
                "https://giftinapp.page.link/xEYL/?link=brandible-app.com/?invitedBy=" + sessionManager!!.getEmail()
            //
//            FirebaseDynamicLinks.getInstance().createDynamicLink()
//                    .setLink(Uri.parse(link))
//                    .setDomainUriPrefix("https://giftinappdev.page.link")
//                    .setAndroidParameters(
//                            new DynamicLink.AndroidParameters.Builder("com.giftinapp.business")
//                                    .build())
//                    .buildShortDynamicLink()
//                    .addOnSuccessListener(shortDynamicLink -> {
//                        Uri mInvitationUrl = shortDynamicLink.getShortLink();

            // ...
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, link)
            startActivity(Intent.createChooser(intent, "Share Brandible With"))
            //});
        } catch (e: Exception) {
        }
    }

    private val numberOfFollowers: Unit
        get() {
            val db = FirebaseFirestore.getInstance()
            val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
            db.firestoreSettings = settings
            db.collection("merchants").get()
                .addOnCompleteListener { task: Task<QuerySnapshot?> ->
                    if (task.isSuccessful) {
                        val result = task.result
                        if (result != null) {
                            val eachRes = result.documents
                            for (i in eachRes.indices) {
                                counter += 1
                                db.collection("merchants").document(eachRes[i].id)
                                    .collection("followers").get()
                                    .addOnCompleteListener { followersTask: Task<QuerySnapshot?> ->
                                        if (followersTask.isSuccessful) {
                                            val followersQuerry = followersTask.result
                                            if (followersQuerry != null) {
                                                val eachFollower = followersQuerry.documents
                                                for (j in eachFollower.indices) {
                                                    if (eachFollower[j].id == sessionManager!!.getEmail()) {
                                                        following += 1
                                                    }
                                                }
                                                Log.d("Followers", following.toString())
                                                if (counter == result.documents.size) {
                                                    sessionManager!!.setFollowingCount(following)
                                                }
                                                navTextView.text = resources.getString(
                                                    R.string.influenca_name_and_status,
                                                        mauth!!.currentUser?.email.toString(),
                                                    sessionManager!!.getFollowingCount().toString(),
                                                    (totalGiftCoin / rewardToBrcBase).toString()
                                                )
                                            }
                                        }
                                    }
                            }
                        }
                    }
                }
        }

    override fun onDestroy() {
        super.onDestroy()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onPause() {
        super.onPause()
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    private val totalReferred: Unit
        get() {
            val db = FirebaseFirestore.getInstance()

            val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
            db.firestoreSettings = settings
            db.collection("users").get().addOnCompleteListener { task: Task<QuerySnapshot> ->
                if (task.isSuccessful) {
                    val result = task.result
                    val eachRes = result.documents
                    var total_referred = 0
                    for (i in eachRes.indices) {
                        if (eachRes[i]["referrer"] != null) {
                            try {
                                if (eachRes[i]["referrer"] == sessionManager!!.getEmail()) {
                                    total_referred += 1
                                }
                            } catch (e: Exception) {
                                Log.d("ErrTotalReffered", e.localizedMessage)
                            }
                        }
                    }
                    compareTotalReferredAgainstTarget(total_referred)
                }
            }
        }

    private fun compareTotalReferredAgainstTarget(totalReferred: Int) {
        Log.d("TotalReffered", totalReferred.toString())
        val db = FirebaseFirestore.getInstance()

        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings
        db.collection("referral_reward").document(
                sessionManager?.getEmail().toString()

        ).get().addOnCompleteListener { task: Task<DocumentSnapshot> ->
            if (task.isSuccessful) {
                val resultDoc = task.result
                if (resultDoc.exists()) {
                    val target =
                        (Objects.requireNonNull(resultDoc["targetToReach"]) as Number).toInt()
                    val referralRewardToken = resultDoc["referralRewardToken"] as String?
                    if (totalReferred >= target) {
                        assert(referralRewardToken != null)
                        if (referralRewardToken == "") {
                            val random = Random()
                            val token = random.nextInt(999999)
                            val referralRewardPojo = ReferralRewardPojo()
                            referralRewardPojo.referralRewardToken =
                                String.format(Locale.ENGLISH, "%06d", token)
                            referralRewardPojo.referralRewardAmount =
                                remoteConfigUtil!!.getReferralRewardBase().toInt() * target
                            referralRewardPojo.targetToReach = target
                            db.collection("referral_reward").document(sessionManager!!.getEmail()!!)
                                .delete()
                                .addOnCompleteListener { task12: Task<Void?> ->
                                    if (task12.isSuccessful) {
                                        db.collection("referral_reward")
                                            .document(sessionManager!!.getEmail()!!)
                                            .set(referralRewardPojo)
                                            .addOnCompleteListener { task1: Task<Void?> ->
                                                if (task1.isSuccessful) {
                                                    showCookieBar(title = "Reward on its way", message = "Your referral reward token will be sent to your email, please check in few minutes- you can check your spam if not in email", delay = 5000L
                                                    )
                                                }
                                            }
                                    }
                                }
                        }
                    }
                }
            }
        }
    }


    private var adViewListener = ViewListener { position ->
        @SuppressLint("InflateParams") val customView = layoutInflater.inflate(R.layout.single_item_carousel_ad_view, null)
        val labelTextView = customView.findViewById<View>(R.id.adDescription) as TextView
        val fruitImageView = customView.findViewById<View>(R.id.adImageView) as ImageView
        when (position) {
            0 -> {
                Log.d("Pos","Zero")
                val remoteConfigUtil = RemoteConfigUtil()
                try {
                    val imageFromConfig = remoteConfigUtil.getCarouselOneImage()
                    Picasso.get().load(imageFromConfig).into(fruitImageView)
                } catch (e: java.lang.Exception) {
                    Picasso.get().load(imageOne).into(fruitImageView)
                }
            }
            1 -> {
                val remoteConfigUtil = RemoteConfigUtil()

                //labelTextView.setText(sampleTitles[position]);
                try {
                    val imageFromConfig = remoteConfigUtil.getCarouselTwoImage()
                    Picasso.get().load(imageFromConfig).into(fruitImageView)
                } catch (e: java.lang.Exception) {
                    Picasso.get().load(imageTwo).into(fruitImageView)
                }
            }
            2 -> {
                val remoteConfigUtil = RemoteConfigUtil()
                try {
                    val imageFromConfig = remoteConfigUtil.getCarouselThreeImage()
                    Picasso.get().load(imageFromConfig).into(fruitImageView)
                } catch (e: java.lang.Exception) {
                    Picasso.get().load(imageThree).into(fruitImageView)
                }
            }
        }
        // binding.carouselView.indicatorGravity = Gravity.CENTER_HORIZONTAL or Gravity.TOP
        customView
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
                        showMessage(showNegBtn = false, dismissable = false, message = updateMessage.toString(), negBtnText = null) {
                            openPlayStore()
                        }
                    }else{
                        if (isForced != null) {
                            showMessage(showNegBtn = true, dismissable = true, updateMessage.toString(),"Cancel"){
                                openPlayStore()
                            }
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val MY_REQUEST_CODE = 100
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun getActivityBinding(inflater: LayoutInflater): ActivityMainBinding {

        binding = ActivityMainBinding.inflate(layoutInflater)

        val navHostFrag = supportFragmentManager.findFragmentById(R.id.main_act_nav_host_fragment) as NavHostFragment
        navController = navHostFrag.findNavController()

        //setSupportActionBar(binding.toolBar)
        //setupActionBarWithNavController(navController)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.navView.setupWithNavController(navController)

        binding.carouselView.pageCount = 3
        binding.carouselView.setViewListener(adViewListener)
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



        remoteConfigUtil = RemoteConfigUtil()
        rewardToBrcBase = Math.toIntExact(remoteConfigUtil!!.rewardToBRCBase().asLong())

        //MediationTestSuite.launch(MainActivity.this);
        mauth = FirebaseAuth.getInstance()

        t = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )

        binding.drawerLayout.addDrawerListener(object : DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
            override fun onDrawerOpened(drawerView: View) {
                getTotalGiftCoin()
                numberOfFollowers
//                binding.btnExploreBrand.translationZ = 0f
            }

            override fun onDrawerClosed(drawerView: View) {
                //binding.btnExploreBrand.translationZ = 2f
            }

            override fun onDrawerStateChanged(newState: Int) {}
        })
        t!!.syncState()
        binding.navView.setNavigationItemSelectedListener { item: MenuItem ->
            selectDrawerItem(item)
            true
        }
        sessionManager = SessionManager(applicationContext)
        storySession = StorySession(this)
        builder = AlertDialog.Builder(this@MainActivity)
        val headerView = binding.navView.getHeaderView(0)
        navTextView = headerView.findViewById<TextView>(R.id.nav_header_textView)
        val navImageView = headerView.findViewById<ImageView>(R.id.nav_header_imageView)
        //ImageView ivRating = headerView.findViewById(R.id.iv_rating);
        //ivRating.setVisibility(View.VISIBLE);
        totalRatingForAllStatus = 0L
        getTotalGiftCoin()
        getLatestAmountRedeemed()
        influencerPoints
        //computeInfluencerRankBasedOnActivity();
        numberOfFollowers
        navTextView.text = resources.getString(
            R.string.influenca_name_and_status,
                mauth!!.currentUser?.email.toString(), following.toString(), (totalGiftCoin / rewardToBrcBase).toString()
        )
        totalReferred

        return binding
    }
}