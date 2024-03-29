package com.giftinapp.business.network.viewmodel.cashoutviewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.giftinapp.business.network.cashoutmodel.InitiateTransferRequestModel
import com.giftinapp.business.network.cashoutmodel.InitiateTransferResponseModel
import com.giftinapp.business.network.cashoutmodel.VerifyAccountResponse
import com.giftinapp.business.network.repository.cashoutrepository.CashOutApiServiceRepository
import com.giftinapp.business.utility.NetworkHelper
import com.giftinapp.business.utility.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class InitiateTransferViewModel @Inject constructor(private val cashOutApiServiceRepository: CashOutApiServiceRepository, private val networkHelper: NetworkHelper) : ViewModel() {

    private var initiateTransferResponse = MutableLiveData<Resource<InitiateTransferResponseModel>>()
    val _initiateTransferResponseObservable: LiveData<Resource<InitiateTransferResponseModel>>
        get() = initiateTransferResponse


    fun initiateTransferProcess(authorization:String,initiateTransferRequestModel: InitiateTransferRequestModel){
        viewModelScope.launch {
            initiateTransferResponse.postValue(Resource.loading())

            try{
                val transferRecieptResponse = cashOutApiServiceRepository.initiateTransferProcess(authorization,initiateTransferRequestModel)
                Log.d("verified",transferRecieptResponse.toString())
                if(transferRecieptResponse.isSuccessful){
                    initiateTransferResponse.postValue(Resource(Resource.Status.SUCCESS,transferRecieptResponse.body(),null))
                }
                else{
                    initiateTransferResponse.postValue(Resource.error("Unable to verify account"))
                }
            }
            catch (e:Exception){
                Log.d("Error",e.message.toString())
                initiateTransferResponse.postValue(Resource.error("Error -> ${e.message}"))
            }
        }
    }

}