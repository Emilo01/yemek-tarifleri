package com.farukayata.yemektarifi.di

import com.farukayata.yemektarifi.data.remote.VisionApiService
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object VisionApiModule {

    private const val BASE_URL = "https://vision.googleapis.com/"

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .serializeNulls() //  Null değerleri de serialize etsin
            .create()
    }

    @Provides
    @Singleton
    fun provideVisionApiService(gson: Gson): VisionApiService {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(VisionApiService::class.java)
    }

}