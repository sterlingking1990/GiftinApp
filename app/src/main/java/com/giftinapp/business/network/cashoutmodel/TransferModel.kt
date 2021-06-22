package com.giftinapp.business.network.cashoutmodel

data class TransferModel(var source:String,
                         var amount:String,
                         var recipient:String,
                         var reason:String)
