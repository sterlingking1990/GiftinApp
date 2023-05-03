package com.giftinapp.business

import android.app.AlertDialog
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.os.Bundle
import android.util.Log
import android.util.SparseArray
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.giftinapp.business.admin.*
import com.giftinapp.business.utility.SessionManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.squareup.picasso.Picasso
import com.synnapps.carouselview.CarouselView
import com.synnapps.carouselview.ViewListener
import java.util.*


class GiftinAppAuthorityActivity : AppCompatActivity() {

    lateinit var bottomNavigationView :BottomNavigationView
    lateinit var sessionManager: SessionManager
    private val mAuth = FirebaseAuth.getInstance()

    var totalRegisteredUsers =0
    var totalGiftedUsers=0
    var totalVerifiedBusiness=0


    protected var carouselViewGiftinAuthority: CarouselView? = null

    protected var holderListMerchant = SparseArray<GiftinAuthViewHolder>()

    private var drawer: DrawerLayout? = null
    private var t: ActionBarDrawerToggle? = null
    private var nv: NavigationView? = null

    var appUpdateManager: AppUpdateManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_giftin_app_authority)

        appUpdateManager = AppUpdateManagerFactory.create(this)

// Returns an intent object that you use to check for an update.
        val appUpdateInfoTask = appUpdateManager!!.appUpdateInfo


// Checks that the platform will allow the specified type of update.

// Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo: AppUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE // This example applies an immediate update. To apply a flexible update
                    // instead, pass in AppUpdateType.FLEXIBLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                // Request the update.
                try {
                    appUpdateManager!!.startUpdateFlowForResult( // Pass the intent that is returned by 'getAppUpdateInfo()'.
                            appUpdateInfo,  // Or 'AppUpdateType.FLEXIBLE' for flexible updates.
                            AppUpdateType.IMMEDIATE,  // The current activity making the update request.
                            this,  // Include a request code to later monitor this update request.
                            1)
                } catch (e: SendIntentException) {
                    e.printStackTrace()
                }
            }
        }

        carouselViewGiftinAuthority = findViewById<CarouselView>(R.id.carouselView)

        carouselViewGiftinAuthority?.pageCount=3
        carouselViewGiftinAuthority?.setViewListener(viewListener)

        sessionManager= SessionManager(this);

        drawer = findViewById(R.id.adminDrawerLayout)
        t = ActionBarDrawerToggle(this, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close)

        drawer?.addDrawerListener(t!!)
        t?.syncState()

        Objects.requireNonNull(supportActionBar)!!.setDisplayHomeAsUpEnabled(true)

        nv = findViewById(R.id.adminNavView)

        nv?.setNavigationItemSelectedListener { item: MenuItem? ->
            selectDrawerItem(item!!)
            true
        }

        val headerView = nv?.getHeaderView(0)
        val navTextView = headerView?.findViewById<TextView>(R.id.nav_header_textView)
        val navImageView = headerView?.findViewById<ImageView>(R.id.nav_header_imageView)
        Picasso.get().load(R.drawable.gift).into(navImageView)
        navTextView?.text = Objects.requireNonNull(mAuth.currentUser)!!.email

        carouselViewGiftinAuthority?.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                updateCounter(position)
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
    }

    var viewListener = ViewListener { position: Int ->
        val customView = layoutInflater.inflate(R.layout.single_item_carousel_giftin_authority_report, null)
        val holder = GiftinAuthViewHolder()
        holder.reportValue = customView.findViewById(R.id.kpi_report_value)
        holder.reportName = customView.findViewById(R.id.kpi_report_name)
        holder.reportIcon = customView.findViewById(R.id.kpi_report_icon)
        when (position) {
            0 -> {
                getTotalRegisteredUsers()
                holder.reportName?.text = "Total Registered Users"
                holder.reportIcon?.setImageResource(R.drawable.ic_gifts)
                holder.reportValue?.text = totalRegisteredUsers.toString()
                holderListMerchant.put(0, holder)
            }
            1 -> {
                getTotalGiftedUsers()
                holder.reportName?.text = "Total Gifted Users"
                holder.reportValue?.text = totalGiftedUsers.toString()
                holder.reportIcon?.setImageResource(R.drawable.ic_gifts)
                holderListMerchant.put(1, holder)
            }
            2 -> {
                getTotalVerifiedBusiness()
                holder.reportName?.text = "Total Verified Business"
                holder.reportValue?.text = totalVerifiedBusiness.toString()
                holder.reportIcon?.setImageResource(R.drawable.ic_gifts)
                holderListMerchant.put(2, holder)
            }
        }
        customView
    }

    private fun updateCounter(position: Int) {

        when (position) {
            0 -> {
                getTotalRegisteredUsers()
                val totalRegistered = totalRegisteredUsers
                holderListMerchant[0].reportValue?.text = totalRegistered.toString()
            }
            1 -> {
                getTotalGiftedUsers()
                val totalGifted = totalGiftedUsers
                holderListMerchant[1].reportValue?.text = totalGifted.toString()
            }
            2 -> {
                getTotalVerifiedBusiness()
                val totalVerified = totalVerifiedBusiness
                holderListMerchant[2].reportValue?.text = totalVerified.toString()
            }
        }
    }

    private fun getTotalRegisteredUsers(){
        val db = FirebaseFirestore.getInstance()
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
        db.firestoreSettings = settings

        db.collection("users").get()
                .addOnCompleteListener{
                    if(it.isSuccessful){
                        totalRegisteredUsers=0
                        for (item in it.result!!){
                            totalRegisteredUsers+=1
                        }
                    }
                    else{
                        totalRegisteredUsers=0
                    }
                }
    }

    private fun getTotalGiftedUsers(){

        val db = FirebaseFirestore.getInstance()
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
        db.firestoreSettings = settings

        db.collection("users").document("giftinappinc@gmail.com").collection("customers_redeemed")
                .get()
                .addOnCompleteListener {
                    if(it.isSuccessful){
                        totalGiftedUsers=0
                        for (item in it.result!!){
                            totalGiftedUsers+=1
                        }
                    }
                    else{
                        totalGiftedUsers=0
                    }
                }

    }

    private fun getTotalVerifiedBusiness(){
        val db = FirebaseFirestore.getInstance()
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
        db.firestoreSettings = settings

        db.collection("merchants").get()
                .addOnCompleteListener {
                    if(it.isSuccessful){
                        totalVerifiedBusiness=0
                        for (item in it.result!!){
                            totalVerifiedBusiness+=1
                        }
                    }
                    else{
                        totalVerifiedBusiness=0
                    }
                }

    }

    class GiftinAuthViewHolder {
        var reportValue: TextView? = null
        var reportName: TextView? = null
        var reportIcon: ImageView? = null
    }

    private fun openFragment(fragment: Fragment?) {
        supportFragmentManager.beginTransaction()
                .replace(R.id.fr_giftin_authority, fragment!!)
                .addToBackStack(null)
                .commit()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.giftin_authority_menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (t!!.onOptionsItemSelected(item)) {
            return true
        }
        when (item.itemId) {
            R.id.giftin_authority_refresh -> {
                val intent = Intent(this, GiftinAppAuthorityActivity::class.java)
                startActivity(intent)
                return true
            }


            R.id.giftin_authority_exit -> {
                val builder = AlertDialog.Builder(this)
                // builder.setTitle("Alert");
                // builder.setIcon(R.drawable.ic_launcher);
                builder.setMessage("   Log out?")
                builder.setCancelable(false)
                builder.setNegativeButton("Cancel"
                ) { _, _ -> }
                builder.setNeutralButton("Ok") { dialog, _ ->
                    mAuth.signOut()
                    sessionManager.clearData()
                    startActivity(Intent(this, SignUpActivity::class.java))
                    dialog.cancel()
                }
                builder.show()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun selectDrawerItem(menuitem: MenuItem) {
        when (menuitem.itemId) {
            R.id.navigation_verify_business -> {
                carouselViewGiftinAuthority?.visibility = View.GONE
                val verifyBusiness = GiftinAppAuthorityVerifyUserFragment()
                openFragment(verifyBusiness)
            }

            R.id.navigation_set_claim -> {
                carouselViewGiftinAuthority?.visibility = View.GONE
                val setInfluencerClaim = SetCanClaimBrC()
                openFragment(setInfluencerClaim)
            }
        }
        drawer!!.close()
    }

    override fun onBackPressed() {
        if (drawer!!.isDrawerOpen(GravityCompat.START)) {
            drawer!!.closeDrawer(GravityCompat.START)
        } else {

            try {
                super.onBackPressed()
                startActivity(Intent(this, GiftinAppAuthorityActivity::class.java))
            }
            catch (e: Exception) {
                mAuth.signOut()
                sessionManager.clearData()
                startActivity(Intent(this, SignUpActivity::class.java))
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        appUpdateManager
                ?.appUpdateInfo
                ?.addOnSuccessListener { appUpdateInfo: AppUpdateInfo ->
                    // If the update is downloaded but not installed,
                    // notify the user to complete the update.
                    if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                        popupSnackbarForCompleteUpdate()
                    }
                }
    }

    private fun popupSnackbarForCompleteUpdate() {
        val snackbar = Snackbar.make(
                findViewById(R.id.cl_activity_main),
                "An update has just been downloaded.",
                Snackbar.LENGTH_INDEFINITE)
        snackbar.setAction("RESTART") { view: View? -> appUpdateManager!!.completeUpdate() }
        snackbar.setActionTextColor(
                resources.getColor(R.color.tabColorLight))
        snackbar.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            if (resultCode != RESULT_OK) {
                Log.d("UpdateFlowFailed", resultCode.toString())
                // If the update is cancelled or fails,
                // you can request to start the update again.
            }
        }
    }

}
