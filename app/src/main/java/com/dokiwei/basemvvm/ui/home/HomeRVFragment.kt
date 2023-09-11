package com.dokiwei.basemvvm.ui.home

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.dokiwei.basemvvm.base.BaseFragment
import com.dokiwei.basemvvm.databinding.FragmentHomeRvBinding
import com.dokiwei.basemvvm.ui.home.adapter.HomeAdapter
import com.dokiwei.basemvvm.util.Constants
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * @author DokiWei
 * @date 2023/9/12 1:11
 */
class HomeRVFragment(private val flag: Constants.HomeApiMethod) :
    BaseFragment<FragmentHomeRvBinding, HomeViewModel>(
        inflater = FragmentHomeRvBinding::inflate, viewModelClass = HomeViewModel::class.java
    ) {
    override fun initFragment(
        binding: FragmentHomeRvBinding, viewModel: HomeViewModel?, savedInstanceState: Bundle?
    ) {
        val adapter = HomeAdapter()
        lifecycleScope.launch {
            when (flag) {
                Constants.HomeApiMethod.Home -> viewModel?.data(Constants.HomeApiMethod.Home)
                    ?.collectLatest { pagingData ->
                        adapter.submitData(pagingData)
                    }

                Constants.HomeApiMethod.Square -> viewModel?.data(Constants.HomeApiMethod.Square)
                    ?.collectLatest { pagingData ->
                        adapter.submitData(pagingData)
                    }

                Constants.HomeApiMethod.Qa -> viewModel?.data(Constants.HomeApiMethod.Qa)
                    ?.collectLatest { pagingData ->
                        adapter.submitData(pagingData)
                    }
            }
        }

        binding.rvList.adapter = adapter
        binding.swipeRefresh.setOnRefreshListener { adapter.refresh() }
    }
}