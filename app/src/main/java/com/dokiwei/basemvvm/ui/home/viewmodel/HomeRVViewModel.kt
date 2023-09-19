package com.dokiwei.basemvvm.ui.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.dokiwei.basemvvm.ui.home.paging.HomePagingSource
import com.dokiwei.basemvvm.util.Constants

/**
 * @author DokiWei
 * @date 2023/9/10 22:21
 */
class HomeRVViewModel: ViewModel() {
    fun data(flag:Constants.HomeApiMethod) = Pager(PagingConfig(pageSize = 20, initialLoadSize = 40)){
        HomePagingSource(flag)
    }.flow.cachedIn(viewModelScope)
}