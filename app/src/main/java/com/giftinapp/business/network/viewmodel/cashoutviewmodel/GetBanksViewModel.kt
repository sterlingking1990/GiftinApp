package com.giftinapp.business.network.viewmodel.cashoutviewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.giftinapp.business.model.BankItem
import com.giftinapp.business.model.FetchBanksResponse
import com.giftinapp.business.network.cashoutmodel.BankResponse
import com.giftinapp.business.network.repository.cashoutrepository.CashOutApiServiceRepository
import com.giftinapp.business.utility.NetworkHelper
import com.giftinapp.business.utility.Resource
import com.skydoves.whatif.whatIfNotNull
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class GetBanksViewModel @Inject constructor(private val cashOutApiServiceRepository: CashOutApiServiceRepository,  private val networkHelper: NetworkHelper) : ViewModel() {

   private var _bankListResponse = MutableLiveData<Resource<FetchBanksResponse>>()
       val bankListResponse:LiveData<Resource<FetchBanksResponse>>
        get() = _bankListResponse

     fun getBankList(){
         if (networkHelper.isNetworkConnected()) {
             _bankListResponse.postValue(Resource.loading(null))
             viewModelScope.launch {
                 with(Dispatchers.IO) {
                     runCatching {
                         cashOutApiServiceRepository.getBankLists()
                     }.onSuccess {
                         Log.d("BanksResponse",it.toString())
                         _bankListResponse.postValue(Resource.success(it))
                     }
                         .onFailure {
                             _bankListResponse.postValue(Resource.error(it.localizedMessage))
                         }
                 }
             }
         } else {
             _bankListResponse.postValue(
                 (Resource.error("No network connectivity"))
             )
         }
    }

    fun sortBanks(data: FetchBanksResponse?): ArrayList<BankItem> {
        val bankNameAndCode = arrayListOf<BankItem>()
        data?.banks.whatIfNotNull {
            it.forEach { bank ->
                bankNameAndCode.add(
                    BankItem(
                        name = bank.name,
                        code = bank.code
                    )
                )
            }
        }
        return bankNameAndCode
    }
}