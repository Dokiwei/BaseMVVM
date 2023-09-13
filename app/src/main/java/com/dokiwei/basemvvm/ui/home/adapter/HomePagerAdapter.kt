package com.dokiwei.basemvvm.ui.home.adapter

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dokiwei.basemvvm.ui.home.HomeRVFragment


/**
 * @author DokiWei
 * @date 2023/9/11 20:53
 */
class HomePagerAdapter(
    fragment: FragmentManager, lifecycle: Lifecycle
) : FragmentStateAdapter(fragment, lifecycle) {
    override fun getItemCount() = 3
    override fun createFragment(position: Int): Fragment {
        val fragment = HomeRVFragment().apply {
            arguments = Bundle().apply { putInt("flag", position) }
        }
        return fragment
    }
}