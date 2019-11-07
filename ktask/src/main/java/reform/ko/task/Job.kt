package reform.ko.task

import reform.ko.log.Logk
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.FutureTask

fun test() {
    Logk.d("ktask", "test()")
    var out = 1
    Job.build {
    }.next<String, Int>(Job.Task) {
        out++
        val a = "a"
        val b = "b"
        val c = a + b
        Logk.d("ktask", "Job->Task $c")
        1
    }.next<Int, String>(Job.Io) {
        val a = 0
        Logk.d("ktask", "Job->Io $a")
        "a"
    }.on(null)
}

/**
 * The [Job] class represents anything that caller want to do.
 *
 * Job is consist of many elements and do each element one by one. If Job is cancelled, all element
 * will be cancelled.
 */
public class Job private constructor() {
    private var element: Element<*, *>? = null

    init {
        /** initialize job properties */
        Logk.i(Job::class.java.simpleName, "Job init")
    }

    companion object {
        val Main = Dispatch.MAIN
        val Logic = Dispatch.LOGIC
        val Task = Dispatch.TASK
        val Io = Dispatch.IO
        inline fun build(block: (Builder.() -> Unit)) = Builder().apply(block).build()
    }

    class Builder {
        fun build() = Job()
    }

    /**
     * [next] function constructs job [Element] that running on different thread. Only constructs
     * element don't execute block code until [Job.on] function.
     */
    fun <T, R> next(dispatch: Dispatch, block: ((T?) -> R)) = apply {
        element = if (element == null) {
            Element(this, dispatch, block)
        } else {
            element!! + Element(this, dispatch, block)
        }
    }

    /**
     * Start execute element's block code one by one.
     */
    fun on(param: Any?) = apply {
        element?.on(param)
    }
}

/**
 * The lambda expression that execute on the dispatcher.
 *
 * One job may consist of multi element linked by [Job.next] function, the dispatcher runs each element
 * one by one. Each element's input parameter is previous element's output result, the first
 * element's input parameter is transferred by [Job.on] function.
 */
internal class Element<T, R>(
        private val job: Job,
        private val dispatch: Dispatch,
        private val block: (T?) -> R
) {

    internal var next: Element<*, *>? = null

    var future: FutureElement<T, R>? = null

    operator fun plus(element: Element<*, *>): Element<T, R> {
        next = if (next == null) {
            element
        } else {
            next!! + element
        }
        return this
    }

    fun on(param: Any?) {
        @Suppress("UNCHECKED_CAST")
        val p = param as T?
        future = FutureElement(this, ElementCallable(p, block))
        JobDispatcher.dispatch(dispatch, this)
    }

    fun doNext() {
        if (future!!.isCancelled) {
            return
        }
        try {
            next?.on(future!!.get())
        } catch (e: InterruptedException) {
            Logk.e(Element::class.java.simpleName, e.localizedMessage)
            return
        } catch (e: ExecutionException) {
            Logk.e(Element::class.java.simpleName, e.localizedMessage)
            return
        }
    }

    internal class ElementCallable<T, R>(
            private var p: T?,
            private val block: (T?) -> R
    ) : Callable<R> {
        override fun call(): R {
            return block(p)
        }
    }

    internal class FutureElement<T, R>(
            private val element: Element<T, R>,
            callable: Callable<R>
    ) : FutureTask<R>(callable) {
        override fun run() {
            super.run()
        }

        override fun done() {
            element.doNext()
        }
    }
}