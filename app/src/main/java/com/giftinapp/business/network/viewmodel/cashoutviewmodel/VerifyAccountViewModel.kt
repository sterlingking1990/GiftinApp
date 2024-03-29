package com.giftinapp.business.network.viewmodel.cashoutviewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.giftinapp.business.network.cashoutmodel.BankResponse
import com.giftinapp.business.network.cashoutmodel.VerifyAccountResponse
import com.giftinapp.business.network.repository.cashoutrepository.CashOutApiServiceRepository
import com.giftinapp.business.utility.NetworkHelper
import com.giftinapp.business.utility.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VerifyAccountViewModel @Inject constructor(private val cashOutApiServiceRepository: CashOutApiServiceRepository, private val networkHelper: NetworkHelper) : ViewModel() {

    private var verifyAccountResponse = MutableLiveData<Resource<VerifyAccountResponse>>()
    val _verifyAccountObservable: LiveData<Resource<VerifyAccountResponse>>
        get() = verifyAccountResponse


    fun verifyAccountNumber(authorization:String,account_number:String,bank_code:String){
        viewModelScope.launch {
            verifyAccountResponse.postValue(Resource.loading())

            try{
                val verifiedAccountResponse = cashOutApiServiceRepository.verifyAccountNumber(authorization,account_number,bank_code)
                Log.d("verified",verifiedAccountResponse.toString())
                if(verifiedAccountResponse.isSuccessful){
                    verifyAccountResponse.postValue(Resource(Resource.Status.SUCCESS,verifiedAccountResponse.body(),null))
                }
                else{
                    verifyAccountResponse.postValue(Resource.error("Unable to verify account"))
                }
            }
            catch (e:Exception){
                Log.d("Error",e.message.toString())
                verifyAccountResponse.postValue(Resource.error("Error -> ${e.message}"))
            }
        }
    }

}