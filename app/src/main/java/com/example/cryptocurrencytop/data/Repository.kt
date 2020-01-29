package com.example.cryptocurrencytop.data

import android.content.Context

class Repository(context: Context) {

    private val db: AppDatabase = AppDatabase.getInstance(context)
    private val coinPriceInfoDao = db.coinPriceInfoDao()

    val fullPriceList = coinPriceInfoDao.getPriceList()
    fun getPriceInfoAboutCoin(symbol: String) = coinPriceInfoDao.getPriceInfoAboutCoin(symbol)
}