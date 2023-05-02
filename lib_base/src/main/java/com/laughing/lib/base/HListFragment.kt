package com.laughing.lib.base


import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.cf.holder.QuickAdapter
import com.cf.holder.list.BaseListManager
import com.cf.holder.list.ListConfig
import com.cf.holder.list.ListManager
import com.cf.holder.list.PullRefreshListener
import com.laughing.lib.base.databinding.FragmentRefreshListBinding
import com.laughing.lib.utils.log
import com.laughing.lib.utils.setIconDrawable
import com.laughing.lib.utils.setVisibility
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import kotlin.collections.MutableMap
import kotlin.collections.set


abstract class HListFragment<DL : HBaseListViewModel> :
    BaseListFragment<DL, FragmentRefreshListBinding>(),
    ListConfig<DL> {

    override fun initRefreshLayout(listManager: ListManager) {
        val refreshLayout = binding.refreshLayout
        listManager.setRefreshListener(refreshLayout.toListener())
        refreshLayout.setOnLoadMoreListener {
            listManager.loadMore()
        }
    }

    override fun setLoadMoreEnable(enable: Boolean) {
        super.setLoadMoreEnable(enable)
        binding.refreshLayout.setEnableRefresh(enable)
    }
}

abstract class BaseListFragment<DL : HBaseListViewModel, VB : ViewBinding> :
    HBaseFragment<DL, VB>(),
    ListConfig<DL> {
    lateinit var listManager: ListManager

    override fun initCompleted() {

    }

    abstract fun initRefreshLayout(listManager: ListManager)


    fun isListManagerInit(): Boolean = ::listManager.isInitialized

    override fun createLayoutManager(): RecyclerView.LayoutManager? {
        return LinearLayoutManager(this.context)
    }

    override fun createAdapter(): QuickAdapter {
        return QuickAdapter(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listManager = createListConfig().apply {
            initRefreshLayout(this)
            getAdapter().setFooterEnable(false)
        }
    }

    open fun createListConfig(): ListManager = BaseListManager(this)

    override fun getRecyclerView(): RecyclerView? {
        return binding.root.findViewById(com.lau.holder.R.id.recyclerView)
    }

    override fun getListDataLoader(): DL {
        return createViewModel()
    }

    open fun setLoadMoreEnable(enable: Boolean) {
        if (::listManager.isInitialized) {
            listManager.setLoadMoreEnable(enable)
        }
    }

    open fun setRefreshEnable(enable: Boolean) {
        if (::listManager.isInitialized) {
            listManager.setRefreshEnable(enable)
        }
    }


    fun showOrHideNoDataView(errorIcon: Int, errorMsg: String, bg: Int = 0) {
        viewModel.dataManagerLiveData.observe(this) {
            val noData = listManager.getAdapter().getRealItemCount() == 0
            "showOrHideNoDataView $noData".log()
            val noDataView = view?.findViewById<TextView>(R.id.noDataView) ?: return@observe
            noDataView.setVisibility(noData)
            noDataView.setIconDrawable(t = errorIcon)
            noDataView.setBackgroundResource(bg)
            noDataView.text = errorMsg
        }


    }

    fun onRefresh() {
        listManager.onRefresh()
    }

    fun getAdapter(): QuickAdapter {
        return listManager.getAdapter()
    }

    override fun registerDebugMethod(map: MutableMap<String, Any>) {
        super.registerDebugMethod(map)
        map["notify"] = getAdapter().notifyDataSetChanged()
        map["notify xx"] = binding.root.requestLayout()
        map["notify xxxx"] = {
            getRecyclerView()?.adapter = getAdapter()
        }
    }
}

fun SmartRefreshLayout.toListener(): PullRefreshListener {
    return object : PullRefreshListener {
        override fun setOnRefresh(onRefresh: () -> Unit) {
            setOnRefreshListener {
                onRefresh()
            }
        }

        override fun complete() {
            finishRefresh()
            finishLoadMore()
        }
    }
}
