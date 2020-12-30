package com.dfrobot.angelo.blunobasicdemo

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.view.menu.MenuView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.slider.Slider
import com.google.android.material.snackbar.Snackbar
import kotlin.math.roundToInt

class MainActivity : BlunoLibrary() {
    //private var buttonScan: Button? = null
    private var buttonSerialSend: Button? = null
    private var serialSendText: EditText? = null
    private var serialReceivedText: TextView? = null
    //private var debugSwitch: SwitchCompat? = null
    //private var buttonSync: Button? = null
    private val fingerText = arrayOfNulls<TextView>(4)
    private val fingerSlider = arrayOfNulls<Slider>(4)
    private var safeZoneSlider: Slider? = null
    private var safeZoneText: TextView? = null
    private val gripButton = arrayOfNulls<Button>(6)
    private var gripSet = arrayOfNulls<String>(6)
    private var isSyncing = false
    private var syncString = ""
    private var topAppBar: MaterialToolbar? = null
    private var bottomAppBar: BottomNavigationView? = null
    private var topAppBarTitle : TextView? = null
    private var fabBluetoothButton : FloatingActionButton? = null
    private var debugStatus: Boolean = false

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //setSupportActionBar(findViewById(R.id.my_toolbar))
        onCreateProcess() //onCreate Process by BlunoLibrary
        topAppBar = findViewById<View>(R.id.topAppBar) as MaterialToolbar
        bottomAppBar = findViewById<View>(R.id.bottomNavigation)as BottomNavigationView
        topAppBarTitle = findViewById<View>(R.id.toolbar_title) as TextView
        fabBluetoothButton = findViewById<View>(R.id.fabBluetooth) as FloatingActionButton
        requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), 1)
        serialBegin(115200) //set the Uart Baudrate on BLE chip to 115200

        serialReceivedText = findViewById<View>(R.id.serialReveicedText) as TextView //initial the EditText of the received data
        serialSendText = findViewById<View>(R.id.serialSendText) as EditText //initial the EditText of the sending data
        buttonSerialSend = findViewById<View>(R.id.buttonSerialSend) as Button //initial the button for sending the data
        buttonSerialSend!!.setOnClickListener {
            serialSend(serialSendText!!.text.toString() + "\n") //send the data to the BLUNO
        }
//        buttonScan = findViewById<View>(R.id.buttonScan) as Button //initial the button for scanning the BLE device
//        buttonScan!!.setOnClickListener {
//            val permissionCheck = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION)
//            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
//                val requestCheck = ActivityCompat.shouldShowRequestPermissionRationale(this@MainActivity, Manifest.permission.ACCESS_FINE_LOCATION)
//                if (requestCheck) {
//                    requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), 1)
//                } else {
//                    AlertDialog.Builder(this@MainActivity)
//                            .setTitle("Permission Required")
//                            .setMessage("Please enable location permission to use this application.")
//                            .setNeutralButton("I Understand", null)
//                            .show()
//                }
//            } else {
//                buttonScanOnClickProcess() //Alert Dialog for selecting the BLE device
//            }
//        }

