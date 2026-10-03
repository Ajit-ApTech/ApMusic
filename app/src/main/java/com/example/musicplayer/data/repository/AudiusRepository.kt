package com.example.musicplayer.data.repository

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudiusRepository @Inject constructor(
    impl: MusicRepositoryImpl
) : MusicRepository by impl
