package com.udacity.project4

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get

@RunWith(AndroidJUnit4::class)
@LargeTest
// END TO END test to black box test the app
class RemindersActivityTest :
    KoinTest { // Extended Koin Test - embed auto close @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application
    // An Idling Resource that waits for Data Binding to have no pending bindings
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Rule
    @JvmField
    var activityScenarioRule = ActivityScenarioRule(RemindersActivity::class.java)

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin() // Stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        // Declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }

        // Get our real repository
        repository = get()

        // Clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    /**
     * Idling resources tell Espresso that the app is idle or busy. This is needed when operations
     * are not scheduled in the main Looper (for example when executed on a different thread).
     */
    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    /**
     * Unregister your Idling Resource so it can be garbage collected and does not leak any memory.
     */
    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    @Test
    fun createdReminder_withLocation() {
        // Launch Activity
        val scenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(scenario)

        onView(withId(R.id.addReminderFAB)).perform(click())

        // Sets & saves the location
        onView(withId(R.id.selectLocation)).perform(click())
        onView(withId(R.id.map)).perform(click())
        onView(withId(R.id.save_button)).perform(click())

        val reminderTitle = "Title!"
        val reminderDescription = "Description!"
        // Set The Title & Description
        onView(withId(R.id.reminderTitle)).perform(replaceText(reminderTitle))
        onView(withId(R.id.reminderDescription)).perform(replaceText(reminderDescription), closeSoftKeyboard())

        // Save Reminder
        onView(withId(R.id.saveReminder)).perform(click())

        onView(withText(reminderTitle)).check(matches(isDisplayed()))
        onView(withText(reminderDescription)).check(matches(isDisplayed()))

        // Close Activity
        scenario.close()
    }

    @Test
    fun lunchMainActivity_showSnakeBar() {
        // Launch Activity
        val scenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(scenario)

        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.reminderTitle)).perform(typeText("Buying"))
        onView(withId(R.id.reminderDescription)).perform(typeText("I need to buy something from this place"), closeSoftKeyboard())
        onView(withId(R.id.saveReminder)).perform(click())
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches
            (withText(appContext.getString(R.string.select_location))))

        // Close Activity
        scenario.close()
    }

    @Test
    fun lunchMainActivity_showToast() {
        // Launch Activity
        val scenario=ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(scenario)

        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.reminderTitle)).perform(typeText("Buying"))
        onView(withId(R.id.reminderDescription)).perform(typeText("I need to buy something from this place"), closeSoftKeyboard())
        onView(withId(R.id.selectLocation)).perform(click())
        onView(withId(R.id.map)).perform(click())
        onView(withId(R.id.save_button)).perform(click())
        onView(withId(R.id.saveReminder)).perform(click())
        onView(withText(appContext.getString(R.string.reminder_saved)))
            .inRoot(ToastMatcher()).check(matches(isDisplayed()))

        // Close Activity
        scenario.close()
    }

    @Test
    fun invalidReminder_TitleError() {
        // Launch Activity
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withId(R.id.addReminderFAB)).perform(click())

        // Save Reminder
        onView(withId(R.id.saveReminder)).perform(click())
        onView(withText(appContext.getString(R.string.err_enter_title))).check(matches(isDisplayed()))

        // Close Activity
        activityScenario.close()
    }

    @Test
    fun invalidReminder_LocationError() {
        // Launch Activity
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withId(R.id.addReminderFAB)).perform(click())

        // Set the Title
        onView(withId(R.id.reminderTitle)).perform(replaceText("Reminder Title"))

        // Save Reminder
        onView(withId(R.id.saveReminder)).perform(click())
        onView(withText(appContext.getString(R.string.err_select_location))).check(matches(isDisplayed()))

        // Close Activity
        activityScenario.close()
    }

    @Test
    fun emptyReminders() {
        // Launch Activity
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))

        // Close Activity
        activityScenario.close()
    }
}
