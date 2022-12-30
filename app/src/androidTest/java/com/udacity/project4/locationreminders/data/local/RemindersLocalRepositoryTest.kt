package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.MainCoroutineRule
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result.Success
import org.junit.Assert.assertNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@MediumTest
class RemindersLocalRepositoryTest {

    // Executes each reminder synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var dao: FakeReminderDao
    private lateinit var repository: RemindersLocalRepository

    @Before
    fun setup() {
        dao = FakeReminderDao()

        repository =
            RemindersLocalRepository(
                dao,
                Dispatchers.Main
            )
    }

    @Test
    fun saveReminder_retrievesReminder(): Unit = runBlocking {
        // GIVEN - A new reminder saved in the database.
        val newReminder = ReminderDTO(
                "",
                "description",
                "location",
                0.0,
                0.0,
                "id"
            )
        repository.saveReminder(newReminder)

        // WHEN  - Reminder retrieved by ID.
        val result = repository.getReminder(newReminder.id) as? Success

        // THEN - Same reminder is returned.
        assertThat(result, `is`(Success(newReminder)))
    }

    @Test
    fun saveReminder_retrievesError(): Unit = runBlocking {
        // GIVEN - A new reminder saved in the database.
        val newReminder = ReminderDTO(
            "",
            "description",
            "location",
            0.0,
            0.0,
            "id"
        )
        repository.saveReminder(newReminder)

        // WHEN  - Reminder retrieved by ID.
        dao.shouldReturnError = true
        val result = repository.getReminder(newReminder.id) as? Success

        // THEN - Same reminder is returned.
        assertNull(result)
    }
}