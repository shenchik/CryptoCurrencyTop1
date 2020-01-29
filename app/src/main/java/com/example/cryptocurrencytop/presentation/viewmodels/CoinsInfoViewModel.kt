package com.example.cryptocurrencytop.presentation.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.cryptocurrencytop.data.Repository

class CoinsInfoViewModel(application: Application): AndroidViewModel(application) {

    private val repository = Repository(application)

    fun getPriceList() = repository.fullPriceList
    fun getPriceInfoAboutCoin(symbol: String) = repository.getPriceInfoAboutCoin(symbol)
}