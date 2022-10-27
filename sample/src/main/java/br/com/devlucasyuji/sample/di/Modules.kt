package br.com.devlucasyuji.sample.di

import br.com.devlucasyuji.sample.feature.camera.CameraViewModel
import br.com.devlucasyuji.sample.data.local.datasource.FileDataSource
import br.com.devlucasyuji.sample.data.local.datasource.UserDataSource
import br.com.devlucasyuji.sample.data.local.UserStore
import br.com.devlucasyuji.sample.data.local.UserStoreImpl
import br.com.devlucasyuji.sample.data.mapper.UserMapper
import br.com.devlucasyuji.sample.feature.configuration.ConfigurationViewModel
import br.com.devlucasyuji.sample.feature.gallery.GalleryViewModel
import kotlinx.serialization.json.Json
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

object Modules {

    val localStore = module {
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
        viewModel { CameraViewModel(get()) }
        viewModel { GalleryViewModel(get()) }
        viewModel { ConfigurationViewModel(get()) }
    }
}