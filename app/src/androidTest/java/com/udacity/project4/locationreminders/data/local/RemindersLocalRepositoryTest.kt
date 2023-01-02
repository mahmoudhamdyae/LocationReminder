package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result.Error
import com.udacity.project4.locationreminders.data.dto.Result.Success
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.Executors

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@MediumTest
class RemindersLocalRepositoryTest {

    private lateinit var database: RemindersDatabase
    private lateinit var repository: RemindersLocalRepository

    // Executes each reminder synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initDb() {
        // Using an in-memory database so that the information stored here disappears when the
        // process is killed.
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), RemindersDatabase::class.java
        ).setTransactionExecutor(Executors.newSingleThreadExecutor()).build()
        val dao = database.reminderDao()

        repository = RemindersLocalRepository(dao, Dispatchers.Unconfined)
    }

    @After
    fun closeDataBase() = database.close()

    @Test
    fun saveReminder_retrievesReminder() = runBlocking {
        // GIVEN - A new reminder saved in the database.
        val reminder = ReminderDTO(
                "title",
                "description",
                "location",
                0.0,
                0.0)
        repository.saveReminder(reminder)

        // WHEN  - Reminder retrieved by ID.
        val result = repository.getReminder(reminder.id) as? Success

        // THEN - Same reminder is returned.
        assertThat(result, `is`(Success(reminder)))
    }

    @Test
    fun saveReminder_retrievesError(): Unit = runBlocking {
        // GIVEN - A new reminder saved in the database.
        val reminder = ReminderDTO(
            "title",
            "description",
            "location",
            0.0,
            0.0)
        repository.saveReminder(reminder)

        // WHEN  - Reminder retrieved by ID.
        val result = repository.getReminder("1") as? Error

        // THEN - Will Return Error.
        assertThat(result, `is`(Error("Reminder not found!")))
    }
}