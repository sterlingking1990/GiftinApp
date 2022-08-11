package com.giftinapp.business.propstates

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReviewState @Inject constructor():ViewModel() {
    private var _sentReview = MutableLiveData(false)
    val sentReviewObservable:LiveData<Boolean> = _sentReview

    fun updateSentReviewState(isReviewSent:Boolean){
        viewModelScope.launch {
            _sentReview.value=isReviewSent
        }
    }


}