package com.adrian.bucayan.scanmecalculator.di

import android.util.Log
import com.adrian.bucayan.scanmecalculator.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import timber.log.Timber
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun providesTimberTree(): Timber.Tree {
        class ReportingTree : Timber.Tree() {
            override fun log(
                priority: Int,
                tag: String?,
                message: String,
                throwable: Throwable?
            ) {
                if (priority == Log.VERBOSE || priority == Log.DEBUG) {
                    return
                }
                // val t = throwable ?: Exception(message)
                // Pass the exception variable t to crash reporting service
            }
        }

        return when(BuildConfig.DEBUG) {
            true -> Timber.DebugTree()
            else -> ReportingTree()
        }
    }


}