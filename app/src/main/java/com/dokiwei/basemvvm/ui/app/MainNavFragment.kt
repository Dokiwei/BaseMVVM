package com.dokiwei.basemvvm.ui.app

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.dokiwei.basemvvm.R
import com.dokiwei.basemvvm.base.BaseFragment
import com.dokiwei.basemvvm.databinding.FragmentMainNavBinding

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

    private fun NavHostFragment.initControllerListener() {
        var currentFragment :Fragment? = null
        this.navController.addOnDestinationChangedListener { controller, destination, arguments ->
            val fragment = when (destination.id) {
                R.id.homeFragment -> childFragmentManager.findFragmentById(R.id.homeFragment)
                R.id.musicFragment -> childFragmentManager.findFragmentById(R.id.musicFragment)
                R.id.accountFragment -> childFragmentManager.findFragmentById(R.id.accountFragment)
                else -> null
            }
            // 如果找到了对应的fragment
            if (fragment != null) {
                // 隐藏当前显示的fragment
                currentFragment?.let {
                    childFragmentManager.beginTransaction().hide(it).commit()
                }
                // 显示目标fragment
                childFragmentManager.beginTransaction().show(fragment).commit()
                // 更新当前显示的fragment
                currentFragment = fragment
            }
        }
    }
}