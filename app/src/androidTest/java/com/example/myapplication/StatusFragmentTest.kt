package com.example.myapplication

import android.content.Context
import android.net.ConnectivityManager
import android.nfc.NfcAdapter
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.junit.Assume.assumeTrue

@RunWith(AndroidJUnit4::class)
class StatusFragmentTest {
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)
    private lateinit var context: Context
    private lateinit var nfcAdapter: NfcAdapter
    private var nfcEnabled = false
    private lateinit var scenario: ActivityScenario<MainActivity>

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
    fun setup() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
        checkNfcStuff()
        // Navigate to the StatusFragment
        onView(withId(R.id.status)).perform(click())
    }

    @Test
    fun testStatusFragment() {
        // Check that all the views with the specified IDs are displayed
        onView(withId(R.id.scopeList)).check(matches(isDisplayed()))
        onView(withId(R.id.employeeDetails)).check(matches(isDisplayed()))
        onView(withId(R.id.displayStaffDetails)).check(matches(isDisplayed()))
        onView(withId(R.id.samplingDetails)).check(matches(isDisplayed()))
        onView(withId(R.id.displaySamplingDetails)).check(matches(isDisplayed()))
        onView(withId(R.id.scopeDetails)).check(matches(isDisplayed()))
        onView(withId(R.id.displayScopeDetailsStatus)).check(matches(isDisplayed()))
    }

    //Test if after clicking, will it change the correct id
    @Test
    fun testClickRecyclerView(){
        //Check if matches is displayed before getting the recyclerview
        onView(withId(R.id.scopeList)).check(matches(isDisplayed()))

        //Get the RecyclerView
        val recyclerView = onView(withId(R.id.scopeList))

        //Find the item with the specified scopeId and click on it
        val scopeId = "00456" //replace with the desired scopeId
        recyclerView.perform(
            RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                hasDescendant(withText(scopeId)),
                click()
            )
        )
        onView(withId(R.id.displayScopeDetailsStatus))
            .check(matches(withSerialNumber(scopeId)))
    }
    @Test
    fun testScrollRecyclerView(){
        // Replace R.id.your_recycler_view_id with the actual RecyclerView resource ID
        onView(withId(R.id.scopeList))
            .check(matches(isRecyclerViewScrollable()))
    }

    //To check for the serialNumber in updated textview matches scopeId
    fun withSerialNumber(serialNumber: String): Matcher<View> {
        return object : BoundedMatcher<View, TextView>(TextView::class.java) {
            override fun matchesSafely(textView: TextView): Boolean {
                val text = textView.text
                return text != null && text.contains("Serial: $serialNumber")
            }

            override fun describeTo(description: Description) {
                description.appendText("with serial number: $serialNumber")
            }
        }
    }

    //Check if recyclerView is scrollable
    fun isRecyclerViewScrollable(): Matcher<View> {
        return object : BoundedMatcher<View, RecyclerView>(RecyclerView::class.java) {
            override fun matchesSafely(recyclerView: RecyclerView): Boolean {
                val layoutManager = recyclerView.layoutManager ?: return false
                val lastItemPosition = layoutManager.itemCount - 1
                val lastVisibleItemPosition =
                    (layoutManager as androidx.recyclerview.widget.LinearLayoutManager).findLastVisibleItemPosition()

                // Check if the last item is visible or not, if not then the RecyclerView is scrollable
                return lastItemPosition > lastVisibleItemPosition
            }

            override fun describeTo(description: Description) {
                description.appendText("is scrollable")
            }
        }
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

