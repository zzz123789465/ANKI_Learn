package com.example.leitner.util

import java.time.Clock
import java.time.LocalDate

interface DateProvider {
    fun today(): LocalDate
    fun currentTimeMillis(): Long
}

class SystemDateProvider(private val clock: Clock = Clock.systemDefaultZone()) : DateProvider {
    override fun today(): LocalDate = LocalDate.now(clock)
    override fun currentTimeMillis(): Long = clock.millis()
}
