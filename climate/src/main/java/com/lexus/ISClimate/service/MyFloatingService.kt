package com.lexus.ISClimate.service

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View

import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.aoe.fytcanbusmonitor.ModuleCodes
import com.aoe.fytcanbusmonitor.MsToolkitConnection
import com.lexus.Climate.IPCConnection
import com.lexus.Climate.MainActivity
import com.lexus.Climate.ModuleCallback
import com.lexus.Climate.R
import com.lexus.ISClimate.trial.TrailFragment
import com.lexus.ISClimate.viewmodel.MyViewModel
import io.paperdb.Paper


class MyFloatingService : Service(), View.OnTouchListener  {
    private lateinit var viewModel: MyViewModel
    private var mWindowManager: WindowManager? = null
    private var mFloatingView: View? = null
    private var valueOfDegree: Int? = 1
    private var valueOfRightDegree: Int? = 1
    private var isCelsius: Boolean? = true

    override fun onBind(intent: Intent): IBinder? {
        return  null
    }

    override fun onCreate() {
        super.onCreate()
        mFloatingView = LayoutInflater.from(this).inflate(R.layout.floating_layout, null)
        viewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
            .create(MyViewModel::class.java)
        setOnToushClickListner()
       showLayout()
        setInitialTemperature()
        Paper.init(this)
        viewModel.getDegreeBoolean()
        observeValue()
    }
    private fun observeValue() {
        viewModel.degreeValue.observeForever(Observer {
            Log.d("TAG", "observeValue: ")
            Toast.makeText(this, "DEGREE" +it, Toast.LENGTH_SHORT).show()
            if (it == null) {
                viewModel.degreeValue.value = true
                setInitialTemperature()

            } else {
                isCelsius = it
                setInitialTemperature()
            }
        })
    }

    private fun showLayout() {
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.BOTTOM

        mWindowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        mWindowManager?.addView(mFloatingView, params)
    }

