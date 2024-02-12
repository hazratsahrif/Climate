package com.lexus.Climate

import android.app.Service
import android.os.Build
import android.os.RemoteException
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.aoe.fytcanbusmonitor.IModuleCallback
import com.lexus.ISClimate.service.FloatingService

class ModuleCallback(name: String, private val view: TextView?) : IModuleCallback.Stub() {
    private val systemName = name

    @Throws(RemoteException::class)
    override fun update(
        updateCode: Int,
        intArray: IntArray?,
        floatArray: FloatArray?,
        strArray: Array<String?>?
    ) {
        logMsg(systemName, updateCode, intArray, floatArray, strArray)
    }

    companion object {
        private lateinit var act: MainActivity
        private lateinit var actService: FloatingService

        @RequiresApi(Build.VERSION_CODES.O)
        fun initService(serviceAct: FloatingService) {
            actService = serviceAct
        }


        @RequiresApi(Build.VERSION_CODES.O)
        fun init(mainAct: MainActivity) {
            act = mainAct
        }

        @Synchronized
        private fun logMsg(
            systemName: String,
            updateCode: Int,
            intArray: IntArray?,
            floatArray: FloatArray?,
            strArray: Array<String?>?
        ) {
            act.runOnUiThread(Runnable {
                act.canBusNotify(systemName, updateCode, intArray, floatArray, strArray)
//                act.canBusNotify(systemName, updateCode, intArray)
            })
            act.runOnUiThread(Runnable {
                actService.canBusNotify(systemName, updateCode, intArray, floatArray, strArray)
//                act.canBusNotify(systemName, updateCode, intArray)
            })
        }
    }
}