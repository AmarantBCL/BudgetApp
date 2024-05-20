package com.amarant.apps.budgetapp.repository

import com.amarant.apps.budgetapp.db.ProfileDao
import com.amarant.apps.budgetapp.entities.Profile
import javax.inject.Inject

class ProfileRepository @Inject constructor(
    val profileDao: ProfileDao
) {

    fun getProfile() = profileDao.getProfileData()

    suspend fun insertProfileData(profile: Profile) = profileDao.insertProfileData(profile)
}