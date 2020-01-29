package com.example.cryptocurrencytop.presentation.screens

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.cryptocurrencytop.BuildConfig
import com.example.cryptocurrencytop.R
import com.example.cryptocurrencytop.data.pojo.CoinPriceInfo
import com.example.cryptocurrencytop.presentation.viewmodels.CoinsInfoViewModel
import com.example.cryptocurrencytop.utils.getTimeHMSFromTimestamp
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_coin_detail.*

class CoinDetailFragment : Fragment() {

    private lateinit var viewModel: CoinsInfoViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProviders.of(this).get(CoinsInfoViewModel::class.java)
        return inflater.inflate(R.layout.fragment_coin_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            if (it.containsKey(ARG_ITEM_ID)) {
                val coinId = it.getString(ARG_ITEM_ID)?:""
                viewModel.getPriceInfoAboutCoin(coinId).observe(this, Observer {
                    showCoinIfoDetails(it)
                })
            }
        }
    }

    private fun showCoinIfoDetails(coinPriceInfo: CoinPriceInfo) {
        //Glide.with(this).load(BuildConfig.BASE_IMAGES_URL + coinPriceInfo.imageUrl).into(image_view_logo_coin)
        Picasso.get().load(BuildConfig.BASE_IMAGES_URL + coinPriceInfo.imageUrl).into(image_view_logo_coin)
        text_view_from_symbol.text = coinPriceInfo.fromSymbol
        text_view_to_symbol.text = coinPriceInfo.toSymbol
        text_view_price.text = coinPriceInfo.price
        text_view_min_day.text = coinPriceInfo.lowDay
        text_view_max_day.text = coinPriceInfo.highDay
        text_view_market.text = coinPriceInfo.lastMarket
        text_view_last_update.text = getTimeHMSFromTimestamp(coinPriceInfo.lastUpdate?.times(1000)?:0)
    }

    companion object {
        const val ARG_ITEM_ID = "item_id"
    }
}
