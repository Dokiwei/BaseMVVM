package com.dokiwei.basemvvm.ui.account

import android.os.Bundle
import com.dokiwei.basemvvm.base.BaseFragment
import com.dokiwei.basemvvm.databinding.FragmentAccountBinding

/**
 * @author DokiWei
 * @date 2023/9/10 23:20
 */
class AccountFragment:BaseFragment<FragmentAccountBinding, AccountViewModel>(
    FragmentAccountBinding::inflate,
    AccountViewModel::class.java
) {
    override fun initFragment(
        binding: FragmentAccountBinding,
        viewModel: AccountViewModel?,
        savedInstanceState: Bundle?
    ) {
    }
}