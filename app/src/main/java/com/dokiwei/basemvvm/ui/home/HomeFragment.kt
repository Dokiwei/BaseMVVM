package com.dokiwei.basemvvm.ui.home

import android.os.Bundle
import androidx.lifecycle.ViewModel
import com.dokiwei.basemvvm.base.BaseFragment
import com.dokiwei.basemvvm.databinding.FragmentHomeBinding
import com.dokiwei.basemvvm.ui.home.adapter.HomePagerAdapter
import com.google.android.material.tabs.TabLayoutMediator

/**
 * @author DokiWei
 * @date 2023/9/10 20:00
 */
class HomeFragment : BaseFragment<FragmentHomeBinding, ViewModel>(
    inflater = FragmentHomeBinding::inflate,
    viewModelClass = null,
    publicViewModelTag = false
) {
    override fun initFragment(
        binding: FragmentHomeBinding, viewModel: ViewModel?, savedInstanceState: Bundle?
    ) {
        binding.viewPager.adapter =
            HomePagerAdapter(requireActivity().supportFragmentManager, lifecycle)
        TabLayoutMediator(binding.topTab, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "热门"
                1 -> "广场"
                2 -> "问答"
                else -> ""
            }
        }.attach()
    }
}