package com.udacity.project4.locationreminders.data.local

import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.dto.Result.Error

class FakeDataSource: ReminderDataSource {

    private var reminders: MutableList<ReminderDTO> = mutableListOf()

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        reminders.let { return Result.Success(it) }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        reminders.firstOrNull { it.id == id }?.let { return Result.Success(it) }
        return Error("Reminder not found")
    }

    override suspend fun deleteAllReminders() {
        reminders = mutableListOf()
    }
}