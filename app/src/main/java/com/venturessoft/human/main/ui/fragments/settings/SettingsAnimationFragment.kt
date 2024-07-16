package com.venturessoft.human.main.ui.fragments.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.venturessoft.human.core.utils.Constants.Companion.LENGUAGE
import com.venturessoft.human.core.utils.Preferences
import com.venturessoft.human.databinding.FragmentSettingsAnimationBinding
import com.venturessoft.human.main.ui.activitys.PrincipalActivity
import com.venturessoft.human.main.ui.interfaces.MainInterface
import com.venturessoft.human.splash.data.Lenguage
import java.util.Locale

class SettingsAnimationFragment : Fragment() {

    private lateinit var binding: FragmentSettingsAnimationBinding
    private val preferences = Preferences()
    private var lenguage = Lenguage()
    private var mainInterface: MainInterface? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let{bundle ->
            lenguage = bundle.getSerializable(LENGUAGE) as Lenguage
        }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSettingsAnimationBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startAnimation()
    }
    private fun startAnimation(){
        binding.lottie.setAnimation("lottieanimation/configuracion.json")
        binding.lottie.playAnimation()
        binding.lottie.repeatMode
        Handler(Looper.getMainLooper()).postDelayed({
            reloadLaguage(lenguage)
        }, 1500)
    }
    @Suppress("DEPRECATION")
    fun reloadLaguage(lenguage: Lenguage) {
        preferences.editLenguaje(lenguage, requireContext())
        val locale = Locale(lenguage.idioma)
        Locale.setDefault(locale)
        val config = requireContext().resources.configuration
        config.setLocale(locale)
        requireContext().resources.updateConfiguration(config, requireContext().resources.displayMetrics)
        requireActivity().finish()
        val intent = Intent(requireContext(), PrincipalActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        startActivity(intent)
        requireActivity().overridePendingTransition(0, 0)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MainInterface) {
            mainInterface = context
        }
    }
    override fun onDetach() {
        super.onDetach()
        mainInterface = null
    }
}