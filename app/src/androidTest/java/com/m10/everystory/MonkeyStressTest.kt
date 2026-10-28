package com.m10.everystory

import android.os.ParcelFileDescriptor
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Runs Android's monkey against the app from inside an instrumentation test, because Firebase Test
 * Lab only accepts Robo or instrumentation runs. A crash in the app takes this test's process down
 * with it, so Test Lab reports it as a failure with the stack trace.
 *
 * Arguments (Test Lab: `--environment-variables monkeyEvents=5000,monkeySeed=7`):
 * `monkeyEvents` (default 3000), `monkeySeed` (default 4242, the seed that found the callback crash).
 */
@RunWith(AndroidJUnit4::class)
class MonkeyStressTest {

    @Test
    fun monkeyFindsNoCrashOrAnr() {
        val arguments = InstrumentationRegistry.getArguments()
        val events = arguments.getString("monkeyEvents") ?: DEFAULT_EVENTS
        val seed = arguments.getString("monkeySeed") ?: DEFAULT_SEED
        val packageName = InstrumentationRegistry.getInstrumentation().targetContext.packageName

        // Few system keys: Home and app switches mostly test going away and coming back, which a
        // handful of presses already covers.
        val command = "monkey -p $packageName -s $seed --throttle $THROTTLE_MS " +
            "--pct-syskeys 5 --pct-appswitch 5 -v -v $events"
        val output = runShell(command)
        println(output)

        assertFalse("Monkey hit a crash:\n${output.takeLast(REPORT_CHARS)}", output.contains("// CRASH"))
        assertFalse("Monkey hit an ANR:\n${output.takeLast(REPORT_CHARS)}", output.contains("// NOT RESPONDING"))
        // "** Monkey aborted due to error." or "No activities found to run, monkey aborted."
        assertFalse(
            "Monkey stopped early:\n${output.takeLast(REPORT_CHARS)}",
            output.contains("monkey aborted", ignoreCase = true),
        )
    }

    private fun runShell(command: String): String {
        val descriptor = InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(command)
        return ParcelFileDescriptor.AutoCloseInputStream(descriptor).bufferedReader().use { it.readText() }
    }

    private companion object {
        const val DEFAULT_EVENTS = "3000"
        const val DEFAULT_SEED = "4242"
        // 200 ms between events: 3000 events take about 10 minutes, inside Test Lab's free timeout.
        const val THROTTLE_MS = 200
        // The full log is printed above; the failure message keeps only the end, where the crash is.
        const val REPORT_CHARS = 4000
    }
}
