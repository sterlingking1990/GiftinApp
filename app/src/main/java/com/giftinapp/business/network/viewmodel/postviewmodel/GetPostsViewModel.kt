package com.giftinapp.business.network.viewmodel.postviewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.giftinapp.business.model.FetchBanksResponse
import com.giftinapp.business.model.posts.Post
import com.giftinapp.business.network.repository.postrepository.PostApiServiceRepo
import com.giftinapp.business.utility.NetworkHelper
import com.giftinapp.business.utility.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import javax.inject.Inject

@HiltViewModel
class GetPostsViewModel @Inject constructor(private val postApiServiceRepo: PostApiServiceRepo, private val networkHelper: NetworkHelper): ViewModel(){

    private var _postListResponse = MutableLiveData<Resource<List<Post>>>()
    val postListResponse:LiveData<Resource<List<Post>>>
        get() = _postListResponse

    private var _postDetailResponse = MutableLiveData<Resource<Post>>()
    val postDetailResponse:LiveData<Resource<Post>>
    get() = _postDetailResponse

    fun getPostList(){
        if (networkHelper.isNetworkConnected()) {
            _postListResponse.postValue(Resource.loading(null))
            viewModelScope.launch {
                with(Dispatchers.IO) {
                    runCatching {
                        postApiServiceRepo.fetchPosts()
                    }.onSuccess {
                        Log.d("BanksResponse",it.toString())
                        _postListResponse.postValue(Resource.success(it))
                    }
                        .onFailure {
                            _postListResponse.postValue(Resource.error(it.localizedMessage))
                        }
                }
            }
        } else {
            _postListResponse.postValue(
                (Resource.error("No network connectivity"))
            )
        }
    }

    fun getPostDetail(postId:Int){
        if (networkHelper.isNetworkConnected()) {
            _postListResponse.postValue(Resource.loading(null))
            viewModelScope.launch {
                with(Dispatchers.IO) {
                    runCatching {
                        postApiServiceRepo.getPostDetail(postId)
                    }.onSuccess {
                        Log.d("BanksResponse",it.toString())
                        _postDetailResponse.postValue(Resource.success(it))
                    }
                        .onFailure {
                            _postDetailResponse.postValue(Resource.error(it.localizedMessage))
                        }
                }
            }
        } else {
            _postDetailResponse.postValue(
                (Resource.error("No network connectivity"))
            )
        }
    }

}