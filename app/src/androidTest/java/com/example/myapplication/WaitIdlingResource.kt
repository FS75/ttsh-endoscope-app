package com.example.myapplication

import androidx.test.espresso.IdlingResource

class WaitIdlingResource(private val waitingTime: Long) : IdlingResource {

    private var startTime: Long = 0
    private var resourceCallback: IdlingResource.ResourceCallback? = null

    override fun getName(): String {
        return WaitIdlingResource::class.java.name
    }

    override fun isIdleNow(): Boolean {
        val elapsed = System.currentTimeMillis() - startTime
        val idle = elapsed >= waitingTime

        if (idle) {
            resourceCallback?.onTransitionToIdle()
        }

        return idle
    }

    override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback?) {
        resourceCallback = callback
    }

    fun start() {
        startTime = System.currentTimeMillis()
    }
}
