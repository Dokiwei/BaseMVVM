package com.dokiwei.basemvvm.ui.home

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.core.view.updatePadding
import androidx.lifecycle.ViewModel
import com.dokiwei.basemvvm.base.BaseFragment
import com.dokiwei.basemvvm.databinding.FragmentHomeBinding
import com.dokiwei.basemvvm.ui.home.adapter.HomePagerAdapter
import com.dokiwei.basemvvm.util.Constants
import com.google.android.material.search.SearchView
import com.google.android.material.tabs.TabLayoutMediator


/**
 * @author DokiWei
 * @date 2023/9/10 20:00
 */
class HomeFragment : BaseFragment<FragmentHomeBinding, ViewModel>(
    inflater = FragmentHomeBinding::inflate, viewModelClass = null, publicViewModelTag = true
) {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var onBackPressedCallback: OnBackPressedCallback

    override fun initFragment(
        binding: FragmentHomeBinding, viewModel: ViewModel?, savedInstanceState: Bundle?
    ) {
        this.binding=binding
        initStatusPadding(Constants.SystemHeightType.StatusBars){binding.appBar.updatePadding(top=it)}

        initSearch()

        initViewPagerAndBindTabLayout()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.root.removeAllViews()
        onBackPressedCallback.remove()
    }

    /**
     * 设置viewPager2适配器
     */
    private fun initViewPagerAndBindTabLayout() {
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
     */
    private fun initSearch() {
        binding.appSearchView.apply {
            onBackPressedCallback=
                object : OnBackPressedCallback(false) {
                    override fun handleOnBackPressed() {
                        binding.appSearchView.hide()
                    }
                }
            requireActivity().onBackPressedDispatcher.addCallback(onBackPressedCallback)
            editText.setOnEditorActionListener { v, actionId, event ->
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