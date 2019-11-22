package reform.ko.task

import android.os.*
import reform.ko.internal.AVAILABLE_PROCESSORS
import reform.ko.log.Logk
import reform.ko.task.Worker.*
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger

/**
 * The four types of dispatcher:
 *
 * * [MAIN]: as name, run on UI thread.
 *
 * * [LOGIC]: process sequence logic on background handler thread. For example,
 * if runnable B dependent A, you can post A and B sequence.
 *
 * * [TASK]：process CPU busy job, don't confirm job's sequence.
 *
 * * [IO]: process IO blocking jobs, ex: network, disk file.
 */
public enum class Worker {
    MAIN, LOGIC, TASK, IO;
}

internal object JobDispatcher {
    private val Main: Dispatcher = Dispatcher(MAIN)

    private val Logic: Dispatcher = Dispatcher(LOGIC)

    private val Task: Dispatcher = Dispatcher(TASK)

    private val Io: Dispatcher = Dispatcher(IO)

    private fun get(worker: Worker) = when (worker) {
        MAIN -> Main
        LOGIC -> Logic
        TASK -> Task
        IO -> Io
    }

    fun <T, R> dispatch(worker: Worker, element: Element<T, R>) {
        get(worker).dispatch(element)
    }

    fun <T, R> cancel(worker: Worker, element: Element<T, R>) {
        get(worker).cancel(element)
    }
}

internal class Dispatcher(private val name: Worker) {
    private val main = DispatchHandler(Looper.getMainLooper())

    private val logic by lazy {
        val thread = HandlerThread("Lg-t")
        thread.start()
        DispatchHandler(thread.looper)
    }

    val transform: Handler by lazy {
        val thread = HandlerThread("tfm-t")
        thread.start()
        Handler(thread.looper)
    }

    private val executor: ExecutorService by lazy {
        when (name) {
            MAIN -> {
                AndroidExecutorService(main)
            }
            LOGIC -> {
                AndroidExecutorService(logic)
            }
            TASK -> {
                ThreadPoolExecutor(AVAILABLE_PROCESSORS, AVAILABLE_PROCESSORS,
                        1L, TimeUnit.SECONDS,
                        LinkedBlockingQueue<Runnable>(),
                        NameThreadFactory("Task-", Process.THREAD_PRIORITY_BACKGROUND)
                )
            }
            IO -> {
                ThreadPoolExecutor(AVAILABLE_PROCESSORS, AVAILABLE_PROCESSORS * 8,
                        1L, TimeUnit.SECONDS,
                        LinkedBlockingQueue<Runnable>(),
                        NameThreadFactory("Io-", Process.THREAD_PRIORITY_BACKGROUND)
                )
            }
        }
    }

    fun <T, R> dispatch(element: Element<T, R>) {
        executor.execute(element.future)
    }

    fun <T, R> cancel(element: Element<T, R>) {
        when(name) {
            MAIN -> {
                main.removeCallbacks(element.future)
                element.future!!.cancel(false)
            }
            LOGIC -> {
                Logk.i(msg = "cancel ${element.future}")
                logic.removeCallbacks(element.future)
                element.future!!.cancel(false)
            }
            else -> {
                element.future!!.cancel(true)
            }
        }
    }
}

internal class DispatchHandler(looper: Looper) : Handler(looper)

internal class AndroidExecutorService(private val handler: DispatchHandler) : ExecutorService {

    override fun shutdown() {
    }

    override fun <T : Any?> submit(task: Callable<T>?): Future<T> {
        val future = FutureTask(task)
        handler.post(future)
        return future
    }

    override fun <T : Any?> submit(task: Runnable?, result: T): Future<T> {
        val future = FutureTask(task, result)
        handler.post(future)
        return future
    }

    override fun submit(task: Runnable?): Future<*> {
        val future = FutureTask(task, null)
        handler.post(future)
        return future
    }

    override fun shutdownNow(): MutableList<Runnable>? {
        return null
    }

    override fun isShutdown(): Boolean {
        return false
    }

    override fun awaitTermination(timeout: Long, unit: TimeUnit?): Boolean {
        return false
    }

    override fun <T : Any?> invokeAny(tasks: MutableCollection<out Callable<T>>?): T? {
        return null
    }

    override fun <T : Any?> invokeAny(tasks: MutableCollection<out Callable<T>>?, timeout: Long, unit: TimeUnit?): T? {
        return null
    }

    override fun isTerminated(): Boolean {
        return false
    }

    override fun <T : Any?> invokeAll(tasks: MutableCollection<out Callable<T>>?): MutableList<Future<T>>? {
        return null
    }

    override fun <T : Any?> invokeAll(tasks: MutableCollection<out Callable<T>>?, timeout: Long, unit: TimeUnit?): MutableList<Future<T>>? {
        return null
    }

    override fun execute(command: Runnable?) {
        Logk.i(msg = "execute post: $command")
        handler.post(command)
    }
}

internal class NameThreadFactory(private val prefix: String, private val priority: Int) : ThreadFactory {
    private val threadNumber = AtomicInteger(1)

    override fun newThread(r: Runnable?): Thread {
        val thread = Thread(r, prefix + threadNumber.getAndIncrement())
        Process.setThreadPriority(priority)
        return thread
    }
}