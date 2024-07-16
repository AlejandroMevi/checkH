package com.venturessoft.human.pictureLocal

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import com.venturessoft.human.R
import com.venturessoft.human.core.BaseApplication.Companion.activityVisible
import com.venturessoft.human.core.DataUser
import com.venturessoft.human.databinding.ActivityLocalPictureBinding
import com.venturessoft.human.pictureLocal.ui.interfaces.PictureLocalInterface
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LocalPictureActivity : AppCompatActivity(), PictureLocalInterface {

    private lateinit var binding: ActivityLocalPictureBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    companion object {
        var isLogin = false
    }
    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        isLogin = intent.getBooleanExtra("login",false)
        binding = ActivityLocalPictureBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initToolbar()
    }

    override fun onResume() {
        super.onResume()
        activityVisible = true
    }

    private fun initToolbar(){
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        setSupportActionBar(binding.toolBar)
        appBarConfiguration = AppBarConfiguration(setOf(R.id.localPictureFragment))
        val inflater = navHostFragment.navController.navInflater
        val graph = inflater.inflate(R.navigation.nav_graph_local)
        if (DataUser.userData.statusFoto.isNotEmpty()){
            if (DataUser.userData.localPictureUriActual.isNotEmpty() || DataUser.userData.localPictureUriPending.isNotEmpty()){
                graph.setStartDestination(R.id.localPicturePreviewFragment)
            }else{
                graph.setStartDestination(R.id.localPictureFragment)
            }
        } else {
            graph.setStartDestination(R.id.localPictureFragment)
        }
        val navController = navHostFragment.navController
        navController.setGraph(graph, intent.extras)
        binding.toolBar.setNavigationOnClickListener {
            backPressed()
        }
    }
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration)
    }
    override fun showLoading(isShowing: Boolean) {
        binding.loadAnimation.root.isVisible = isShowing
        if (isShowing) {
            window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }
    }
    override fun backPressed() {
        if (!navController.popBackStack()) this.finish()
    }
}