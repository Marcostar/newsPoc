package com.m10.everystory.core.notifications

// Story asks for a reminder when something is saved; tests swap in a fake that just counts calls.
fun interface SavedReminderScheduler {
    fun schedule()
}
