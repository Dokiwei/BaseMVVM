package com.dokiwei.basemvvm.ui.account

import android.animation.ObjectAnimator
import android.os.Bundle
import androidx.lifecycle.ViewModel
import com.dokiwei.basemvvm.base.BaseFragment
import com.dokiwei.basemvvm.databinding.FragmentAccountBinding
import com.dokiwei.basemvvm.util.randomAvatar

/**
 * @author DokiWei
 * @date 2023/9/10 23:20
 */
class AccountFragment:BaseFragment<FragmentAccountBinding, ViewModel>(
    FragmentAccountBinding::inflate,
    null
) {
    override fun initFragment(
        binding: FragmentAccountBinding,
        viewModel: ViewModel?,
        savedInstanceState: Bundle?
    ) {
        binding.avatar.setImageResource(randomAvatar())
    }
}