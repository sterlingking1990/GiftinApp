package com.giftinapp.business

import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.giftinapp.business.customer.MerchantStoryList
import com.giftinapp.business.databinding.ActivityInfluencerBinding
import com.giftinapp.business.model.ReferralRewardPojo
import com.giftinapp.business.utility.RemoteConfigUtil
import com.giftinapp.business.utility.SessionManager
import com.giftinapp.business.utility.StorySession
import com.giftinapp.business.utility.base.BaseActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject
import kotlin.properties.Delegates
import com.facebook.AccessToken

import com.facebook.LoginStatusCallback

import com.facebook.login.LoginManager




@AndroidEntryPoint
open class InfluencerActivity : BaseActivity<ActivityInfluencerBinding>() {

    @Inject
    lateinit var firebaseRemoteConfig: FirebaseRemoteConfig

    private var remoteConfigUtil: RemoteConfigUtil? = null

    private lateinit var binding: ActivityInfluencerBinding

    private lateinit var navController: NavController
    private var t: ActionBarDrawerToggle? = null
    private var rewardToBrcBase = 2
    var mauth: FirebaseAuth? = null
    var sessionManager: SessionManager? = null
    private lateinit var navTextView: TextView
    var storySession: StorySession? = null
    var builder: AlertDialog.Builder? = null
    var totalRatingForAllStatus by Delegates.notNull<Long>()
    var counter = 0
    var following = 0
    var totalGiftCoin = 0L
    var userId = ""
    var revenue_multiplier = 0.1
    private val latestAmountRedeemed: Long? = null
    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun getActivityBinding(inflater: LayoutInflater): ActivityInfluencerBinding {

        binding = ActivityInfluencerBinding.inflate(layoutInflater)

        sessionManager = SessionManager(this)


        builder = AlertDialog.Builder(this)

        getUserId()
        getTotalGiftCoin()

        if(sessionManager!!.isFirstTimeLogin()) {
            Handler().postDelayed({
                val i = Intent(this, InfluencerActivity::class.java)
                startActivity(i)
                finish()
            }, 3000)
            sessionManager!!.setFirstTimeLogin(false)
        }

        remoteConfigUtil = RemoteConfigUtil()

        revenue_multiplier = remoteConfigUtil!!.getRevenueMultiplier().asDouble()


        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        mauth = FirebaseAuth.getInstance()

        checkAppUpdate()

        val navHostFrag = supportFragmentManager.findFragmentById(R.id.main_act_nav_host_fragment) as NavHostFragment
        navController = navHostFrag.findNavController()


        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.navView.setupWithNavController(navController)

        t = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )

        binding.drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
            override fun onDrawerOpened(drawerView: View) {
                getUserId()
                getTotalGiftCoin()
                numberOfFollowers
//                binding.btnExploreBrand.translationZ = 0f
                Log.d("userId",userId)
            }

            override fun onDrawerClosed(drawerView: View) {
                //binding.btnExploreBrand.translationZ = 2f
                t!!.onDrawerClosed(drawerView)
            }

            override fun onDrawerStateChanged(newState: Int) {

            }
        })

        t!!.syncState()
        binding.navView.setNavigationItemSelectedListener { item: MenuItem ->
            selectDrawerItem(item)
            true
        }

        storySession = StorySession(this)
        builder = AlertDialog.Builder(this@InfluencerActivity)
        val headerView = binding.navView.getHeaderView(0)
        navTextView = headerView.findViewById<TextView>(R.id.nav_header_textView)
        val navImageView = headerView.findViewById<ImageView>(R.id.nav_header_imageView)
        //ImageView ivRating = headerView.findViewById(R.id.iv_rating);
        //ivRating.setVisibility(View.VISIBLE);
        totalRatingForAllStatus = 0L
        //computeInfluencerRankBasedOnActivity();
        navTextView.text = resources.getString(
            R.string.influenca_name_and_status,
            userId, following.toString(), ((totalGiftCoin - (revenue_multiplier * totalGiftCoin))/rewardToBrcBase).toString()
        )
        totalReferred

        checkIfUserHasViewedStatsBefore()

       // getFacebookHash()
        return binding
    }

