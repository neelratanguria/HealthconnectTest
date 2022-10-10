package com.example.healthconnecttest

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.healthconnecttest.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import kotlin.math.log

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        // build a set of permissions for required data types
        val PERMISSIONS =
            setOf(
                HealthPermission.createReadPermission(HeartRateRecord::class),
                HealthPermission.createWritePermission(HeartRateRecord::class),
                HealthPermission.createReadPermission(StepsRecord::class),
                HealthPermission.createWritePermission(StepsRecord::class)
            )


        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAnchorView(R.id.fab)
                .setAction("Action", null).show()
        }

        if (HealthConnectClient.isAvailable(applicationContext)) {
            // Health Connect is available and installed.
            val healthConnectClient = HealthConnectClient.getOrCreate(applicationContext)
            Log.d("Neel", "onCreate: Health initialised")

            // Create the permissions launcher.
            val requestPermissionActivityContract = healthConnectClient.permissionController.createRequestPermissionActivityContract()

            val requestPermissions =
                registerForActivityResult(requestPermissionActivityContract) { granted ->
                    if (granted.containsAll(PERMISSIONS)) {
                        // Permissions successfully granted
                        Log.d("Neel", "onCreate: All permissions granted")
                    } else {
                        // Lack of required permissions
                        Log.d("Neel", "onCreate: Lacking permission")
                    }
                }

            fun checkPermissionsAndRun(client: HealthConnectClient) {
                lifecycleScope.launch {
                    val granted = client.permissionController.getGrantedPermissions(PERMISSIONS)
                    if (granted.containsAll(PERMISSIONS)) {
                        // Permissions already granted
                        var now: LocalDateTime = LocalDateTime.now()
                        now = now.plusDays(2)
                        val yesterday: LocalDateTime = now.minusDays(2)
                        val response =
                            healthConnectClient.readRecords(
                                ReadRecordsRequest(
                                    HeartRateRecord::class,
                                    timeRangeFilter = TimeRangeFilter.between(yesterday, now)
                                )
                            )
                        // FIND results here
                        Log.d("Neel", "Response: "+response.pageToken)
                        Log.d("Neel", "Yesterday: $yesterday")
                        Log.d("Neel", "Now: $now")
                    } else {
                        requestPermissions.launch(PERMISSIONS)
                    }
                }
            }
            checkPermissionsAndRun(healthConnectClient)
        } else {
            Log.d("Neel", "onCreate: Health not initialised")
        }
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