package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    // Set the main coroutines dispatcher for unit testing.
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // Subject under test
    private lateinit var viewModel: SaveReminderViewModel

    // Use a fake repository to be injected into the view model.
    private lateinit var repository: FakeDataSource

    @Before
    fun setupRemindersListViewModel() {
        // Initialise the repository with no reminders.
        repository = FakeDataSource()

        viewModel = SaveReminderViewModel(
            ApplicationProvider.getApplicationContext(), repository)
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun givenEmptyTitle_shouldReturnError() = runBlockingTest {
        // GIVEN
        val reminder = ReminderDataItem(
            "",
            "description",
            "location",
            0.0,
            0.0)

        // THEN
        assertThat(viewModel.validateEnteredData(reminder), `is`(false))
    }

    @Test
    fun givenNullTitle_shouldReturnError() = runBlockingTest {
        // GIVEN
        val reminder = ReminderDataItem(
            null,
            "description",
            "location",
            0.0,
            0.0)

        // THEN
        assertThat(viewModel.validateEnteredData(reminder), `is`(false))
    }

    @Test
    fun givenEmptyLocation_shouldReturnError() = runBlockingTest {
        // GIVEN
        val reminder = ReminderDataItem(
            "title",
            "description",
            "",
            0.0,
            0.0)

        // THEN
        assertThat(viewModel.validateEnteredData(reminder), `is`(false))
    }

    @Test
    fun givenNullLocation_shouldReturnError() = runBlockingTest {
        // GIVEN
        val reminder = ReminderDataItem(
            "title",
            "description",
            null,
            0.0,
            0.0)

        // THEN
        assertThat(viewModel.validateEnteredData(reminder), `is`(false))
    }

    @Test
    fun givenProperItem_shouldNotReturnError() = runBlockingTest {
        // GIVEN
        val reminder = ReminderDataItem(
            "title",
            "description",
            "location",
            0.0,
            0.0)

        // THEN
        assertThat(viewModel.validateEnteredData(reminder), `is`(true))
    }

    @Test
    fun saveReminder_checkSaved() = runBlockingTest {
        // GIVEN
        val reminder = ReminderDTO(
            "title",
            "description",
            "location",
            0.0,
            0.0)
        viewModel.saveReminder(ReminderDataItem(
            reminder.title,
            reminder.description,
            reminder.location,
            reminder.latitude,
            reminder.longitude,
            reminder.id))

        // WHEN
        val saved = repository.reminders.first()

        // THEN
        assertThat(reminder, `is`(saved))
    }

    @Test
    fun check_loading() {
        // Pause dispatcher so you can verify initial values.
        mainCoroutineRule.pauseDispatcher()

        // Load the reminders in the view model.
        val reminderDateItem = ReminderDataItem(
            "title",
            "description",
            "location",
            0.0,
            0.0)
        viewModel.validateAndSaveReminder(reminderDateItem)

        // Then progress indicator is shown.
        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(true))

        // Execute pending coroutines actions.
        mainCoroutineRule.resumeDispatcher()

        // Then progress indicator is hidden.
        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(false))
    }
}