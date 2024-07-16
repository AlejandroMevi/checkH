package com.venturessoft.human.main.ui.fragments.main

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.parseAsHtml
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.MutableLiveData
import androidx.viewpager.widget.ViewPager
import com.venturessoft.human.R
import com.venturessoft.human.databinding.DialogProgressBinding

class ProgressDialog: DialogFragment() {

    private lateinit var binding: DialogProgressBinding
    var checkType = MutableLiveData(R.string.checkin_text)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogProgressBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return binding.root
    }
    override fun onStart() {
        super.onStart()
        val width: Int = ViewPager.LayoutParams.MATCH_PARENT
        val height = ViewPager.LayoutParams.MATCH_PARENT
        dialog?.window?.setLayout(width, height)
        dialog?.setCancelable(false)
        checkType.observe(viewLifecycleOwner){
            binding.tvTipe.text =  getString(R.string.process_input,getString(it)).parseAsHtml()
        }
    }
}