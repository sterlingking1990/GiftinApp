package com.giftinapp.business.network.cashoutmodel

data class InitiateTransferRequestModel(var type:String,
                                        var name:String,
                                        var account_number:String,
                                        var bank_code:String,
                                        var currency:String)


