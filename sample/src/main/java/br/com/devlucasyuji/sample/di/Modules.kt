package br.com.devlucasyuji.sample.di

import br.com.devlucasyuji.sample.feature.camera.CameraViewModel
import br.com.devlucasyuji.sample.feature.camera.datasource.FileDataSource
import br.com.devlucasyuji.sample.feature.configuration.ConfigurationViewModel
import br.com.devlucasyuji.sample.feature.gallery.GalleryViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

object Modules {

    val dataSources = module {
        factory { FileDataSource() }
    }

    val viewModels = module {
        viewModel { CameraViewModel(get()) }
        viewModel { GalleryViewModel(get()) }
        viewModel { ConfigurationViewModel() }
    }
}