//        buttonSync = findViewById<View>(R.id.syncButton) as Button
//        buttonSync!!.setOnClickListener {
//            serialSend("Z\n")
//            isSyncing = true
//        }



        //textView fingers
        fingerText[0] = findViewById<View>(R.id.finger0text) as TextView
        fingerText[1] = findViewById<View>(R.id.finger1text) as TextView
        fingerText[2] = findViewById<View>(R.id.finger2text) as TextView
        fingerText[3] = findViewById<View>(R.id.finger3text) as TextView
        //Seekbar declarations
        fingerSlider[0] = findViewById<View>(R.id.finger0Bar) as Slider
        fingerSlider[1] = findViewById<View>(R.id.finger1Bar) as Slider
        fingerSlider[2] = findViewById<View>(R.id.finger2Bar) as Slider
        fingerSlider[3] = findViewById<View>(R.id.finger3Bar) as Slider
        for (a in 0..3) {
            fingerSlider[a]!!.addOnChangeListener { slider, i, fromUser ->
                if(!isSyncing) {
                    val toSend = "F$a P${i.roundToInt()}"
                    serialSendText!!.setText(toSend)
                    serialSend(toSend + "\n")
                }
                fingerText[a]!!.text = "Finger $a: ${i.roundToInt()}"
            }
        }

        safeZoneText = findViewById<View>(R.id.safeZoneText) as TextView
        safeZoneSlider = findViewById<View>(R.id.safeZoneBar) as Slider
        safeZoneSlider!!.addOnChangeListener { slider, value, fromUser ->
            if (!isSyncing) {
                val toSend = "B${value.roundToInt()}"
                safeZoneText!!.text = "Safe Zone: ${value.roundToInt()}"
                serialSendText!!.setText(toSend)
                serialSend(toSend + "\n")
            }
            safeZoneText!!.text = "Safe Zone: ${value.roundToInt()}"
        }
        gripButton[0] = findViewById<View>(R.id.fistBtn) as Button
        gripButton[1] = findViewById<View>(R.id.palmBtn) as Button
        gripButton[2] = findViewById<View>(R.id.tripodBtn) as Button
        gripButton[3] = findViewById<View>(R.id.pinchBtn) as Button
        gripButton[4] = findViewById<View>(R.id.pointBtn) as Button
        gripButton[5] = findViewById<View>(R.id.customBtn) as Button
        for (i in 0..5) {
            gripButton[i]!!.setOnClickListener {
                val progress = gripSet[i]!!.split(",").toTypedArray()
                for (j in 0..3) {
                    fingerSlider[j]!!.value = progress[j].toFloat()
                }
                //Toast.makeText(MainActivity.this, gripSet[finalI], Toast.LENGTH_SHORT).show();
            }
            gripButton[i]!!.setOnLongClickListener {
                var getFingerPos = ""
                for (j in 0..3) {
                    getFingerPos += fingerSlider[j]!!.value.roundToInt().toString()
                    if (j < 3) getFingerPos += ","
                }
                gripSet[i] = getFingerPos
                Toast.makeText(this@MainActivity, "Fingers saved to: " + gripButton[i]!!.text.toString(), Toast.LENGTH_SHORT).show()
                saveData() //saves the current set of positions
                val v = getSystemService(VIBRATOR_SERVICE) as Vibrator
                // Vibrate for 100 milliseconds
                v.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                false
            }
        }
        //val receivedEditText = findViewById<View>(R.id.editText2) as TextView
