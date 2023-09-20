package com.dokiwei.basemvvm.ui.home

import android.os.Bundle
import android.view.KeyEvent
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import com.dokiwei.basemvvm.base.BaseFragment
import com.dokiwei.basemvvm.databinding.FragmentHomeBinding
import com.dokiwei.basemvvm.ui.home.adapter.HomePagerAdapter
import com.dokiwei.basemvvm.util.Constants
import com.dokiwei.basemvvm.util.MyCoroutineExceptionHandler
import com.google.android.material.search.SearchView
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


/**
 * @author DokiWei
 * @date 2023/9/10 20:00
 */
class HomeFragment : BaseFragment<FragmentHomeBinding, ViewModel>(
    inflater = FragmentHomeBinding::inflate, viewModelClass = null, publicViewModelTag = true
) {
    override fun initFragment(
        binding: FragmentHomeBinding, viewModel: ViewModel?, savedInstanceState: Bundle?
    ) {
        //设置appbar的paddingTop以解决状态栏下沉
        lifecycleScope.launch(MyCoroutineExceptionHandler.handler) {
            publicViewModel?.statusBarsHeight?.let { flow ->
                //通过flow来监听statusBarsHeight的变化
                //因为值在activity获取,而只有view创建成功后才会对activity进行创建,所以需要用flow来获取
                flow.collectLatest {
                    binding.appBar.setPadding(0, it, 0, 0)
                }
            }
        }

        binding.appSearchView.apply {
            //当appSearchView打开时按返回键退出appSearchView而不是退出程序
            val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(false) {
                override fun handleOnBackPressed() {
                    binding.appSearchView.hide()
                }
            }
            //获取fragmentActivity,并将返回事件提交给他
            requireActivity().onBackPressedDispatcher.addCallback(onBackPressedCallback)
            //通过editText变化时的事件监听搜索
            editText.setOnEditorActionListener { v, actionId, event ->
                //如果event != null那么说明用户点击了软键盘的回车(搜索)
                //返回参数为是否完成任务的提交
                if (event != null) {
                    v?.text.let { binding.appSearchBar.hint = it }
                    binding.appSearchView.hide()
                    true
                } else false
            }
            //通过过渡监听来识别当前searchView是否展开 如果展开则将onBackPressedCallback的事件开启
            addTransitionListener { searchView, previousState, newState ->
                onBackPressedCallback.isEnabled = newState == SearchView.TransitionState.SHOWN
            }
        }

        //设置viewPager2适配器
        //传的是childFragmentManager这很重要
        //如果传的是parentFragmentManager viewPager创建的是与本fragment同级的fragment 因为本fragment无法管理同级的fragment最终会导致异常
        binding.viewPager.adapter = HomePagerAdapter(childFragmentManager, lifecycle)
        //这是viewPager2与tabLayout绑定的方法
        TabLayoutMediator(binding.topTab, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> Constants.HomeViewPage.Home.title
                1 -> Constants.HomeViewPage.Square.title
                2 -> Constants.HomeViewPage.Qa.title
                else -> ""
            }
        }.attach()
    }

}