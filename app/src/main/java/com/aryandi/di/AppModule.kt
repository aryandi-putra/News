package com.aryandi.di

import com.aryandi.data.network.ApiService
import com.aryandi.data.repository.NewsRepositoryImpl
import com.aryandi.domain.repository.NewsRepository
import com.aryandi.domain.usecase.GetNewsListUseCase
import com.aryandi.domain.usecase.GetSourceListUseCase
import com.aryandi.news.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder().baseUrl("https://newsapi.org/v2/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideNewsRepository(apiService: ApiService): NewsRepository {
        return NewsRepositoryImpl(apiService)
    }

    @Provides
    @Singleton
    fun provideGetNewsListUseCase(repository: NewsRepository): GetNewsListUseCase {
        return GetNewsListUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetSourceListUseCase(repository: NewsRepository): GetSourceListUseCase {
        return GetSourceListUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetPaginatedNewsUseCase(repository: NewsRepository): com.aryandi.domain.usecase.GetPaginatedNewsUseCase {
        return com.aryandi.domain.usecase.GetPaginatedNewsUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideSearchNewsUseCase(): com.aryandi.domain.usecase.SearchNewsUseCase {
        return com.aryandi.domain.usecase.SearchNewsUseCase()
    }

    @Provides
    @Singleton
    fun provideSearchSourcesUseCase(): com.aryandi.domain.usecase.SearchSourcesUseCase {
        return com.aryandi.domain.usecase.SearchSourcesUseCase()
    }
}