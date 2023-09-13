package com.dokiwei.basemvvm.ui.app

import android.os.Bundle
import android.view.MenuItem
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.dokiwei.basemvvm.R
import com.dokiwei.basemvvm.base.BaseFragment
import com.dokiwei.basemvvm.databinding.FragmentMainNavBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * @author DokiWei
 * @date 2023/9/10 23:37
 */
class MainNavFragment : BaseFragment<FragmentMainNavBinding, ViewModel>(
    FragmentMainNavBinding::inflate,
    null
) {
    override fun initFragment(
        binding: FragmentMainNavBinding,
        viewModel: ViewModel?,
        savedInstanceState: Bundle?
    ) {
        (childFragmentManager.findFragmentById(R.id.main_nav_view) as NavHostFragment).apply {
            binding.bottomMainNav.setupWithNavController(this.navController)
        }
    }
}