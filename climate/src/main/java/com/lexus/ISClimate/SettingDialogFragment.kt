package com.lexus.ISClimate

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.lexus.Climate.R
import com.lexus.Climate.databinding.FragmentSettingDialogBinding
import com.lexus.ISClimate.trial.TrailFragment
import com.lexus.ISClimate.viewmodel.MyViewModel
import io.paperdb.Paper


class SettingDialogFragment : DialogFragment() {
    private lateinit var binding: FragmentSettingDialogBinding
    private lateinit var viewModel: MyViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(MyViewModel::class.java)
        binding = FragmentSettingDialogBinding.inflate(layoutInflater)
        Paper.init(requireContext())
        viewModel.getBoolean()
        viewModel.getDegreeBoolean()
        viewModel.getAccessValue()
        viewModel.getSoundBoolean()
        observeValue()
    }

    override fun onStart() {
//        requireActivity().window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
//        val actionBar = (activity as? AppCompatActivity)?.supportActionBar
//        actionBar?.hide()
        super.onStart()
    }

    override fun onDestroy() {
//        requireActivity().window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
//        val actionBar = (activity as? AppCompatActivity)?.supportActionBar
//        actionBar?.hide()
        super.onDestroy()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        binding = FragmentSettingDialogBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(requireActivity()).get(MyViewModel::class.java)
        Paper.init(context)

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        binding.btnLeft.setOnClickListener {
            setLeftColor()
            viewModel.updateValues(5, 1)
            viewModel.updateBoolean(false,requireContext())

        }
        binding.btnRight.setOnClickListener {
            setRightColor()
            viewModel.updateValues(1,4)
            viewModel.updateBoolean(true,requireContext())
        }
        binding.btnON.setOnClickListener {
            setONColor()
            viewModel.updateSoundBoolean(true)

        }
        binding.btnOFF.setOnClickListener {
            setOFFColor()
            viewModel.updateSoundBoolean(false)

        }

        binding.btnCelcius.setOnClickListener {
            setCelsiusColor()
            viewModel.updateDegreeBoolean(true)
        }
        binding.btnFahren.setOnClickListener {
            setFahrenheitColor()
            viewModel.updateDegreeBoolean(false)
        }
        binding.btnActivate.setOnClickListener {
            val intent = Intent(requireContext(), TrailFragment::class.java)
            startActivity(intent)
        }
        return  binding.root
    }

    private fun observeValue() {
        viewModel.degreeValue.observe(this,Observer{
            if (it != null && it == true) {
                setCelsiusColor()
            } else {
                setFahrenheitColor()
            }
        })
        viewModel.booleanValue.observe(this,Observer{
            if (it != null && it == true) {
                setRightColor()
            } else {
                if(it==null){
                    setRightColor()
                }
                else{
                    setLeftColor()
                }

            }
        })
        viewModel.accessGranted.observe(this, Observer{
            if(it != null && it == true){
                binding.tvActive.visibility = View.VISIBLE
                binding.layoutActivation.visibility = View.GONE

            }
            else{
                binding.tvActive.visibility = View.GONE
                binding.layoutActivation.visibility = View.VISIBLE
            }
        })
        viewModel.isSound.observe(this, Observer{
            if(it != null && it == true){
                setONColor()
            }
            else{
                setOFFColor()
            }
        })

    }

    private fun setCelsiusColor() {
        binding.btnCelcius.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.blue))
        binding.btnFahren.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.dark_gray))
    }
    private fun setFahrenheitColor() {
        binding.btnFahren.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.blue))
        binding.btnCelcius.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.dark_gray))
    }

    private fun setRightColor() {
        binding.btnRight.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.blue))
        binding.btnLeft.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.dark_gray))
    }
    private fun setLeftColor() {
        binding.btnLeft.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.blue))
        binding.btnRight.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.dark_gray))
    }

    private fun setONColor() {
        binding.btnON.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.blue))
        binding.btnOFF.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.dark_gray))
    }

    private fun setOFFColor() {
        binding.btnOFF.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.blue))
        binding.btnON.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.dark_gray))
    }




}