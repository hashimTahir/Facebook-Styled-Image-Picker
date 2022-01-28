package com.hashim.filespickerrunner

import android.app.Application
import timber.log.Timber

class FilePickerRunnerApplication : Application() {

    val hTag = "HashimTimberTag %s"

    override fun onCreate() {
        super.onCreate()
        hInitTimber()
    }

    private fun hInitTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(
                object : Timber.DebugTree() {
                    override fun log(
                        priority: Int,
                        tag: String?,
                        message: String,
                        throwable: Throwable?
                    ) {
                        super.log(
                            priority,
                            String.format(hTag, tag),
                            message,
                            throwable
                        )
                    }
                }
            )
        }
    }
}