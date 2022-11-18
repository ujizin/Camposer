package com.ujizin.sample.router

import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable

fun NavGraphBuilder.route(
    route: Router,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    content: @Composable (NavBackStackEntry) -> Unit
) {
    composable(route.route, arguments, deepLinks, content)
}

fun NavHostController.navigate(route: Router) {
    navigate(route.route) {
        popUpTo(route.route) {
            inclusive = true
        }
    }
}