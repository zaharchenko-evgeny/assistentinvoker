package ru.zaharchenko.assistentinvoker.fragments

import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.zaharchenko.assistentinvoker.activity.MediaButtonEventReceiver
import ru.zaharchenko.assistentinvoker.model.BtDevice
import ru.zaharchenko.assistentinvoker.model.BtDevicesViewModel
import ru.zaharchenko.assistentinvoker.model.UiState

/**
 * A fragment representing a list of Items.
 */
@AndroidEntryPoint
class BtDeviceFragment : Fragment() {

    private val viewModel by activityViewModels<BtDevicesViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                BtDevicesPage()
            }
        }
    }

    @Composable
    fun BtDevicesPage(viewModel: BtDevicesViewModel = this.viewModel) {
        val uiState = viewModel.uiState

        val btDevicesState = viewModel.btDevices.observeAsState()
        val btDevices = btDevicesState.value


        Box(modifier = Modifier.padding(4.dp)) {
            ConfigureDialog()
            if (uiState.value == UiState.InProgress || btDevices == null) {
                CircularProgressIndicator(modifier = Modifier.wrapContentWidth(CenterHorizontally))
            } else {
                PairedBtDevicesList(btDevices)
            }
        }
    }

    @Composable
    fun PairedBtDevicesList(btDevices: List<BtDevice>) {
        if (btDevices.isEmpty()) {
            MessageForEmptyPairedDevices()
        } else {
            Column {
                BtDevicesColumn("Connected bluetooth devices", btDevices.filter { it.isActive })
                Spacer(modifier = Modifier.height(8.dp))
                BtDevicesColumn("Disconnected bluetooth devices", btDevices.filter { !it.isActive })
            }
        }
    }

    @Composable
    private fun BtDevicesColumn(caption: String, btDevices: List<BtDevice>) {
        if (btDevices.isNotEmpty()) {
            Text(
                text = caption,
                color = MaterialTheme.colors.primary,
                style = MaterialTheme.typography.h6
            )
            Spacer(modifier = Modifier.height(4.dp))
            LazyColumn(
                modifier = Modifier.padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(btDevices) { btDevice ->
                    BtDeviceCard(btDevice)
                }
            }
        }
    }

    @Composable
    fun BtDeviceCard(btDevice: BtDevice) {
        Row {
            Icon(
                Icons.Default.Bluetooth,
                tint = MaterialTheme.colors.secondary,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1.0f)) {
                Text(
                    text = btDevice.name,
                    color = MaterialTheme.colors.secondaryVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                val key = btDevice.key
                if (key != null) {
                    Text(
                        text = if (btDevice.isLongPressed) "$key long pressed" else "$key",
                        color = MaterialTheme.colors.primaryVariant
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            if (btDevice.isActive) {
                Button(modifier = Modifier
                    // Set image size to 40 dp
                    .wrapContentWidth(Alignment.End),
                    onClick = { assignBtKey(btDevice) }) {
                    Text(text = "Configure")
                }
            }
        }
    }

    private fun assignBtKey(btDevice: BtDevice) {
        viewModel.btKeyDialogIsActive.value = true
        viewModel.selectedBtDevice.value = btDevice
    }

    @Composable
    fun MessageForEmptyPairedDevices() {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = "No paired bluetooth devices.",
                color = MaterialTheme.colors.primaryVariant,
                style = MaterialTheme.typography.h4
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Pair bluetooth device first.",
                color = MaterialTheme.colors.primaryVariant,
                style = MaterialTheme.typography.h5
            )
        }
    }

    @Composable
    fun ConfigureDialog() {
        val requester = remember { FocusRequester() }
        if (viewModel.btKeyDialogIsActive.value) {
            AlertDialog(
                title = {
                    Text(text = "Configure assistant key")
                },
                modifier = Modifier
                    .focusRequester(requester)
                    .focusable()
                    .onKeyEvent {
                        val nativeKeyEvent = it.nativeKeyEvent
                        if (nativeKeyEvent.keyCode == KeyEvent.KEYCODE_BACK) {
                            resetDialogModelState()
                            return@onKeyEvent false
                        }
                        false
                    },
                text = {
                    Column {
                        val keyPressed = viewModel.btKeyPressed.value
                        if (keyPressed == null) {
                            Text("Press key on bluetooth device")
                            Spacer(modifier = Modifier.height(4.dp))
                            CircularProgressIndicator(
                                modifier = Modifier.wrapContentWidth(
                                    CenterHorizontally
                                )
                            )
                        } else {
                            Text("Pressed key")
                            Spacer(modifier = Modifier.height(4.dp))
                            Row {
                                Text("${keyPressed.first}")
                                if (keyPressed.second) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("long pressed")
                                }
                            }
                        }
                    }
                },
                buttons = {
                    Row(
                        modifier = Modifier
                            .wrapContentWidth(Alignment.End)
                    ) {
                        Button(onClick = {
                            //Persist value
                            viewModel.apply {
                                viewModel.selectedBtDevice.value?.let { selectedBtDevice ->
                                    selectedBtDevice.key = btKeyPressed.value!!.first
                                    selectedBtDevice.isLongPressed = btKeyPressed.value!!.second
                                    viewModelScope.launch {
                                        viewModel.repository.insert(selectedBtDevice)
                                    }
                                }
                            }

                            resetDialogModelState()
                        }) {
                            Text(text = "Save key")
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Button(onClick = {
                            viewModel.apply {
                                viewModel.selectedBtDevice.value?.let { selectedBtDevice ->
                                    viewModelScope.launch {
                                        viewModel.repository.delete(selectedBtDevice)
                                    }
                                }
                            }

                            resetDialogModelState()
                        }) {
                            Text(text = "Reset")
                        }
                    }
                    LaunchedEffect(Unit) {
                        requester.requestFocus()
                    }

                },
                onDismissRequest = {

                }
            )
        }
    }

    private fun resetDialogModelState() {
        viewModel.apply {
            btKeyPressed.value = null
            selectedBtDevice.value = null
        }

        //Close dialog
        viewModel.btKeyDialogIsActive.value = false
    }

    @Preview(
        name = "Light Mode",
        showBackground = true
    )
    @Preview(
        name = "Dark Mode",
        uiMode = Configuration.UI_MODE_NIGHT_YES,
        showBackground = true
    )
    @Composable
    fun DefaultPreview() {
        Column {
            MessageForEmptyPairedDevices()
            Spacer(modifier = Modifier.height(4.dp))
            PairedBtDevicesList(
                listOf(
                    BtDevice("Address", "Name", isActive = true),
                    BtDevice("Address 2", "Name 2", 10, isLongPressed = true)
                )
            )
        }
    }
}