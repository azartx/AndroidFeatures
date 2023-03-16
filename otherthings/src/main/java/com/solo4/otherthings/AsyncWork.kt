package com.solo4.otherthings

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AsyncWork {

    private val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
        println(throwable.message ?: "Coroutine was cancelled!")
    }

    private val ownScope = CoroutineScope(Dispatchers.Default + exceptionHandler + SupervisorJob())

    private inline fun lazyScope(crossinline block: suspend () -> Unit) = ownScope.launch(
        start = CoroutineStart.LAZY,
        block = { block.invoke() }
    )

    private inline fun scope(crossinline block: suspend () -> Unit) =
        ownScope.launch(block = { block.invoke() })

    fun printLazy(text: String) = scope {
        val job = lazyScope { println(text) }
        println("Start after creating new coroutine -> delay 2 sec")
        delay(2000)
        job.join() // awake lazy with suspension ( or job.start() )
        delay(200)
        println("End of Code")
    }

    fun supervizorJobTest() {
        CoroutineScope(Dispatchers.Default).launch {
            val childJob = launch(SupervisorJob()) {
                throw Exception("Child Exception")
            }
            delay(2000)
            println("isChildCancelled: ${childJob.isCancelled}")
            println("Parent is still alive!")
        }
    }

    fun jobTest() {
        CoroutineScope(Dispatchers.Default + exceptionHandler).launch {
            val childJob = launch(Job()) {
                throw Exception("Child Exception")
            }
            delay(2000)
            println("isChildCancelled: ${childJob.isCancelled}")
            println("Parent is still alive!")
        }
    }

    // create coroutines and sync await each coroutine
    fun launchWorksSync() {
        ownScope.launch {
            buildList {
                for (key in 1..10) {
                    add(async(start = CoroutineStart.LAZY) {
                        println("AsyncCoroutine number $key")
                        delay(1000)
                    })
                }
            }
                .forEach { it.await() }
        }
    }
}