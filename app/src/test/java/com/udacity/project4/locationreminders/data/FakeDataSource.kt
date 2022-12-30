package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.dto.Result.Success
import com.udacity.project4.locationreminders.data.dto.Result.Error

// Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(
    private var reminders: MutableList<ReminderDTO>? = mutableListOf()
) : ReminderDataSource {

    private var shouldReturnError = false

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (shouldReturnError) {
            return Error("Test exception")
        }
        reminders?.let { return Success(ArrayList(it)) }
        return Error("Reminders not found")
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (shouldReturnError) {
            return Error("Test exception")
        }
        reminders?.firstOrNull { it.id == id }?.let { return Success(it)}
        return Error("Reminder not found")
    }

    override suspend fun deleteAllReminders() {
        reminders?.clear()
    }

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }
}