package com.example.data.repository

import com.example.data.local.Converters
import com.example.data.local.ProfileEntity
import com.example.data.local.UserProfileEntity

object SeedData {
  fun getInitialProfiles(): List<ProfileEntity> = listOf(
    ProfileEntity(
      id = "profile_1",
      name = "Maya Lin",
      age = 25,
      occupation = "Architectural Designer & Ceramicist",
      company = "Studio Forma",
      education = "Rhode Island School of Design",
      location = "3 miles away • Williamsburg",
      bio = "Obsessed with Scandinavian design, sourdough baking, and rooftop sunsets 🌅 Seeking someone who can appreciate mid-century furniture and late-night ramen runs.",
      photosJoined = Converters.listToString(listOf(
        "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=900&auto=format&fit=crop&q=80",
        "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=900&auto=format&fit=crop&q=80",
        "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=900&auto=format&fit=crop&q=80"
      )),
      promptQuestion = "The quickest way to my heart is...",
      promptAnswer = "Surprising me with a fresh batch of pain au chocolat and taking me to a hidden botanical garden 🌿",
      passionsJoined = Converters.listToString(listOf("Architecture", "Ceramics", "Matcha", "Art Galleries", "Baking", "Indie Pop")),
      zodiac = "Taurus ♉",
      height = "5'7\" (170 cm)",
      datingIntention = "Long-term relationship 💍",
      drinking = "Socially 🥂",
      smoking = "Never 🚭",
      pets = "Dog mom (Golden Retriever) 🐶",
      anthemSong = "Glue Song",
      anthemArtist = "beabadoobee",
      isVerified = true,
      likedMe = true, // Mutually swipable match!
      isLikedByMe = false,
      isPassedByMe = false,
      isSuperLikedByMe = false,
      isMutualMatch = false,
      matchedTimestamp = null
    ),
    ProfileEntity(
      id = "profile_2",
      name = "Lucas Thorne",
      age = 27,
      occupation = "Audio Engineer & Producer",
      company = "Electric Lady Studios",
      education = "Berklee College of Music",
      location = "1 mile away • Lower East Side",
      bio = "Synthesizer hoarder, vintage motorcycle restorer, and espresso snob ☕ When I'm not in the studio, you'll find me browsing flea markets or camping in the Catskills.",
      photosJoined = Converters.listToString(listOf(
        "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=900&auto=format&fit=crop&q=80",
        "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=900&auto=format&fit=crop&q=80",
        "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=900&auto=format&fit=crop&q=80"
      )),
      promptQuestion = "Two truths and a lie...",
      promptAnswer = "1. I played guitar on a Grammy-nominated album. 2. I've never had a cavity. 3. I survived being lost in the desert for 24 hours.",
      passionsJoined = Converters.listToString(listOf("Music Production", "Vinyl", "Camping", "Motorcycles", "Espresso", "Live Gigs")),
      zodiac = "Scorpio ♏",
      height = "6'1\" (185 cm)",
      datingIntention = "Looking for my person ✨",
      drinking = "Frequently 🍸",
      smoking = "Socially 💨",
      pets = "Love all animals 🐱🐶",
      anthemSong = "Less I Know the Better",
      anthemArtist = "Tame Impala",
      isVerified = true,
      likedMe = true, // Mutually swipable match!
      isLikedByMe = false,
      isPassedByMe = false,
      isSuperLikedByMe = false,
      isMutualMatch = false,
      matchedTimestamp = null
    ),
    ProfileEntity(
      id = "profile_3",
      name = "Chloe Dubois",
      age = 24,
      occupation = "Pastry Chef & Food Stylist",
      company = "Le Petit Four",
      education = "Ferrandi Paris",
      location = "4 miles away • SoHo",
      bio = "Living life one flaky croissant at a time 🥐 Fluent in French, sarcasm, and movie trivia. Looking for someone willing to be my chief recipe taste tester!",
      photosJoined = Converters.listToString(listOf(
        "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=900&auto=format&fit=crop&q=80",
        "https://images.unsplash.com/photo-1529626455594-4ff0802cfb7e?w=900&auto=format&fit=crop&q=80",
        "https://images.unsplash.com/photo-1488426862026-3ee34a7d66df?w=900&auto=format&fit=crop&q=80"
      )),
      promptQuestion = "A life goal of mine...",
      promptAnswer = "Open a cozy late-night dessert speakeasy with jazz vinyl records and vintage velvet couches 🎶",
      passionsJoined = Converters.listToString(listOf("Baking", "Foodie", "French Cinema", "Wine Tasting", "Travel", "Museums")),
      zodiac = "Cancer ♋",
      height = "5'5\" (165 cm)",
      datingIntention = "Long-term dating ❤️",
      drinking = "Socially 🍷",
      smoking = "Never 🚭",
      pets = "Have a chubby British Shorthair cat named Brioche 🥐🐱",
      anthemSong = "La Vie En Rose",
      anthemArtist = "Édith Piaf",
      isVerified = true,
      likedMe = false,
      isLikedByMe = false,
      isPassedByMe = false,
      isSuperLikedByMe = false,
      isMutualMatch = false,
      matchedTimestamp = null
    )
  )

  fun getInitialUserProfile(): UserProfileEntity = UserProfileEntity(
    id = "my_profile",
    name = "Alex Rivera",
    age = 24,
    gender = "Non-binary",
    pronouns = "They/Them",
    bio = "Creative photographer & warm coffee enthusiast ☕ Searching for someone to explore indie bookstores, cook pasta from scratch, and swap vinyl records with.",
    occupation = "UX Designer & Visual Artist",
    education = "NYU Tisch School of the Arts",
    hometown = "Brooklyn, NY",
    height = "5'9\"",
    zodiac = "Sagittarius ♐",
    datingIntention = "Long-term relationship 💖",
    drinking = "Socially 🍷",
    smoking = "Never 🚭",
    pets = "Have 2 rescue cats 🐾",
    passionsJoined = Converters.listToString(listOf("Photography", "Coffee", "Vinyl Records", "Art Galleries", "Cooking", "Cats", "Hiking", "Indie Pop")),
    photosJoined = Converters.listToString(listOf(
      "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=900&auto=format&fit=crop&q=80",
      "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=900&auto=format&fit=crop&q=80",
      "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=900&auto=format&fit=crop&q=80"
    )),
    promptQuestion = "My simple pleasures in life...",
    promptAnswer = "Freshly baked croissants, golden hour light, and warm purring cats on a Sunday morning.",
    isOnboardingCompleted = false,
    phoneNumber = "",
    countryCode = "+91",
    email = "",
    dob = "2000-05-14",
    currentLocationCity = "Mumbai",
    currentLocationCountry = "India",
    latitude = 19.0760,
    longitude = 72.8777,
    isPhoneVerified = false
  )
}
