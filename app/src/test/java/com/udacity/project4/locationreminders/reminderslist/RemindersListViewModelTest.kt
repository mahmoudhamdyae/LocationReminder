package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

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
    private lateinit var reminderListViewModel: RemindersListViewModel

    // Use a fake repository to be injected into the view model.
    private lateinit var remindersRepository: FakeDataSource

    @Before
    fun setupRemindersListViewModel() {
        // Initialise the repository with no reminders.
        remindersRepository = FakeDataSource()

        reminderListViewModel = RemindersListViewModel(
            ApplicationProvider.getApplicationContext(), remindersRepository)
    }

    @Test
    fun shouldReturnError() {
        // Make the repository return errors.
        remindersRepository.setReturnError(true)

        reminderListViewModel.loadReminders()

        assertThat(reminderListViewModel.showNoData.getOrAwaitValue(), `is`(true))
        assertThat(reminderListViewModel.showSnackBar.getOrAwaitValue(), `is`("Test exception"))
    }

    @Test
    fun check_loading() {
        // Pause dispatcher so you can verify initial values.
        mainCoroutineRule.pauseDispatcher()

        // Load the reminders in the view model.
        reminderListViewModel.loadReminders()

        // Then progress indicator is shown.
        assertThat(reminderListViewModel.showLoading.getOrAwaitValue(), `is`(true))

        // Execute pending coroutines actions.
        mainCoroutineRule.resumeDispatcher()

        // Then progress indicator is hidden.
        assertThat(reminderListViewModel.showLoading.getOrAwaitValue(), `is`(false))
    }
}