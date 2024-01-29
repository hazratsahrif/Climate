package com.lexus.ISClimate.viewmodel

import android.content.Context
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.paperdb.Paper

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MyViewModel: ViewModel() {
    private val startValue = MutableLiveData<Int>()
    private val endValue = MutableLiveData<Int>()
    val booleanValue = MutableLiveData<Boolean>()
    val degreeValue = MutableLiveData<Boolean>()
    val accessGranted = MutableLiveData<Boolean?>()
    val isSound = MutableLiveData<Boolean>()

//    val switchStateLiveData = MutableLiveData<Boolean>()


    fun updateValues(start: Int, end: Int) {
        startValue.value = start
        endValue.value = end
    }
    private val startAndEndValues = MediatorLiveData<Pair<Int?, Int?>>()

    init {
        startAndEndValues.addSource(startValue) { start ->
            val end = endValue.value
            if (start != null && end != null) {
                startAndEndValues.value = Pair(start, end)
            }
        }

        startAndEndValues.addSource(endValue) { end ->
            val start = startValue.value
            if (start != null && end != null) {
                startAndEndValues.value = Pair(start, end)
            }
        }
    }
    fun getStartAndEndValuesLiveData(): MutableLiveData<Pair<Int?, Int?>> {
        return startAndEndValues
    }

    fun updateBoolean(newValue: Boolean,context: Context) {
        booleanValue.value = newValue
        saveLeftRightValue(newValue,context)
    }

    fun updateSoundBoolean(newValue: Boolean) {
        isSound.value = newValue
        saveSoundValue(newValue)
    }
    fun getSoundBoolean(): Boolean? {
        isSound.value = getSoundStoreValue()
        return isSound.value

    }
    fun getBoolean(): Boolean? {
         booleanValue.value = getLeftRightValue()
        return booleanValue.value

    }


    fun updateDegreeBoolean(newValue: Boolean) {
        degreeValue.value = newValue
        saveDegreeBoolean(newValue)

    }

    private fun saveDegreeBoolean(degreeValue: Boolean) {
        Paper.book().write("degree",degreeValue)
    }

    fun getDegreeBoolean(): Boolean? {
        degreeValue.value = getSaveDegreeValue()
        return degreeValue.value

    }
    private fun getSaveDegreeValue(): Boolean? {
        return Paper.book().read("degree")
    }
    private fun saveLeftRightValue(isLeftRight:Boolean,context: Context){
        Paper.book().write("mode",isLeftRight)
    }

    private fun saveSoundValue(isSound:Boolean){
        Paper.book().write("sound",isSound)
    }
    private fun getSoundStoreValue(): Boolean? {
        return Paper.book().read("sound")
    }
    private fun getLeftRightValue(): Boolean? {
        return Paper.book().read("mode")
    }
    fun saveCurrentDateTime() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentDateTime = Date()
        Paper.book().write("CURRENT_DATE",dateFormat.format(currentDateTime))

    }

    fun currentDateTime():String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentDateTime = Date()
        return  dateFormat.format(currentDateTime)

    }
    fun getCurrentDateTime():String {
        val value: String? = Paper.book().read<String>("CURRENT_DATE")
        if (value != null) {
            return value
        } else {
            return ""
        }
    }

    fun setAccessGrantedValue(newValue: Boolean) {
        accessGranted.value = newValue
        saveAccessGrantValue(newValue)
    }

    fun getAccessValue(): Boolean? {
        accessGranted.value = getSaveAccessValue()
        return accessGranted.value

    }
    private fun saveAccessGrantValue(isGrantedAccess:Boolean){
        Paper.book().write("access",isGrantedAccess)
    }
    private fun getSaveAccessValue(): Boolean? {

        val value: Boolean? = Paper.book().read<Boolean>("access")
        return value != null
    }
}