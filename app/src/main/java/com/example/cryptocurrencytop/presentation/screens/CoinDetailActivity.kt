package com.example.cryptocurrencytop.presentation.screens

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.cryptocurrencytop.R

class CoinDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coin_detail)

        if (savedInstanceState == null) {
            val fragment = CoinDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(
                        CoinDetailFragment.ARG_ITEM_ID,
                        intent.getStringExtra(CoinDetailFragment.ARG_ITEM_ID)
                    )
                }
            }


            supportFragmentManager.beginTransaction()
                .add(R.id.item_detail_container, fragment)
                .commit()
        }
    }
}
