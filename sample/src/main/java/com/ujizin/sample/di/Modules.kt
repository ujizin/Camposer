package com.ujizin.sample.di

import com.ujizin.sample.feature.camera.CameraViewModel
import com.ujizin.sample.data.local.datasource.FileDataSource
import com.ujizin.sample.data.local.datasource.UserDataSource
import com.ujizin.sample.data.local.UserStore
import com.ujizin.sample.data.local.UserStoreImpl
import com.ujizin.sample.data.mapper.UserMapper
import com.ujizin.sample.feature.configuration.ConfigurationViewModel
import com.ujizin.sample.feature.gallery.GalleryViewModel
import com.ujizin.sample.feature.preview.PreviewViewModel
import kotlinx.serialization.json.Json
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

object Modules {

    val localStores = module {
        factory<UserStore> {
            UserStoreImpl(get(), Json)
        }
    }

    val mappers = module {
        factory { UserMapper() }
    }

    val dataSources = module {
        factory { FileDataSource() }
        factory { UserDataSource(get(), get()) }
    }

    val viewModels = module {
        viewModel { CameraViewModel(get(), get()) }
        viewModel { GalleryViewModel(get()) }
        viewModel { ConfigurationViewModel(get()) }
        viewModel { PreviewViewModel(get()) }
    }
}