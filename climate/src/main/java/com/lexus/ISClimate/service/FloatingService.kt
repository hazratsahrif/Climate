package com.lexus.ISClimate.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.media.MediaPlayer
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
import android.widget.LinearLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.viewModelFactory
import com.aoe.fytcanbusmonitor.ModuleCodes
import com.aoe.fytcanbusmonitor.MsToolkitConnection
import com.aoe.fytcanbusmonitor.RemoteModuleProxy
import com.lexus.Climate.IPCConnection
import com.lexus.Climate.MainActivity
import com.lexus.Climate.ModuleCallback
import com.lexus.Climate.R
import com.lexus.ISClimate.trial.TrailFragment
import com.lexus.ISClimate.viewmodel.MyViewModel
import com.shared.Utils
import io.paperdb.Paper
import org.koin.androidx.viewmodel.ext.android.viewModel
import androidx.lifecycle.ViewModel
import com.aoe.fytcanbusmonitor.ModuleObject.Companion.get
import com.event.BooleanCelsiousChangedEvent
import com.koinmoduel.viewModelModule
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class FloatingService : Service(), View.OnTouchListener {
    private lateinit var viewModel: MyViewModel

    private var mWindowManager: WindowManager? = null
    private var mFloatingView: View? = null
    private var params: WindowManager.LayoutParams? = null
    private lateinit var utils: Utils


    //    private var isSound: Boolean? = false
    private var isCelsius: Boolean? = true
    private lateinit var parentLayout: LinearLayout
    private var valueOfDegree: Int? = 1
    private var valueOfRightDegree: Int? = 1
    private val handler = Handler()
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate() {
        super.onCreate()
        viewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(application).create(MyViewModel::class.java)
        EventBus.getDefault().register(this)
        mFloatingView = LayoutInflater.from(this).inflate(R.layout.floating_layout, null)
        utils = Utils()
        Paper.init(this)
        startForeground()
        showFloatingView()
        parentLayout = mFloatingView!!.findViewById<TableRow>(R.id.parentLayout)
        viewModel.getStartAndEndValuesLiveData().observeForever{ pair ->
            val (start, end) = pair ?: Pair(null, null)
            utils.switchLayout(parentLayout,end!!-4,start!!-5,mFloatingView!!.findViewById(R.id.layoutLeft),mFloatingView!!.findViewById(R.id.layoutRight))
        }
        viewModel.getSoundBoolean()
        viewModel.getBoolean()
        viewModel.getDegreeBoolean()
        viewModel.getAccessValue()
        observeValue()
        startService(Intent(this, FloatingService::class.java))
        setOnToushClickListner()
//        val closeButtonCollapsed = mFloatingView!!.findViewById<ImageButton>(R.id.btnAuto) as ImageButton
//        closeButtonCollapsed.setOnClickListener { //close the service and remove the from from the window
//            stopSelf()
//        }

    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onBooleanValueChanged(event: BooleanCelsiousChangedEvent) {

        // Handle the boolean value change here
        val value = event.value
        isCelsius = value
        viewModel.updateDegreeBoolean(isCelsius!!)
//        if(isCelsius!!) utils.switchLayout(parentLayout, 0,2,mFloatingView!!.findViewById(R.id.layoutLeft),mFloatingView!!.findViewById(R.id.layoutRight))
//        else utils.switchLayout(parentLayout,2,0,mFloatingView!!.findViewById(R.id.layoutLeft),mFloatingView!!.findViewById(R.id.layoutRight))

        viewModel.updateValues(event.start!!,event.end!!)
        viewModel.updateValues(event.start!!,event.end!!)
//        setInitialTemperature()

    }
    private fun observeValue() {
        viewModel.degreeValue.observeForever{
            Log.d("TAG", "Observe value in service $it")
            if (it == null) {
                viewModel.degreeValue.value = true
                setInitialTemperature()
            } else {
                isCelsius = it
                setInitialTemperature()
            }
        }
    }
    private fun setInitialTemperature() {
        val txtTemperature = mFloatingView!!.findViewById<TextView>(R.id.txtLeftTemperature)
        val txtRightTemperature = mFloatingView!!.findViewById<TextView>(R.id.txtRightTemperature)
        utils.updateTemperatureText(valueOfDegree!!+4, txtTemperature,isCelsius)
        utils.updateTemperatureText(valueOfRightDegree!!, txtRightTemperature,isCelsius)
    }
    private fun showFloatingView() {
        val LAYOUT_FLAG: Int
        Log.d("TAG", "showFloatingView: IF Condition")
        LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            LAYOUT_FLAG,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        //Specify the view position
        params!!.gravity =
            Gravity.BOTTOM //Initially view will be added to top-left corner
//        params!!.x = 0
//        params!!.y = 0
        //Add the view to the window
        mWindowManager = getSystemService(WINDOW_SERVICE) as WindowManager?
        mWindowManager!!.addView(mFloatingView, params)
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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mFloatingView!!.findViewById<FrameLayout>(R.id.root_conatiner).visibility = View.GONE
//        visibleLayout()
        return START_REDELIVER_INTENT

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
            var rm =
                MsToolkitConnection.instance.remoteToolkit?.getRemoteModule(ModuleCodes.MODULE_CODE_CANBUS)
            rm?.cmd(0, intArrayOf(canBusCommand, if (startEvent) 1 else 0), null, null)
            if (startEvent) {
                view?.performClick()
            }
        }
        return false
    }

    private fun visibleLayout() {
        mFloatingView!!.findViewById<FrameLayout>(R.id.root_conatiner).visibility = View.VISIBLE
        handler.postDelayed({
            mFloatingView!!.findViewById<FrameLayout>(R.id.root_conatiner).visibility = View.GONE
                },
            4000L)
    }

    override fun onDestroy() {
        if (mFloatingView != null) mWindowManager!!.removeView(mFloatingView)
        EventBus.getDefault().unregister(this)
        super.onDestroy()

    }

    private fun connectMain() {
        val callback = ModuleCallback("Main", mFloatingView!!.findViewById(R.id.text_view))
        val connection = IPCConnection(ModuleCodes.MODULE_CODE_MAIN)
        for (i in 0..119) {
            connection.addCallback(callback, i)
        }
        MsToolkitConnection.instance.addObserver(connection)
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

                11 -> {
                    val newTemp = intArray?.get(0)
                    if (newTemp != null) {
                        valueOfDegree = newTemp
                        val txtTemperature =mFloatingView!!. findViewById<TextView>(R.id.txtLeftTemperature)
                        utils.updateTemperatureText(newTemp!!, txtTemperature,isCelsius)
//                        playAudio(isSound!!)
                    }
                }

                12 -> {
                    val newTemp = intArray?.get(0)

                    if (newTemp != null) {
                        valueOfRightDegree = newTemp
                        val txtTemperature = mFloatingView!!.findViewById<TextView>(R.id.txtRightTemperature)
                        utils.updateTemperatureText(newTemp!!, txtTemperature,isCelsius)
//                        playAudio(isSound!!)
                    }
                }

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


    private fun startForeground() {
        val channelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel("my_service", "My Background Service")
            } else {
                // If earlier version channel ID is not used
                // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
                ""
            }

        val notificationBuilder = NotificationCompat.Builder(this, channelId )
        val notification = notificationBuilder.setOngoing(true)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
        startForeground(101, notification)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String{
        val chan = NotificationChannel(channelId,
            channelName, NotificationManager.IMPORTANCE_NONE)
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }


}