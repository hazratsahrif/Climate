package com.lexus.Climate
import android.graphics.Color
import android.graphics.PorterDuff
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.MotionEvent
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.aoe.fytcanbusmonitor.ModuleCodes.MODULE_CODE_CANBUS
import com.aoe.fytcanbusmonitor.ModuleCodes.MODULE_CODE_CAN_UP
import com.aoe.fytcanbusmonitor.ModuleCodes.MODULE_CODE_MAIN
import com.aoe.fytcanbusmonitor.ModuleCodes.MODULE_CODE_SOUND
import com.aoe.fytcanbusmonitor.MsToolkitConnection
import com.aoe.fytcanbusmonitor.RemoteModuleProxy
import com.lexus.ISClimate.viewmodel.MyViewModel

class MainActivity : AppCompatActivity(), View.OnTouchListener {
    private lateinit var viewModel: MyViewModel
    private val remoteProxy = RemoteModuleProxy()
//    private var mediaPlayer: MediaPlayer? = null
    private var isSound: Boolean? = false
    private var isCelsius: Boolean? = true
    private lateinit var saveDate: String
    private lateinit var currentDate: String
    private lateinit var parentLayout: LinearLayout
    var addedValueInDegree: Int = 0
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
        setContentView(R.layout.activity_main)
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
        parentLayout = findViewById<TableRow>(R.id.parentLayout)
        viewModel = ViewModelProvider(this)[MyViewModel::class.java]


//        findViewById<TextView>(R.id.text_view).append("hi")
//        findViewById<TextView>(R.id.text_view).movementMethod = ScrollingMovementMethod()
    }
    override fun onStart() {
        super.onStart()
        ModuleCallback.init(this)
        connectMain()
        connectCanbus()
        connectSound()
        connectCanUp()
        MsToolkitConnection.instance.connect(this)
    }
    override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean {
        var canBusCommand: Int = -1
        when (view?.id) {
            R.id.btnLeftTempPlus -> { canBusCommand = 3 }
            R.id.btnLeftTempMinus -> { canBusCommand = 2 }
            R.id.btnRightTempPlus -> { canBusCommand = 5 }
            R.id.btnRightTempMinus -> { canBusCommand = 4 }
            R.id.btnAuto -> { canBusCommand = 21 }
            R.id.btnVentTop -> { canBusCommand = 36 }
            R.id.btnRecirc -> { canBusCommand = 25 }
            R.id.btnAC -> { canBusCommand = 23 }
            R.id.btnDualClimate -> { canBusCommand = 16 }
            R.id.btnDefrost -> { canBusCommand = 18 }
            R.id.btnRearDefrost -> { canBusCommand = 20 }
            R.id.btnFanPlus -> { canBusCommand = 10 }
            R.id.btnFanMinus -> { canBusCommand = 9 }
            R.id.btnFanOff -> { canBusCommand = 1 }
        }

        val image = view as ImageView
        when (motionEvent?.action) {
            MotionEvent.ACTION_DOWN -> {
                var highlight = Color.argb(50, 255, 255, 255)
                if (view.id == R.id.btnLeftTempMinus) {
                    highlight = Color.argb(50, 0, 0, 255)
                }
                else if (view.id == R.id.btnLeftTempMinus) {
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

    fun canBusNotify(systemName: String, updateCode: Int, intArray: IntArray?, floatArray: FloatArray?, strArray: Array<String?>?) {
        if (systemName.lowercase().equals("canbus")) {
            if (updateCode in 1..16 || updateCode in 69..81 && updateCode != 77) {
                findViewById<TextView>(R.id.text_view).append(
                    "updateCode: " + updateCode + " value: " + intArray?.get(
                        0
                    ) + "\n"
                )
            }
            when (updateCode) {
//                11 -> {
//                    val newTemp = intArray?.get(0)
//                    val txtTemperature = findViewById<TextView>(R.id.txtLeftTemperature)
//                    if (newTemp == -2) {
//                        txtTemperature.text = "LO"
//                    } else if (newTemp == -3) {
//                        txtTemperature.text = "HI"
//                    } else {
//                        val inF: Int? = newTemp?.plus(64)
//                        if (inF != null) {
//                            txtTemperature.text = inF.toString()
//                        }
//                    }
//                }
//                11 -> {
//                    val newTemp = intArray?.get(0)
//                    val txtTemperature = findViewById<TextView>(R.id.txtLeftTemperature)
//                    if (newTemp == -2) {
//                        txtTemperature.text = "LO"
//                    } else if (newTemp == -3) {
//                        txtTemperature.text = "HI"
//                    } else {
//                        val inF: Int? = newTemp?.plus(64)
//                        if (inF != null) {
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
//                }
//
//                12 -> {
//                    val newTemp = intArray?.get(0)
//                    val txtTemperature = findViewById<TextView>(R.id.txtRightTemperature)
//                    if (newTemp == -2) {
//                        txtTemperature.text = "LO"
//                    } else if (newTemp == -3) {
//                        txtTemperature.text = "HI"
//                    } else {
//                        val inF: Int? = newTemp?.plus(64)
//                        if (inF != null) {
//
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
//                }
//                4 -> {
//                    val autoOn = intArray?.get(0)
//                    findViewById<ImageButton>(R.id.btnAuto).setImageResource(if (autoOn == 1) R.drawable.auto else R.drawable.auto_unselect)
//
////                    findViewById<TextView>(R.id.lblAuto).visibility =
////                        if (autoOn == 0) View.INVISIBLE else View.VISIBLE
//                }
//                2 -> {
//                    val acOn = intArray?.get(0)
//                    findViewById<ImageButton>(R.id.btnAC).setImageResource(if (acOn == 1) R.drawable.ac else R.drawable.ac__unselect)
////                    findViewById<ImageView>(R.id.imgAC).setImageResource(if (acOn == 1) R.drawable.img_ac_on else R.drawable.img_ac_off)
//                }
//                3 -> {
//                    val recircOn = intArray?.get(0)
//                    findViewById<ImageButton>(R.id.btnRecirc).setImageResource(if (recircOn == 1) R.drawable.recirculation else R.drawable.recirculation__unselect)
////                    findViewById<ImageView>(R.id.imgRecirc).setImageResource(if (recircOn == 1) R.drawable.img_recirc_on else R.drawable.img_recirc_off)
//                }
//                15 -> {
//                    val recircAutoOn = intArray?.get(0)
////                    findViewById<TextView>(R.id.lblAutoRecirc).visibility =
////                        if (recircAutoOn == 0) View.INVISIBLE else View.VISIBLE
//                }
//                14 -> {
//                    val rearDefrost = intArray?.get(0)
//                    findViewById<ImageButton>(R.id.btnRearDefrost).setImageResource(if (rearDefrost == 1) R.drawable.rear else R.drawable.rear__unselect)
//
//             }
//                10 -> {
//                    val fanSpeed = intArray?.get(0)
////                    findViewById<TextView>(R.id.txtFanSpeed).text = fanSpeed.toString()
//                }
//                6 -> {
//                    val defrostOn = intArray?.get(0)
//                    findViewById<ImageButton>(R.id.btnDefrost).setImageResource(if (defrostOn == 1) R.drawable.defrost_vector else R.drawable.front_defrost_vector)
////                    findViewById<ImageView>(R.id.imgDefrost).setImageResource(if (defrostOn == 1) R.drawable.img_defrost_on else R.drawable.img_defrost_off)
//                }
//                7 -> {
//                    windowOn = intArray?.get(0) == 1
//                    handleVentStatus()
//                }
//                8 -> {
//                    faceOn = intArray?.get(0) == 1
//                    handleVentStatus()
//                }
//                9 -> {
//                    feetOn = intArray?.get(0) == 1
//                    handleVentStatus()
//                }

                11 -> {
                    val newTemp = intArray?.get(0)
                    val txtTemperature = findViewById<TextView>(R.id.txtLeftTemperature)
                    if (newTemp == -2) {
                        txtTemperature.text = "LO"
                    } else if (newTemp == -3) {
                        txtTemperature.text = "HI"
                    } else {
                        val inF: Int? = newTemp?.plus(64)
                        if (inF != null) {
                            txtTemperature.text = "$inF°"
//                            if(isCelsius!!){
//                                val celsiusValue = fahrenheitToCelsius(inF)
//                                txtTemperature.text = "$celsiusValue°"
//                            }
//                            else{
//                                txtTemperature.text = "$inF°"
//                            }


                        }
                    }
                }

                12 -> {
                    val newTemp = intArray?.get(0)
                    val txtTemperature = findViewById<TextView>(R.id.txtRightTemperature)
                    if (newTemp == -2) {
                        txtTemperature.text = "LO"
                    } else if (newTemp == -3) {
                        txtTemperature.text = "HI"
                    } else {
                        val inF: Int? = newTemp?.plus(64)
                        if (inF != null) {
                            txtTemperature.text = "$inF°"
//                            if(isCelsius!!){
//
//                                val celsiusValue = fahrenheitToCelsius(inF)
//                                txtTemperature.text = "$celsiusValue°"
//                            }
//                            else{
//                                txtTemperature.text = "$inF°"
//
//                            }
                        }
                    }
                }

                4 -> {
                    val autoOn = intArray?.get(0)
                    findViewById<ImageButton>(R.id.btnAuto).setImageResource(if (autoOn == 1) R.drawable.auto else R.drawable.auto_unselect)
//
                    //                    playAudio(isSound!!)

                }

                2 -> {
                    val acOn = intArray?.get(0)
                    findViewById<ImageButton>(R.id.btnAC).setImageResource(if (acOn == 1) R.drawable.ac else R.drawable.ac__unselect)
//                    playAudio(isSound!!)
                }

                3 -> {
                    val recircOn = intArray?.get(0)
                    findViewById<ImageButton>(R.id.btnRecirc).setImageResource(if (recircOn == 1) R.drawable.recirculation else R.drawable.recirculation__unselect)
//                    playAudio(isSound!!)
                }

                15 -> {
                    val recircAutoOn = intArray?.get(0)

                }

                14 -> {
                    val rearDefrost = intArray?.get(0)
                    findViewById<ImageButton>(R.id.btnRearDefrost).setImageResource(if (rearDefrost == 1) R.drawable.rear else R.drawable.rear__unselect)
//                    playAudio(isSound!!)
                }

                10 -> {
                    val fanSpeed = intArray?.get(0)
                    if (fanSpeed != null && fanSpeed <= 7) {
                        currentLoaderIndex = fanSpeed
                        findViewById<ImageButton>(R.id.btnFanOff).setImageResource(loaderDrawables[currentLoaderIndex])
//                        playAudio(isSound!!)
                    }
                }

                6 -> {
                    val defrostOn = intArray?.get(0)
                    findViewById<ImageButton>(R.id.btnDefrost).setImageResource(if (defrostOn == 1) R.drawable.defrost_vector else R.drawable.front_defrost_vector)
//                    playAudio(isSound!!)
                }

                5 -> {
                    val dualClimate = intArray?.get(0)
                    findViewById<ImageButton>(R.id.btnDualClimate).setImageResource(if (dualClimate == 1) R.drawable.dual else R.drawable.dual_climate_unselect)
//                    playAudio(isSound!!)
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
            findViewById<ImageButton>(R.id.btnVentMiddle).setColorFilter(R.color.blue)
        } else if (feetOn && windowOn) {
            /// Middle button should chnage to blue
            findViewById<ImageButton>(R.id.btnVentMiddle).setColorFilter(R.color.blue)
        } else if (feetOn) {
            /// Bottom button should change to blue
            findViewById<ImageButton>(R.id.btnVentEnd).setColorFilter(R.color.blue)
        } else if (faceOn) {
            /// Top button should change to blue
            findViewById<ImageButton>(R.id.btnVentTop).setColorFilter(R.color.blue)
        } else {
            findViewById<ImageButton>(R.id.btnVentTop).setColorFilter(R.color.white)
            findViewById<ImageButton>(R.id.btnVentMiddle).setColorFilter(R.color.white)
            findViewById<ImageButton>(R.id.btnVentEnd).setColorFilter(R.color.white)
        }
    }
    }








//package com.lexus.Climate
//
//import android.annotation.SuppressLint
//import android.content.Intent
//import android.graphics.Color
//import android.graphics.PorterDuff
//import android.media.MediaPlayer
//import android.os.Build
//import android.os.Bundle
//import android.util.Log
//import android.view.MotionEvent
//import android.view.View
//import android.widget.ImageButton
//import android.widget.ImageView
//import android.widget.LinearLayout
//import android.widget.TableRow
//import android.widget.TextView
//import android.widget.Toast
//import androidx.annotation.RequiresApi
//import androidx.appcompat.app.AppCompatActivity
//import androidx.lifecycle.ViewModelProvider
//import com.aoe.fytcanbusmonitor.ModuleCodes.MODULE_CODE_CANBUS
//import com.aoe.fytcanbusmonitor.ModuleCodes.MODULE_CODE_CAN_UP
//import com.aoe.fytcanbusmonitor.ModuleCodes.MODULE_CODE_MAIN
//import com.aoe.fytcanbusmonitor.ModuleCodes.MODULE_CODE_SOUND
//import com.aoe.fytcanbusmonitor.MsToolkitConnection
//import com.aoe.fytcanbusmonitor.RemoteModuleProxy
//import com.lexus.ISClimate.viewmodel.MyViewModel
//import java.text.SimpleDateFormat
//import java.util.Locale
//import java.util.concurrent.TimeUnit
//import androidx.lifecycle.Observer
//import com.lexus.ISClimate.SettingDialogFragment
//import io.paperdb.Paper
//
//class MainActivity : AppCompatActivity(), View.OnTouchListener {
//
//    private lateinit var viewModel: MyViewModel
//    private val remoteProxy = RemoteModuleProxy()
//    private var mediaPlayer: MediaPlayer? = null
//    private var isSound: Boolean? = false
//    private var isCelsius: Boolean? = true
//    private lateinit var saveDate: String
//    private lateinit var currentDate: String
//    private lateinit var parentLayout: LinearLayout
//    var addedValueInDegree: Int = 0
//
//    private var windowOn: Boolean = false
//    private var faceOn: Boolean = false
//    private var feetOn: Boolean = false
//    private var currentLoaderIndex: Int = 0
//    private val loaderDrawables = intArrayOf(
//        R.drawable.loader,
//        R.drawable.loader1,
//        R.drawable.loader2,
//        R.drawable.loader3, // Add more drawable resources here
//        R.drawable.loader4, // Add more drawable resources here
//        R.drawable.loader5, // Add more drawable resources here
//        R.drawable.loader6, // Add more drawable resources here
//        R.drawable.loader7, // Add more drawable resources here
//        // Add more drawable resources here
//    )
//    @RequiresApi(Build.VERSION_CODES.O)
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//        findViewById<ImageButton>(R.id.btnLeftTempPlus).setOnTouchListener(this)
//        findViewById<ImageButton>(R.id.btnRightTempPlus).setOnTouchListener(this)
//        findViewById<ImageButton>(R.id.btnLeftTempMinus).setOnTouchListener(this)
//        findViewById<ImageButton>(R.id.btnRightTempMinus).setOnTouchListener(this)
//        findViewById<ImageButton>(R.id.btnAuto).setOnTouchListener(this)
//        findViewById<ImageButton>(R.id.btnVent).setOnTouchListener(this)
//        findViewById<ImageButton>(R.id.btnVentTop).setOnTouchListener(this)
//        findViewById<ImageButton>(R.id.btnVentEnd).setOnTouchListener(this)
//        findViewById<ImageButton>(R.id.btnRecirc).setOnTouchListener(this)
//        findViewById<ImageButton>(R.id.btnAC).setOnTouchListener(this)
//        findViewById<ImageButton>(R.id.btnRearDefrost).setOnTouchListener(this)
//        findViewById<ImageButton>(R.id.btnDefrost).setOnTouchListener(this)
//        findViewById<ImageButton>(R.id.btnDualClimate).setOnTouchListener(this)
//        findViewById<ImageButton>(R.id.btnFanPlus).setOnTouchListener(this)
//        findViewById<ImageButton>(R.id.btnFanOff).setOnTouchListener(this)
//        findViewById<ImageButton>(R.id.btnFanMinus).setOnTouchListener(this)
//        parentLayout = findViewById<TableRow>(R.id.parentLayout)
//        viewModel = ViewModelProvider(this)[MyViewModel::class.java]
//        Paper.init(this)
//        viewModel.getSoundBoolean()
//        viewModel.getBoolean()
//        viewModel.getDegreeBoolean()
//        viewModel.getAccessValue()
//        observeValue()
//        setDriverMode()
//        viewModel.getStartAndEndValuesLiveData().observe(this) { pair ->
//            val (start, end) = pair ?: Pair(null, null)
//            switchLayout(parentLayout, end!!, start!!)
//        }
////        mediaPlayer = MediaPlayer.create(this, R.raw.beep)
//        findViewById<ImageButton>(R.id.btnSetting).setOnClickListener {
//            val dialogFragment = SettingDialogFragment()
//            dialogFragment.show(supportFragmentManager, "MyDialogFragment")
//        }
//    }
//    override fun onStart() {
//        super.onStart()
//        ModuleCallback.init(this)
//        connectMain()
//        connectCanbus()
//        connectSound()
//        connectCanUp()
//        MsToolkitConnection.instance.connect(this)
//    }
//
//    private fun observeValue() {
//        viewModel.isSound.observe(this, Observer {
//            if (it != null && it == true) {
//                isSound = it
////                playAudio(it)
//            } else {
//
//                isSound = false
//            }
//        })
//        viewModel.degreeValue.observe(this, Observer {
//            it
//            if (it != null && it == true) {
//                isCelsius = it
//            } else {
//                isCelsius = false
//            }
//        })
//        viewModel.accessGranted.observe(this, Observer {
//            if (it == true && it != null) {
//            } else {
//                saveDate = viewModel.getCurrentDateTime()
//                if (saveDate != "") {
//                    currentDate = viewModel.currentDateTime()
//                    val daysDifference = calculateDateDifference(saveDate, currentDate)
//                    if (daysDifference >= 8) {
//
////                        val intent = Intent(this, TrailFragment::class.java)
//                        startActivity(intent)
//
//                    } else {
//                    }
//
//                } else {
//                    viewModel.saveCurrentDateTime()
//                }
//            }
//        })
//    }
//
//    private fun playAudio(sound: Boolean) {
//        if (sound) {
//            if (mediaPlayer != null && !mediaPlayer!!.isPlaying) {
//                // Start playing the audio
//                mediaPlayer!!.start()
//            }
//        } else {
//
//        }
//    }
//    private fun setDriverMode() {
//        if (viewModel.booleanValue.value == null) {
//
//        } else {
//            if (viewModel.getBoolean()!!) {
//                switchLayout(parentLayout, 4, 1)
//            } else {
//                switchLayout(parentLayout, 1, 5)
//            }
//        }
//    }
//
//    private fun calculateDateDifference(startDateStr: String, endDateStr: String): Long {
//        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
//        try {
//            val startDate = dateFormat.parse(startDateStr)
//            val endDate = dateFormat.parse(endDateStr)
//
//            if (startDate != null && endDate != null) {
//                // Calculate the difference in milliseconds
//                val differenceMillis = endDate.time - startDate.time
//
//                // Convert milliseconds to days
//                return TimeUnit.MILLISECONDS.toDays(differenceMillis)
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//
//        return 0
//    }
//
//        private fun switchLayout(
//        parentLayout: LinearLayout,
//        leftIndex: Int,
//        rightIndex: Int
//    ) {
//        val layoutLeft = findViewById<LinearLayout>(R.id.layoutLeft)
//        val layoutRight = findViewById<LinearLayout>(R.id.layoutRight)
//        parentLayout.removeView(layoutLeft)
//        parentLayout.removeView(layoutRight)
//
//        // Adding the layouts in the reversed order
//        parentLayout.addView(layoutRight, leftIndex)
//        parentLayout.addView(layoutLeft, rightIndex)
//    }
//
////    override fun onResume() {
////        super.onResume()
////        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
////        supportActionBar?.hide()
////    }
//    override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean {
//        var canBusCommand: Int = -1
//        when (view?.id) {
//            R.id.btnLeftTempPlus -> { canBusCommand = 3 }
//            R.id.btnLeftTempMinus -> { canBusCommand = 2 }
//            R.id.btnRightTempPlus -> { canBusCommand = 5 }
//            R.id.btnRightTempMinus -> { canBusCommand = 4 }
//            R.id.btnAuto -> { canBusCommand = 21 }
//            R.id.btnVentTop -> { canBusCommand = 36 }
//            R.id.btnRecirc -> { canBusCommand = 25 }
//            R.id.btnAC -> { canBusCommand = 23 }
//            R.id.btnDualClimate -> { canBusCommand = 16 }
//            R.id.btnDefrost -> { canBusCommand = 18 }
//            R.id.btnRearDefrost -> { canBusCommand = 20 }
//            R.id.btnFanPlus -> { canBusCommand = 10 }
//            R.id.btnFanMinus -> { canBusCommand = 9 }
//            R.id.btnFanOff -> { canBusCommand = 1 }
//        }
//        val image = view as ImageView
//        when (motionEvent?.action) {
//            MotionEvent.ACTION_DOWN -> {
//                var highlight = Color.argb(50, 255, 255, 255)
//                if (view.id == R.id.btnLeftTempMinus) {
//                    highlight = Color.argb(50, 0, 0, 255)
//                }
//                else if (view.id == R.id.btnLeftTempMinus) {
//                    highlight = Color.argb(50, 255, 0, 0)
//                }
//
//                image?.setColorFilter(highlight, PorterDuff.Mode.SRC_ATOP)
//                image?.invalidate()
//            }
//            MotionEvent.ACTION_UP -> {
//                image?.clearColorFilter()
//                image?.invalidate()
//            }
//        }
//
//        if (canBusCommand != -1) {
//            val startEvent: Boolean = motionEvent?.action == MotionEvent.ACTION_DOWN
//            var rm = MsToolkitConnection.instance.remoteToolkit?.getRemoteModule(MODULE_CODE_CANBUS)
//            rm?.cmd(0, intArrayOf(canBusCommand, if (startEvent) 1 else 0), null, null)
//            if (startEvent) {
//                view?.performClick()
//            }
//        }
//
//        return false
//
//    }
//
//    private fun connectMain() {
//        val callback = ModuleCallback("Main", findViewById(R.id.text_view))
//        val connection = IPCConnection(MODULE_CODE_MAIN)
//        for (i in 0..119) {
//            connection.addCallback(callback, i)
//        }
//        MsToolkitConnection.instance.addObserver(connection)
//    }
//
//    private fun connectCanbus() {
//        val callback = ModuleCallback("Canbus", findViewById(R.id.text_view))
//        val connection = IPCConnection(MODULE_CODE_CANBUS)
//        for (i in 0..50) {
//            connection.addCallback(callback, i)
//        }
//        for (i in 1000..1036) {
//            connection.addCallback(callback, i)
//        }
//        MsToolkitConnection.instance.addObserver(connection)
//    }
//
//    private fun connectSound() {
//
//        val callback = ModuleCallback("Sound", findViewById(R.id.text_view))
//        val connection = IPCConnection(MODULE_CODE_SOUND)
//        for (i in 0..49) {
//            connection.addCallback(callback, i)
//        }
//
//        MsToolkitConnection.instance.addObserver(connection)
//    }
//
//    private fun connectCanUp() {
//        val callback = ModuleCallback("CanUp", findViewById(R.id.text_view))
//        val connection = IPCConnection(MODULE_CODE_CAN_UP)
//        connection.addCallback(callback, 100)
//        MsToolkitConnection.instance.addObserver(connection)
//    }
//
//        fun canBusNotify(systemName: String, updateCode: Int, intArray: IntArray?, floatArray: FloatArray?, strArray: Array<String?>?) {
//        Log.d("TAG", "canBusNotify  get called")
//        if (systemName.lowercase() == "can-bus") {
//            if (updateCode in 1..16 || updateCode in 69..81 && updateCode != 77) {
//                findViewById<TextView>(R.id.text_view).append(
//                    "updateCode: $updateCode value: " + intArray?.get(
//                        0
//                    ) + "\n"
//                )
//            }
//
//            when (updateCode) {
//                11 -> {
//                    val newTemp = intArray?.get(0)
//                    val txtTemperature = findViewById<TextView>(R.id.txtLeftTemperature)
//                    if (newTemp == -2) {
//                        txtTemperature.text = "LO"
//                    } else if (newTemp == -3) {
//                        txtTemperature.text = "HI"
//                    } else {
//                        val inF: Int? = newTemp?.plus(64)
//                        if (inF != null) {
//                            txtTemperature.text = "$inF°"
////                            if(isCelsius!!){
////                                val celsiusValue = fahrenheitToCelsius(inF)
////                                txtTemperature.text = "$celsiusValue°"
////                            }
////                            else{
////                                txtTemperature.text = "$inF°"
////                            }
//
//
//                        }
//                    }
//                }
//
//                12 -> {
//                    val newTemp = intArray?.get(0)
//                    val txtTemperature = findViewById<TextView>(R.id.txtRightTemperature)
//                    if (newTemp == -2) {
//                        txtTemperature.text = "LO"
//                    } else if (newTemp == -3) {
//                        txtTemperature.text = "HI"
//                    } else {
//                        val inF: Int? = newTemp?.plus(64)
//                        if (inF != null) {
//                            txtTemperature.text = "$inF°"
////                            if(isCelsius!!){
////
////                                val celsiusValue = fahrenheitToCelsius(inF)
////                                txtTemperature.text = "$celsiusValue°"
////                            }
////                            else{
////                                txtTemperature.text = "$inF°"
////
////                            }
//                        }
//                    }
//                }
//
//                4 -> {
//                    val autoOn = intArray?.get(0)
//                    findViewById<ImageButton>(R.id.btnAuto).setImageResource(if (autoOn == 1) R.drawable.auto else R.drawable.auto_unselect)
////
//                    //                    playAudio(isSound!!)
//
//                }
//
////                2 -> {
////                    val acOn = intArray?.get(0)
////                    findViewById<ImageButton>(R.id.btnAC).setImageResource(if (acOn == 1) R.drawable.ac else R.drawable.ac__unselect)
//////                    playAudio(isSound!!)
////                }
////
////                3 -> {
////                    val recircOn = intArray?.get(0)
////                    findViewById<ImageButton>(R.id.btnRecirc).setImageResource(if (recircOn == 1) R.drawable.recirculation else R.drawable.recirculation__unselect)
//////                    playAudio(isSound!!)
////                }
////
////                15 -> {
////                    val recircAutoOn = intArray?.get(0)
////
////                }
////
////                14 -> {
////                    val rearDefrost = intArray?.get(0)
////                    findViewById<ImageButton>(R.id.btnRearDefrost).setImageResource(if (rearDefrost == 1) R.drawable.rear else R.drawable.rear__unselect)
//////                    playAudio(isSound!!)
////                }
////
////                10 -> {
////                    val fanSpeed = intArray?.get(0)
//////                    if (fanSpeed != null && fanSpeed <= 7) {
//////                        currentLoaderIndex = fanSpeed
//////                        findViewById<ImageButton>(R.id.btnFanOff).setImageResource(loaderDrawables[currentLoaderIndex])
////////                        playAudio(isSound!!)
//////                    }
////                }
////
////                6 -> {
////                    val defrostOn = intArray?.get(0)
////                    findViewById<ImageButton>(R.id.btnDefrost).setImageResource(if (defrostOn == 1) R.drawable.defrost_vector else R.drawable.front_defrost_vector)
//////                    playAudio(isSound!!)
////                }
////
////                5 -> {
////                    val dualClimate = intArray?.get(0)
////                    findViewById<ImageButton>(R.id.btnDualClimate).setImageResource(if (dualClimate == 1) R.drawable.dual else R.drawable.dual_climate_unselect)
//////                    playAudio(isSound!!)
////                }
//
//                7 -> {
//                    windowOn = intArray?.get(0) == 1
//                    handleVentStatus()
////                    playAudio(isSound!!)
//                }
//
//                8 -> {
//                    faceOn = intArray?.get(0) == 1
//                    handleVentStatus()
////                    playAudio(isSound!!)
//                }
//
//                9 -> {
//                    feetOn = intArray?.get(0) == 1
//                    handleVentStatus()
////                    playAudio(isSound!!)
//                }
//
//            }
//        }
//
//    }
//
//    private fun fahrenheitToCelsius(fahrenheit: Int): Double {
//        val initialValue = 17
//        if (fahrenheit == 1) {
//            return (initialValue.toDouble() + fahrenheit)
//        } else {
//            if (fahrenheit % 2 == 0) {
//                return (initialValue.toDouble() + (fahrenheit / 2.0) + 0.5)
//            } else {
//                return (initialValue.toDouble() + (fahrenheit / 2.0) + 0.5)
//            }
//
//        }
//
//    }
//
//    private fun handleVentStatus() {
//        if (faceOn && feetOn) {
//            /// Middle button should chnage to blue
//            findViewById<ImageButton>(R.id.btnVentMiddle).setColorFilter(R.color.blue)
//        } else if (feetOn && windowOn) {
//            /// Middle button should chnage to blue
//            findViewById<ImageButton>(R.id.btnVentMiddle).setColorFilter(R.color.blue)
//        } else if (feetOn) {
//            /// Bottom button should change to blue
//            findViewById<ImageButton>(R.id.btnVentEnd).setColorFilter(R.color.blue)
//        } else if (faceOn) {
//            /// Top button should change to blue
//            findViewById<ImageButton>(R.id.btnVentTop).setColorFilter(R.color.blue)
//        } else {
//            findViewById<ImageButton>(R.id.btnVentTop).setColorFilter(R.color.white)
//            findViewById<ImageButton>(R.id.btnVentMiddle).setColorFilter(R.color.white)
//            findViewById<ImageButton>(R.id.btnVentEnd).setColorFilter(R.color.white)
//        }
//    }
//
//    override fun onDestroy() {
//        mediaPlayer?.release()
//        super.onDestroy()
//    }
//
//}



