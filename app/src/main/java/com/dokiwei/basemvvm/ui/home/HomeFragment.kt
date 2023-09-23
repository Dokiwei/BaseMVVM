package com.dokiwei.basemvvm.ui.home

import android.os.Bundle
import android.view.KeyEvent
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.view.updatePadding
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import com.dokiwei.basemvvm.base.BaseFragment
import com.dokiwei.basemvvm.databinding.FragmentHomeBinding
import com.dokiwei.basemvvm.ui.home.adapter.HomePagerAdapter
import com.dokiwei.basemvvm.util.Constants
import com.dokiwei.basemvvm.util.MyCoroutineExceptionHandler
import com.google.android.material.search.SearchView
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


/**
 * @author DokiWei
 * @date 2023/9/10 20:00
 */
class HomeFragment : BaseFragment<FragmentHomeBinding, ViewModel>(
    inflater = FragmentHomeBinding::inflate, viewModelClass = null, publicViewModelTag = true
) {
    override fun initFragment(
        binding: FragmentHomeBinding, viewModel: ViewModel?, savedInstanceState: Bundle?
    ) {
        initStatusPadding{binding.appBar.updatePadding(top=it)}

        initSearch(binding)

        initViewPagerAndBindTabLayout(binding)
    }


    /**
     * 设置viewPager2适配器
     *
     * @param binding 获取布局
     */
    private fun initViewPagerAndBindTabLayout(binding: FragmentHomeBinding) {
        binding.viewPager.adapter = HomePagerAdapter(childFragmentManager, lifecycle)
        TabLayoutMediator(binding.topTab, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> Constants.HomeViewPage.Home.title
                1 -> Constants.HomeViewPage.Square.title
                2 -> Constants.HomeViewPage.Qa.title
                else -> ""
            }
        }.attach()
    }

    /**
     * 初始化搜索功能
     *
     * @param binding
     */
    private fun initSearch(binding: FragmentHomeBinding) {
        binding.appSearchView.apply {
            //当appSearchView打开时按返回键退出appSearchView而不是退出程序
            val onBackPressedCallback: OnBackPressedCallback =
                object : OnBackPressedCallback(false) {
                    override fun handleOnBackPressed() {
                        binding.appSearchView.hide()
                    }
                }
            //获取fragmentActivity,并将返回事件提交给他
            requireActivity().onBackPressedDispatcher.addCallback(onBackPressedCallback)
            //通过editText变化时的事件监听搜索
            editText.setOnEditorActionListener { v, actionId, event ->
                //如果event != null那么说明用户点击了软键盘的回车(搜索)
                //返回参数为是否完成任务的提交
                if (event != null) {
                    v?.text.let { binding.appSearchBar.hint = it }
                    binding.appSearchView.hide()
                    true
                } else false
            }
            addTransitionListener { searchView, previousState, newState ->
                onBackPressedCallback.isEnabled = newState == SearchView.TransitionState.SHOWN
            }
        }
    }

}