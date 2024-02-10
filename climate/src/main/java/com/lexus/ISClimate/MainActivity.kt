package com.lexus.Climate

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.aoe.fytcanbusmonitor.ModuleCodes.MODULE_CODE_CANBUS
import com.aoe.fytcanbusmonitor.ModuleCodes.MODULE_CODE_CAN_UP
import com.aoe.fytcanbusmonitor.ModuleCodes.MODULE_CODE_MAIN
import com.aoe.fytcanbusmonitor.ModuleCodes.MODULE_CODE_SOUND
import com.aoe.fytcanbusmonitor.MsToolkitConnection
import com.aoe.fytcanbusmonitor.RemoteModuleProxy
import com.lexus.ISClimate.SettingDialogFragment
import com.lexus.ISClimate.service.MyFloatingService
import com.lexus.ISClimate.trial.TrailFragment
import com.lexus.ISClimate.viewmodel.MyViewModel
import io.paperdb.Paper
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), View.OnTouchListener {
    private lateinit var viewModel: MyViewModel
    private val remoteProxy = RemoteModuleProxy()
    private val SYSTEM_ALERT_WINDOW_PERMISSION_CODE = 101

        private var mediaPlayer: MediaPlayer? = null
    private var isSound: Boolean? = false
    private var isCelsius: Boolean? = true
    private lateinit var saveDate: String
    private lateinit var currentDate: String
    private lateinit var parentLayout: LinearLayout
    private var valueOfDegree: Int? = 1
    private var valueOfRightDegree: Int? = 1
    private var windowOn: Boolean = false
    private var faceOn: Boolean = false
    private var feetOn: Boolean = false
    private var currentLoaderIndex: Int = 0
    private val loaderDrawables = intArrayOf(
        R.drawable.loader,
        R.drawable.loader1,
        R.drawable.loader2,
        R.drawable.loader3, // Add more drawable resources here
        R.drawable.loader4, // Add more drawable resources here
        R.drawable.loader5, // Add more drawable resources here
        R.drawable.loader6, // Add more drawable resources here
        R.drawable.loader7, // Add more drawable resources here
        // Add more drawable resources here
    )

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
         setOnToushClickListner()
//        setOnClick()
        parentLayout = findViewById<TableRow>(R.id.parentLayout)
        viewModel = ViewModelProvider(this)[MyViewModel::class.java]
        Paper.init(this)
        viewModel.getSoundBoolean()
        viewModel.getBoolean()
        viewModel.getDegreeBoolean()
        viewModel.getAccessValue()
        observeValue()
        setDriverMode()

        viewModel.getStartAndEndValuesLiveData().observe(this) { pair ->
            val (start, end) = pair ?: Pair(null, null)
            switchLayout(parentLayout, end!!, start!!)
        }
        mediaPlayer = MediaPlayer.create(this, R.raw.beep)
        findViewById<ImageButton>(R.id.btnSetting).setOnClickListener {
            val dialogFragment = SettingDialogFragment()
            dialogFragment.show(supportFragmentManager, "MyDialogFragment")
        }
    }

    private fun setOnToushClickListner() {
        findViewById<ImageButton>(R.id.btnLeftTempPlus).setOnTouchListener(this)
        findViewById<ImageButton>(R.id.btnRightTempPlus).setOnTouchListener(this)
        findViewById<ImageButton>(R.id.btnLeftTempMinus).setOnTouchListener(this)
        findViewById<ImageButton>(R.id.btnRightTempMinus).setOnTouchListener(this)
        findViewById<ImageButton>(R.id.btnAuto).setOnTouchListener(this)
        findViewById<ImageButton>(R.id.btnVent).setOnTouchListener(this)
        findViewById<ImageButton>(R.id.btnVentTop).setOnTouchListener(this)
        findViewById<ImageButton>(R.id.btnVentEnd).setOnTouchListener(this)
        findViewById<ImageButton>(R.id.btnRecirc).setOnTouchListener(this)
        findViewById<ImageButton>(R.id.btnAC).setOnTouchListener(this)
        findViewById<ImageButton>(R.id.btnRearDefrost).setOnTouchListener(this)
        findViewById<ImageButton>(R.id.btnDefrost).setOnTouchListener(this)
        findViewById<ImageButton>(R.id.btnDualClimate).setOnTouchListener(this)
        findViewById<ImageButton>(R.id.btnFanPlus).setOnTouchListener(this)
        findViewById<ImageButton>(R.id.btnFanOff).setOnTouchListener(this)
        findViewById<ImageButton>(R.id.btnFanMinus).setOnTouchListener(this)

    }


    private fun observeValue() {
        viewModel.isSound.observe(this, Observer {
            if (it != null && it == true) {
                isSound = it
            } else {
                isSound = false
            }
        })
        viewModel.degreeValue.observe(this, Observer {
            if (it == null) {
                viewModel.degreeValue.value = true
                setInitialTemperature()

            } else {
                 isCelsius = it
                setInitialTemperature()
            }
        })
        viewModel.accessGranted.observe(this, Observer {
            if (it == true && it != null) {
            } else {
                saveDate = viewModel.getCurrentDateTime()
                if (saveDate != "") {
                    currentDate = viewModel.currentDateTime()
                    val daysDifference = calculateDateDifference(saveDate, currentDate)
                    if (daysDifference >= 8) {
                        val intent = Intent(this, TrailFragment::class.java)
                        startActivity(intent)

                    } else {
                    }

                } else {
                    viewModel.saveCurrentDateTime()
                }
            }
        })
    }

    private fun setInitialTemperature() {
        val txtTemperature = findViewById<TextView>(R.id.txtLeftTemperature)
        val txtRightTemperature = findViewById<TextView>(R.id.txtRightTemperature)
        updateTemperatureText(valueOfDegree!!, txtTemperature)
        updateTemperatureText(valueOfRightDegree!!, txtRightTemperature)
    }

    private fun calculateDateDifference(startDateStr: String, endDateStr: String): Long {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        try {
            val startDate = dateFormat.parse(startDateStr)
            val endDate = dateFormat.parse(endDateStr)

            if (startDate != null && endDate != null) {
                // Calculate the difference in milliseconds
                val differenceMillis = endDate.time - startDate.time
                // Convert milliseconds to days
                return TimeUnit.MILLISECONDS.toDays(differenceMillis)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return 0
    }

    private fun setDriverMode() {
        if (viewModel.booleanValue.value == null) {

        } else {
            if (viewModel.getBoolean()!!) {
                switchLayout(parentLayout, 4, 1)
            } else {
                switchLayout(parentLayout, 1, 5)
            }
        }
    }
    private fun switchLayout(
        parentLayout: LinearLayout,
        leftIndex: Int,
        rightIndex: Int
    ) {
        val layoutLeft = findViewById<LinearLayout>(R.id.layoutLeft)
        val layoutRight = findViewById<LinearLayout>(R.id.layoutRight)
        parentLayout.removeView(layoutLeft)
        parentLayout.removeView(layoutRight)

        // Adding the layouts in the reversed order
        parentLayout.addView(layoutRight, leftIndex)
        parentLayout.addView(layoutLeft, rightIndex)
    }

    override fun onStart() {
        super.onStart()
        getPermisson()


        ModuleCallback.init(this)
        connectMain()
        connectCanbus()
        connectSound()
        connectCanUp()
        MsToolkitConnection.instance.connect(this)


    }

    private fun getPermisson() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            // If not, request the permission
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, SYSTEM_ALERT_WINDOW_PERMISSION_CODE)
        } else {
            // Permission has been granted, start the service
            startService(Intent(this,MyFloatingService::class.java))
        }
    }

    override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean {
        var canBusCommand: Int = -1
        when (view?.id) {
            R.id.btnLeftTempPlus -> {

                canBusCommand = 3
//                playAudio(isSound!!)
            }

            R.id.btnLeftTempMinus -> {
                canBusCommand = 2
//                playAudio(isSound!!)
            }

            R.id.btnRightTempPlus -> {
                canBusCommand = 5
//                playAudio(isSound!!)
            }

            R.id.btnRightTempMinus -> {
                canBusCommand = 4
//                playAudio(isSound!!)
            }

            R.id.btnAuto -> {
                canBusCommand = 21
            }

            R.id.btnVent -> {
                canBusCommand = 36
            }

            R.id.btnRecirc -> {
                canBusCommand = 25
            }

            R.id.btnAC -> {
                canBusCommand = 23
            }

            R.id.btnDualClimate -> {
                canBusCommand = 16
            }

            R.id.btnDefrost -> {
                canBusCommand = 18
            }

            R.id.btnRearDefrost -> {
                canBusCommand = 20
            }
            R.id.btnFanPlus -> {
                canBusCommand = 10
            }

            R.id.btnFanMinus -> {
                canBusCommand = 9
            }

            R.id.btnFanOff -> {
                canBusCommand = 1
            }
        }

        val image = view as ImageView
        when (motionEvent?.action) {
            MotionEvent.ACTION_DOWN -> {
                var highlight = Color.argb(50, 255, 255, 255)
                if (view.id == R.id.btnLeftTempMinus) {
                    highlight = Color.argb(50, 0, 0, 255)
                } else if (view.id == R.id.btnLeftTempMinus) {
                    highlight = Color.argb(50, 255, 0, 0)
                }

                image?.setColorFilter(highlight, PorterDuff.Mode.SRC_ATOP)
                image?.invalidate()
            }

            MotionEvent.ACTION_UP -> {
                image?.clearColorFilter()
                image?.invalidate()
            }
        }

        if (canBusCommand != -1) {
            val startEvent: Boolean = motionEvent?.action == MotionEvent.ACTION_DOWN
            var rm = MsToolkitConnection.instance.remoteToolkit?.getRemoteModule(MODULE_CODE_CANBUS)
            rm?.cmd(0, intArrayOf(canBusCommand, if (startEvent) 1 else 0), null, null)
            if (startEvent) {
                view?.performClick()
            }
        }
        return false
    }

    private fun connectMain() {
        val callback = ModuleCallback("Main", findViewById(R.id.text_view))
        val connection = IPCConnection(MODULE_CODE_MAIN)
        for (i in 0..119) {
            connection.addCallback(callback, i)
        }
        MsToolkitConnection.instance.addObserver(connection)
    }

    private fun connectCanbus() {
        val callback = ModuleCallback("Canbus", findViewById(R.id.text_view))
        val connection = IPCConnection(MODULE_CODE_CANBUS)
        for (i in 0..50) {
            connection.addCallback(callback, i)
        }
        for (i in 1000..1036) {
            connection.addCallback(callback, i)
        }
        MsToolkitConnection.instance.addObserver(connection)
    }

    private fun connectSound() {
        val callback = ModuleCallback("Sound", findViewById(R.id.text_view))
        val connection = IPCConnection(MODULE_CODE_SOUND)
        for (i in 0..49) {
            connection.addCallback(callback, i)
        }
        MsToolkitConnection.instance.addObserver(connection)
    }

    private fun connectCanUp() {
        val callback = ModuleCallback("CanUp", findViewById(R.id.text_view))
        val connection = IPCConnection(MODULE_CODE_CAN_UP)
        connection.addCallback(callback, 100)
        MsToolkitConnection.instance.addObserver(connection)
    }

    fun canBusNotify(
        systemName: String,
        updateCode: Int,
        intArray: IntArray?,
        floatArray: FloatArray?,
        strArray: Array<String?>?
    ) {
        if (systemName.lowercase().equals("canbus")) {
            if (updateCode in 1..16 || updateCode in 69..81 && updateCode != 77) {
                findViewById<TextView>(R.id.text_view).append(
                    "updateCode: " + updateCode + " value: " + intArray?.get(
                        0
                    ) + "\n"
                )
            }
            when (updateCode) {

                11 -> {
                    val newTemp = intArray?.get(0)
                    if (newTemp != null) {
                        valueOfDegree = newTemp
                        val txtTemperature = findViewById<TextView>(R.id.txtLeftTemperature)
                        updateTemperatureText(newTemp!!, txtTemperature)
                        playAudio(isSound!!)
                    }

//                    if (newTemp == -2) {
//                        txtTemperature.text = "LO"
//                    } else if (newTemp == -3) {
//                        txtTemperature.text = "HI"
//                    } else {
//                        val inF: Int? = newTemp?.plus(64)
//                        if (inF != null) {
////                            txtTemperature.text = "$inF°"
//                            if(isCelsius!!){
//                                val celsiusValue = fahrenheitToCelsius(inF)
//                                txtTemperature.text = "$celsiusValue°"
//                            }
//                            else{
//                                txtTemperature.text = "$inF°"
//                            }
//
//
//                        }
//                    }
                }

                12 -> {
                    val newTemp = intArray?.get(0)

                    if (newTemp != null) {
                        valueOfRightDegree = newTemp
                        val txtTemperature = findViewById<TextView>(R.id.txtRightTemperature)
                        updateTemperatureText(newTemp!!, txtTemperature)
                        playAudio(isSound!!)
                    }
//                    if (newTemp == -2) {
//                        txtTemperature.text = "LO"
//                    } else if (newTemp == -3) {
//                        txtTemperature.text = "HI"
//                    } else {
//                        val inF: Int? = newTemp?.plus(64)
//                        if (inF != null) {
////                            txtTemperature.text = "$inF°"
//                            if(isCelsius!!){
//
//                                val celsiusValue = fahrenheitToCelsius(inF)
//                                txtTemperature.text = "$celsiusValue°"
//                            }
//                            else{
//                                txtTemperature.text = "$inF°"
//
//                            }
//                        }
//                    }
                }

                4 -> {
                    val autoOn = intArray?.get(0)
                    findViewById<ImageButton>(R.id.btnAuto).setImageResource(if (autoOn == 1) R.drawable.auto else R.drawable.auto_unselect)
                    playAudio(isSound!!)

                }

                2 -> {
                    val acOn = intArray?.get(0)
                    findViewById<ImageButton>(R.id.btnAC).setImageResource(if (acOn == 1) R.drawable.ac else R.drawable.ac__unselect)
                    playAudio(isSound!!)
                }

                3 -> {
                    val recircOn = intArray?.get(0)
                    findViewById<ImageButton>(R.id.btnRecirc).setImageResource(if (recircOn == 1) R.drawable.recirculation else R.drawable.recirculation__unselect)
                    playAudio(isSound!!)
                }

                15 -> {
                    val recircAutoOn = intArray?.get(0)

                }

                14 -> {
                    val rearDefrost = intArray?.get(0)
                    findViewById<ImageButton>(R.id.btnRearDefrost).setImageResource(if (rearDefrost == 1) R.drawable.rear else R.drawable.rear__unselect)
                    playAudio(isSound!!)
                }

                10 -> {
                    val fanSpeed = intArray?.get(0)
                    if (fanSpeed != null && fanSpeed <= 7) {
                        currentLoaderIndex = fanSpeed
                        findViewById<ImageButton>(R.id.btnFanOff).setImageResource(loaderDrawables[currentLoaderIndex])

                    }
//                    else if( fanSpeed == 0 || fanSpeed == 10){
//                        playAudio(isSound!!)
//                    }
                }

                6 -> {
                    val defrostOn = intArray?.get(0)
                    findViewById<ImageButton>(R.id.btnDefrost).setImageResource(if (defrostOn == 1) R.drawable.defrost_vector else R.drawable.front_defrost_vector)
                    playAudio(isSound!!)
                }

                5 -> {
                    val dualClimate = intArray?.get(0)
                    findViewById<ImageButton>(R.id.btnDualClimate).setImageResource(if (dualClimate == 1) R.drawable.dual else R.drawable.dual_climate_unselect)
                    playAudio(isSound!!)
                }

                7 -> {
                    windowOn = intArray?.get(0) == 1
                    handleVentStatus()
//                    playAudio(isSound!!)
                }

                8 -> {
                    faceOn = intArray?.get(0) == 1
                    handleVentStatus()
//                    playAudio(isSound!!)
                }

                9 -> {
                    feetOn = intArray?.get(0) == 1
                    handleVentStatus()
//                    playAudio(isSound!!)
                }

            }
        }

    }

    private fun playAudio(sound: Boolean) {
        if (sound) {
            if (mediaPlayer != null && !mediaPlayer!!.isPlaying) {
                // Start playing the audio
                mediaPlayer!!.start()
            }
        } else {

        }
    }

    private fun updateTemperatureText(newTemp: Int, textView: TextView) {
        if (newTemp == -2) {
            textView.text = "LO"
        } else if (newTemp == -3) {
            textView.text = "HI"
        } else {
            val inF: Int? = newTemp?.plus(64)
            if (inF != null) {
//                            txtTemperature.text = "$inF°"
                if (isCelsius!!) {
                    val celsiusValue = fahrenheitToCelsius(newTemp)
                    Log.d("TAG", "LEFT VALUE$celsiusValue")
                    Log.d("TAG", "RIGHT VALUE$celsiusValue")
                    textView.text = "$celsiusValue°"
                } else {
                    textView.text = "$inF°"
                    Log.d("TAG", "LEFT VALUE$inF")
                    Log.d("TAG", "RIGHT VALUE$inF")

                }
            }
        }
    }

    private fun fahrenheitToCelsius(fahrenheit: Int): Double {
        val initialValue = 17
        if (fahrenheit == 1) {
            return (initialValue.toDouble() + fahrenheit)
        } else {
            if (fahrenheit % 2 == 0) {
                return (initialValue.toDouble() + (fahrenheit / 2.0) + 0.5)
            } else {
                return (initialValue.toDouble() + (fahrenheit / 2.0) + 0.5)
            }

        }

    }

    fun handleVentStatus() {
        if (faceOn && feetOn) {
            /// Middle button should chnage to blue
//            findViewById<ImageButton>(R.id.btnVentMiddle).setColorFilter(R.color.blue)
            setVentColor(false,R.id.btnVentEnd)
            setVentColor(false,R.id.btnVentTop)
            setVentColor(true,R.id.btnVentMiddle)
        } else if (feetOn && windowOn) {
            /// Middle button should chnage to blue
//            findViewById<ImageButton>(R.id.btnVentMiddle).setColorFilter(R.color.blue)
            setVentColor(false,R.id.btnVentEnd)
            setVentColor(false,R.id.btnVentTop)
            setVentColor(true,R.id.btnVentMiddle)
        } else if (feetOn) {
            /// Bottom button should change to blue
//            findViewById<ImageButton>(R.id.btnVentEnd).setColorFilter(R.color.blue)
            setVentColor(true,R.id.btnVentEnd)
            setVentColor(false,R.id.btnVentTop)
            setVentColor(false,R.id.btnVentMiddle)
        } else if (faceOn) {
            /// Top button should change to blue
//            findViewById<ImageButton>(R.id.btnVentTop).setColorFilter(R.color.blue)
            setVentColor(false,R.id.btnVentEnd)
            setVentColor(true,R.id.btnVentTop)
            setVentColor(false,R.id.btnVentMiddle)
        } else {
            setVentColor(false,R.id.btnVentEnd)
            setVentColor(false,R.id.btnVentTop)
            setVentColor(false,R.id.btnVentMiddle)
        }
    }

    private fun setVentColor(isBlue: Boolean, button: Int) {
         if(isBlue){
             val color = ContextCompat.getColor(this, R.color.blue)
             findViewById<ImageButton>(button).setColorFilter(color)
         }
        else{
             val color = ContextCompat.getColor(this, R.color.white) // Replace R.color.your_color with your desired color resource
             findViewById<ImageButton>(button).setColorFilter(color)
        }
    }
    override fun onDestroy() {

        mediaPlayer?.release()
        super.onDestroy()
    }
}




