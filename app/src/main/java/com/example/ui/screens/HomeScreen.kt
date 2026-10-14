package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Offer
import com.example.data.model.SupportedLocations
import com.example.ui.components.AiInsightBanner
import com.example.ui.components.DealFilterComponent
import com.example.ui.components.OfferCard
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.OfferViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: OfferViewModel,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = false,
    onToggleTheme: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val filterState by viewModel.filterState.collectAsState()
    val offers by viewModel.displayedOffers.collectAsState()
    val savedIds by viewModel.savedOfferIds.collectAsState()
    val reminders by viewModel.reminders.collectAsState()
    val aiInsights by viewModel.aiInsights.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()

    var showCountryDropdown by remember { mutableStateOf(false) }
    var showCityDropdown by remember { mutableStateOf(false) }

    val countryInfo = remember(filterState.selectedCountry) {
        SupportedLocations.getCountryInfo(filterState.selectedCountry)
    }

    val reminderOfferIds = reminders.map { it.offerId }.toSet()

    val sourceOptions = listOf(
        "All Sources",
        "Amazon",
        "ClickFlyers",
        "Banks & Cards",
        "Supermarkets"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen")
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "OfferScout",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "LIVE AGGREGATOR",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 9.sp
                        )
                    }
                }
            },
            actions = {
                // Sync Online Deals from Google/Amazon/ClickFlyers button
                IconButton(
                    onClick = { viewModel.syncLiveDeals() },
                    modifier = Modifier.testTag("sync_deals_btn")
                ) {
                    if (isAiLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sync Online Deals",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Theme Appearance Toggle button
                if (onToggleTheme != null) {
                    IconButton(
                        onClick = onToggleTheme,
                        modifier = Modifier.testTag("theme_toggle_btn")
                    ) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (isDarkTheme) "Switch to Light Theme" else "Switch to Dark Theme",
                            tint = if (isDarkTheme) Color(0xFFFBBF24) else MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Wishlist / Saved Deals quick icon
                IconButton(
                    onClick = { viewModel.navigateTo(AppScreen.WISHLIST) },
                    modifier = Modifier.testTag("home_wishlist_top_btn")
                ) {
                    if (savedIds.isNotEmpty()) {
                        BadgedBox(
                            badge = {
                                Badge(containerColor = MaterialTheme.colorScheme.tertiary) {
                                    Text("${savedIds.size}")
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = "View Wishlist (${savedIds.size})",
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = "View Wishlist",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Test Push Notification quick icon
                IconButton(
                    onClick = { viewModel.testPushNotification() },
                    modifier = Modifier.testTag("test_notification_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Test Push Notification",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Share Live App with Others
                IconButton(
                    onClick = {
                        val shareText = "🚀 Check out OfferScout! Discover verified flight deals, supermarket coupons, and credit card perks in your city:\nhttps://ais-pre-raljsmfhhbcu3ebxnn5b7j-429304375038.europe-west1.run.app"
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share OfferScout via"))
                    },
                    modifier = Modifier.testTag("home_share_app_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share OfferScout Live App",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        // Country & City Dropdowns Bar with Currency Indicator
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            tonalElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Country Dropdown Selector
                    Box(modifier = Modifier.weight(1f)) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showCountryDropdown = true }
                                .testTag("country_dropdown_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(text = countryInfo.flag, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = filterState.selectedCountry,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showCountryDropdown,
                            onDismissRequest = { showCountryDropdown = false }
                        ) {
                            Text(
                                text = "Select Country",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                            HorizontalDivider()

                            SupportedLocations.LOCATIONS.forEach { loc ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(text = loc.flag, fontSize = 18.sp)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "${loc.country} (${loc.currencySymbol})",
                                                fontWeight = if (loc.country.equals(filterState.selectedCountry, ignoreCase = true)) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    },
                                    onClick = {
                                        viewModel.updateCountry(loc.country)
                                        showCountryDropdown = false
                                    }
                                )
                            }

                            HorizontalDivider()
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Public, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("More Countries & Search...", fontWeight = FontWeight.SemiBold)
                                    }
                                },
                                onClick = {
                                    showCountryDropdown = false
                                    viewModel.setLocationDialog(true)
                                }
                            )
                        }
                    }

                    // City Dropdown Selector
                    Box(modifier = Modifier.weight(1f)) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showCityDropdown = true }
                                .testTag("city_dropdown_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationCity,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = filterState.selectedCity,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showCityDropdown,
                            onDismissRequest = { showCityDropdown = false }
                        ) {
                            Text(
                                text = "Cities in ${countryInfo.country}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                            HorizontalDivider()

                            countryInfo.cities.forEach { city ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = city,
                                            fontWeight = if (city.equals(filterState.selectedCity, ignoreCase = true)) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        viewModel.updateCity(city)
                                        showCityDropdown = false
                                    }
                                )
                            }

                            HorizontalDivider()
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Search / Custom City...", fontWeight = FontWeight.SemiBold)
                                    }
                                },
                                onClick = {
                                    showCityDropdown = false
                                    viewModel.setLocationDialog(true)
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Currency & Location Info Strip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "Currency: ${countryInfo.currencySymbol} (${countryInfo.currencyCode})",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                fontSize = 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sources: Google • Amazon • ClickFlyers • Banks",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                    }

                    Text(
                        text = "Change",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { viewModel.setLocationDialog(true) }
                    )
                }
            }
        }

        // Search Input Bar
        OutlinedTextField(
            value = filterState.searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            placeholder = { Text("Search Amazon, ClickFlyers, Panda, banks, flights...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                if (filterState.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp)
                .testTag("search_text_input")
        )

        // Organizational Deal Types: Travel, Credit Card, Local (Room Database Partition)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(
                "All" to "🌟 All Deals",
                "Travel" to "✈️ Travel",
                "Credit Card" to "💳 Credit Card",
                "Local" to "📍 Local"
            ).forEach { (typeKey, label) ->
                val isSelected = filterState.selectedDealType == typeKey
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.updateDealType(typeKey) },
                    label = {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.tertiary,
                        selectedLabelColor = MaterialTheme.colorScheme.onTertiary
                    ),
                    modifier = Modifier.testTag("deal_type_${typeKey.replace(" ", "_")}")
                )
            }
        }

        // Sources Filter Chips (All, Amazon, ClickFlyers, Banks, Supermarkets)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            sourceOptions.forEach { source ->
                FilterChip(
                    selected = filterState.selectedSource == source,
                    onClick = { viewModel.updateSource(source) },
                    label = { Text(source, fontSize = 11.sp, fontWeight = if (filterState.selectedSource == source) FontWeight.Bold else FontWeight.Normal) },
                    shape = RoundedCornerShape(14.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.testTag("filter_source_${source.replace(" ", "_")}")
                )
            }
        }

        // Dedicated Jetpack Compose Deal Filter Component:
        // Allows toggling between deal categories (Travel, Supermarket, Credit Card, All, etc.)
        // and sorting by expiry date or discount percentage!
        DealFilterComponent(
            selectedCategory = filterState.selectedCategory,
            onCategoryToggle = { viewModel.toggleCategory(it) },
            selectedSortOption = filterState.sortOption,
            onSortOptionSelected = { viewModel.updateSortOption(it) },
            onlyExpiringSoon = filterState.onlyExpiringSoon,
            onToggleExpiringSoon = { viewModel.toggleExpiringSoonFilter() },
            onlyWithPromoCode = filterState.onlyWithPromoCode,
            onTogglePromoCode = { viewModel.togglePromoCodeFilter() },
            onlyWishlisted = filterState.onlyWishlisted,
            onToggleWishlist = { viewModel.toggleWishlistFilter() },
            wishlistCount = savedIds.size,
            totalDealsCount = offers.size,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
        )

        // Offers Feed
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(top = 6.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Proactive AI Insight Banner based on pattern
            if (aiInsights.isNotEmpty() && filterState.searchQuery.isEmpty()) {
                item(key = "ai_insight_header") {
                    AiInsightBanner(
                        insights = aiInsights,
                        onInsightClick = { insight ->
                            viewModel.updateCategory(insight.relatedCategory)
                        }
                    )
                }
            }

            // Results summary header
            item(key = "feed_header") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${offers.size} Offers in ${filterState.selectedCity}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Sorted: ${filterState.sortOption.displayName} • ${countryInfo.currencySymbol} (${countryInfo.currencyCode})",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { viewModel.syncLiveDeals() }
                            .padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Sync",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Sync Live",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (offers.isEmpty()) {
                item(key = "empty_state") {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                            .testTag("empty_offers_card"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No offers found matching criteria",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Looking for deals in ${filterState.selectedCity}, ${filterState.selectedCountry}? You can scan online offers from Amazon, ClickFlyers, and Bank websites right now in ${countryInfo.currencySymbol}.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = { viewModel.syncLiveDeals() },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("sync_empty_deals_btn")
                                ) {
                                    Icon(imageVector = Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Sync Web Deals")
                                }

                                OutlinedButton(
                                    onClick = {
                                        viewModel.updateCategory(com.example.data.model.OfferCategory.ALL)
                                        viewModel.updateSource("All Sources")
                                        viewModel.updateSearchQuery("")
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Reset Filters")
                                }
                            }
                        }
                    }
                }
            } else {
                items(
                    items = offers,
                    key = { it.id }
                ) { offer ->
                    OfferCard(
                        offer = offer,
                        isSaved = savedIds.contains(offer.id),
                        hasReminder = reminderOfferIds.contains(offer.id),
                        onCardClick = { viewModel.selectOffer(offer) },
                        onToggleSave = { viewModel.toggleSaveOffer(offer) },
                        onSetReminderClick = { viewModel.openReminderDialog(offer) }
                    )
                }
            }
        }
    }
}
