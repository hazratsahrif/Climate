package com.lexus.ISClimate.trial

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.lexus.Climate.MainActivity
import com.lexus.Climate.R
import com.lexus.Climate.databinding.FragmentTrailBinding
import com.lexus.ISClimate.viewmodel.MyViewModel
import io.paperdb.Paper
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class TrailFragment : AppCompatActivity() {
    private lateinit var binding:FragmentTrailBinding
    private lateinit var viewModel: MyViewModel

    private var id:String = ""
    private lateinit var serialKey: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentTrailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[MyViewModel::class.java]
//        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
//        supportActionBar?.hide()
        Paper.init(this)
        getAndroidId(this)
        generateHashKey()
        setClick()
//        viewModel.setAccessGrantedValue(false)
    }
    private fun setClick() {
        binding.btnSumbit.setOnClickListener {
            binding.btnSumbit.visibility = View.GONE
            binding.progressBar.visibility = View.VISIBLE
            var enterKey: String = binding.etKey.text.toString()
            if (enterKey == null){
                binding.etKey.error = "Please enter your valid key!"
                binding.btnSumbit.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
            }
            else{

                 if (serialKey == enterKey){
                     Toast.makeText(
                         this,
                         "Access Granted",
                         Toast.LENGTH_SHORT
                     ).show()
                     viewModel.setAccessGrantedValue(true)
                     startActivity(Intent(this,MainActivity::class.java))
                     finish()
                 }
                else{
                    Toast.makeText(this,"Invalid serial key",Toast.LENGTH_SHORT).show()
                     binding.btnSumbit.visibility = View.VISIBLE
                     binding.progressBar.visibility = View.GONE
                }
            }
        }
        binding.btnCopy.setOnClickListener {
            copyToClipboard(binding.tvDeviceId.text.toString())
        }
    }

    private fun generateHashKey():String {
    val mdEnc: MessageDigest?
    return try {
        var salt: String = "L3ivXu5ANd"
        Log.d("TAG_ID", "SALT $salt")
        var hashCode = salt + id
        mdEnc = MessageDigest.getInstance("MD5")
        serialKey = BigInteger(1, mdEnc.digest(hashCode.toByteArray())).toString(16).padStart(32, '0')
        Log.d("TAG", "MD5 $serialKey")
        serialKey     // convert from base16 to base64 and remove the new line character
    } catch (e: NoSuchAlgorithmException) {
        e.printStackTrace()
        "null"
    }
}


private fun getAndroidId(context: Context): String {
    id =  Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    binding.tvDeviceId.text = id

    return id
}


    private fun copyToClipboard(text: String) {
        val clipboardManager: ClipboardManager =
            getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("Copied Text", text)
        clipboardManager.setPrimaryClip(clipData)
         Toast.makeText(this, "Text copied", Toast.LENGTH_SHORT).show()
    }



}


