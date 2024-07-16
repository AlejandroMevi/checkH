package com.venturessoft.human.main.ui.fragments.listEmployees

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.venturessoft.human.core.ViewHolderGeneral
import com.venturessoft.human.core.utils.Utilities
import com.venturessoft.human.databinding.ItemEmployeeBinding
import com.venturessoft.human.login.data.models.UserEntity

class ListEmployeesAdapter(
    private val item: List<UserEntity>,
) : RecyclerView.Adapter<ViewHolderGeneral<*>>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderGeneral<*> {
        val itemBinding =
            ItemEmployeeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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
    private inner class ViewHolder(
        val binding: ItemEmployeeBinding,
        val context: Context
    ) : ViewHolderGeneral<UserEntity>(binding.root) {
        override fun bind(item: UserEntity) {
            binding.tvName.text = item.name
            if (!item.palabraClave.isNullOrEmpty()){
                binding.materialCardView3.isVisible = true
                binding.keyword.text = item.palabraClave
            }
            try {
                val profileUri = Utilities.uriToBase64(Uri.parse(item.photoUrl))
                val bitmap: Bitmap = Utilities.base64ToBitmap(profileUri)
                Glide.with(context).load(bitmap)
                    .diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true)
                    .into(binding.imgEmployee)
            } catch (_: Exception) { }
        }
    }
}