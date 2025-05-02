package com.farukayata.yemektarifi.di

import com.farukayata.yemektarifi.BuildConfig
import com.farukayata.yemektarifi.data.remote.OpenAiService
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton
import java.util.concurrent.TimeUnit

@Module
@InstallIn(SingletonComponent::class)
object OpenAiModule {

    private const val BASE_URL = "https://api.openai.com/"

    @Provides
    @Singleton
    fun provideOpenAiHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { chain: Interceptor.Chain ->
                val request: Request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer ${BuildConfig.OPENAI_API_KEY}")
                    .build()
                chain.proceed(request)
            }
            .connectTimeout(60, TimeUnit.SECONDS) //Sunucuya bağlanmak için beklenen süre
            .readTimeout(75, TimeUnit.SECONDS) //Sunucudan yanıt alınana kadar bekleme süresi
            .writeTimeout(75, TimeUnit.SECONDS) //base64 görseli yazarken ağın yavaşlamasına karşı bekleme süresi
            .build()
    }

    @Provides
    @Singleton
    fun provideOpenAiService(client: OkHttpClient): OpenAiService {
        /*
        val client = OkHttpClient.Builder()
            .addInterceptor { chain: Interceptor.Chain ->
                val request: Request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer ${BuildConfig.OPENAI_API_KEY}")
                    //Authorization header zaten OkHttpClient içinde verdik
                    .build()
                chain.proceed(request)
            }
            .build()

         */

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenAiService::class.java)
    }
}
