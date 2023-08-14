package com.example.myapplication

import android.content.Context
import android.net.ConnectivityManager
import android.nfc.NfcAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith
import org.hamcrest.Matchers.allOf
import java.text.SimpleDateFormat
import java.util.Locale
import org.junit.Assert.assertTrue
import androidx.test.platform.app.InstrumentationRegistry
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.Matchers.`is`
import org.junit.*

@RunWith(AndroidJUnit4::class)
class ScheduleFragmentTest {
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
            Assume.assumeTrue(false)
        } else {
            // Device is connected to a network, continue with the test
            Assume.assumeTrue(true)
        }
    }
    @Before
    fun disableAnimations() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val packageName = context.packageName

        // Grant the SET_ANIMATION_SCALE permission to the test package
        InstrumentationRegistry.getInstrumentation().uiAutomation
            .executeShellCommand("pm grant $packageName android.permission.SET_ANIMATION_SCALE")

        // Disable animations
        InstrumentationRegistry.getInstrumentation().uiAutomation
            .executeShellCommand("settings put global window_animation_scale 0")
        InstrumentationRegistry.getInstrumentation().uiAutomation
            .executeShellCommand("settings put global transition_animation_scale 0")
        InstrumentationRegistry.getInstrumentation().uiAutomation
            .executeShellCommand("settings put global animator_duration_scale 0")
    }
    @Before
    fun setup() {
        scenario = ActivityScenario.launch(MainActivity::class.java)

        checkNfcStuff()
        // Navigate to the StatusFragment
        onView(withId(R.id.schedule)).perform(click())
    }


    @After
    fun enableAnimations() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val packageName = context.packageName

        // Grant the SET_ANIMATION_SCALE permission to the test package
        InstrumentationRegistry.getInstrumentation().uiAutomation
            .executeShellCommand("pm grant $packageName android.permission.SET_ANIMATION_SCALE")

        // Re-enable animations
        InstrumentationRegistry.getInstrumentation().uiAutomation
            .executeShellCommand("settings put global window_animation_scale 1")
        InstrumentationRegistry.getInstrumentation().uiAutomation
            .executeShellCommand("settings put global transition_animation_scale 1")
        InstrumentationRegistry.getInstrumentation().uiAutomation
            .executeShellCommand("settings put global animator_duration_scale 1")
    }
    @Test
    fun testScheduleFragment() {
        // Check that all the views with the specified IDs are displayed
        onView(withId(R.id.imageButtonHome)).check(matches(isDisplayed()))
        onView(withId(R.id.textViewLabel)).check(matches(isDisplayed()))
        onView(withId(R.id.textSort)).check(matches(isDisplayed()))
        onView(withId(R.id.spinnerSort)).check(matches(isDisplayed()))
        onView(withId(R.id.horizontalScrollView)).check(matches(isDisplayed()))
        onView(withId(R.id.tableRecyclerView)).check(matches(isDisplayed()))
    }
    @Test
    fun testScheduleSort(){
        //Check is displayed before getting the spinner to perform click
        onView(withId(R.id.spinnerSort)).check(matches(isDisplayed()))

        // Perform a click on the spinner to open the dropdown menu
        onView(withId(R.id.spinnerSort)).perform(click())

        // Scroll to the "Date" item and click on it
        onData(allOf(`is`(instanceOf(String::class.java)), `is`("Date")))
            .perform(click())

        //perform sorting check on recycler view
        onView(withId(R.id.tableRecyclerView)).check { view, _ ->
            val recyclerView = view as RecyclerView
            val adapter = recyclerView.adapter as ScheduleAdapter
            val itemCount = adapter.itemCount - 1 // Subtract 1 to exclude the header

            // Define the date format used in your app
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

            // Iterate through the list of dates and compare each date with the next date
            for (i in 1 until itemCount) {
                val currentItem = adapter.getItem(i)
                val nextItem = adapter.getItem(i + 1)

                if (currentItem != null && nextItem != null) {
                    // Extract the dates from the items
                    val currentDate = dateFormat.parse(currentItem.date)
                    val nextDate = dateFormat.parse(nextItem.date)

                    // Check if the current date is less than or equal to the next date
                    assertTrue("The list is not sorted in ascending order", currentDate <= nextDate)
                }
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

