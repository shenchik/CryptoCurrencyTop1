package com.example.cryptocurrencytop.domain

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.cryptocurrencytop.R
import com.example.cryptocurrencytop.domain.api.ApiFactory
import com.example.cryptocurrencytop.data.pojo.CoinPriceInfo
import com.example.cryptocurrencytop.data.AppDatabase
import com.example.cryptocurrencytop.presentation.App
import com.example.cryptocurrencytop.presentation.screens.CoinsListActivity
import com.example.cryptocurrencytop.utils.convertPercentOfMinutesToSeconds
import com.example.cryptocurrencytop.utils.getTimeHMSFromTimestamp
import com.google.gson.Gson
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.lang.StringBuilder

class ServiceOfLoadingData : Service() {

    private val compositeDisposable = CompositeDisposable()
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var pendingIntent: PendingIntent
    private var notificationManager: NotificationManager? = null

    private lateinit var db: AppDatabase
    private lateinit var preferences: SharedPreferences
    private lateinit var prefsChangeListener: SharedPreferences.OnSharedPreferenceChangeListener

    private var timeout = 18
    private val handler = Handler()
    private var timer: Runnable? = null

    private var coinsName: List<String>? = null

    companion object {
        const val TAG = "ServiceOfLoadingData"
        const val FOREGROUND_SERVICE_ID = 1
        const val NOTIFICATION_CHANNEL_ID = "channel_loading_data_id"
        const val NOTIFICATION_CHANNEL_NAME = "channel_loading_data_name"
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager?.createNotificationChannel(channel)
        }
        db = AppDatabase.getInstance(this)
        val intent = Intent(this, CoinsListActivity::class.java)
        pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
        notificationBuilder = NotificationCompat.Builder(
            this,
            NOTIFICATION_CHANNEL_ID
        )
        prefsChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, _ ->
            prefs?.let {
                if (it.contains(App.KEY_REFRESHING_PERIOD)) {
                    val periodPercent = it.getInt(App.KEY_REFRESHING_PERIOD, 30)
                    timeout = convertPercentOfMinutesToSeconds(periodPercent)
                    notificationBuilder.setContentTitle(
                        String.format(
                            getString(R.string.period_of_refreshing_label),
                            timeout
                        )
                    )
                    notificationManager?.notify(FOREGROUND_SERVICE_ID, notificationBuilder.build())
                }
            }
        }
        preferences = getSharedPreferences(App.SHARED_PREFS_NAME, Context.MODE_PRIVATE)
        if (preferences.contains(App.KEY_REFRESHING_PERIOD)) {
            val periodPercent = preferences.getInt(App.KEY_REFRESHING_PERIOD, 30)
            timeout = convertPercentOfMinutesToSeconds(periodPercent)
        }
        preferences.registerOnSharedPreferenceChangeListener(prefsChangeListener)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        timer?.let {
            handler.removeCallbacks(it)
        }
        timer = object : Runnable {
            override fun run() {
                loadData()
                handler.postDelayed(this, timeout * 1000L)
                Log.d("RefreshingTest", "Refresh")
            }
        }
        timer?.let { handler.post(it) }
        notificationBuilder.setContentText(
            String.format(
                getString(R.string.last_update_label_with_placeholder),
                getTimeHMSFromTimestamp(System.currentTimeMillis(), true)
            )
        )
            .setContentTitle(String.format(getString(R.string.period_of_refreshing_label), timeout))
            .setSmallIcon(R.drawable.ic_notification_coin)
            .setContentIntent(pendingIntent)
        startForeground(FOREGROUND_SERVICE_ID, notificationBuilder.build())
        return super.onStartCommand(intent, flags, startId)
    }


    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        clearResources()
        super.onDestroy()
    }

    fun loadData() {
        if (coinsName.isNullOrEmpty()) {
            val disposable = ApiFactory.apiService.getTopCoinsInfo(20)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe({
                    db.coinInfoDao().insertCoins(it.listOfCoins.map { it.coinInfo })
                    val symbols = db.coinInfoDao().getAllCoinsNames()
                    coinsName = symbols
                    loadPriceList(symbols)
                }, {
                    Log.d(TAG, it.message ?: "Unknown error")
                })
            compositeDisposable.add(disposable)
        } else {
            coinsName?.let {
                loadPriceList(it)
            }
        }
    }

    private fun loadPriceList(symbols: List<String?>) {
        val symbolsBuilder = StringBuilder()
        for (i in 0 until symbols.size) {
            symbolsBuilder.append(symbols[i])
            if (i != symbols.size - 1) {
                symbolsBuilder.append(",")
            }
        }
        val disposable = ApiFactory.apiService.getFullPriceList(symbolsBuilder.toString())
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe({
                val listOfFullPriceInfo = mutableListOf<CoinPriceInfo>()
                val coinPriceInfoJSONObject = it.coinPriceInfoJSONObject
                coinPriceInfoJSONObject?.apply {
                    for (key in keySet()) {
                        val infoJsonObject = getAsJsonObject(key)
                        for (currency in infoJsonObject.keySet()) {
                            val priceInfo =
                                Gson().fromJson(infoJsonObject.getAsJsonObject(currency), CoinPriceInfo::class.java)
                            listOfFullPriceInfo.add(priceInfo)
                        }
                    }
                }
                db.coinPriceInfoDao().insertPriceList(listOfFullPriceInfo)
                notificationBuilder.setContentText(
                    String.format(
                        getString(R.string.last_update_label_with_placeholder),
                        getTimeHMSFromTimestamp(System.currentTimeMillis(), true)
                    )
                )
                notificationManager?.notify(FOREGROUND_SERVICE_ID, notificationBuilder.build())
            }, {
                Log.d(TAG, it.message ?: "Unknown error")
            })
        compositeDisposable.add(disposable)
    }

    private fun clearResources() {
        timer?.let {
            handler.removeCallbacks(it)
        }
        preferences.unregisterOnSharedPreferenceChangeListener(prefsChangeListener)
        compositeDisposable.dispose()
    }

}