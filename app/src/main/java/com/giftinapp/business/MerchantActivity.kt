package com.giftinapp.business

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.text.Html
import android.util.Log
import android.util.SparseArray
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import co.paystack.android.PaystackSdk
import com.giftinapp.business.databinding.ActivityMerchantBinding
import com.giftinapp.business.utility.RemoteConfigUtil
import com.giftinapp.business.utility.SessionManager
import com.giftinapp.business.utility.base.BaseActivity
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.synnapps.carouselview.CarouselView
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
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
    private var imageOne = ""
    private var imageTwo = ""
    private var imageThree = ""
    private var carouselViewMerchant: CarouselView? = null
    private var holderListMerchant = SparseArray<MerchantReportsViewHolder>()
    private var numberOfCustomerGifted by Delegates.notNull<Int>()
    private var totalAmountGifted by Delegates.notNull<Long>()
    var userId = ""
    var totalWalletBalance: Long? = null
    private var t: ActionBarDrawerToggle? = null
    var counter = 0
    var following = 0
    private lateinit var navTextView: TextView


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
                navController.navigate(R.id.merchantInfoUpdate)
                return true
            }

            R.id.merchant_about_giftin -> {
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

        when(menuitem.itemId){
            R.id.navigation_wallet_info->{
                navController.navigate(R.id.walletInfo)
            }
            R.id.navigation_set_reward_deal->{
                navController.navigate(R.id.setRewardDeal)
            }
            R.id.navigation_view_reward_deal->{
                navController.navigate(R.id.merchantStoryList2)
            }
            R.id.navigation_merchant_follow_brands->{
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

    override fun onResume() {
        super.onResume()

        if(navController.currentDestination?.id == R.id.giftinAboutForMerchant){
            val intent = Intent(this, MerchantActivity::class.java)
            startActivity(intent)
        }
        if(navController.currentDestination?.id == R.id.merchantInfoUpdate){
            val intent = Intent(this, MerchantActivity::class.java)
            startActivity(intent)
        }
        if(navController.currentDestination?.id == R.id.brandPreferenceFragment2){
            val intent = Intent(this, MerchantActivity::class.java)
            startActivity(intent)
        }
        if(navController.currentDestination?.id == R.id.walletInfo){
            val intent = Intent(this, MerchantActivity::class.java)
            startActivity(intent)
        }
        if(navController.currentDestination?.id == R.id.merchantStoryList2){
            val intent = Intent(this, MerchantActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data != null) {
            Log.d("Data", data.toString())
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun getUserId(){
        val db = FirebaseFirestore.getInstance()
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings

            db.collection("merchants").document(
                    sessionManager!!.getEmail().toString()).get()
                    .addOnCompleteListener { task: Task<DocumentSnapshot> ->
                        if (task.isSuccessful) {
                            val documentSnapshot = task.result
                            if (documentSnapshot.exists()) {

                                userId =
                                    if (documentSnapshot["giftorId"] != null) documentSnapshot["giftorId"]
                                        .toString() else  documentSnapshot.id
                            }
                        }
                    }
        }

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
                               userId,
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

        if(sessionManager!!.isFirstTimeLogin()) {
            Handler().postDelayed({
                val i = Intent(this, MerchantActivity::class.java)
                startActivity(i)
                finish()
            }, 3000)
            sessionManager!!.setFirstTimeLogin(false)
        }

        t = ActionBarDrawerToggle(
            this,
            binding.merchantNavDrawerLayout,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )

        binding.merchantNavDrawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
            override fun onDrawerOpened(drawerView: View) {
                getUserId()
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
            userId,
            following.toString()
        )
        //btnExploreBrand = findViewById(R.id.btnExploreBrand)
        remoteConfigUtil = RemoteConfigUtil()
        //binding.btnExploreBrand.setOnClickListener(View.OnClickListener { openWebView(remoteConfigUtil!!.getBrandLink()) })

        return binding
    }
}