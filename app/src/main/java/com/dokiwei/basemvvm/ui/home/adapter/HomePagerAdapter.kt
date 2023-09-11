package com.dokiwei.basemvvm.ui.home.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dokiwei.basemvvm.ui.home.HomeRVFragment
import com.dokiwei.basemvvm.util.Constants


/**
 * @author DokiWei
 * @date 2023/9/11 20:53
 */
class HomePagerAdapter(fragment: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragment, lifecycle) {
    override fun getItemCount() = 3
    override fun createFragment(position: Int): Fragment {
        return HomeRVFragment(
            when (position) {
                0 -> Constants.HomeApiMethod.Home
                1 -> Constants.HomeApiMethod.Square
                else -> Constants.HomeApiMethod.Qa
            }
        )
    }

}