package com.venturessoft.human.core

import android.view.View
import androidx.recyclerview.widget.RecyclerView

abstract class ViewHolderGeneral<T>(itemView: View):RecyclerView.ViewHolder(itemView) {
    abstract fun bind(item: T)
}