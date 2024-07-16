package com.venturessoft.human.main.ui.fragments.about

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.venturessoft.human.BuildConfig
import com.venturessoft.human.databinding.FragmentAboutBinding
import com.venturessoft.human.main.ui.interfaces.MainInterface

class AboutFragment : Fragment() {

    private lateinit var binding: FragmentAboutBinding
    private var mainInterface: MainInterface? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }
    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvVersion.text = BuildConfig.VERSION_NAME
        binding.tvCompilation.text = BuildConfig.VERSION_CODE.toString()
    }

    override fun onResume() {
        super.onResume()
        mainInterface?.showImageToolbar(false)
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