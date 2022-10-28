package br.com.devlucasyuji.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import br.com.devlucasyuji.sample.feature.camera.CameraScreen
import br.com.devlucasyuji.sample.feature.configuration.ConfigurationScreen
import br.com.devlucasyuji.sample.feature.gallery.GalleryScreen
import br.com.devlucasyuji.sample.feature.permission.AppPermission
import br.com.devlucasyuji.sample.feature.preview.PreviewScreen
import br.com.devlucasyuji.sample.router.Router
import br.com.devlucasyuji.sample.router.navigate
import br.com.devlucasyuji.sample.router.route

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colors = MaterialTheme.colors.copy(primary = colorResource(id = R.color.primary))
            ) {
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
                    onPreviewClick = { navHost.navigate(Router.Preview) }
                )
            }
            route(Router.Configuration) {
                ConfigurationScreen(onBackPressed = { navHost.navigateUp() })
            }
            route(Router.Preview) {
                PreviewScreen(onBackPressed = {

                })
            }
        }
    }
}
