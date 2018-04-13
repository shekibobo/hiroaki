package me.jorgecastillo.hiroaki.internal

import android.support.test.InstrumentationRegistry
import me.jorgecastillo.hiroaki.dispatcher.DispatcherRetainer
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.rules.ExternalResource

/**
 * JUnit4 Rule to provide the mock server before and after the test execution. This is the Android version which also
 * sets the android Context up into the library so it can easily reach asset resources for json body files.
 */
class AndroidMockServerRule : ExternalResource() {

    lateinit var server: MockWebServer

    @Before
    override fun before() {
        server = MockWebServer()
        val androidContext = InstrumentationRegistry.getContext()
        DispatcherRetainer.fileContentAsStringOverride = { fileName ->
            androidContext.resources.assets.open(fileName).bufferedReader().use { it.readText() }
        }
        DispatcherRetainer.registerRetainer()
        DispatcherRetainer.resetDispatchers()
        server.start()
    }

    override fun after() {
        server.shutdown()
        DispatcherRetainer.resetDispatchers()
        server.setDispatcher(DispatcherRetainer.queueDispatcher)
    }
}
