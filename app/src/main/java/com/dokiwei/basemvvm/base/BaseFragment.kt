package com.dokiwei.basemvvm.base

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.dokiwei.basemvvm.ui.app.PublicViewModel
import com.dokiwei.basemvvm.util.Constants

/**
 * @author DokiWei
 * @date 2023/8/5 19:16
 */
abstract class BaseFragment<VB : ViewBinding, VM : ViewModel>(
    private val inflater: (LayoutInflater, ViewGroup?, Boolean) -> VB,
    private val viewModelClass: Class<VM>?,
    private val publicViewModelTag: Boolean = false
) : Fragment() {
    private val viewModel by lazy {
        val viewModelProvider =
            ViewModelProvider(this, SavedStateViewModelFactory(activity?.application, this))
        viewModelClass?.let {
            viewModelProvider[it]
        }
    }
    val publicViewModel: PublicViewModel? by lazy {
        if (publicViewModelTag) {
            ViewModelProvider(requireActivity())[PublicViewModel::class.java]
        } else {
            null
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d(Constants.LogTag.Lifecycle.tag, "${this.javaClass.simpleName}:onAttach")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(Constants.LogTag.Lifecycle.tag, "${this.javaClass.simpleName}:onCreate")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(Constants.LogTag.Lifecycle.tag, "${this.javaClass.simpleName}:onCreateView")
        val binding = inflater(inflater, container, false)
        initFragment(binding, viewModel, savedInstanceState)
        return binding.root
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d(Constants.LogTag.Lifecycle.tag, "${this.javaClass.simpleName}:onActivityCreated")
    }

    override fun onStart() {
        super.onStart()
        Log.d(Constants.LogTag.Lifecycle.tag, "${this.javaClass.simpleName}:onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d(Constants.LogTag.Lifecycle.tag, "${this.javaClass.simpleName}:onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d(Constants.LogTag.Lifecycle.tag, "${this.javaClass.simpleName}:onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d(Constants.LogTag.Lifecycle.tag, "${this.javaClass.simpleName}:onStop")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(Constants.LogTag.Lifecycle.tag, "${this.javaClass.simpleName}:onDestroyView")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(Constants.LogTag.Lifecycle.tag, "${this.javaClass.simpleName}:onDestroy")
    }

    override fun onDetach() {
        super.onDetach()
        Log.d(Constants.LogTag.Lifecycle.tag, "${this.javaClass.simpleName}:onDetach")
    }

    abstract fun initFragment(binding: VB, viewModel: VM?, savedInstanceState: Bundle?)
}