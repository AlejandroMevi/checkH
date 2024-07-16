package com.venturessoft.human.main.ui.fragments.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.venturessoft.human.core.BaseActivity.Companion.dataBSSIDConected
import com.venturessoft.human.core.BaseActivity.Companion.dataBSSIDList
import com.venturessoft.human.databinding.FragmentSettingsWifiBinding

class SettingsWifiFragment : Fragment() {

    private lateinit var binding: FragmentSettingsWifiBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSettingsWifiBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        dataBSSIDList.observe(viewLifecycleOwner){ bssid->
            dataBSSIDConected.observe(viewLifecycleOwner){wifi->
                if (wifi?.bssid.isNullOrEmpty()){
                    binding.cvWifi.isVisible = false
                }else{
                    binding.cvWifi.isVisible = true
                    binding.tvWifi.text = wifi?.name
                    binding.tvBssid.text = wifi?.bssid
                }
                binding.clEmpty.isVisible = !(!bssid.isNullOrEmpty() || !wifi?.bssid.isNullOrEmpty())
            }
            if (!bssid.isNullOrEmpty()){
                binding.rvWifi.adapter = SettingsWifiAdapter(bssid)
            }else{
                binding.rvWifi.adapter = SettingsWifiAdapter(arrayListOf())
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        dataBSSIDList.removeObservers(this)
        dataBSSIDConected.removeObservers(this)
    }
}