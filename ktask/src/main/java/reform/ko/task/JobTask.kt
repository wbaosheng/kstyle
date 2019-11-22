package reform.ko.task

import reform.ko.log.Logk
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.FutureTask

/**
 * The [JobTask] class represents anything that caller want to do.
 *
 * JobTask is consist of many elements and do each element one by one. If JobTask is cancelled, all element
 * will be cancelled.
 */
public class JobTask private constructor() {
    private var element: Element<*, *>? = null

    init {
        /** initialize job properties */
        Logk.i(JobTask::class.java.simpleName, "JobTask init")
    }

    companion object {
        val Main = Dispatch.MAIN
        val Logic = Dispatch.LOGIC
        val Task = Dispatch.TASK
        val Io = Dispatch.IO
        inline fun build(block: (Builder.() -> Unit)) = Builder().apply(block).build()
    }

    class Builder {
        fun build() = JobTask()
    }

    /**
     * [next] function constructs job [Element] that running on different thread. Only constructs
     * element don't execute block code until [JobTask.start] function.
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
    fun start(param: Any? = null) = apply {
        element?.start(param)
    }
}

/**
 * The lambda expression that execute on the dispatcher.
 *
 * One job may consist of multi element linked by [JobTask.next] function, the dispatcher runs each element
 * one by one. Each element's input parameter is previous element's output result, the first
 * element's input parameter is transferred by [JobTask.start] function.
 */
internal class Element<T, R>(
        private val job: JobTask,
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

    fun start(param: Any?) {
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
            next?.start(future!!.get())
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