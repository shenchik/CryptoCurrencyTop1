package com.example.cryptocurrencytop.presentation.adapters

import androidx.recyclerview.widget.DiffUtil
import com.example.cryptocurrencytop.data.pojo.CoinPriceInfo

class PriceDiffUtilsCallback: DiffUtil.ItemCallback<CoinPriceInfo>() {
    override fun areItemsTheSame(oldItem: CoinPriceInfo, newItem: CoinPriceInfo): Boolean {
        return oldItem.fromSymbol == newItem.fromSymbol && oldItem.lastUpdate == newItem.lastUpdate
    }

    override fun areContentsTheSame(oldItem: CoinPriceInfo, newItem: CoinPriceInfo): Boolean {
        return oldItem == newItem
    }
}