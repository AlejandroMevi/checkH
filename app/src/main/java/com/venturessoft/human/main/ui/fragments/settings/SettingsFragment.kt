package com.venturessoft.human.main.ui.fragments.settings

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.venturessoft.human.R
import com.venturessoft.human.core.DataUser
import com.venturessoft.human.core.utils.Constants.Companion.LENGUAGE
import com.venturessoft.human.core.utils.Preferences
import com.venturessoft.human.databinding.FragmentSettings2Binding
import com.venturessoft.human.main.ui.activitys.PrincipalActivity.Companion.statusNet
import com.venturessoft.human.main.ui.interfaces.MainInterface
import com.venturessoft.human.main.ui.vm.MainVM
import com.venturessoft.human.pictureLocal.LocalPictureActivity
import com.venturessoft.human.splash.data.Lenguage

class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettings2Binding
    private var lenguage = Lenguage()
    private var mainInterface: MainInterface? = null
    private val preferences = Preferences()
    private val mainVM: MainVM by activityViewModels()
    private var lenguages = mutableListOf<String>()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSettings2Binding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lenguages = mutableListOf(getString(R.string.settings_spanish), getString(R.string.settings_english), getString(R.string.settings_portuguese))
        eventOnClick()
    }
    override fun onResume() {
        super.onResume()
        mainInterface?.showImageToolbar(false)
        mainInterface?.stopSpeach()
        loadLenguagePeferences()
        loadSpinner()
        binding.etKeyWord.setText(DataUser.userData.palabraClave)
    }
    private fun loadLenguagePeferences() {
        val lenguagePreferences = preferences.getLanguage(requireContext())
        if (lenguagePreferences != null){
            lenguage.idioma = lenguagePreferences.idioma
            binding.acLenguage.setText(lenguages[lenguagePreferences.lenguagePosition],false)
            if (statusNet.value == false){
                binding.swAsistent.isChecked = false
            }else{
                binding.swAsistent.isChecked = lenguagePreferences.voiceAssistant
            }
        }
    }
    private fun loadSpinner() {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, lenguages)
        binding.acLenguage.setAdapter(adapter)
        binding.acLenguage.setOnItemClickListener { _, _, position, _ ->
            lenguage.lenguagePosition = position
            when (position) {
                0 -> lenguage.idioma = "es"
                1 -> lenguage.idioma = "en"
                2 -> lenguage.idioma = "pt"
                3 -> lenguage.idioma = "fr"
            }
        }
    }
    private fun stopObserverService() {
        mainVM.statusData.value = null
        mainVM.statusData.removeObservers(this)
        mainInterface?.showLoading(false)
    }
    @SuppressLint("SetTextI18n")
    private fun eventOnClick(){
        binding.btnLocalPicture.isVisible = DataUser.userData.fotoLocal

        val dialogKeyWord = KeyWordDialog(mainVM,mainInterface,binding.etKeyWord)

        binding.btnEmployees.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment2_to_listEmployeesFragment)
        }
        binding.acBssid.setOnClickListener{
            findNavController().navigate(R.id.action_settingsFragment2_to_settingsWifi)
        }
        binding.btnSave.setOnClickListener {
            saveData()
        }
        binding.etKeyWord.setOnClickListener {
            dialogKeyWord.show(childFragmentManager,"dialog")
        }
        binding.btnLocalPicture.setOnClickListener {
            val intent = Intent(requireActivity(),LocalPictureActivity::class.java)
            startActivity(intent)
        }
        binding.swAsistent.setOnCheckedChangeListener { compoundButton, b ->
            if (compoundButton.isChecked){
                if(statusNet.value == false){
                    Toast.makeText(requireContext(),getString(R.string.assistant_disable), Toast.LENGTH_SHORT).show()
                    binding.swAsistent.isChecked = false
                }
            }
        }
    }
    private fun saveData(){
        lenguage.voiceAssistant = binding.swAsistent.isChecked
        val bundle = Bundle()
        bundle.putSerializable(LENGUAGE,lenguage)
        findNavController().navigate(R.id.action_settingsFragment2_to_settingsAnimationFragment,bundle)
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
    override fun onPause() {
        stopObserverService()
        super.onPause()
    }
}