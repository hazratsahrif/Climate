package com.koinmoduel

import com.lexus.ISClimate.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { MyViewModel() }
}