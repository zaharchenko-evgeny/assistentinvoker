package ru.zaharchenko.assistentinvoker.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.zaharchenko.assistentinvoker.model.BtDevice

@Dao
interface BtDeviceDao {

    @Query("SELECT * FROM bt_device")
    fun getBtDevices(): Flow<List<BtDevice>>

    @Query("SELECT * FROM bt_device WHERE address = :address")
    fun getBtDevice(address: String): Flow<BtDevice>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(btDevice: BtDevice)

    @Query("DELETE FROM bt_device where address = :address")
    suspend fun deleteByAddress(address:String)

    @Delete
    suspend fun delete(btDevice: BtDevice)

}