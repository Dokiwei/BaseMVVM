package com.dokiwei.basemvvm.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.dokiwei.basemvvm.ui.app.PublicViewModel

/**
 * @author DokiWei
 * @date 2023/8/5 19:16
 */
abstract class BaseFragment<VB: ViewBinding,VM:ViewModel>(
    private val inflater: (LayoutInflater,ViewGroup?,Boolean)->VB,
    private val viewModelClass:Class<VM>?,
    private val publicViewModelTag:Boolean=false
):Fragment() {
    private val viewModel by lazy {
        val viewModelProvider=ViewModelProvider(this)
        viewModelClass?.let {
            viewModelProvider[it]
        }
    }
    val publicViewModel: PublicViewModel? by lazy {
        if (publicViewModelTag){
            ViewModelProvider(requireActivity())[PublicViewModel::class.java]
        }else{
            null
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = inflater(inflater,container,false)
        initFragment(binding,viewModel,savedInstanceState)
        return binding.root
    }
    abstract fun initFragment(binding:VB,viewModel:VM?,savedInstanceState: Bundle?)
}