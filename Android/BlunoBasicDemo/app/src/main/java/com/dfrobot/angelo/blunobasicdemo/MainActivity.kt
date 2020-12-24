package com.dfrobot.angelo.blunobasicdemo

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.view.View
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener

class MainActivity : BlunoLibrary() {
    private var buttonScan: Button? = null
    private var buttonSerialSend: Button? = null
    private var serialSendText: EditText? = null
    private var serialReceivedText: TextView? = null
    private var debugSwitch: Switch? = null
    private val fingerText = arrayOfNulls<TextView>(4)
    private val fingerSeek = arrayOfNulls<SeekBar>(4)
    private val gripButton = arrayOfNulls<Button>(6)
    private var gripSet = arrayOfNulls<String>(6)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        onCreateProcess() //onCreate Process by BlunoLibrary

        requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), 1)
        serialBegin(115200) //set the Uart Baudrate on BLE chip to 115200



        serialReceivedText = findViewById<View>(R.id.serialReviecedText) as TextView //initial the EditText of the received data
        serialSendText = findViewById<View>(R.id.serialSendText) as EditText //initial the EditText of the sending data
        buttonSerialSend = findViewById<View>(R.id.buttonSerialSend) as Button //initial the button for sending the data
        buttonSerialSend!!.setOnClickListener {
            serialSend(serialSendText!!.text.toString()+"\n") //send the data to the BLUNO
        }
        buttonScan = findViewById<View>(R.id.buttonScan) as Button //initial the button for scanning the BLE device
        buttonScan!!.setOnClickListener {
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

        //textView fingers
        fingerText[0] = findViewById<View>(R.id.finger0text) as TextView
        fingerText[1] = findViewById<View>(R.id.finger1text) as TextView
        fingerText[2] = findViewById<View>(R.id.finger2text) as TextView
        fingerText[3] = findViewById<View>(R.id.finger3text) as TextView
        //Seekbar declarations
        fingerSeek[0] = findViewById<View>(R.id.finger0Bar) as SeekBar
        fingerSeek[1] = findViewById<View>(R.id.finger1Bar) as SeekBar
        fingerSeek[2] = findViewById<View>(R.id.finger2Bar) as SeekBar
        fingerSeek[3] = findViewById<View>(R.id.finger3Bar) as SeekBar
        for (a in 0..3) {
            fingerSeek[a]!!.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                    val toSend = "F$a P$i"
                    fingerText[a]!!.text = "Finger $a: $i"
                    serialSendText!!.setText(toSend)
                    serialSend(toSend + "\n")
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}
                override fun onStopTrackingTouch(seekBar: SeekBar) {}
            })
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
                    fingerSeek[j]!!.progress = progress[j].toInt()
                }
                //Toast.makeText(MainActivity.this, gripSet[finalI], Toast.LENGTH_SHORT).show();
            }
            gripButton[i]!!.setOnLongClickListener {
                var getFingerPos = ""
                for (j in 0..3) {
                    getFingerPos += fingerSeek[j]!!.progress.toString()
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
        debugSwitch = findViewById(R.id.debuggingSwitch)
        debugSwitch!!.setOnClickListener {
            if (debugSwitch!!.isChecked) setDebug(View.VISIBLE)
            else setDebug(View.GONE)
        }

        loadData()
    }
    private fun setDebug(v: Int){
        buttonSerialSend!!.visibility = v
        serialSendText!!.visibility = v
        findViewById<View>(R.id.editText2)!!.visibility = v
        findViewById<View>(R.id.scrollView)!!.visibility = v
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
        editor.putBoolean(DEBUG_SAVE, debugSwitch!!.isChecked)
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
        var isChecked: Boolean = sharedPreferences.getBoolean(DEBUG_SAVE, true)
        debugSwitch?.isChecked = isChecked //set checked value
        if (isChecked) setDebug(View.VISIBLE)
        else setDebug(View.GONE)
    }

    override fun onResume() {
        super.onResume()
        println("BlUNOActivity onResume")
        onResumeProcess() //onResume Process by BlunoLibrary
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        onActivityResultProcess(requestCode, resultCode, data) //onActivityResult Process by BlunoLibrary
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

    override fun onConectionStateChange(theConnectionState: connectionStateEnum) { //Once connection state changes, this function will be called
        when (theConnectionState) {
            connectionStateEnum.isConnected -> buttonScan!!.text = "Connected"
            connectionStateEnum.isConnecting -> buttonScan!!.text = "Connecting"
            connectionStateEnum.isToScan -> buttonScan!!.text = "Scan"
            connectionStateEnum.isScanning -> buttonScan!!.text = "Scanning"
            connectionStateEnum.isDisconnecting -> buttonScan!!.text = "isDisconnecting"
            else -> {
            }
        }
    }

    override fun onSerialReceived(theString: String) {                            //Once connection data received, this function will be called
        // TODO Auto-generated method stub
        serialReceivedText!!.append(theString) //append the text into the EditText
        //The Serial data from the BLUNO may be sub-packaged, so using a buffer to hold the String is a good choice.
        (serialReceivedText!!.parent as ScrollView).fullScroll(View.FOCUS_DOWN)
    }

    companion object {
        const val SHARED_PREFS = "sharedPrefs"
        const val GRIP_SAVE = "gripSave"
        const val DEBUG_SAVE = "debugSave"
    }
}