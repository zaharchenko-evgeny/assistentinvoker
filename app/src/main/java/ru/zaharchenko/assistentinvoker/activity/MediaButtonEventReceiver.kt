package ru.zaharchenko.assistentinvoker.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import android.view.KeyEvent

class MediaButtonEventReceiver() : BroadcastReceiver() {

    var keyPressed: ((event: KeyEvent) -> Unit)?=null

    override fun onReceive(context: Context?, intent: Intent) {
        val intentAction = intent.action
        if (Intent.ACTION_MEDIA_BUTTON != intentAction) {
            return
        }
        val event: KeyEvent =
            intent.getParcelableExtra<Parcelable>(Intent.EXTRA_KEY_EVENT) as KeyEvent?
                ?: return
        keyPressed?.let { it(event) }
        abortBroadcast()
    }
}