//        debugSwitch = findViewById(R.id.debuggingSwitch)
//        debugSwitch!!.setOnClickListener {
//            if (debugSwitch!!.isChecked) setDebug(View.VISIBLE)
//            else setDebug(View.GONE)
//        }
        fabBluetoothButton!!.setOnClickListener {
            val permissionCheck = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION)
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                val requestCheck = ActivityCompat.shouldShowRequestPermissionRationale(this@MainActivity, Manifest.permission.ACCESS_FINE_LOCATION)
                if (requestCheck) {
                    requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), 1)
                } else {
                    AlertDialog.Builder(this@MainActivity)
                            .setTitle("Permission Required")
                            .setMessage("Please enable location permission to use this application.")
                            .setNeutralButton("I Understand", null)
                            .show()
                }
            } else {
                buttonScanOnClickProcess() //Alert Dialog for selecting the BLE device
            }
        }
        bottomAppBar!!.setOnNavigationItemSelectedListener { menuItem ->
            when(menuItem.itemId){
                R.id.syncButtonToolbar->{
                    serialSend("Z\n")
                    isSyncing = true
                    true
                }
//                R.id.connect->{
//                    //Snackbar.make(bottomAppBar!!, "connecting", Snackbar.LENGTH_SHORT).show()
//                    val permissionCheck = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION)
//                    if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
//                        val requestCheck = ActivityCompat.shouldShowRequestPermissionRationale(this@MainActivity, Manifest.permission.ACCESS_FINE_LOCATION)
//                        if (requestCheck) {
//                            requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), 1)
//                        } else {
//                            AlertDialog.Builder(this@MainActivity)
//                                    .setTitle("Permission Required")
//                                    .setMessage("Please enable location permission to use this application.")
//                                    .setNeutralButton("I Understand", null)
//                                    .show()
//                        }
//                    } else {
//                        buttonScanOnClickProcess() //Alert Dialog for selecting the BLE device
//                    }
//                    true
//                }
                R.id.toolbarDebug->{
                    menuItem.isChecked = !menuItem.isChecked
                    debugStatus = !debugStatus
                    Snackbar.make(bottomAppBar!!, debugStatus.toString(), Snackbar.LENGTH_SHORT).setAnchorView(R.id.fabBluetooth).show()
                    if (debugStatus) setDebug(View.VISIBLE)
                    else setDebug(View.GONE)
                    true
                }
                else->false
            }
        }
        loadData()
    }
    private fun setDebug(v: Int){
        if (v == View.VISIBLE){

            Snackbar.make(bottomAppBar!!, debugStatus.toString(), Snackbar.LENGTH_SHORT).setAnchorView(R.id.fabBluetooth).show()
            bottomAppBar!!.setItemIconTintList(null);
            bottomAppBar!!.menu.findItem(R.id.toolbarDebug)
        }
        else bottomAppBar!!.menu.findItem(R.id.toolbarDebug).icon.setTint(ContextCompat.getColor(this,R.color.gray))
        buttonSerialSend!!.visibility = v
        serialSendText!!.visibility = v
        findViewById<View>(R.id.editText2)!!.visibility = v
        findViewById<View>(R.id.scrollView)!!.visibility = v
        safeZoneSlider!!.visibility = v
        safeZoneText!!.visibility = v
    }
    private fun saveData() {
        var gripSet_All: String? = ""
        for (i in gripSet.indices) {
            gripSet_All += gripSet[i]
            if (gripSet.size - 1 > i) gripSet_All += ";"
        }
        val sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(GRIP_SAVE, gripSet_All)
        editor.apply()
    }
    private fun saveExit(){
        val sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean(DEBUG_SAVE, debugStatus)
        editor.apply()
    }
    private fun loadData() {
        val sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
        val defaultSet = "ERROR"
        val forSplitting = sharedPreferences.getString(GRIP_SAVE, defaultSet)
        if (forSplitting !== "ERROR") {
            try {
                gripSet = forSplitting!!.split(";").toTypedArray()
            } catch (e: Exception) {
                Toast.makeText(this, "loadData() Error on split", Toast.LENGTH_SHORT).show()
            }
        } else { //set Default grip sets
            gripSet = arrayOf(
                    "100,100,100,100",   //fist
                    "100,0,100,100",     //palm
                    "50,100,100,100",    //tripod
                    "50,100,50,100",     //pinch
                    "100,100,100,0",     //Point
                    "0,0,0,0")           //open

        }
        val isChecked: Boolean = sharedPreferences.getBoolean(DEBUG_SAVE, true)
        debugStatus = isChecked
        if (isChecked) setDebug(View.VISIBLE)
        else setDebug(View.GONE)
    }

    override fun onResume() {
        super.onResume()
        println("BlUNOActivity onResume")
        onResumeProcess() //onResume Process by BlunoLibrary
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        onActivityResultProcess(requestCode, resultCode) //onActivityResult Process by BlunoLibrary
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onPause() {
        super.onPause()
        onPauseProcess() //onPause Process by BlunoLibrary
    }

    override fun onStop() {
        super.onStop()
        onStopProcess() //onStop Process by BlunoLibrary
        saveExit()
    }

    override fun onDestroy() {
        super.onDestroy()
        onDestroyProcess() //onDestroy Process by BlunoLibrary
    }

    @SuppressLint("SetTextI18n")
    override fun onConectionStateChange(theConnectionState: connectionStateEnum) { //Once connection state changes, this function will be called
        when (theConnectionState) {
            connectionStateEnum.isConnected -> {
                //buttonScan!!.text = "Connected"
                //bottomAppBar!!.menu.findItem(R.id.connect).icon = ContextCompat.getDrawable(this, R.drawable.ic_baseline_bluetooth_connected_24)
                fabBluetoothButton!!.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_baseline_bluetooth_connected_24))
                bottomAppBar!!.menu.findItem(R.id.syncButtonToolbar).isEnabled = true
                topAppBarTitle!!.text = "Connected"
            }
            connectionStateEnum.isConnecting -> {
                //buttonScan!!.text = "Connecting"
                //bottomAppBar!!.menu.findItem(R.id.connect).icon = ContextCompat.getDrawable(this, R.drawable.ic_baseline_more_horiz_24)
                fabBluetoothButton!!.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_baseline_more_horiz_24))
                topAppBarTitle!!.text = "Connecting"
            }
            connectionStateEnum.isToScan -> {
                //buttonScan!!.text = "Scan"
                //bottomAppBar!!.menu.findItem(R.id.connect).icon = ContextCompat.getDrawable(this, R.drawable.ic_baseline_bluetooth_disabled_24)
                fabBluetoothButton!!.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_baseline_bluetooth_disabled_24))
                bottomAppBar!!.menu.findItem(R.id.syncButtonToolbar).isEnabled = false
                topAppBarTitle!!.text = "Disconnected"
            }
            connectionStateEnum.isScanning -> {
                //buttonScan!!.text = "Scanning"
                //bottomAppBar!!.menu.findItem(R.id.connect).icon = ContextCompat.getDrawable(this, R.drawable.ic_baseline_bluetooth_searching_24)
                fabBluetoothButton!!.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_baseline_bluetooth_searching_24))
                topAppBarTitle!!.text = "Scanning"
            }
            connectionStateEnum.isDisconnecting -> {
                //buttonScan!!.text = "isDisconnecting"
                //bottomAppBar!!.menu.findItem(R.id.connect).icon = ContextCompat.getDrawable(this, R.drawable.ic_baseline_more_horiz_24)
                fabBluetoothButton!!.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_baseline_more_horiz_24))
                topAppBarTitle!!.text = "Disconnecting"
            }
            else -> {
            }
        }
    }

    override fun onSerialReceived(theString: String) {                            //Once connection data received, this function will be called
        if (isSyncing) {
            try {
                syncString = theString.takeLastWhile { it < 'L' }.trim()
                val inputArraySync = syncString.split(",").toTypedArray()
                for (j in 0..3) {
                    var minMaxVal :Float = inputArraySync[j].toFloat()
                    if (minMaxVal > 100) minMaxVal = 100F
                    else if (minMaxVal<0) minMaxVal = 0F
                    fingerSlider[j]!!.value = minMaxVal
                }
                //Toast.makeText(this, inputArraySync[4], Toast.LENGTH_SHORT).show();
                safeZoneSlider!!.value = inputArraySync[4].toFloat() // Get safezone
            }
            catch (e: Exception){
                Toast.makeText(this, "ERROR not synced, try again", Toast.LENGTH_SHORT).show()
            }
            isSyncing = false
        }

        serialReceivedText!!.append(theString) //append the text into the EditText
        //The Serial data from the BLUNO may be sub-packaged, so using a buffer to hold the String is a good choice.
        (serialReceivedText!!.parent as ScrollView).fullScroll(View.FOCUS_DOWN);
    }

    companion object {
        const val SHARED_PREFS = "sharedPrefs"
        const val GRIP_SAVE = "gripSave"
        const val DEBUG_SAVE = "debugSave"
    }
}