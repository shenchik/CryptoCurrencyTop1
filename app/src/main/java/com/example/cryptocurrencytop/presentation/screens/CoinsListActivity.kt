package com.example.cryptocurrencytop.presentation.screens

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.cryptocurrencytop.R
import com.example.cryptocurrencytop.domain.ServiceOfLoadingData
import com.example.cryptocurrencytop.presentation.App.Companion.KEY_REFRESHING_PERIOD
import com.example.cryptocurrencytop.presentation.App.Companion.SHARED_PREFS_NAME
import com.example.cryptocurrencytop.presentation.adapters.PriceListAdapter
import com.example.cryptocurrencytop.presentation.adapters.PriceDiffUtilsCallback
import com.example.cryptocurrencytop.presentation.viewmodels.CoinsInfoViewModel
import com.example.cryptocurrencytop.utils.convertPercentOfMinutesToSeconds
import kotlinx.android.synthetic.main.activity_coins_list.*
import kotlinx.android.synthetic.main.coins_list.*

class CoinsListActivity : AppCompatActivity() {

    private var twoPane: Boolean = false

    private lateinit var adapter: PriceListAdapter
    private lateinit var coinsInfoViewModel: CoinsInfoViewModel
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var serviceLoadingIntent: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coins_list)
        sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
        coinsInfoViewModel = ViewModelProviders.of(this).get(CoinsInfoViewModel::class.java)
        serviceLoadingIntent = Intent(this, ServiceOfLoadingData::class.java)
        if (item_detail_container != null) {
            twoPane = true
        }
        setupRecyclerView(item_list)
        setupSeekBar()
        coinsInfoViewModel.getPriceList().observe(this, Observer {
            if (it.isEmpty()) {
                progress_bar_loading.visibility = View.VISIBLE
            } else {
                progress_bar_loading.visibility = View.GONE
            }
            adapter.submitList(it)
        })
        switchLoadingService(serviceLoadingIntent, true)
    }

    private fun switchLoadingService(serviceIntent: Intent, shouldTurnOn: Boolean) {
        if (shouldTurnOn) {
            ContextCompat.startForegroundService(this, serviceIntent)
        } else {
            stopService(serviceIntent)
        }
    }

    private fun setupSeekBar() {
        seek_bar_time_of_refreshing.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                val progress = convertPercentOfMinutesToSeconds(p1)
                sharedPreferences.edit().putInt(KEY_REFRESHING_PERIOD, p1).apply()
                text_view_period_of_refreshing_label.text = String.format(getString(R.string.period_of_refreshing_label), progress)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }
        })
        seek_bar_time_of_refreshing.progress = if (sharedPreferences.contains(KEY_REFRESHING_PERIOD)) {
            (sharedPreferences.getInt(KEY_REFRESHING_PERIOD, 50))
        } else {
            50 //default value of refreshing period (percent from minutes)
        }
        text_view_period_of_refreshing_label.text = String.format(getString(R.string.period_of_refreshing_label), convertPercentOfMinutesToSeconds(seek_bar_time_of_refreshing.progress))
    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        adapter = PriceListAdapter(this, PriceDiffUtilsCallback())
        adapter.onItemClickListener = {
            if (twoPane) {
                val fragment = CoinDetailFragment().apply {
                    arguments = Bundle().apply {
                        putString(
                            CoinDetailFragment.ARG_ITEM_ID,
                            it.fromSymbol
                        )
                    }
                }

                supportFragmentManager.beginTransaction()
                    .replace(R.id.item_detail_container, fragment)
                    .commit()
            } else {
                val intent = Intent(this, CoinDetailActivity::class.java)
                intent.putExtra(CoinDetailFragment.ARG_ITEM_ID, it.fromSymbol)
                startActivity(intent)
            }
        }
        recyclerView.adapter = adapter
        recyclerView.itemAnimator = null
    }

    override fun onDestroy() {
        switchLoadingService(serviceLoadingIntent, false)
        super.onDestroy()
    }
}
