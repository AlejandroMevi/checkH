package com.venturessoft.human.main.ui.interfaces

import android.os.Bundle

interface MainInterface {
    fun showLoading(isShowing: Boolean)
    fun showCollaborator(bundle: Bundle)
    fun setupActionBarWithNavController(isPrincipalUser:Boolean)
    fun startServiceOffline()
    fun startSpeach()
    fun stopSpeach()
    fun getDialogNetwork()
    fun showIconToolbar(isShowing: Boolean,type:Int)
    fun showImageToolbar(isShowing: Boolean)
    fun showDialogProgress(isShowing: Boolean,checkType:String?)
}