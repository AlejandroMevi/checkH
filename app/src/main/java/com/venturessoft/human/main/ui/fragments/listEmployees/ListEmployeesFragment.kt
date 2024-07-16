package com.venturessoft.human.main.ui.fragments.listEmployees

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.venturessoft.human.R
import com.venturessoft.human.core.utils.Utilities
import com.venturessoft.human.databinding.FragmentListEmployeesBinding
import com.venturessoft.human.login.data.models.UserEntity
import com.venturessoft.human.main.ui.interfaces.MainInterface
import com.venturessoft.human.main.ui.vm.MainVM
import java.util.Locale

class ListEmployeesFragment : Fragment() {

    private lateinit var binding: FragmentListEmployeesBinding
    private val mainVM: MainVM by activityViewModels()
    private var listFilter: List<UserEntity> = arrayListOf()
    private var mainInterface: MainInterface? = null
    private var type = 2
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentListEmployeesBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainVM.getEmployees()
    }
    override fun onResume() {
        super.onResume()
        getData()
    }
    private fun getData(){
        mainVM.dataEmployees.observe(viewLifecycleOwner){listEmployees->
            if (listEmployees != null){
                if (listEmployees.isNotEmpty()){
                    if (listEmployees.size <= 5){
                        mainInterface?.showIconToolbar(true,2)
                        type = 2
                    }
                    if (listEmployees.size > 5){
                        mainInterface?.showIconToolbar(true,3)
                        type = 3
                    }
                    listFilter = listEmployees
                    initView()
                }else{
                    Utilities.showErrorDialog(getString(R.string.noRecords), childFragmentManager)
                }
            }
        }
    }
    private fun initView(){
        binding.rvEmployees.adapter = ListEmployeesAdapter(listFilter)
        binding.rvEmployees.setItemViewCacheSize(listFilter.size)
        binding.etSearch.doOnTextChanged { text, _, _, _ ->
            if (!text.isNullOrEmpty()){
                filter(text.toString())
            }else{
                binding.rvEmployees.adapter = ListEmployeesAdapter(listFilter)
            }
        }
    }
    private fun filter(text: String) {
        val filteredlist: ArrayList<UserEntity> = arrayListOf()
        for (item in listFilter) {
            if (item.name.lowercase().contains(text.lowercase(Locale.getDefault())) || item.palabraClave.lowercase().contains(text.lowercase(Locale.getDefault()))) {
                filteredlist.add(item)
            }
        }
        if (filteredlist.isEmpty()) {
            binding.rvEmployees.adapter = ListEmployeesAdapter(arrayListOf())
        } else {
            binding.rvEmployees.adapter = ListEmployeesAdapter(filteredlist)
        }
    }
    override fun onStop() {
        super.onStop()
        mainInterface?.showIconToolbar(false,type)
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
}