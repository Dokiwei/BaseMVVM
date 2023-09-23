package com.dokiwei.basemvvm.ui.app

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * @author DokiWei
 * @date 2023/9/10 22:15
 */
class PublicViewModel: ViewModel() {
    var statusBarsHeight = MutableStateFlow(0)
    var navigationBarsHeight = MutableStateFlow(0)
    var havePermission = MutableStateFlow(false)
}