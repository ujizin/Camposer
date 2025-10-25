package com.ujizin.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ujizin.sample.feature.camera.CameraScreen
import com.ujizin.sample.feature.configuration.ConfigurationScreen
import com.ujizin.sample.feature.gallery.GalleryScreen
import com.ujizin.sample.feature.permission.AppPermission
import com.ujizin.sample.feature.preview.PreviewScreen
import com.ujizin.sample.router.Router

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContent {
            CamposerTheme {
                AppPermission {
                    val navHost = rememberNavController()
                    NavGraph(navHost)
//                    com.ujizin.camposer.shared.CameraScreen()
                }
            }
        }
    }

    @Composable
    fun NavGraph(navHost: NavHostController) {
        NavHost(navHost, startDestination = Router.Camera) {
            composable<Router.Camera> {
                CameraScreen(
                    onGalleryClick = { navHost.navigate(Router.Gallery) },
                    onConfigurationClick = { navHost.navigate(Router.Configuration) }
                )
            }
            composable<Router.Gallery> {
                GalleryScreen(
                    onBackPressed = { navHost.navigateUp() },
                    onPreviewClick = { navHost.navigate(Router.Preview(it)) }
                )
            }
            composable<Router.Configuration> {
                ConfigurationScreen(onBackPressed = { navHost.navigateUp() })
            }
            composable<Router.Preview>{
                PreviewScreen(onBackPressed = { navHost.navigateUp() })
            }
        }
    }
}
