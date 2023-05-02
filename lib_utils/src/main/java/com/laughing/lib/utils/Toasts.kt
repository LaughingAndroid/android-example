package com.laughing.lib.utils

import android.widget.Toast

fun showToast(msg: String?) {
    runInMain {
        if (!msg.isNullOrEmpty()) {
            Toast.makeText(application, msg, Toast.LENGTH_SHORT).show()
        }
    }
}

fun showToast(msgRes: Int) {
    runInMain {
        Toast.makeText(application, msgRes, Toast.LENGTH_SHORT).show()
    }
}


fun showLongToast(msg: String?) {
    runInMain {
        if (!msg.isNullOrEmpty()) {
            Toast.makeText(application, msg, Toast.LENGTH_LONG).show()
        }
    }
}

fun showLongToast(msgRes: Int) {
    runInMain {
        Toast.makeText(application, msgRes, Toast.LENGTH_LONG).show()
    }
}


