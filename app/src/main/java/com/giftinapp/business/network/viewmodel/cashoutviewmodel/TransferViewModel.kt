package com.giftinapp.business.network.viewmodel.cashoutviewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.giftinapp.business.network.cashoutmodel.InitiateTransferRequestModel
import com.giftinapp.business.network.cashoutmodel.InitiateTransferResponseModel
import com.giftinapp.business.network.cashoutmodel.TransferModel
import com.giftinapp.business.network.cashoutmodel.TransferModelResponse
import com.giftinapp.business.network.repository.cashoutrepository.CashOutApiServiceRepository
import com.giftinapp.business.utility.NetworkHelper
import com.giftinapp.business.utility.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransferViewModel @Inject constructor(private val cashOutApiServiceRepository: CashOutApiServiceRepository, private val networkHelper: NetworkHelper) : ViewModel() {

    private var transferResponse = MutableLiveData<Resource<TransferModelResponse>>()
    val _transferResponseObservable: LiveData<Resource<TransferModelResponse>>
        get() = transferResponse


    fun transferToBank(authorization:String,transferRequest: TransferModel){
        viewModelScope.launch {
            transferResponse.postValue(Resource.loading())

            try{
                val transferResponseReceived = cashOutApiServiceRepository.transfer(authorization,transferRequest)
                Log.d("verified",transferResponseReceived.toString())
                if(transferResponseReceived.isSuccessful){
                    transferResponse.postValue(Resource(Resource.Status.SUCCESS,transferResponseReceived.body(),null))
                }
                else{
                    transferResponse.postValue(Resource.error("Unable to verify account"))
                }
            }
            catch (e:Exception){
                Log.d("Error",e.message.toString())
                transferResponse.postValue(Resource.error("Error -> ${e.message}"))
            }
        }
    }

}

