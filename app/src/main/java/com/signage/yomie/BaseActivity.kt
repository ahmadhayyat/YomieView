package com.signage.yomie

import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {
    abstract fun initViews()
    abstract fun initVariables()
    abstract fun setUpClicks()
}