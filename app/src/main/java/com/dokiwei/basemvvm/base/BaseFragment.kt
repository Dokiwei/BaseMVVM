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
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.dokiwei.basemvvm.ui.app.PublicViewModel
import com.dokiwei.basemvvm.util.Constants
import com.dokiwei.basemvvm.util.MyCoroutineExceptionHandler
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * @author DokiWei
 * @date 2023/8/5 19:16
 */
abstract class BaseFragment<VB : ViewBinding, VM : ViewModel>(
    private val inflater: (LayoutInflater, ViewGroup?, Boolean) -> VB,
    private val viewModelClass: Class<VM>?,
    private val publicViewModelTag: Boolean = false
) : Fragment() {
    companion object {
        private const val TAG = "BaseFragment"
    }
    private val viewModel by lazy {
        val viewModelProvider =
            ViewModelProvider(this, SavedStateViewModelFactory(activity?.application, this))
        viewModelClass?.let {
            viewModelProvider[it]
        }
    }
    /**
     * 一个可以全局调用的共享数据的viewModel
     */
    val publicViewModel: PublicViewModel? by lazy {
        if (publicViewModelTag) {
            ViewModelProvider(requireActivity())[PublicViewModel::class.java]
        } else {
            null
        }
    }

    /**
     * 在onCreateView生命周期中初始化fragment
     */
    abstract fun initFragment(binding: VB, viewModel: VM?, savedInstanceState: Bundle?)

    /**
     * 设置appbar的paddingTop以解决状态栏下沉
     * @param setPadding 为外部的函数提供一个statusBarsHeight,以实现填充状态栏
     */
    fun initStatusPadding(setPadding:(Int)->Unit){
        lifecycleScope.launch(MyCoroutineExceptionHandler.handler) {
            publicViewModel?.statusBarsHeight?.let { flow ->
                //通过flow来监听statusBarsHeight的变化
                //因为值在activity获取,而只有view创建成功后才会对activity进行创建,所以需要用flow来获取
                flow.collectLatest {
                    //通过flow来监听statusBarsHeight的变化
                    //因为值在activity获取,而只有view创建成功后才会对activity进行创建,所以需要用flow来获取
                    setPadding(it)
                }
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d(TAG, "${this.javaClass.simpleName}:onAttach")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "${this.javaClass.simpleName}:onCreate")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "${this.javaClass.simpleName}:onCreateView")
        val binding = inflater(inflater, container, false)
        initFragment(binding, viewModel, savedInstanceState)
        return binding.root
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d(TAG, "${this.javaClass.simpleName}:onActivityCreated")
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "${this.javaClass.simpleName}:onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "${this.javaClass.simpleName}:onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "${this.javaClass.simpleName}:onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "${this.javaClass.simpleName}:onStop")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "${this.javaClass.simpleName}:onDestroyView")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "${this.javaClass.simpleName}:onDestroy")
    }

    override fun onDetach() {
        super.onDetach()
        Log.d(TAG, "${this.javaClass.simpleName}:onDetach")
    }

}