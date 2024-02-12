package com.koinmoduel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.lexus.ISClimate.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    single { MyViewModel() }


}