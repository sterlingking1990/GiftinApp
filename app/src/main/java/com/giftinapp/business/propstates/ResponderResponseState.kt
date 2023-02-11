package com.giftinapp.business.propstates

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ResponderResponseState @Inject constructor(): ViewModel() {
    private var _taskResponseSubmitted = MutableLiveData(false)
    val responseSubmittedObservable: LiveData<Boolean> = _taskResponseSubmitted

    fun submitTaskResponse(status:Boolean){
        viewModelScope.launch {
            _taskResponseSubmitted.value=status
        }
    }
}