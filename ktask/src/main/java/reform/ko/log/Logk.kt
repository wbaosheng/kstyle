package reform.ko.log

import android.util.Log

object Logk {

    const val debug = true

    private fun threadTag(): String {
        val name = Thread.currentThread().name
        return "[%-8s] ".format(name)
    }

    fun v(tag: String = Logk::class.java.simpleName, msg: String) {
        Log.v(tag, threadTag() + msg)
    }

    fun d(tag: String = Logk::class.java.simpleName, msg: String) {
        Log.d(tag, threadTag() + msg)
    }

    fun i(tag: String = Logk::class.java.simpleName, msg: String) {
        Log.i(tag, threadTag() + msg)
    }

    fun w(tag: String = Logk::class.java.simpleName, msg: String) {
        Log.w(tag, threadTag() + msg)
    }

    fun e(tag: String = Logk::class.java.simpleName, msg: String) {
        Log.e(tag, threadTag() + msg)
    }
}