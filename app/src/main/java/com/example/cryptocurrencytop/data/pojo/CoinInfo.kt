package com.example.cryptocurrencytop.data.pojo

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Entity(tableName = "coins")
class CoinInfo (
    @PrimaryKey
    @SerializedName("Id")
    @Expose
    val id: String,
    @SerializedName("Name")
    @Expose
    val name: String?,
    @SerializedName("FullName")
    @Expose
    val fullName: String?,
    @SerializedName("ImageUrl")
    @Expose
    val imageUrl: String?
)
