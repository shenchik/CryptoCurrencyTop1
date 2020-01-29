package com.example.cryptocurrencytop.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.cryptocurrencytop.domain.dao.CoinInfoDao
import com.example.cryptocurrencytop.domain.dao.CoinPriceInfoDao
import com.example.cryptocurrencytop.data.pojo.CoinInfo
import com.example.cryptocurrencytop.data.pojo.CoinPriceInfo

@Database(entities = [CoinPriceInfo::class, CoinInfo::class], version = 2 , exportSchema = false)
abstract class AppDatabase: RoomDatabase() {
    companion object {
        private const val DB_NAME = "main.db"
        private var db: AppDatabase? = null
        private val LOCK = Any()
        fun getInstance(context: Context): AppDatabase {
            synchronized(LOCK) {
                db?.let { return it }
                val instance = Room.databaseBuilder(
                    context, AppDatabase::class.java,
                    DB_NAME
                ).fallbackToDestructiveMigration()
                    .build()
                db = instance
                return instance
            }
        }
    }

    abstract fun coinPriceInfoDao(): CoinPriceInfoDao
    abstract fun coinInfoDao(): CoinInfoDao
}