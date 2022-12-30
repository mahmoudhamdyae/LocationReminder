package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import junit.framework.Assert.assertNull
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

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase

    @Before
    fun initDb() {
        // Using an in-memory database so that the information stored here disappears when the
        // process is killed.
        database = Room.inMemoryDatabaseBuilder(
            getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun insertTaskAndGetById() = runBlockingTest {
        // GIVEN - Insert a reminder.
        val reminderDateItem = ReminderDTO(
            "",
            "description",
            "location",
            0.0,
            0.0,
            "id")
        database.reminderDao().saveReminder(reminderDateItem)

        // WHEN - Get the reminder by id from the database.
        val loaded = database.reminderDao().getReminderById(reminderDateItem.id)

        // THEN - The loaded data contains the expected values.
        assertThat(loaded, `is`(reminderDateItem))
    }

    @Test
    fun predictErrorsMessages() = runBlockingTest {
        // GIVEN - a random reminder id
        val reminderId = UUID.randomUUID().toString()

        // WHEN - Get the reminder by id from the database.
        val loaded = database.reminderDao().getReminderById(reminderId)

        // THEN - The loaded data contains the expected values.
        assertNull(loaded)
    }
}