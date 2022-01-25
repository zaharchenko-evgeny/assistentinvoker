package ru.zaharchenko.assistentinvoker.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bt_device")
class BtDevice(
    @PrimaryKey @ColumnInfo(name = "address") val address: String,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "key") var key: Int? = null,
    @ColumnInfo(name = "isLong") var isLongPressed: Boolean = false,
    var isActive: Boolean = false
)