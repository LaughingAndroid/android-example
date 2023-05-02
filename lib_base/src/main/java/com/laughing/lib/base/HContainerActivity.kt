package com.laughing.lib.base

import android.content.Intent
import android.graphics.Color
import androidx.fragment.app.Fragment
import com.alibaba.android.arouter.facade.annotation.Route
import com.gyf.immersionbar.ktx.immersionBar
import com.laughing.lib.base.databinding.ActivityContainerBinding
import com.laughing.lib.utils.Router
import com.laughing.lib.utils.addFragment
import com.laughing.lib.utils.log

@Route(path = Router.PAGE_CONTAINER)
class HContainerActivity : HBaseActivity<HBaseViewModel, ActivityContainerBinding>() {

    var fragmentPathUrl: String = ""
    override fun initCompleted() {
        fragmentPathUrl = intent.getStringExtra(Router.A_ROUTER_PATH) ?: ""
        "fragmentPathUrl $fragmentPathUrl".log()
        addFragment(fragmentPathUrl, R.id.frameLayout_container)
    }

    override fun createViewModel(): HBaseViewModel = getVm()
    override fun setImmersionBar() {
        immersionBar {
            statusBarColorInt(Color.WHITE)
            statusBarDarkFont(true)
            navigationBarDarkIcon(true)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (supportFragmentManager.fragments.size > 0) {
            val fragments: List<Fragment> = supportFragmentManager.fragments
            for (mFragment in fragments) {
                mFragment.onActivityResult(requestCode, resultCode, data)
            }
        }
    }
}