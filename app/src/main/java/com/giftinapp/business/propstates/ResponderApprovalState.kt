package com.giftinapp.business.propstates

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResponderApprovalState @Inject constructor(): ViewModel() {
    private var _approveResponderReview = MutableLiveData(false)
    val approveResponderReviewObservable: LiveData<Boolean> = _approveResponderReview

    fun approveResponderReview(status:Boolean){
        viewModelScope.launch {
            _approveResponderReview.value=status
        }
    }
}