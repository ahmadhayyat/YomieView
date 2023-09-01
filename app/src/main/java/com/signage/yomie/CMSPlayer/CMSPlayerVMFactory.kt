package com.signage.yomie.CMSPlayer

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.signage.yomie.database.YomieViewModel

class CMSPlayerVMFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(YomieViewModel::class.java)) {
            return YomieViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown View Model Class")
    }
}