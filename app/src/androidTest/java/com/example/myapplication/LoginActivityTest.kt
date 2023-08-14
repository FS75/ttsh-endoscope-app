package com.example.myapplication

import android.content.Context
import android.net.ConnectivityManager
import android.nfc.NfcAdapter
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.annotation.UiThread
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import androidx.test.annotation.UiThreadTest
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.pressImeActionButton
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import org.hamcrest.Matchers.not
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.internal.statement.UiThreadStatement
import androidx.test.rule.UiThreadTestRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.junit.*
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement


class LoginActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(LoginActivity::class.java)

    private lateinit var context: Context
    private lateinit var nfcAdapter: NfcAdapter
    private var nfcEnabled = false
    private val validEmail = "cheekangchen@gmail.com"
    private val validPassword = "P@ssw0rd"
    private val invalidEmail = "invalid@example.com"
    private val invalidPassword = "wrongpassword"

    lateinit var waitIdlingResource : WaitIdlingResource

    @Before
    fun setUp() {
        Intents.init()
        checkNfcStuff()
    }

    @Before
    fun checkNfc(){
        context = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
        try {
            nfcAdapter = NfcAdapter.getDefaultAdapter(context)
            nfcEnabled = true
        } catch (e: Exception) {
            nfcEnabled = false
        }
    }

    @Before
    fun checkNetworkConnectivity() {
        val connectivityManager = InstrumentationRegistry.getInstrumentation().context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        if (networkInfo == null || !networkInfo.isConnected) {
            // Device is not connected to any network, skip the test
            Assume.assumeTrue(false)
        } else {
            // Device is connected to a network, continue with the test
            Assume.assumeTrue(true)
        }
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun testLoginSuccess() {
        // Type the email and press the 'Next' button
        onView(withId(R.id.editEmail))
            .perform(typeText(validEmail), closeSoftKeyboard(), pressImeActionButton())

        // Type the password and press the 'Done' button
        onView(withId(R.id.editPassword))
            .perform(typeText(validPassword), closeSoftKeyboard(), pressImeActionButton())

        val waitIdlingResource = WaitIdlingResource(1000)
        IdlingRegistry.getInstance().register(waitIdlingResource)

        onView(withId(R.id.btnLogin))
            .perform(click())

        waitIdlingResource.start()

        checkNfcStuff()
        // Check if the container holding the SampleScopeFragment is displayed
        onView(withId(R.id.sample_scope_fragment)).check(matches(isDisplayed()))

        IdlingRegistry.getInstance().unregister(waitIdlingResource)
    }
    @Test
    fun testLoginFailure() {
        // Type the email and press the 'Next' button
        onView(withId(R.id.editEmail))
            .perform(typeText(invalidEmail), closeSoftKeyboard(), pressImeActionButton())

        // Type the password and press the 'Done' button
        onView(withId(R.id.editPassword))
            .perform(typeText(invalidPassword), closeSoftKeyboard(), pressImeActionButton())

        waitIdlingResource = WaitIdlingResource(1000)
        IdlingRegistry.getInstance().register(waitIdlingResource)

        onView(withId(R.id.btnLogin))
            .perform(click())

        waitIdlingResource.start()

    // Check if the container holding the SampleScopeFragment is displayed
    // If does not exist, means login failed
        onView(withId(R.id.sample_scope_fragment)).check(doesNotExist())

        IdlingRegistry.getInstance().unregister(waitIdlingResource)
    }
    fun checkNfcStuff(){
        if(nfcEnabled){
            if(!nfcAdapter.isEnabled){
                // NFC is not enabled - dismiss the NFC disabled dialog
                onView(withText("Please Enable NFC"))
                    .check(matches(isDisplayed()))
                    .perform(click())

                // Perform a click on the positive button to go to the NFC settings
                onView(withText(R.string.cancel))
                    .check(matches(isDisplayed()))
                    .perform(click())
            }
        }
    }
}