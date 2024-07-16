package com.venturessoft.human.main.ui.fragments.settings

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.venturessoft.human.R
import com.venturessoft.human.core.ViewHolderGeneral
import com.venturessoft.human.databinding.WifiItemBinding
import com.venturessoft.human.main.data.models.WifiModel

class SettingsWifiAdapter(private val item: List<WifiModel>) : RecyclerView.Adapter<ViewHolderGeneral<*>>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderGeneral<*> {
        val itemBinding = WifiItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(itemBinding, parent.context)
    }
    override fun onBindViewHolder(holder: ViewHolderGeneral<*>, position: Int) {
        when (holder) {
            is ViewHolder -> holder.bind(item[position])
        }
    }
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
    override fun getItemViewType(position: Int): Int {
        return position
    }
    override fun getItemCount(): Int = item.size
    inner class ViewHolder(val binding: WifiItemBinding, val context:Context) : ViewHolderGeneral<WifiModel>(binding.root) {
        override fun bind(item: WifiModel) {
            binding.tvWifi.text = item.name
            binding.tvBssid.text = item.bssid
        }
    }
}