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
class GetBanksViewModel @Inject constructor(private val cashOutApiServiceRepository: CashOutApiServiceRepository) : ViewModel() {

   private var _bankListResponse = MutableLiveData<Resource<BankResponse>>()
       val bankListObservable:LiveData<Resource<BankResponse>>
        get() = _bankListResponse

     fun getBankList(){
        viewModelScope.launch {
            try{
               val bankList = cashOutApiServiceRepository.getBankLists()

                if(bankList.isSuccessful){
                    _bankListResponse.postValue(Resource(Resource.Status.SUCCESS,bankList.body(),null))
                }
                if(bankList.errorBody()!=null){
                    _bankListResponse.postValue(Resource.error("Unable to fetch bank list"))
                }
            }
            catch (e:Exception){
                Log.d("Error",e.message.toString())
                _bankListResponse.postValue(Resource.error("Error -> ${e.message}"))
            }
        }
    }
}