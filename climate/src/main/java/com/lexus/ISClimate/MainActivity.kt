package com.lexus.Climate

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.MotionEvent
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.aoe.fytcanbusmonitor.ModuleCodes.MODULE_CODE_CANBUS
import com.aoe.fytcanbusmonitor.ModuleCodes.MODULE_CODE_CAN_UP
import com.aoe.fytcanbusmonitor.ModuleCodes.MODULE_CODE_MAIN
import com.aoe.fytcanbusmonitor.ModuleCodes.MODULE_CODE_SOUND
import com.aoe.fytcanbusmonitor.MsToolkitConnection
import com.aoe.fytcanbusmonitor.RemoteModuleProxy

class MainActivity : AppCompatActivity(), View.OnTouchListener {


    private val remoteProxy = RemoteModuleProxy()

    var windowOn: Boolean = false
    var faceOn: Boolean = false
    var feetOn: Boolean = false
    private var isCelsius: Boolean? = true

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<ImageButton>(R.id.btnLeftTempPlus).setOnTouchListener(this)
        findViewById<ImageButton>(R.id.btnLeftTempMinus).setOnTouchListener(this)
        findViewById<ImageButton>(R.id.btnAuto).setOnTouchListener(this)
        findViewById<ImageButton>(R.id.btnVent).setOnTouchListener(this)
        findViewById<ImageButton>(R.id.btnRecirc).setOnTouchListener(this)
        findViewById<ImageButton>(R.id.btnAC).setOnTouchListener(this)
        findViewById<ImageButton>(R.id.btnDefrost).setOnTouchListener(this)
        findViewById<ImageButton>(R.id.btnFanPlus).setOnTouchListener(this)
        findViewById<ImageButton>(R.id.btnFanMinus).setOnTouchListener(this)
        findViewById<ImageButton>(R.id.btnFanOff).setOnTouchListener(this)
        findViewById<TextView>(R.id.text_view).append("hi")
        findViewById<TextView>(R.id.text_view).movementMethod = ScrollingMovementMethod()
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
            R.id.btnLeftTempPlus->{ canBusCommand = 3 }
            R.id.btnLeftTempMinus->{ canBusCommand = 2 }
            R.id.btnAuto->{ canBusCommand = 21 }
            R.id.btnVent->{ canBusCommand = 36 }
            R.id.btnRecirc->{ canBusCommand = 25 }
            R.id.btnAC->{ canBusCommand = 23 }
            R.id.btnDefrost->{ canBusCommand = 18 }
            R.id.btnFanPlus->{ canBusCommand = 10 }
            R.id.btnFanMinus->{ canBusCommand = 9 }
            R.id.btnFanOff->{ canBusCommand = 1 }
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
                            if(isCelsius!!){
                                val celsiusValue = fahrenheitToCelsius(inF)
                                txtTemperature.text = "$celsiusValue°"
                            }
                            else{
                                txtTemperature.text = "$inF°"
                            }


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

                            if(isCelsius!!){

                                val celsiusValue = fahrenheitToCelsius(inF)
                                txtTemperature.text = "$celsiusValue°"
                            }
                            else{
                                txtTemperature.text = "$inF°"

                            }
                        }
                    }
                }
                4 -> {
                    val autoOn = intArray?.get(0)
                    findViewById<ImageButton>(R.id.btnAuto).setImageResource(if (autoOn == 1) R.drawable.auto else R.drawable.auto_unselect)

//                    findViewById<TextView>(R.id.lblAuto).visibility =
//                        if (autoOn == 0) View.INVISIBLE else View.VISIBLE
                }
                2 -> {
                    val acOn = intArray?.get(0)
                    findViewById<ImageButton>(R.id.btnAC).setImageResource(if (acOn == 1) R.drawable.ac else R.drawable.ac__unselect)
//                    findViewById<ImageView>(R.id.imgAC).setImageResource(if (acOn == 1) R.drawable.img_ac_on else R.drawable.img_ac_off)
                }
                3 -> {
                    val recircOn = intArray?.get(0)
//                    findViewById<ImageView>(R.id.imgRecirc).setImageResource(if (recircOn == 1) R.drawable.img_recirc_on else R.drawable.img_recirc_off)
                }
                15 -> {
                    val recircAutoOn = intArray?.get(0)
//                    findViewById<TextView>(R.id.lblAutoRecirc).visibility =
//                        if (recircAutoOn == 0) View.INVISIBLE else View.VISIBLE
                }
                10 -> {
                    val fanSpeed = intArray?.get(0)
//                    findViewById<TextView>(R.id.txtFanSpeed).text = fanSpeed.toString()
                }
                6 -> {
                    val defrostOn = intArray?.get(0)
//                    findViewById<ImageView>(R.id.imgDefrost).setImageResource(if (defrostOn == 1) R.drawable.img_defrost_on else R.drawable.img_defrost_off)
                }
                7 -> {
                    windowOn = intArray?.get(0) == 1
                    handleVentStatus()
                }
                8 -> {
                    faceOn = intArray?.get(0) == 1
                    handleVentStatus()
                }
                9 -> {
                    feetOn = intArray?.get(0) == 1
                    handleVentStatus()
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
//            findViewById<ImageView>(R.id.imgVents).setImageResource(R.drawable.img_vents_foot_face)
        } else if (feetOn && windowOn) {
//            findViewById<ImageView>(R.id.imgVents).setImageResource(R.drawable.img_vents_foot_defrost)
        } else if (feetOn) {
//            findViewById<ImageView>(R.id.imgVents).setImageResource(R.drawable.img_vents_foot)
        } else if (faceOn) {
//            findViewById<ImageView>(R.id.imgVents).setImageResource(R.drawable.img_vents_face)
        } else {
//            findViewById<ImageView>(R.id.imgVents).setImageResource(R.drawable.img_vents_off)
        }
    }


}