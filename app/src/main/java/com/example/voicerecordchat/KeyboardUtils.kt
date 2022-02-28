package com.example.voicerecordchat

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager

import java.util.HashMap

/**
 * Based on the following Stackoverflow answer:
 * http://stackoverflow.com/questions/2150078/how-to-check-visibility-of-software-keyboard-in-android
 */
internal class KeyboardUtils private constructor(val act: Activity, private var mCallback: SoftKeyboardToggleListener?) : ViewTreeObserver.OnGlobalLayoutListener {

    private val mRootView: View
    private var mScreenDensity = 1f
    private var lastChange = 0f

    override fun onGlobalLayout() {
        val r = Rect()
        //r will be populated with the coordinates of your view that area still visible.
        mRootView.getWindowVisibleDisplayFrame(r)

        val heightDiff = mRootView.rootView.height - (r.bottom - r.top)
        val dp = heightDiff / mScreenDensity
        isShowingKeyboard = dp > 200
        if (mCallback != null && dp != lastChange) {
            lastChange = dp
            mCallback!!.onToggleSoftKeyboard(dp > 200)
        }
    }

    interface SoftKeyboardToggleListener {
        fun onToggleSoftKeyboard(isVisible: Boolean)
    }

    private fun removeListener() {
        mCallback = null
        mRootView.viewTreeObserver.removeOnGlobalLayoutListener(this)
    }

    init {
        mRootView = (act.findViewById<View>(android.R.id.content) as ViewGroup).getChildAt(0)
        mRootView.viewTreeObserver.addOnGlobalLayoutListener(this)
        mScreenDensity = act.resources.displayMetrics.density
    }

    companion object {

        var isShowingKeyboard = false
            private set
        private val sListenerMap = HashMap<SoftKeyboardToggleListener, KeyboardUtils>()


        fun addKeyboardToggleListener(act: Activity, listener: SoftKeyboardToggleListener) {
            removeKeyboardToggleListener(listener)
            sListenerMap[listener] = KeyboardUtils(act, listener)
        }

        fun removeKeyboardToggleListener(listener: SoftKeyboardToggleListener) {
            if (sListenerMap.containsKey(listener)) {
                val k = sListenerMap[listener]
                k?.removeListener()
                sListenerMap.remove(listener)
            }
        }

        fun removeAllKeyboardToggleListeners() {
            for (l in sListenerMap.keys) sListenerMap[l]?.removeListener()
            sListenerMap.clear()

        }

        fun hideKeyboard(focusView: View?) {
            if (focusView != null) {
                val imm = focusView.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(focusView.windowToken, 0)
            }
        }

        fun showKeyboard(focusView: View?) {
            if (focusView != null) {
                val imm = focusView.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(focusView, 0)
            }
        }
    }
}
