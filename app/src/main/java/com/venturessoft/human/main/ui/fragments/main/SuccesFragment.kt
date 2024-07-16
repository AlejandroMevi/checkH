package com.venturessoft.human.main.ui.fragments.main

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.venturessoft.human.R
import com.venturessoft.human.core.utils.Constants.Companion.ONLINE
import com.venturessoft.human.core.utils.Constants.Companion.STATE_OFFLINE
import com.venturessoft.human.core.utils.Constants.Companion.SUCCESS_DATA
import com.venturessoft.human.core.utils.Utilities
import com.venturessoft.human.core.utils.Utilities.Companion.getSerializable
import com.venturessoft.human.core.utils.Utilities.Companion.reverseOrderOfWords
import com.venturessoft.human.databinding.FragmentSuccesBinding
import com.venturessoft.human.main.data.models.SuccesModel
import com.venturessoft.human.main.ui.interfaces.BaseInterface
import com.venturessoft.human.main.ui.interfaces.MainInterface
class SuccesFragment : Fragment() {
    private lateinit var binding: FragmentSuccesBinding
    private var mainInterface: MainInterface? = null
    private var baseInterface: BaseInterface? = null
    private var dataSucces = SuccesModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { bundle ->
            dataSucces = getSerializable(bundle, SUCCESS_DATA, SuccesModel()::class.java)
        }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSuccesBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }
    @SuppressLint("SetTextI18n")
    private fun initView() {
        mainInterface?.showLoading(false)
        binding.tvDateName.text = Utilities.getDayOfDate(requireContext())
        binding.tvDate.text = reverseOrderOfWords(dataSucces.date)
        binding.tvTime.text = dataSucces.time
        binding.tvStation.text = if (dataSucces.station == STATE_OFFLINE) {
            "${requireContext().getString(R.string.terminal)} ${getString(R.string.datosenviar_title)}"
        } else {
            "${requireContext().getString(R.string.terminal)} ${dataSucces.station}"
        }
        val type = if (dataSucces.typeofCheck == "E") getString(R.string.checkin) else getString(R.string.checkout)
        if (dataSucces.connection == ONLINE) {
            binding.tvStatus.text = "$type ${getString(R.string.message_succesfull_checked)}"
        } else {
            binding.tvStatus.text = "${getString(R.string.mensajeoffline)} $type ${getString(R.string.mensajeoffline1)}"
            binding.tv2.setTextColor(requireContext().getColor(R.color.textColor6))
            binding.tvStatus.setTextColor(requireContext().getColor(R.color.textColor6))
            binding.materialCardView2.backgroundTintList = requireContext().getColorStateList(R.color.viewColor8)
        }
        val bitmap: Bitmap = Utilities.base64ToBitmap(dataSucces.photoEmployee)
        try {
            Glide.with(this)
                .load(bitmap)
                .diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true)
                .into(binding.ivEmployee)
        } catch (_: Exception) {
        }
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MainInterface) {
            mainInterface = context
        }
        if (context is BaseInterface) {
            baseInterface = context
        }
    }
    override fun onDetach() {
        super.onDetach()
        mainInterface = null
        baseInterface = null
    }
}