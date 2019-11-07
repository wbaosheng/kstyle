package reform.ko.log

import android.util.Log

object Logk {

    private fun threadTag(): String {
        val name = Thread.currentThread().name
        return "[%-8s] ".format(name)
    }

    fun v(tag: String, msg: String) {
        Log.v(tag, threadTag() + msg)
    }

    fun d(tag: String, msg: String) {
        Log.d(tag, threadTag() + msg)
    }

    fun i(tag: String, msg: String) {
        Log.i(tag, threadTag() + msg)
    }

    fun w(tag: String, msg: String) {
        Log.w(tag, threadTag() + msg)
    }

    fun e(tag: String, msg: String) {
        Log.e(tag, threadTag() + msg)
    }
}