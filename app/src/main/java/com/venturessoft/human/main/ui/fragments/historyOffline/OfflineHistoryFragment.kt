package com.venturessoft.human.main.ui.fragments.historyOffline

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.venturessoft.human.databinding.FragmentOfflineHistoryBinding
import com.venturessoft.human.main.data.models.SuccesModel
import com.venturessoft.human.main.ui.interfaces.MainInterface
import com.venturessoft.human.main.ui.vm.MainVM
import java.util.Locale

class OfflineHistoryFragment : Fragment(){

    private lateinit var binding: FragmentOfflineHistoryBinding
    private val mainVM: MainVM by activityViewModels()
    private var mainInterface: MainInterface? = null
    private var listFilter: List<SuccesModel> = arrayListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentOfflineHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }
    private fun initView(){
        mainVM.dataServicesOffline.value = null
        mainVM.getServicesOffline()
        getDataOffline()
        binding.etSearch.doOnTextChanged { text, _, _, _ ->
            if (!text.isNullOrEmpty()){
                filter(text.toString())
            }else{
                binding.rvOffline.adapter = OfflineHistoryAdapter(listFilter)
            }
        }
    }

    private fun filter(text: String) {
        val filteredlist: ArrayList<SuccesModel> = arrayListOf()
        for (item in listFilter) {
            if (item.date.lowercase().contains(text.lowercase(Locale.getDefault())) ||
                item.time.lowercase().contains(text.lowercase(Locale.getDefault())) ||
                item.empleado.lowercase().contains(text.lowercase(Locale.getDefault()))) {
                filteredlist.add(item)
            }
        }
        if (filteredlist.isEmpty()) {
            binding.rvOffline.adapter = OfflineHistoryAdapter(arrayListOf())
        } else {
            binding.rvOffline.adapter = OfflineHistoryAdapter(filteredlist)
        }
    }

    override fun onResume() {
        super.onResume()
        mainInterface?.showImageToolbar(false)
        mainInterface?.showIconToolbar(true,1)
    }
    private fun getDataOffline(){
        mainVM.dataServicesOffline.observe(viewLifecycleOwner){listSucces->
            if (listSucces != null){
                if (listSucces.isNotEmpty()){
                    binding.rvOffline.isVisible = true
                    listFilter = listSucces
                    binding.rvOffline.adapter = OfflineHistoryAdapter(listSucces)
                    binding.clEmpty.isVisible = false
                    deleteAndUndo()
                }else{
                    binding.rvOffline.isVisible = false
                    binding.clEmpty.isVisible = true
                }
            }
        }
    }
    override fun onStop() {
        super.onStop()
        mainInterface?.showIconToolbar(false,1)
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
    private fun deleteAndUndo() {
        val swipeHandler = object : SwipeToDeleteCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val id:String = (viewHolder as OfflineHistoryAdapter.ViewHolder).binding.tvId.text.toString()
                mainVM.deleteServicesOffline(id.toInt())
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(binding.rvOffline)
    }

    override fun onDestroy() {
        super.onDestroy()
        mainVM.dataServicesOffline.removeObservers(this)
    }
}