package com.giftinapp.business.homefragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.facebook.AccessToken
import com.facebook.login.LoginManager
import com.giftinapp.business.R
import com.giftinapp.business.business.GptTopicsAdapter
import com.giftinapp.business.business.PostListAdapter
import com.giftinapp.business.databinding.FragmentCarouselHomeBinding
import com.giftinapp.business.model.GptContent
import com.giftinapp.business.model.gptcontent.GptPrompt
import com.giftinapp.business.network.viewmodel.GetGptContentViewModel
import com.giftinapp.business.utility.*
import com.giftinapp.business.utility.base.BaseFragment
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.QuerySnapshot
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.combine


@AndroidEntryPoint
class CarouselHome : BaseFragment<FragmentCarouselHomeBinding>(), GptTopicsAdapter.ClickablePost {
    private lateinit var binding: FragmentCarouselHomeBinding
    private lateinit var remoteConfigUtil: RemoteConfigUtil
    //private val getPostListViewModel: GetPostsViewModel by viewModels()
    private val getGptContentViewModel: GetGptContentViewModel by viewModels()
    lateinit var postListAdapter: PostListAdapter
    lateinit var gptListAdapter: GptTopicsAdapter
    var sessionManager: SessionManager? = null
    var totalGiftCoin = 0L
    private var rewardToBrcBase = 2
    var revenue_multiplier = 0.1
    var counter = 0
    var following = 0
    var influencer_following = 0
    var contentIs = "Hello apsoskals alksalslkkls"
    var totalWalletBalance = 0


    //private lateinit var imageList:List<Uri>
    private var imageOne =
        "https://www.brandibleinc.com/brandiblehosts/kilishimperio/kilishi_imperio.jpg"
    private var imageTwo =
        "https://www.brandibleinc.com/brandiblehosts/kilishimperio/kilishi_imperio2.jpg"
    private var imageThree =
        "https://www.brandibleinc.com/brandiblehosts/kilishimperio/kilishi_imperio3.jpg"


    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentCarouselHomeBinding {
        binding = FragmentCarouselHomeBinding.inflate(layoutInflater, container, false)

        remoteConfigUtil = RemoteConfigUtil()

        sessionManager = SessionManager(requireActivity())
        if (sessionManager?.getUserMode() == "customer") {
            getTotalGiftCoin()
            numberOfFollowers
        } else {
            influencerFollowing
            walletBalance
        }

        val animation =
            android.view.animation.AnimationUtils.loadAnimation(requireContext(), R.anim.bounce);


        //postListAdapter = PostListAdapter(this)
        gptListAdapter = GptTopicsAdapter(this)
        //binding.rvPostList.adapter = postListAdapter
        binding.rvPostList.adapter = gptListAdapter
        binding.rvPostList.layoutManager = GridLayoutManager(requireContext(), 2)

        val intent = getTopicFromRemoteConfig()

        val promptList = listOf(
            getGptContentViewModel.getAgriculturePoem(
                GptPrompt(
                "Provide a 1000-word current news topic on the headline $intent Poem","text-davinci-002", 0.5,1000)),
            getGptContentViewModel.getAgricultureAllergy(GptPrompt("Provide a 1000-word current news topic on the headline $intent Allegory","text-davinci-002", 0.5,1000)),
            getGptContentViewModel.getAgricultureArticle(GptPrompt("Provide a 1000-word current news topic on the headline $intent Article","text-davinci-002", 0.5,1000)),
            getGptContentViewModel.getAgricultureNews(GptPrompt("Provide a 1000-word current news topic on the headline $intent News","text-davinci-002", 0.5,1000))
        )

        lifecycleScope.launch {
            combine(promptList) { contents->
                contents.toList()
            }.collect { content->
                binding.pgLoadingPost.gone()
                Log.d("Contents",content.toString())
                gptListAdapter.setGptItems(content)
                gptListAdapter.notifyDataSetChanged()
            }
        }
        //getPostListViewModel.getPostList()
        //fetchPost()

        //observePosts()

        //populateData()

//        binding.carouselView.pageCount = 3
//        binding.carouselView.setImageListener(imageListener)
//
//        binding.btnExploreBrand.setOnClickListener {
//            it.startAnimation(animation)
//            openWebView(remoteConfigUtil.getBrandLink())

        //}

//        showHelpBar(
//            title = "Learn more about this brand",
//            content = "Click to Open and learn more about the products and service offering of this brand",
//            targetView = binding.btnExploreBrand
//        )

        return binding
    }

