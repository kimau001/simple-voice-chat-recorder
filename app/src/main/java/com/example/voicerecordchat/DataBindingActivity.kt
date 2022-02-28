package com.example.voicerecordchat

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

/**
 *  BaseActivity for keeping an instance of [ViewDataBinding]
 */
abstract class DataBindingActivity<VB : ViewDataBinding> : AppCompatActivity() {

    lateinit var viewBinding: VB

    @LayoutRes
    abstract fun layoutId(): Int

    abstract fun startPage()

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutId())
    }

    override fun setContentView(layoutResID: Int) {
        viewBinding = DataBindingUtil.inflate(layoutInflater, layoutResID, null, false)
        super.setContentView(viewBinding.root)
        startPage()
    }

}