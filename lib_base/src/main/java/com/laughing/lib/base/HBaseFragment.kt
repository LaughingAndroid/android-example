package com.laughing.lib.base

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.viewbinding.ViewBinding
import com.alibaba.android.arouter.launcher.ARouter
import com.laughing.lib.base.debug.IRegisterDebugMethod
import com.laughing.lib.utils.Logs
import com.laughing.lib.utils.as2

abstract class HBaseFragment<V : HBaseViewModel, VB : ViewBinding> : Fragment(),
    Loading, IRegisterDebugMethod, ViewModelFactory<V> {
    var TAG: String = javaClass.name

    val binding: VB by lazy(mode = LazyThreadSafetyMode.NONE) {
        BindingReflex.reflexViewBinding<VB>(javaClass, layoutInflater).apply {
            if (this is ViewDataBinding) {
                lifecycleOwner = this@HBaseFragment
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ARouter.getInstance().inject(this)
        Logs.d("当前Fragment=====>" + javaClass.name)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = createViewModel()
        registerChangeLiveDataCallBack()
        initCompleted()
    }

    /**
     * 是否注册事件分发
     *
     * @return true绑定EventBus事件分发，默认不绑定，子类需要绑定的话复写此方法返回true.
     */
    open fun isRegisterEventBus(): Boolean {
        return false
    }


    /**
     * 监听拦截返回事件
     */
    open fun onBackPressed(): Boolean {
        return true
    }

    /**
     * 触发返回事件
     */
    open fun backPressed() {
        activity?.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.dispose()
    }

    private var HDialogLoadingFragment: HDialogLoadingFragment? = null

    override fun showLoading() {
        activity.as2<Loading>()?.showLoading()

    }

    override fun hideLoading() {
        activity.as2<Loading>()?.hideLoading()
    }

    fun addFragment(fragment: Fragment, containerViewId: Int) {
        if (!fragment.isAdded) {
            childFragmentManager.beginTransaction().add(containerViewId, fragment).commit()
        }
    }

    fun addFragment(fragment: Fragment, containerViewId: Int, bundle: Bundle) {
        if (!fragment.isAdded) {
            fragment.arguments = bundle
            childFragmentManager.beginTransaction().add(containerViewId, fragment).commit()
        }
    }

    fun finish() {
        requireActivity().finish()
    }

    lateinit var viewModel: V


    protected abstract fun initCompleted()

    @SuppressLint("FragmentLiveDataObserve")
    private fun registerChangeLiveDataCallBack() {
        viewModel.mShowLoading.observe(this, Observer {
            if (it) {
                showLoading()
            } else {
                hideLoading()
            }
        })
    }

    override fun registerDebugMethod(map: MutableMap<String, Any>) {

    }
}

abstract class BaseFragmentNoVM<VB : ViewBinding> : HBaseFragment<HBaseViewModel, VB>() {
    override fun createViewModel(): HBaseViewModel = HBaseViewModel()

    override fun initCompleted() {
    }
}