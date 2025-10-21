package com.surzhykov.navsymphony.domain


interface Logger {
    fun e(tag: String?, msg: String)
    fun e(tag: String?, msg: String, exception: Throwable)
    fun w(tag: String?, msg: String)
    fun d(tag: String?, msg: String)
    fun i(tag: String?, msg: String)

    object AndroidLogger : Logger {
        override fun e(tag: String?, msg: String) {
            android.util.Log.e(tag, msg)
        }

        override fun e(tag: String?, msg: String, exception: Throwable) {
            android.util.Log.e(tag, msg, exception)
        }

        override fun w(tag: String?, msg: String) {
            android.util.Log.w(tag, msg)
        }

        override fun d(tag: String?, msg: String) {
            android.util.Log.d(tag, msg)
        }

        override fun i(tag: String?, msg: String) {
            android.util.Log.i(tag, msg)
        }
    }

    class Console : Logger {
        override fun e(tag: String?, msg: String) {
            println("ERROR: $msg")
        }

        override fun e(tag: String?, msg: String, exception: Throwable) {
            println("ERROR: $msg")
        }

        override fun w(tag: String?, msg: String) {
            println("WARNING: $msg")
        }

        override fun d(tag: String?, msg: String) {
            println("DEBUG: $msg")
        }

        override fun i(tag: String?, msg: String) {
            println("INFO: $msg")
        }
    }
}