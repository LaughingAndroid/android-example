package com.laughing.lib.base

import android.view.ViewGroup
import com.cf.holder.BaseHolder

/**
 *
 * @ClassName: BaseVMHolder
 * @Description:
 * @Author: Laughing
 * @CreateDate: 2022/7/9 11:35
 * @Version:
 */
abstract class HBaseVMHolder<T, VM : HBaseViewModel> @JvmOverloads constructor(parent: ViewGroup? = null, layoutId: Int) : BaseHolder<T>(parent, layoutId) {
    abstract fun createVm():VM?
    var vm: VM? = null

    override fun onContextSet() {
        super.onContextSet()
        vm = createVm()
    }
}

