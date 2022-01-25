package ru.zaharchenko.assistentinvoker.service

import androidx.lifecycle.LiveData
import ru.zaharchenko.assistentinvoker.model.BtDevice

interface BtServiceProvider {

    fun bondedBtDevices(): LiveData<List<BtDevice>>
}