package com.venturessoft.human.core.utils

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.viewpager.widget.ViewPager
import com.venturessoft.human.R
import com.venturessoft.human.databinding.ViewGenericDialog2ButtonBinding

@SuppressLint("SetTextI18n")
class DialogGeneral(
    private val title: String? = null,
    private val message: String,
    private val textPositive: String? = null,
    private val textNegative: String? = null,
    private val actionPositive: (() -> Unit)? = null,
    private val actionNegative: (() -> Unit)? = null
) : DialogFragment() {

    private lateinit var binding: ViewGenericDialog2ButtonBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ViewGenericDialog2ButtonBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val width: Int = ViewPager.LayoutParams.MATCH_PARENT
        val height = ViewPager.LayoutParams.MATCH_PARENT
        dialog?.window?.setLayout(width, height)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.dialogTitle.text = if (!title.isNullOrEmpty()) title else getString(R.string.aviso)

        binding.dialogMessage.text = message

        binding.dialogPositiveButton.text = if (!textPositive.isNullOrEmpty()) textPositive else getString(R.string.accept)

        if (!textNegative.isNullOrEmpty()) {
            binding.dialogNegativeButton.isVisible = true
            binding.dialogNegativeButton.text = textNegative
        } else {
            binding.dialogNegativeButton.isVisible = false
        }

        binding.dialogPositiveButton.setOnClickListener {
            actionPositive?.invoke()
            dismiss()
        }

        binding.dialogNegativeButton.setOnClickListener {
            actionNegative?.invoke()
            dismiss()
        }

        binding.clBackgroung.setOnClickListener {
            dismiss()
        }
    }
}