    override fun onStart(intent: Intent?, startId: Int) {
        super.onStart(intent, startId)
        ModuleCallback.initService(this)
        connectMain()
        connectCanbus()
        connectSound()
        connectCanUp()
        MsToolkitConnection.instance.connect(this)
    }
    private fun visibleLayout() {
        mFloatingView!!.findViewById<FrameLayout>(R.id.root_conatiner).visibility = View.VISIBLE
        Handler().postDelayed({
            mFloatingView!!.findViewById<FrameLayout>(R.id.root_conatiner).visibility = View.GONE
        },
            4000L)
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mFloatingView!!.findViewById<FrameLayout>(R.id.root_conatiner).visibility = View.GONE
        return START_STICKY
    }
    private fun connectMain() {
        val callback = ModuleCallback("Main", mFloatingView!!.findViewById(R.id.text_view))
        val connection = IPCConnection(ModuleCodes.MODULE_CODE_MAIN)
        for (i in 0..119) {
            connection.addCallback(callback, i)
        }
        MsToolkitConnection.instance.addObserver(connection)
    }
    private fun setInitialTemperature() {
        val txtTemperature = mFloatingView!!.findViewById<TextView>(R.id.txtLeftTemperature)
        val txtRightTemperature = mFloatingView!!.findViewById<TextView>(R.id.txtRightTemperature)
        updateTemperatureText(valueOfDegree!!, txtTemperature)
        updateTemperatureText(valueOfRightDegree!!, txtRightTemperature)
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

    private fun connectCanbus() {
        val callback = ModuleCallback("Canbus", mFloatingView!!.findViewById(R.id.text_view))
        val connection = IPCConnection(ModuleCodes.MODULE_CODE_CANBUS)
        for (i in 0..50) {
            connection.addCallback(callback, i)
        }
        for (i in 1000..1036) {
            connection.addCallback(callback, i)
        }
        MsToolkitConnection.instance.addObserver(connection)
    }

    private fun connectSound() {
        val callback = ModuleCallback("Sound", mFloatingView!!.findViewById(R.id.text_view))
        val connection = IPCConnection(ModuleCodes.MODULE_CODE_SOUND)
        for (i in 0..49) {
            connection.addCallback(callback, i)
        }
        MsToolkitConnection.instance.addObserver(connection)
    }

    private fun connectCanUp() {
        val callback = ModuleCallback("CanUp", mFloatingView!!.findViewById(R.id.text_view))
        val connection = IPCConnection(ModuleCodes.MODULE_CODE_CAN_UP)
        connection.addCallback(callback, 100)
        MsToolkitConnection.instance.addObserver(connection)
    }

    override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean {
        var canBusCommand: Int = -1
        when (view?.id) {
            R.id.btnLeftTempPlus -> {
                canBusCommand = 3
            }

            R.id.btnLeftTempMinus -> {
                canBusCommand = 2
            }

            R.id.btnRightTempPlus -> {
                canBusCommand = 5
            }

            R.id.btnRightTempMinus -> {
                canBusCommand = 4
            }

            R.id.btnAuto -> {
                canBusCommand = 21
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

//            R.id.btnFanOff -> {
//                canBusCommand = 1
//            }
        }

        val image = view as ImageView
        when (motionEvent?.action) {
            MotionEvent.ACTION_DOWN -> {
                var highlight = Color.argb(50, 255, 255, 255)
                if (view!!.id == R.id.btnLeftTempMinus) {
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
            var rm =
                MsToolkitConnection.instance.remoteToolkit?.getRemoteModule(ModuleCodes.MODULE_CODE_CANBUS)
            rm?.cmd(0, intArrayOf(canBusCommand, if (startEvent) 1 else 0), null, null)
            if (startEvent) {
                view?.performClick()
            }
        }
        return false
    }
    private fun setOnToushClickListner() {
        mFloatingView!!.findViewById<ImageButton>(R.id.btnAuto).setOnTouchListener(this)
        mFloatingView!!.findViewById<ImageButton>(R.id.btnAC).setOnTouchListener(this)
        mFloatingView!!.findViewById<ImageButton>(R.id.btnDefrost).setOnTouchListener(this)
        mFloatingView!!.findViewById<ImageButton>(R.id.btnLeftTempMinus).setOnTouchListener(this)
        mFloatingView!!.findViewById<ImageButton>(R.id.btnFanOff).setOnTouchListener(this)
        mFloatingView!!.findViewById<ImageButton>(R.id.btnDualClimate).setOnTouchListener(this)
        mFloatingView!!.findViewById<ImageButton>(R.id.btnRearDefrost).setOnTouchListener(this)
        mFloatingView!!.findViewById<ImageButton>(R.id.btnRecirc).setOnTouchListener(this)
        mFloatingView!!.findViewById<ImageButton>(R.id.btnLeftTempMinus).setOnTouchListener(this)
        mFloatingView!!.findViewById<ImageButton>(R.id.btnRightTempMinus).setOnTouchListener(this)
        mFloatingView!!.findViewById<ImageButton>(R.id.btnRightTempPlus).setOnTouchListener(this)
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
//                mFloatingView!!.findViewById<TextView>(R.id.text_view).append(
//                    "updateCode: " + updateCode + " value: " + intArray?.get(
//                        0
//                    ) + "\n"
//                )
            }
            when (updateCode) {

//                11 -> {
//                    val newTemp = intArray?.get(0)
//                    if (newTemp != null) {
//                        valueOfDegree = newTemp
//                        val txtTemperature =mFloatingView!!. findViewById<TextView>(R.id.txtLeftTemperature)
//                        utils.updateTemperatureText(newTemp!!, txtTemperature,isCelsius)
////                        playAudio(isSound!!)
//                    }
//                }

//                12 -> {
//                    val newTemp = intArray?.get(0)
//
//                    if (newTemp != null) {
//                        valueOfRightDegree = newTemp
//                        val txtTemperature = mFloatingView!!.findViewById<TextView>(R.id.txtRightTemperature)
//                        utils.updateTemperatureText(newTemp!!, txtTemperature,isCelsius)
////                        playAudio(isSound!!)
//                    }
//                }

                4 -> {
                    val autoOn = intArray?.get(0)
                    visibleLayout()
                    mFloatingView!!.findViewById<ImageButton>(R.id.btnAuto)
                        .setImageResource(if (autoOn == 1) R.drawable.f_auto_on else R.drawable.f_auto_off)


                }

                2 -> {
                    val acOn = intArray?.get(0)
                    visibleLayout()
                    mFloatingView!!.findViewById<ImageButton>(R.id.btnAC)
                        .setImageResource(if (acOn == 1) R.drawable.f_ac_on else R.drawable.f_ac_off)
                }

                3 -> {
                    val recircOn = intArray?.get(0)
                    visibleLayout()
                    mFloatingView!!.findViewById<ImageButton>(R.id.btnRecirc)
                        .setImageResource(if (recircOn == 1) R.drawable.f_circular_on else R.drawable.f_circular_off)
                }

                15 -> {
                    val recircAutoOn = intArray?.get(0)


                }

                14 -> {
                    val rearDefrost = intArray?.get(0)
                    visibleLayout()
                    mFloatingView!!.findViewById<ImageButton>(R.id.btnRearDefrost)
                        .setImageResource(if (rearDefrost == 1) R.drawable.f_rear_on else R.drawable.fr_rear_off)
                }

                10 -> {
                    val fanSpeed = intArray?.get(0)
                    visibleLayout()
//                    mFloatingView!!.findViewById<ImageButton>(R.id.btnFanOff)
//                        .setImageResource(if (fanSpeed != 0) R.drawable.f_fan_on else R.drawable.f_fan_off)

                }

                6 -> {
                    val defrostOn = intArray?.get(0)
                    visibleLayout()
                    mFloatingView!!.findViewById<ImageButton>(R.id.btnDefrost)
                        .setImageResource(if (defrostOn == 1) R.drawable.f_defrost_on else R.drawable.f_defrost_off)

                }

                5 -> {
                    val dualClimate = intArray?.get(0)
                    visibleLayout()
                    mFloatingView!!.findViewById<ImageButton>(R.id.btnDualClimate)
                        .setImageResource(if (dualClimate == 1) R.drawable.f_dual_on else R.drawable.f_dual_off)

                }


            }
        }

    }


    override fun onDestroy() {
        super.onDestroy()
        if (mFloatingView != null) mWindowManager?.removeView(mFloatingView)
    }


}class MyViewModelStoreOwner : ViewModelStoreOwner {
    private val viewModelStore = ViewModelStore()

    override fun getViewModelStore(): ViewModelStore {
        return viewModelStore
    }
}