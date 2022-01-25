package ru.zaharchenko.assistentinvoker.model

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ru.zaharchenko.assistentinvoker.data.BtDeviceRepository
import ru.zaharchenko.assistentinvoker.service.BtServiceProvider
import javax.inject.Inject

sealed class UiState {
    object InProgress : UiState()
    object Loaded : UiState()
}

@HiltViewModel
class BtDevicesViewModel @Inject internal constructor(
    btDeviceRepository: BtDeviceRepository
) : ViewModel() {

    val btKeyPressed = mutableStateOf<Pair<Int, Boolean>?>(null)
    val btKeyDialogIsActive = mutableStateOf(false)
    val selectedBtDevice = mutableStateOf<BtDevice?>(null)

    private val _uiState = mutableStateOf<UiState>(UiState.InProgress)
    val uiState: State<UiState>
        get() = _uiState

    private var _btDevices: LiveData<List<BtDevice>> = MutableLiveData()
    val btDevices: LiveData<List<BtDevice>>
        get() = _btDevices

    val repository = btDeviceRepository

    fun loadData(btService: BtServiceProvider) {
        _btDevices = btService.bondedBtDevices()
        _uiState.value = UiState.Loaded
    }

    fun isWaitingForBtKeyPressed() = btKeyDialogIsActive.value && btKeyPressed.value == null
}