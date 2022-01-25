package ru.zaharchenko.assistentinvoker.activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.zaharchenko.assistentinvoker.R
import ru.zaharchenko.assistentinvoker.databinding.ActivityMainBinding
import ru.zaharchenko.assistentinvoker.model.BtDevicesViewModel
import ru.zaharchenko.assistentinvoker.service.BtService
import android.content.IntentFilter




@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private val viewModel by viewModels<BtDevicesViewModel>()

    var btService: BtService? = null
    var isBound = false

    private val mConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            btService = (service as BtService.LocalBinder).getService()
            viewModel.loadData(btService!!)
        }

        override fun onServiceDisconnected(className: ComponentName) {
            btService = null
            isBound = false
        }
    }

    fun doBindService() {
        bindService(
            Intent(
                this,
                BtService::class.java
            ), mConnection, Context.BIND_AUTO_CREATE
        )
        isBound = true
    }

    fun doUnbindService() {
        if (isBound) {
            // Detach our existing connection.
            unbindService(mConnection)
            isBound = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        doUnbindService()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startService(Intent(this, BtService::class.java))
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        doBindService()
        doBindMediaButtonReceiver()
        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
    }

    private fun doBindMediaButtonReceiver() {
        val filter = IntentFilter(Intent.ACTION_MEDIA_BUTTON)
        val r = MediaButtonEventReceiver();
        r.keyPressed = {
            if (viewModel.isWaitingForBtKeyPressed()){
                viewModel.btKeyPressed.value = Pair(it.keyCode, it.isLongPress)
            }
        }
        filter.priority = 1000 //this line sets receiver priority

        registerReceiver(r, filter)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

}