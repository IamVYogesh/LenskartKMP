package com.yogesh.lenskart

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateBottomPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.yogesh.lenskart.core.designsystem.resources.*
import com.yogesh.lenskart.feature.account.AccountRoute
import com.yogesh.lenskart.feature.cart.CartRoute
import com.yogesh.lenskart.feature.catalog.CatalogRoute
import com.yogesh.lenskart.feature.catalog.SearchRoute
import com.yogesh.lenskart.feature.home.HomeRoute
import com.yogesh.lenskart.feature.product.ProductRoute
import com.yogesh.lenskart.feature.tryon.TryOnRoute
import com.yogesh.lenskart.feature.wishlist.WishlistRoute
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.reflect.KClass

/** Type-safe routes: @Serializable classes are the navigation graph (no string routes). */
sealed interface Route {
    @Serializable data object Home : Route
    @Serializable data class Catalog(val category: String = "") : Route
    @Serializable data object Wishlist : Route
    @Serializable data object Cart : Route
    @Serializable data object Account : Route
    @Serializable data object Search : Route
    @Serializable data class Product(val productId: String) : Route
    @Serializable data class TryOn(val productId: String = "") : Route
}

private class TopLevel(val route: Route, val routeClass: KClass<out Route>, val label: StringResource, val icon: ImageVector)

private val topLevelDestinations = listOf(
    TopLevel(Route.Home, Route.Home::class, Res.string.tab_home, Icons.Default.Home),
    TopLevel(Route.Catalog(), Route.Catalog::class, Res.string.tab_shop, Icons.Default.Search),
    TopLevel(Route.Wishlist, Route.Wishlist::class, Res.string.tab_wishlist, Icons.Default.Favorite),
    TopLevel(Route.Cart, Route.Cart::class, Res.string.tab_cart, Icons.Default.ShoppingCart),
    TopLevel(Route.Account, Route.Account::class, Res.string.tab_account, Icons.Default.Person),
)

private fun NavHostController.navigateTopLevel(route: Route) = navigate(route) {
    popUpTo(graph.findStartDestination().id) { saveState = true }
    launchSingleTop = true
    restoreState = true
}

@Composable
fun MainScaffold() {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val destination = backStack?.destination
    val showBottomBar = topLevelDestinations.any { destination?.hasRoute(it.routeClass) == true }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (showBottomBar) NavigationBar {
                topLevelDestinations.forEach { tab ->
                    NavigationBarItem(
                        selected = destination?.hasRoute(tab.routeClass) == true,
                        onClick = { nav.navigateTopLevel(tab.route) },
                        icon = { Icon(tab.icon, null) },
                        label = { Text(stringResource(tab.label)) },
                    )
                }
            }
        },
    ) { inner ->
        NavHost(nav, startDestination = Route.Home, modifier = Modifier.padding(bottom = inner.calculateBottomPadding())) {
            composable<Route.Home> {
                HomeRoute(
                    onSearchClick = { nav.navigate(Route.Search) },
                    onCategoryClick = { nav.navigate(Route.Catalog(it?.name.orEmpty())) },
                    onProductClick = { nav.navigate(Route.Product(it)) },
                )
            }
            composable<Route.Catalog> { entry ->
                CatalogRoute(entry.toRoute<Route.Catalog>().category, onProductClick = { nav.navigate(Route.Product(it)) })
            }
            composable<Route.Search> { SearchRoute(onBack = { nav.popBackStack() }, onProductClick = { nav.navigate(Route.Product(it)) }) }
            composable<Route.Wishlist> { WishlistRoute(onProductClick = { nav.navigate(Route.Product(it)) }) }
            composable<Route.Cart> { CartRoute(onLoginRequired = { nav.navigateTopLevel(Route.Account) }) }
            composable<Route.Account> { AccountRoute() }
            composable<Route.Product> { entry ->
                ProductRoute(
                    productId = entry.toRoute<Route.Product>().productId,
                    onBack = { nav.popBackStack() },
                    onOpenCart = { nav.navigateTopLevel(Route.Cart) },
                    onTryOn = { nav.navigate(Route.TryOn(it)) },
                )
            }
            composable<Route.TryOn> { entry ->
                TryOnRoute(entry.toRoute<Route.TryOn>().productId, onClose = { nav.popBackStack() })
            }
        }
    }
}
