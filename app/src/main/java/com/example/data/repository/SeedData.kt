package com.example.data.repository

import com.example.data.local.Converters
import com.example.data.local.ProfileEntity
import com.example.data.local.UserProfileEntity

object SeedData {
  fun getInitialProfiles(): List<ProfileEntity> = emptyList()

  fun getInitialUserProfile(): UserProfileEntity = UserProfileEntity(
    id = "my_profile",
    name = "",
    age = 0,
    gender = "",
    pronouns = "",
    bio = "",
    occupation = "",
    education = "",
    hometown = "",
    height = "",
    zodiac = "",
    datingIntention = "",
    drinking = "",
    smoking = "",
    pets = "",
    passionsJoined = "",
    photosJoined = "",
    promptQuestion = "My simple pleasures in life...",
    promptAnswer = "",
    isOnboardingCompleted = false,
    phoneNumber = "",
    countryCode = "+91",
    email = "",
    dob = "",
    currentLocationCity = "",
    currentLocationCountry = "",
    latitude = 0.0,
    longitude = 0.0,
    isPhoneVerified = false,
    isAccountDisabled = false
  )
}
