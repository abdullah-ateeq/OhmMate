package com.example.ohmmate.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ohmmate.data.PreferencesManager

class OhmMateViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OhmMateViewModel::class.java)) {
            val preferencesManager = PreferencesManager(context.applicationContext)
            return OhmMateViewModel(preferencesManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
