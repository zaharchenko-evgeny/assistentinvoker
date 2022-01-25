package ru.zaharchenko.assistentinvoker.service

class BluetoothUtils {
    companion object {
        val ACTION_DISCOVERY_STARTED = "ACTION_DISCOVERY_STARTED"
        val ACTION_DISCOVERY_STOPPED = "ACTION_DISCOVERY_STOPPED"
        val ACTION_DEVICE_FOUND = "ACTION_DEVICE_FOUND"
        val ACTION_DEVICE_CONNECTED = "ACTION_DEVICE_CONNECTED"
        val ACTION_DEVICE_DISCONNECTED = "ACTION_DEVICE_DISCONNECTED"
        val ACTION_MESSAGE_RECEIVED = "ACTION_MESSAGE_RECEIVED"
        val ACTION_MESSAGE_SENT = "ACTION_MESSAGE_SENT"
        val ACTION_CONNECTION_ERROR = "ACTION_CONNECTION_ERROR"
        val EXTRA_DEVICE = "EXTRA_DEVICE"
        val EXTRA_MESSAGE = "EXTRA_MESSAGE"
    }
}