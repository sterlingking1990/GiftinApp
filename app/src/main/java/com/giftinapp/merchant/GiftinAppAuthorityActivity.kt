package com.giftinapp.merchant

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Vibrator
import android.util.SparseArray
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.synnapps.carouselview.CarouselView
import com.synnapps.carouselview.ViewListener
import kotlin.system.exitProcess

class GiftinAppAuthorityActivity : AppCompatActivity() {

    lateinit var bottomNavigationView :BottomNavigationView
    lateinit var sessionManager:SessionManager
    private val mAuth = FirebaseAuth.getInstance()

    var totalRegisteredUsers =0
    var totalGiftedUsers=0
    var totalVerifiedBusiness=0


    protected var carouselViewGiftinAuthority: CarouselView? = null

    protected var holderListMerchant = SparseArray<GiftinAuthViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_giftin_app_authority)

        bottomNavigationView=findViewById(R.id.bottom_navigation_giftin_authority)
        bottomNavigationView.setOnNavigationItemSelectedListener(navigationItem)

//        bottomNavigation.setOnNavigationItemSelectedListener(navigationItem);
//        openFragment(new GiftACustomerFragment());
        carouselViewGiftinAuthority = findViewById<CarouselView>(R.id.carouselView)

        carouselViewGiftinAuthority?.pageCount=3
        carouselViewGiftinAuthority?.setViewListener(viewListener)

        sessionManager= SessionManager(this);

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
                val totalRegistered = if (totalRegisteredUsers == null) 0 else totalRegisteredUsers
                holderListMerchant[0].reportValue?.text = totalRegistered.toString()
            }
            1 -> {
                getTotalGiftedUsers()
                val totalGifted = if (totalGiftedUsers == null) 0 else totalGiftedUsers
                holderListMerchant[1].reportValue?.text = totalGifted.toString()
            }
            2 -> {
                getTotalVerifiedBusiness()
                val totalVerified = if (totalVerifiedBusiness == null) 0 else totalVerifiedBusiness
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


    var navigationItem = BottomNavigationView.OnNavigationItemSelectedListener { item: MenuItem ->
        when (item.itemId) {
            R.id.navigation_verify_business -> {
                carouselViewGiftinAuthority?.visibility = View.GONE
                var verifyBusiness: Fragment = GiftinAppAuthorityVerifyUserFragment()
                openFragment(verifyBusiness)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_redeem_gift -> {
                carouselViewGiftinAuthority?.visibility = View.GONE
                val giftinAppAuthorityRedeemGiftFragment: Fragment = GiftinAppAuthorityRedeemGiftFragment()
                openFragment(giftinAppAuthorityRedeemGiftFragment)
                return@OnNavigationItemSelectedListener true
            }

            R.id.navigation_gifted_customers -> {
                carouselViewGiftinAuthority?.visibility = View.GONE
                val giftinAppAuthorityRedeemedCustomersFragment: Fragment = GiftinAppAuthorityRedeemedCustomersFragment()
                openFragment(giftinAppAuthorityRedeemedCustomersFragment)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
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
        when (item.itemId) {
            R.id.giftin_authority_refresh -> {
                val intent = Intent(this, GiftinAppAuthorityActivity::class.java)
                startActivity(intent)
                return true
            }

            R.id.giftin_authority_add_gifts -> {
                carouselViewGiftinAuthority?.visibility = View.GONE
                val giftinAppAuthorityAddGiftsFragment = GiftinAppAuthorityAddGiftsFragment()
                openFragment(giftinAppAuthorityAddGiftsFragment)
                return true
            }

            R.id.giftin_authority_customers_to_redeem -> {
                carouselViewGiftinAuthority?.visibility = View.GONE
                val giftinAppAuthorityRedeemableCustomers = GiftinAppAuthorityRedeemableCustomers()
                openFragment(giftinAppAuthorityRedeemableCustomers)
                return true
            }


            R.id.giftin_authority_exit -> {
                val vibrator = this.getSystemService(VIBRATOR_SERVICE) as Vibrator
                vibrator.vibrate(500)
                val builder = AlertDialog.Builder(this)
                // builder.setTitle("Alert");
                // builder.setIcon(R.drawable.ic_launcher);
                builder.setMessage("   Log out?")
                builder.setCancelable(false)
                builder.setNegativeButton("Cancel"
                ) { _, _ -> }
                builder.setNeutralButton("Ok") { dialog, _ ->
                    mAuth.signOut()
                    dialog.cancel()
                    this.finish()
                    exitProcess(0)
                }
                builder.show()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }




}
