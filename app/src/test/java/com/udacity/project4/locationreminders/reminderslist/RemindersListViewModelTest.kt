package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
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

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    // Set the main coroutines dispatcher for unit testing.
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // Subject under test
    private lateinit var viewModel: RemindersListViewModel

    // Use a fake repository to be injected into the view model.
    private lateinit var repository: FakeDataSource

    @Before
    fun setupRemindersListViewModel() {
        // Initialise the repository with no reminders.
        repository = FakeDataSource()

        viewModel = RemindersListViewModel(
            ApplicationProvider.getApplicationContext(), repository)
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun t() {
        // GIVEN
        val reminder = ReminderDTO(
            "title",
            "description",
            "location",
            0.0,
            0.0)

        // WHEN

        // THEN
    }

    @Test
    fun shouldReturnError() {
        // GIVEN - Make the repository return errors.
        repository.setReturnError(true)

        // WHEN
        viewModel.loadReminders()

        // THEN
        assertThat(viewModel.showNoData.getOrAwaitValue(), `is`(true))
        assertThat(viewModel.showSnackBar.getOrAwaitValue(), `is`("Error retrieving reminders"))
    }

    @Test
    fun check_loading() {
        // GIVEN - Pause dispatcher so you can verify initial values.
        mainCoroutineRule.pauseDispatcher()

        // WHEN - Load the reminders in the view model.
        viewModel.loadReminders()

        // THEN - Progress indicator is shown.
        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(true))

        // Execute pending coroutines actions.
        mainCoroutineRule.resumeDispatcher()

        // THEN - progress indicator is hidden.
        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(false))
    }
}