package com.dokiwei.basemvvm.ui.home

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
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
class HomeRVFragment() :
    BaseFragment<FragmentHomeRvBinding, HomeRVViewModel>(
        inflater = FragmentHomeRvBinding::inflate, viewModelClass = HomeRVViewModel::class.java
    ) {

    private val tag = "HomeRVFragment"
    override fun initFragment(
        binding: FragmentHomeRvBinding, viewModel: HomeRVViewModel?, savedInstanceState: Bundle?
    ) {
        val adapter = HomeAdapter()
        val flag = when (arguments?.getInt("flag")) {
            0 -> Constants.HomeApiMethod.Home
            1 -> Constants.HomeApiMethod.Square
            else -> Constants.HomeApiMethod.Qa
        }
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
        binding.swipeRefresh.setOnRefreshListener {
            adapter.refresh()
        }
        lifecycleScope.launch {
            adapter.loadStateFlow.collect {
                when (it.refresh) {
                    is LoadState.NotLoading -> {
                        binding.swipeRefresh.isRefreshing = false
                    }

                    is LoadState.Loading -> {
                        binding.swipeRefresh.isRefreshing = true
                    }

                    is LoadState.Error -> {
                        Log.e(tag, (it.refresh as LoadState.Error).toString())
                        binding.swipeRefresh.isRefreshing = false
                    }
                }
            }
        }
    }
}