package com.example.myapplication

import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.nfc.NfcAdapter
import android.nfc.NfcManager
import android.util.Log
import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.ImageView
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.intent.Intents
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.*
import org.junit.Assume.assumeTrue
import java.util.regex.Matcher


class SampleScopeTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(LoginActivity::class.java)

    private lateinit var context: Context
    private lateinit var nfcAdapter: NfcAdapter
    private var nfcEnabled = false
    // Set wait timer
    private val waitIdlingResource = WaitIdlingResource(1000)

    private val validEmail = "cheekangchen@gmail.com"
    private val validPassword = "P@ssw0rd"
    private val validAssistantId = "A1534"
    private val validAssistantName = "Juleus Seah"
    private val validSerialId = "00123"
    private val validTypeName = "Scope A"
    private val validBrandName = "Logitech"
    private val validModelName = "66666"

    private val invalidAssistantId = "E123"
    private val invalidAssistantName = "James"
    private val invalidSerialId = "40000"
    private val invalidTypeName = "Scope F"
    private val invalidBrandName = "Chocolate"
    private val invalidModelName = "00000"

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
            assumeTrue(false)
        } else {
            // Device is connected to a network, continue with the test
            assumeTrue(true)
        }
    }

    @Before
    fun setUp() {
        Intents.init()
        checkNfcStuff()
    }
    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun testSampleScopeSuccess(){

        //Register Idling
        IdlingRegistry.getInstance().register(waitIdlingResource)
        //Call login functions
        login(validEmail, validPassword)

        //Timer to wait for authentication
        waitIdlingResource.start()
        //Check NFC stuff
        checkNfcStuff()
        //Check if the container holding the SampleScopeFragment is displayed
        onView(withId(R.id.sample_scope_fragment)).check(matches(isDisplayed()))
        //Unregister the resources
        IdlingRegistry.getInstance().unregister(waitIdlingResource)

        //Call assistant functions
        assistant(validAssistantId, validAssistantName)

        //Call scan scope functions
        scanScope(validSerialId, validTypeName, validBrandName, validModelName)

        //Call confirm scope
        confirmScope()

        //Call finish sample
        finishSample(validPassword)

        //Scope scan success, proceed to status frag
        onView(withId(R.id.status_fragment)).check(matches(isDisplayed()))
    }

    @Test
    fun testSampleScopeWrongScope(){

        //Register Idling
        IdlingRegistry.getInstance().register(waitIdlingResource)

        //Call login functions
        login(validEmail, validPassword)

        //Timer to wait for authentication
        waitIdlingResource.start()
        //Check NFC stuff
        checkNfcStuff()
        // Check if the container holding the SampleScopeFragment is displayed
        onView(withId(R.id.sample_scope_fragment)).check(matches(isDisplayed()))
        //Unregister the resources
        IdlingRegistry.getInstance().unregister(waitIdlingResource)

        //Call assistant functions
        assistant(validAssistantId, validAssistantName)

        //Call scan scope functions
        scanScope(invalidSerialId, invalidTypeName, invalidBrandName, invalidModelName)

        //Call confirm scope
        confirmScope()

        //Call finish sample
        finishSample(validPassword)

        //Scope scan failed hence route to sample scope frag
        onView(withId(R.id.sample_scope_fragment)).check(matches(isDisplayed()))

    }

    @Test
    fun testSampleScopeWrongAssistant(){

        //Register Idling
        IdlingRegistry.getInstance().register(waitIdlingResource)

        //Call login functions
        login(validEmail, validPassword)

        //Timer to wait for authentication
        waitIdlingResource.start()
        //Check NFC stuff
        checkNfcStuff()
        // Check if the container holding the SampleScopeFragment is displayed
        onView(withId(R.id.sample_scope_fragment)).check(matches(isDisplayed()))
        //Unregister the resources
        IdlingRegistry.getInstance().unregister(waitIdlingResource)

        //Call assistant functions
        assistant(invalidAssistantId, invalidAssistantName)

    }

    fun login(validEmail: String, validPassword: String){
        // Type the email and press the 'Next' button
        onView(withId(R.id.editEmail))
            .perform(typeText(validEmail), closeSoftKeyboard(), pressImeActionButton())

        // Type the password and press the 'Done' button
        onView(withId(R.id.editPassword))
            .perform(typeText(validPassword), closeSoftKeyboard(), pressImeActionButton())

        onView(withId(R.id.btnLogin))
            .perform(click())
    }

    fun assistant(assistantId: String, assistantName: String){

        // Type the assistant ID and press the 'Next' button
        onView(withId(R.id.assistantidEditText))
            .perform(typeText(assistantId), closeSoftKeyboard(), pressImeActionButton())

        // Type the assistant Name and press the 'Done' button
        onView(withId(R.id.assistantnameEditText))
            .perform(typeText(assistantName), closeSoftKeyboard(), pressImeActionButton())

        onView(withId(R.id.loginBtn))
            .perform(click())
    }

    fun scanScope(serialId: String, typeName: String, brandName: String, modelName: String){

        //Check if assistant details keyed in and go to next page
        onView(withId(R.id.scope_scan_fragment)).check(matches(isDisplayed()))

        onView(withId(R.id.manualBtn))
            .perform(click())

        // Type the assistant ID and press the 'Next' button
        onView(withId(R.id.serialEditText))
            .perform(typeText(serialId), closeSoftKeyboard(), pressImeActionButton())

        // Type the assistant Name and press the 'Done' button
        onView(withId(R.id.typeEditText))
            .perform(typeText(typeName), closeSoftKeyboard(), pressImeActionButton())

        // Type the assistant ID and press the 'Next' button
        onView(withId(R.id.brandEditText))
            .perform(typeText(brandName), closeSoftKeyboard(), pressImeActionButton())

        // Type the assistant Name and press the 'Done' button
        onView(withId(R.id.modelEditText))
            .perform(typeText(modelName), closeSoftKeyboard(), pressImeActionButton())

        onView(withId(R.id.proceedBtn))
            .perform(click())
    }

    fun finishSample(password: String){

        onView(withId(R.id.pwBtn))
            .perform(click())

        onView(withId(R.id.pwEditText))
            .perform(typeText(password), closeSoftKeyboard(), pressImeActionButton())

        onView(withId(R.id.submitBtn))
            .perform(click())
    }

    fun confirmScope(){

        //Check if scope details keyed in and go to next page, click on submit button after
        onView(withId(R.id.confirmation_fragment)).check(matches(isDisplayed()))

        //Scroll down and click
        onView(withId(R.id.submitBtn)).perform(scrollTo(), click())

        //Check if page is finish sample
        onView(withId(R.id.finish_sample_fragment)).check(matches(isDisplayed()))
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
