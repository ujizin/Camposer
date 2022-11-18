package com.ujizin.sample

import android.app.Application
import com.ujizin.sample.di.Modules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class SampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@SampleApplication)
            modules(
                Modules.localStores,
                Modules.mappers,
                Modules.dataSources,
                Modules.viewModels,
            )
        }
    }
}
