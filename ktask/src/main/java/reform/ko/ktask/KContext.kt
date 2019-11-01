package reform.ko.ktask

fun withContext(dispatcher: Dispatcher, block: () -> Unit) {
    dispatcher.dispatch(Runnable { block() })
}