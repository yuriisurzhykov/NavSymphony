package com.surzhykov.navsymphony.choreographer.common

import com.surzhykov.navsymphony.domain.Logger

private const val NAV_LOGGING_ENABLED = true
private val androidLogger = Logger.AndroidLogger
private val internalLogger = object : Logger {

    override fun e(tag: String?, msg: String) {
        if (NAV_LOGGING_ENABLED) androidLogger.e(tag, msg)
    }

    override fun e(tag: String?, msg: String, exception: Throwable) {
        if (NAV_LOGGING_ENABLED) androidLogger.e(tag, msg, exception)
    }

    override fun w(tag: String?, msg: String) {
        if (NAV_LOGGING_ENABLED) androidLogger.w(tag, msg)
    }

    override fun d(tag: String?, msg: String) {
        if (NAV_LOGGING_ENABLED) androidLogger.d(tag, msg)
    }

    override fun i(tag: String?, msg: String) {
        if (NAV_LOGGING_ENABLED) androidLogger.i(tag, msg)
    }
}

fun navigationLogger(): Logger = internalLogger