    private fun getTopicFromRemoteConfig(): String {
        return remoteConfigUtil.getGptTopicFromRemoteConfig()
    }


//    private fun observePosts(){
//        getPostListViewModel.postListResponse.observe(viewLifecycleOwner) {
//            when(it.status){
//                Resource.Status.LOADING -> {
//                    Log.d("ItLoaded","Loading")
//                    binding.pgLoadingPost.visible()
//                }
//                Resource.Status.SUCCESS -> {
//                    binding.pgLoadingPost.gone()
//                    binding.rlNoPost.gone()
//                    Log.d("Data",it.data.toString())
//                    it.data?.let { it1 -> postListAdapter.setRespondersList(it1) }
//                    postListAdapter.notifyDataSetChanged()
//                }
//                Resource.Status.ERROR -> {
//                    binding.pgLoadingPost.gone()
//                    binding.rlNoPost.visible()
//                    Toast.makeText(requireContext(),"Unable to load posts",Toast.LENGTH_LONG).show()
//                    Log.d("Error",it.message.toString())
//                }
//            }
//        }
//    }

    private fun getTotalGiftCoin() {
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

                    val divCoin: Int = if (rewardToBrcBase == 0) {
                        ((totalGiftCoin / 1).toInt())
                    } else {
                        ((totalGiftCoin - (revenue_multiplier * totalGiftCoin)) / rewardToBrcBase).toInt()
                    }
                    binding.tvBrcTotal.text = resources.getString(
                        R.string.your_brc,
                        divCoin.toString()
                    )
                } else {
                    totalGiftCoin = 0L
                }
            }
    }

    private val numberOfFollowers: Unit
        get() {
            sessionManager = SessionManager(requireActivity())
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
                                                val divCoinUse = if (rewardToBrcBase == 0) {
                                                    ((totalGiftCoin / 1).toInt())
                                                } else {
                                                    ((totalGiftCoin - (revenue_multiplier * totalGiftCoin)) / rewardToBrcBase).toInt()
                                                }
                                                binding.tvBrandFollowing.text = resources.getString(
                                                    R.string.brand_following,
                                                    following.toString()
                                                )
                                            }
                                        }
                                    }
                            }
                        }
                    }
                }
        }

    private val influencerFollowing: Unit
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
                            influencer_following = followersQuerry.size()
                            sessionManager!!.setFollowingCount(following)
                            binding.tvBrandFollowing.text = resources.getString(
                                R.string.influencer_followers,
                                influencer_following.toString()
                            )
                        }
                    }
                }
        }


    private fun getBrandValue() {
        val db = FirebaseFirestore.getInstance()

        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings

        db.collection("challenge").document(sessionManager?.getEmail().toString())
            .collection("challengelist").get()
            .addOnCompleteListener { eachChallenge ->
                if (eachChallenge.isSuccessful) {
                    var totalApprovedResponse = 0
                    for (challenge in eachChallenge.result.documents) {
                        val challengeId = challenge.id

                        //get the responders in this challengeId and count the responders which has been approved
                        db.collection("challenge").document(sessionManager?.getEmail().toString())
                            .collection("challengelist").document(challengeId)
                            .collection("responders")
                            .get().addOnCompleteListener { responders ->
                                for (responder in responders.result.documents) {
                                    if (responder.get("status") == "approved") {
                                        totalApprovedResponse += 1
                                    }
                                }
                            }

                    }
                    binding.tvBrcTotal.text = resources.getString(
                        R.string.brand_value,
                        totalApprovedResponse.toString()
                    )
                }
            }
    }

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
                        totalWalletBalance = 0L.toInt()
                        val documentSnapshot = task.result
                        if (documentSnapshot.exists()) {
                            totalWalletBalance = (documentSnapshot["merchant_wallet_amount"] as Long).toInt()
                            binding.tvBrcTotal.text = resources.getString(
                                R.string.wallet_balance,
                                totalWalletBalance
                            )
                        }
                    } else {
                        totalWalletBalance = 0
                        binding.tvBrcTotal.text = resources.getString(
                            R.string.wallet_balance,
                            totalWalletBalance
                        )
                    }
                }
        }


