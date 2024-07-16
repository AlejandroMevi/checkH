package com.venturessoft.human.main.ui.fragments.settings

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.viewpager.widget.ViewPager
import com.google.android.material.textfield.TextInputEditText
import com.venturessoft.human.R
import com.venturessoft.human.core.ApiResponceStatus
import com.venturessoft.human.core.utils.Utilities
import com.venturessoft.human.databinding.DialogKeyWordBinding
import com.venturessoft.human.main.ui.activitys.PrincipalActivity.Companion.statusNet
import com.venturessoft.human.main.ui.interfaces.MainInterface
import com.venturessoft.human.main.ui.vm.MainVM

class KeyWordDialog(
    private val mainVM: MainVM,
    private val mainInterface: MainInterface?,
    private val etKeyWord: TextInputEditText
): DialogFragment() {

    private lateinit var binding: DialogKeyWordBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogKeyWordBinding.inflate(inflater, container, false)
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
        binding.dialogPositiveButton.setOnClickListener {
            if (statusNet.value == true) {
                if (binding.etKeyWord.text.toString().isNotEmpty()){
                    getStatus()
                    mainVM.funGetKeyword(binding.etKeyWord.text.toString(), binding.etKeyWord,requireContext())
                }else{
                    Toast.makeText(requireContext(),context?.getString(R.string.no_word), Toast.LENGTH_SHORT).show()
                }
            }else{
                mainInterface?.getDialogNetwork()
            }
        }
        binding.clBackgroung.setOnClickListener {
            dismiss()
        }
    }

    private fun getStatus() {
        mainVM.statusData.observe(viewLifecycleOwner) { status ->
            if(status != null){
                when (status) {
                    is ApiResponceStatus.Loading -> mainInterface?.showLoading(true)
                    is ApiResponceStatus.Success ->{
                        stopObserverService()
                        etKeyWord.text = binding.etKeyWord.text
                        dismiss()
                    }
                    is ApiResponceStatus.Error -> {
                        stopObserverService()
                        val text = Utilities.textcode(status.messageId, requireContext())
                        Utilities.showErrorDialog(text, childFragmentManager)
                    }
                }
            }
        }
    }
    private fun stopObserverService() {
        mainVM.statusData.value = null
        mainVM.statusData.removeObservers(this)
        mainInterface?.showLoading(false)
    }
}