//    private fun getFacebookHash(){
//        FacebookSdk.sdkInitialize(applicationContext);
//        Log.d("AppLog", "FBkey:" + FacebookSdk.getApplicationSignature(this));
//    }

    private fun checkIfUserHasViewedStatsBefore(){
        val db = FirebaseFirestore.getInstance()

        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings

        db.collection("users").document(sessionManager?.getEmail().toString()).get()
            .addOnCompleteListener {
                if(it.isSuccessful){
                    val hasViewedFirstStats = it.result.get("hasViewedFirstStats")
                    if(hasViewedFirstStats==null){
                        db.collection("users").document(sessionManager?.getEmail().toString()).update("hasViewedFirstStats",false)
                    }
                }
            }
    }

    private fun selectDrawerItem(menuitem: MenuItem) {

        if (menuitem.itemId == R.id.navigation_view_reward_deal) {
            //binding.carouselView.visibility = View.GONE
            val merchantStoryList = MerchantStoryList()
            navController.navigate(R.id.merchantStoryList)
        }

        if (menuitem.itemId == R.id.navigation_view_brand_preference) {
           // binding.carouselView.visibility = View.GONE
            navController.navigate(R.id.brandPreferenceFragment)
        }
        if (menuitem.itemId == R.id.navigation_referral_deal) {
            //binding.carouselView.visibility = View.GONE
            navController.navigate(R.id.myReferralDealFragment)
        }
//        if (menuitem.itemId == R.id.navigation_share_n_earn) {
//            //binding.carouselView.visibility = View.GONE
//            Log.d("AccessToken",AccessToken.getCurrentAccessToken()?.token.toString())
//
//            startActivity(Intent(this,InfluencerSharersActivity::class.java))
//            //navController.navigate(R.id.shareNEarnFragment)
//        }
        binding.drawerLayout.close()
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
                val intent = Intent(this, InfluencerActivity::class.java)
                startActivity(intent)
                return true
            }

            R.id.update_info -> {
                //binding.carouselView.visibility = View.GONE
                navController.navigate(R.id.settingsFragment)
                return true
            }

            R.id.about_giftin -> {
                //binding.carouselView.visibility = View.GONE
                navController.navigate(R.id.aboutFragment)
                return true
            }

            R.id.referwin -> {
                //binding.carouselView.visibility = View.VISIBLE
                //check if user has set target of referral before sharing referral link
                checkIfTargetSetBeforeSharingLink()
                //shareAppLink()
                return true
            }

            R.id.cash_out -> {
                //binding.carouselView.visibility = View.GONE
                builder?.setTitle("Cash out?")
                    ?.setMessage("Please select an option to cash out from")
                    ?.setCancelable(false)
                    ?.setPositiveButton("Naira", DialogInterface.OnClickListener { _: DialogInterface?, _: Int ->
                        navController.navigate(R.id.cashoutFragment)
                    })
                    ?.setNegativeButton("Mpesa") { _: DialogInterface?, _: Int ->
                        navController.navigate(R.id.mpesaCashoutFragment)
                    }
                val alert: AlertDialog? = builder?.create()
                alert?.show()
                return true
            }

            R.id.exit -> {
                showMessageDialog(title = "Log out", message = "Will you like to logout from Brandible?",
                    hasNegativeBtn = true, negbtnText = "No", posBtnText = "Yes", listener = {
                        mauth!!.signOut()
                        sessionManager!!.clearData()
                        storySession!!.clearData()
                        startActivity(Intent(this@InfluencerActivity, SignUpActivity::class.java))
                        finish()
                    }

                )
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun checkIfTargetSetBeforeSharingLink() {
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
                    Log.d("Target",target.toString())
                    if(target!=0){
                        shareAppLink()
                    }else{
                        showMessageDialog(
                            title = "Set Referral Target",
                            message = "Please set referral target before sharing referral link to your friends to enable you get referral token and earn seamlessly",
                            posBtnText = "Ok",
                            listener = {
                                navController.navigate(R.id.myReferralDealFragment)
                            }
                        )
                    }
                }else{
                    showMessageDialog(
                        title = "Set Referral Target",
                        message = "Please set referral target before sharing referral link to your friends to enable you get referral token and earn seamlessly",
                        posBtnText = "Ok",
                        listener = {
                            navController.navigate(R.id.myReferralDealFragment)
                        }
                    )
                }
            }
        }
    }

    private fun shareAppLink() {
        try {
            val link =
                "https://giftinapp.page.link/xEYL/?link=brandible-app.com/?invitedBy=" + sessionManager!!.getEmail()
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, link)
            startActivity(Intent.createChooser(intent, "Share Brandible With"))

        } catch (e: Exception) {

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
                                                val divCoinUse = if(rewardToBrcBase==0){
                                                    ((totalGiftCoin/1).toInt())
                                                }else{
                                                    ((totalGiftCoin - (revenue_multiplier * totalGiftCoin))/rewardToBrcBase).toInt()
                                                }
                                                navTextView.text = resources.getString(
                                                    R.string.influenca_name_and_status,
                                                    userId,
                                                    sessionManager!!.getFollowingCount().toString(),
                                                    divCoinUse.toString()
                                                )
                                            }
                                        }
                                    }
                            }
                        }
                    }
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
                    eachRes.forEach {
                        val refererr = it.get("referrer")
                        try {
                            if (refererr == sessionManager!!.getEmail().toString()) {
                                total_referred += 1
                            }
                        } catch (e: Exception) {
                            Log.d("ErrTotalReffered", e.localizedMessage)
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

    fun getUserId(){
        val db = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings
        db.collection("users").document(
            sessionManager?.getEmail().toString()).get()
            .addOnCompleteListener { task: Task<DocumentSnapshot> ->
                if (task.isSuccessful) {
                    val documentSnapshot = task.result
                    if (documentSnapshot.exists()) {
                       userId = documentSnapshot.getString("giftingId").toString()
                    }
                }
            }
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

                    val divCoin: Int = if(rewardToBrcBase==0){
                        ((totalGiftCoin/1).toInt())
                    }else{
                        ((totalGiftCoin - (revenue_multiplier * totalGiftCoin))/rewardToBrcBase).toInt()
                    }
                    navTextView.text = resources.getString(
                        R.string.influenca_name_and_status,
                        userId ?: "",
                        sessionManager!!.getFollowingCount().toString(),
                        divCoin.toString()
                    )
                } else {
                    totalGiftCoin = 0L
                }
            }
    }

}