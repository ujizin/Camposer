package com.ujizin.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ujizin.sample.feature.camera.CameraScreen
import com.ujizin.sample.feature.configuration.ConfigurationScreen
import com.ujizin.sample.feature.gallery.GalleryScreen
import com.ujizin.sample.feature.permission.AppPermission
import com.ujizin.sample.feature.preview.PreviewScreen
import com.ujizin.sample.router.Args
import com.ujizin.sample.router.Router
import com.ujizin.sample.router.navigate
import com.ujizin.sample.router.route

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContent {
            CamposerTheme {
                AppPermission {
                    val navHost = rememberNavController()
                    NavGraph(navHost)
                }
            }
        }
    }

    @Composable
    fun NavGraph(navHost: NavHostController) {
        NavHost(navHost, startDestination = Router.Camera.route) {
            route(Router.Camera) {
                CameraScreen(
                    onGalleryClick = { navHost.navigate(Router.Gallery) },
                    onConfigurationClick = { navHost.navigate(Router.Configuration) }
                )
            }
            route(Router.Gallery) {
                GalleryScreen(
                    onBackPressed = { navHost.navigateUp() },
                    onPreviewClick = { navHost.navigate(Router.Preview.createRoute(it)) }
                )
            }
            route(Router.Configuration) {
                ConfigurationScreen(onBackPressed = { navHost.navigateUp() })
            }
            route(
                route = Router.Preview,
                arguments = listOf(
                    navArgument(Args.Path) { type = NavType.StringType },
                )
            ) {
                PreviewScreen(onBackPressed = { navHost.navigateUp() })
            }
        }
    }
}
