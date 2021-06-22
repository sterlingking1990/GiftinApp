package com.giftinapp.business.network.viewmodel.cashoutviewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.giftinapp.business.network.cashoutmodel.BankResponse
import com.giftinapp.business.network.repository.cashoutrepository.CashOutApiServiceRepository
import com.giftinapp.business.utility.NetworkHelper
import com.giftinapp.business.utility.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class GetBanksViewModel @Inject constructor(private val cashOutApiServiceRepository: CashOutApiServiceRepository, private val networkHelper: NetworkHelper) : ViewModel() {

   private var bankListResponse = MutableLiveData<Resource<BankResponse>>()
       val _bankListObservable:LiveData<Resource<BankResponse>>
        get() = bankListResponse


    fun getBankList(authorization:String,country:String){
        viewModelScope.launch {
            bankListResponse.postValue(Resource.loading())

            try{
               val bankList = cashOutApiServiceRepository.getBankLists(authorization,country)

                if(bankList.isSuccessful){
                    bankListResponse.postValue(Resource(Resource.Status.SUCCESS,bankList.body(),null))
                }
                else{
                    bankListResponse.postValue(Resource.error("Unable to fetch bank list"))
                }
            }
            catch (e:Exception){
                Log.d("Error",e.message.toString())
                bankListResponse.postValue(Resource.error("Error -> ${e.message}"))
            }
        }
    }

}