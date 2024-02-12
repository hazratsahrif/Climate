package com.shared

import android.util.Log
import android.widget.TextView

class Utils {
    fun updateTemperatureText(newTemp: Int, textView: TextView,isCelsius:Boolean?) {
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

}