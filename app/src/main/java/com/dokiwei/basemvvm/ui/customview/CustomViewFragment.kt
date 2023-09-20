package com.dokiwei.basemvvm.ui.customview

import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModel
import com.dokiwei.basemvvm.base.BaseFragment
import com.dokiwei.basemvvm.databinding.FragmentCustomViewBinding

/**
 * @author DokiWei
 * @date 2023/9/15 16:45
 */
class CustomViewFragment: BaseFragment<FragmentCustomViewBinding, ViewModel>(
    FragmentCustomViewBinding::inflate,null
) {
    override fun initFragment(
        binding: FragmentCustomViewBinding,
        viewModel: ViewModel?,
        savedInstanceState: Bundle?
    ) {
        val windowInsetsCompat = ViewCompat.getRootWindowInsets(binding.root.findViewById<FrameLayout>(android.R.id.content))
        val statusBarsHeight =
            windowInsetsCompat?.getInsets(WindowInsetsCompat.Type.statusBars())?.top
        val navigationBarsHeight =
            windowInsetsCompat?.getInsets(WindowInsetsCompat.Type.navigationBars())?.top
        Log.e("状态栏高度","$statusBarsHeight--$navigationBarsHeight")
    }
}