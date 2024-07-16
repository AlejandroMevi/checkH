package com.venturessoft.human.main.ui.fragments.historyOffline

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.venturessoft.human.R
import com.venturessoft.human.core.ViewHolderGeneral
import com.venturessoft.human.core.utils.Constants.Companion.ERROR
import com.venturessoft.human.core.utils.Constants.Companion.INPUT
import com.venturessoft.human.core.utils.Constants.Companion.SUCCES
import com.venturessoft.human.core.utils.Utilities
import com.venturessoft.human.core.utils.Utilities.Companion.reverseOrderOfWords
import com.venturessoft.human.databinding.ItemOfflineHistoryBinding
import com.venturessoft.human.main.data.models.SuccesModel

class OfflineHistoryAdapter(
    private val item: List<SuccesModel>
) : RecyclerView.Adapter<ViewHolderGeneral<*>>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderGeneral<*> {
        val itemBinding =
            ItemOfflineHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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
    inner class ViewHolder(
        val binding: ItemOfflineHistoryBinding,
        val context: Context
    ) : ViewHolderGeneral<SuccesModel>(binding.root) {
        @SuppressLint("SetTextI18n")
        override fun bind(item: SuccesModel) {
            binding.tvId.text = item.id.toString()
            binding.tvDateTime.text = "${reverseOrderOfWords(item.date)} ${context.getString(R.string.at)} ${item.time}"
            binding.tvIdEmployee.text = "${context.getString(R.string.employee_number_splash)}: ${item.empleado}"
            if (item.typeofCheck == INPUT) {
                binding.ivType.setImageResource(R.drawable.ic_login)
            } else {
                binding.ivType.setImageResource(R.drawable.ic_logout)
            }
            when (item.statusOffline) {
                SUCCES -> {
                    binding.ivStatus.setImageResource(R.drawable.ic_succes)
                }
                ERROR -> {
                    binding.ivStatus.setImageResource(R.drawable.ic_failed)
                    binding.tvStatus.visibility = View.VISIBLE
                    binding.tvStatus.text = Utilities.textcode(item.station, context)
                }
                else -> {
                    binding.ivStatus.setImageResource(R.drawable.ic_pending)
                }
            }
        }
    }
}