//    private var imageListener = ImageListener { position: Int, imageView: ImageView? ->
//        imageView?.scaleType = ImageView.ScaleType.FIT_XY
//        val shimmer = Shimmer.ColorHighlightBuilder()
//            .setBaseColor(Color.parseColor("#f3f3f3"))
//            .setHighlightColor(Color.parseColor("#E7E7E7"))
//            .setHighlightAlpha(1F)
//            .setRepeatCount(2)
//            .setDropoff(10F)
//            .setShape(Shimmer.Shape.RADIAL)
//            .setAutoStart(true)
//            .build()
//        val shimmerDrawable = ShimmerDrawable()
//        shimmerDrawable.setShimmer(shimmer)
//        when (position) {
//            0 -> {
//                val remoteConfigUtil = RemoteConfigUtil()
//                imageOne = remoteConfigUtil.getCarouselOneImage()
////                Log.d("AmHere", imageOne)
//                if(imageOne.isNotEmpty()) {
//                    Picasso.get().load(imageOne).placeholder(shimmerDrawable)
//                        .error(R.drawable.brand_img_load_error).into(imageView)
//                }else{
//                    val imageOne =
//                        "https://www.brandibleinc.com/brandiblehosts/kilishimperio/kilishi_imperio.jpg"
//                    Picasso.get().load(imageOne).placeholder(shimmerDrawable)
//                        .error(R.drawable.brand_img_load_error).into(imageView)
//                }
//            }
//            1 -> {
//                val remoteConfigUtil = RemoteConfigUtil()
//                imageTwo = remoteConfigUtil.getCarouselTwoImage()
////                Log.d("AmHere", imageTwo)
//                if(imageTwo.isNotEmpty())
//                    Picasso.get().load(imageTwo).placeholder(shimmerDrawable)
//                        .error(R.drawable.brand_img_load_error).into(imageView)
//
//            }
//            2 -> {
//                 val remoteConfigUtil = RemoteConfigUtil()
//                imageThree = remoteConfigUtil.getCarouselThreeImage()
//                if(imageThree.isNotEmpty())
//                    Picasso.get().load(imageThree).placeholder(shimmerDrawable)
//                        .error(R.drawable.brand_img_load_error).into(imageView)
//            }
//        }
//    }

//    private fun openWebView(brandLink: String) {
//        val intent = Intent()
//        intent.data = Uri.parse(brandLink)
//        intent.action = Intent.ACTION_VIEW
//        startActivity(intent)
//
//    }

    override fun onPostClicked(content: GptContent) {

        val bundle = Bundle()
        bundle.putString("PostTitle",content.title)
        bundle.putString("PostContent",content.content)
        bundle.putString("PostImage",content.image)
        bundle.putString("ImageOwner",content.imageOwner)
        bundle.putString("ImageOwnerLink",content.imageOwnerLink)
        bundle.putString("ImageOwnerUsername",content.imageOwnerUsername)
        try {
            findNavController().navigate(R.id.action_carouselHome_to_postDetailFragment, bundle)
        }catch (e:Exception){
            findNavController().navigate(R.id.action_carouselHome2_to_postDetailFragment2, bundle)
        }
    }

}
