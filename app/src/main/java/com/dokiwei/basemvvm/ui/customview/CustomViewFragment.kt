package com.dokiwei.basemvvm.ui.customview

import android.os.Bundle
import androidx.lifecycle.ViewModel
import com.dokiwei.basemvvm.base.BaseFragment
import com.dokiwei.basemvvm.databinding.FragmentCustomViewBinding
import com.dokiwei.basemvvm.component.Fish

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
        binding.fish.setImageDrawable(Fish())

    }

}