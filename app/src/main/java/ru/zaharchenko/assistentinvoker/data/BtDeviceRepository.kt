package ru.zaharchenko.assistentinvoker.data

import kotlinx.coroutines.flow.Flow
import ru.zaharchenko.assistentinvoker.model.BtDevice
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BtDeviceRepository @Inject constructor(
    private val btDeviceDao: BtDeviceDao
) {

    fun getBtDevices(): Flow<List<BtDevice>> = btDeviceDao.getBtDevices()

    fun getPlant(address: String): Flow<BtDevice> = btDeviceDao.getBtDevice(address)

    suspend fun insert(btDevice: BtDevice) {
        btDeviceDao.insert(btDevice)
    }

    suspend fun delete(btDevice: BtDevice) {
        btDeviceDao.delete(btDevice)
    }
}