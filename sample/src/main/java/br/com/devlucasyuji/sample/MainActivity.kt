package br.com.devlucasyuji.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import br.com.devlucasyuji.sample.feature.camera.CameraScreen
import br.com.devlucasyuji.sample.feature.gallery.GalleryScreen
import br.com.devlucasyuji.sample.feature.permission.AppPermission
import br.com.devlucasyuji.sample.router.Router
import br.com.devlucasyuji.sample.router.route

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppPermission {
                val navHost = rememberNavController()
                NavGraph(navHost)
            }
        }
    }

    @Composable
    fun NavGraph(navHost: NavHostController) {
        NavHost(navHost, startDestination = Router.Camera.route) {
            route(Router.Camera) { CameraScreen() }
            route(Router.Gallery) { GalleryScreen() }
        }
    }
}
