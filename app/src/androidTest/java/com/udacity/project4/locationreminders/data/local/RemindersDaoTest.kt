package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import org.junit.Assert.assertNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class RemindersDaoTest {

    private lateinit var dao: RemindersDao
    private lateinit var database: RemindersDatabase

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initDb() {
        // Using an in-memory database so that the information stored here disappears when the
        // process is killed.
        database = Room.inMemoryDatabaseBuilder(
            getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
        dao = database.reminderDao()
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun saveReminderAndGetById() = runBlockingTest {
        // GIVEN - Insert a reminder.
        val reminderDateItem = ReminderDTO(
            "title",
            "description",
            "location",
            0.0,
            0.0)
        dao.saveReminder(reminderDateItem)

        // WHEN - Get the reminder by id from the database.
        val loaded = dao.getReminderById(reminderDateItem.id)

        // THEN - The loaded data contains the expected values.
        assertThat(loaded, `is`(reminderDateItem))
    }

    @Test
    fun saveRemindersAndGetAll() = runBlockingTest {
        // GIVEN - Insert a reminder.
        val reminder = ReminderDTO(
            "title",
            "description",
            "location",
            0.0,
            0.0)
        dao.saveReminder(reminder)

        // WHEN - Get all the reminder from the database.
        val reminders = dao.getReminders()

        // THEN - The loaded data contains the expected values.
        assertThat(reminders.contains(reminder), `is`(true))
    }

    @Test
    fun saveRemindersAndDeleteAll() = runBlockingTest {
        // GIVEN - Insert a reminder.
        val reminder = ReminderDTO(
            "title",
            "description",
            "location",
            0.0,
            0.0)
        dao.saveReminder(reminder)

        // WHEN - Get all the reminder from the database.
        dao.deleteAllReminders()
        val reminders = dao.getReminders()

        // THEN - The loaded data contains the expected values.
        assertThat(reminders.contains(reminder), `is`(false))
    }

    @Test
    fun getReminderById_returnNull() = runBlockingTest {
        // GIVEN - a random reminder id
        val reminderId = UUID.randomUUID().toString()

        // WHEN - Get the reminder by id from the database.
        val loaded = dao.getReminderById(reminderId)

        // THEN - The loaded data contains the expected values.
        assertNull(loaded)
    }
}