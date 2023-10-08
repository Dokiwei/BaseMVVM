package com.dokiwei.basemvvm.ui.music.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.media3.common.util.UnstableApi
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dokiwei.basemvvm.ui.music.MusicPlayAlbumAndLyricFragment
import com.dokiwei.basemvvm.ui.music.MusicPlayLyricFragment

@UnstableApi /**
 * @author DokiWei
 * @date 2023/10/5 22:38
 */
class MusicPlayPagerAdapter(
    fragment: FragmentManager, lifecycle: Lifecycle
) : FragmentStateAdapter(fragment, lifecycle) {
    override fun getItemCount()=2

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0->MusicPlayAlbumAndLyricFragment()
            else->MusicPlayLyricFragment()
        }
    }
}