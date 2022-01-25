package ru.zaharchenko.assistentinvoker.service

import android.app.Service
import android.bluetooth.*
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import ru.zaharchenko.assistentinvoker.data.BtDeviceRoomDatabase
import ru.zaharchenko.assistentinvoker.model.BtDevice
import java.io.IOException
import java.util.*
import android.system.Os.socket
import java.io.InputStream


class BtService : Service(), BtServiceProvider {

    protected val btDevices = MutableLiveData<List<BtDevice>>()

    protected val bondedDevices = mutableListOf<BtDevice>()

    private lateinit var btDeviceRoomDatabase: BtDeviceRoomDatabase

    private lateinit var bluetoothManager: BluetoothManager

    private val localBinder = LocalBinder();

    inner class LocalBinder : Binder() {
        fun getService(): BtService {
            return this@BtService
        }
    }


    override fun onCreate() {
        btDeviceRoomDatabase = BtDeviceRoomDatabase.getDatabase(this)
        subscribeOnBtDevicesDao()
        bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter.getProfileProxy(
            this,
            HeadSetsBluetoothListener(this),
            BluetoothProfile.HEADSET
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    protected fun fillBtDevicesList() {
        val persistedBtDevices = btDeviceRoomDatabase.btDeviceDao()
            .getBtDevices().asLiveData()
        btDevices.value = collectBtDevices(persistedBtDevices.value)
    }

    private fun subscribeOnBtDevicesDao() {
        val btDeviceDao = btDeviceRoomDatabase.btDeviceDao()
        val persistedBtDevices = btDeviceDao.getBtDevices().asLiveData()
        persistedBtDevices.observeForever {
            btDevices.value = collectBtDevices(it)
        }
    }

    private fun collectBtDevices(persistedDevices: List<BtDevice>?): List<BtDevice> {
        val bondedDevices = bondedDevices()
        if (persistedDevices == null) {
            return bondedDevices
        }
        persistedDevices.forEach { btDevice ->
            bondedDevices.find { it.address == btDevice.address }
                ?.apply {
                    isActive = true
                }
        }

        val result = persistedDevices.plus(
            bondedDevices.filter { bonded ->
                persistedDevices.none { it.address == bonded.address }
            }
        )

        return result
    }

    private fun bondedDevices(): List<BtDevice> {
        return bondedDevices
    }

    override fun onBind(intent: Intent): IBinder {
        return localBinder;
    }

    override fun bondedBtDevices(): LiveData<List<BtDevice>> {
        return btDevices
    }

    class HeadSetsBluetoothListener(private val btService: BtService) :
        BluetoothProfile.ServiceListener {

        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
            btService.bondedDevices.clear()
//            val bluetoothA2dp = proxy as BluetoothHeadset
//            bluetoothA2dp.startVoiceRecognition()
            proxy!!.connectedDevices
                .map {
                    btService.ConnectThread(it).start()
                    BtDevice(it.address, it.name, isActive = true)
                }.forEach {
                    btService.bondedDevices.add(it)
                }

            btService.fillBtDevicesList();
        }

        override fun onServiceDisconnected(profile: Int) {
            btService.bondedDevices.clear()
            btService.fillBtDevicesList();
        }

    }

    private inner class ConnectThread(device: BluetoothDevice) : Thread() {

        private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
            device.createRfcommSocketToServiceRecord(UUID.randomUUID())
        }

        override fun run() {
            // Cancel discovery because it otherwise slows down the connection.
            bluetoothManager.adapter?.cancelDiscovery()

            mmSocket?.let { socket ->
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                socket.connect()

                // The connection attempt succeeded. Perform work associated with
                // the connection in a separate thread.
                manageMyConnectedSocket(socket)
            }
        }

        private fun manageMyConnectedSocket(socket: BluetoothSocket) {
            val socketInputStream: InputStream = socket.inputStream
            val buffer = ByteArray(256)
            var bytes: Int

            // Keep looping to listen for received messages

            // Keep looping to listen for received messages
            while (true) {
                try {
                    bytes = socketInputStream.read(buffer) //read bytes from input buffer
                    val readMessage = String(buffer, 0, bytes)
                    // Send the obtained bytes to the UI Activity via handler
                    Log.i("logging", readMessage + "")
                } catch (e: IOException) {
                    break
                }
            }
        }

        // Closes the client socket and causes the thread to finish.
        fun cancel() {
            try {
                mmSocket?.close()
            } catch (e: IOException) {
//                Log.e(TAG, "Could not close the client socket", e)
            }
        }
    }
}