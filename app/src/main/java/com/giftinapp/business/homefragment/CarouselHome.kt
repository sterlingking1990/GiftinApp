package com.giftinapp.business.homefragment

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerDrawable
import com.giftinapp.business.R
import com.giftinapp.business.databinding.FragmentCarouselHomeBinding
import com.giftinapp.business.utility.RemoteConfigUtil
import com.giftinapp.business.utility.base.BaseFragment
import com.squareup.picasso.Picasso
import com.synnapps.carouselview.ImageListener
import java.util.*


class CarouselHome : BaseFragment<FragmentCarouselHomeBinding>() {

//    private val getPostListViewModel: GetPostsViewModel by viewModels()
//    lateinit var postListAdapter:PostListAdapter
    private lateinit var binding: FragmentCarouselHomeBinding
    private lateinit var remoteConfigUtil: RemoteConfigUtil
    //private lateinit var imageList:List<Uri>
    private var imageOne =
        "https://www.brandibleinc.com/brandiblehosts/kilishimperio/kilishi_imperio.jpg"
    private var imageTwo =
        "https://www.brandibleinc.com/brandiblehosts/kilishimperio/kilishi_imperio2.jpg"
    private var imageThree ="https://www.brandibleinc.com/brandiblehosts/kilishimperio/kilishi_imperio3.jpg"



    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentCarouselHomeBinding {
        binding = FragmentCarouselHomeBinding.inflate(layoutInflater,container,false)





        val animation = android.view.animation.AnimationUtils.loadAnimation(requireContext(),R.anim.bounce);

//        postListAdapter = PostListAdapter()
//        binding.rvPostList.adapter = postListAdapter
//        binding.rvPostList.layoutManager = GridLayoutManager(requireContext(),2)
//
//        getPostListViewModel.getPostList()
//        //fetchPost()
//
//        observePosts()


        remoteConfigUtil = RemoteConfigUtil()

        binding.carouselView.pageCount = 3
        binding.carouselView.setImageListener(imageListener)

        binding.btnExploreBrand.setOnClickListener {
            it.startAnimation(animation)
            openWebView(remoteConfigUtil.getBrandLink())

        }

        showHelpBar(
            title = "Learn more about this brand",
            content = "Click to Open and learn more about the products and service offering of this brand",
            targetView = binding.btnExploreBrand
        )

        return binding
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
//                    Log.d("Data",it.data.toString())
//                    it.data?.let { it1 -> postListAdapter.setRespondersList(it1) }
//                    postListAdapter.notifyDataSetChanged()
//                }
//                Resource.Status.ERROR -> {
//                    binding.pgLoadingPost.gone()
//                    Toast.makeText(requireContext(),"Unable to load posts",Toast.LENGTH_LONG).show()
//                    Log.d("Error",it.message.toString())
//                }
//            }
//        }
//    }

    private var imageListener = ImageListener { position: Int, imageView: ImageView? ->
        imageView?.scaleType = ImageView.ScaleType.FIT_XY
        val shimmer = Shimmer.ColorHighlightBuilder()
            .setBaseColor(Color.parseColor("#f3f3f3"))
            .setHighlightColor(Color.parseColor("#E7E7E7"))
            .setHighlightAlpha(1F)
            .setRepeatCount(2)
            .setDropoff(10F)
            .setShape(Shimmer.Shape.RADIAL)
            .setAutoStart(true)
            .build()
        val shimmerDrawable = ShimmerDrawable()
        shimmerDrawable.setShimmer(shimmer)
        when (position) {
            0 -> {
                val remoteConfigUtil = RemoteConfigUtil()
                imageOne = remoteConfigUtil.getCarouselOneImage()
//                Log.d("AmHere", imageOne)
                if(imageOne.isNotEmpty()) {
                    Picasso.get().load(imageOne).placeholder(shimmerDrawable)
                        .error(R.drawable.brand_img_load_error).into(imageView)
                }else{
                    val imageOne =
                        "https://www.brandibleinc.com/brandiblehosts/kilishimperio/kilishi_imperio.jpg"
                    Picasso.get().load(imageOne).placeholder(shimmerDrawable)
                        .error(R.drawable.brand_img_load_error).into(imageView)
                }
            }
            1 -> {
                val remoteConfigUtil = RemoteConfigUtil()
                imageTwo = remoteConfigUtil.getCarouselTwoImage()
//                Log.d("AmHere", imageTwo)
                if(imageTwo.isNotEmpty())
                    Picasso.get().load(imageTwo).placeholder(shimmerDrawable)
                        .error(R.drawable.brand_img_load_error).into(imageView)

            }
            2 -> {
                 val remoteConfigUtil = RemoteConfigUtil()
                imageThree = remoteConfigUtil.getCarouselThreeImage()
                if(imageThree.isNotEmpty())
                    Picasso.get().load(imageThree).placeholder(shimmerDrawable)
                        .error(R.drawable.brand_img_load_error).into(imageView)
            }
        }
    }

    private fun openWebView(brandLink: String) {
        val intent = Intent()
        intent.data = Uri.parse(brandLink)
        intent.action = Intent.ACTION_VIEW
        startActivity(intent)

    }
}