package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DealSortOption
import com.example.data.model.Offer
import com.example.data.model.OfferCategory
import com.example.data.model.SupportedLocations
import com.example.ui.components.OfferCard
import com.example.ui.theme.BrandSecondaryLight
import com.example.ui.theme.BrandTertiaryLight
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.OfferViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistScreen(
    viewModel: OfferViewModel,
    modifier: Modifier = Modifier
) {
    val savedOffers by viewModel.savedOffers.collectAsState()
    val savedIds by viewModel.savedOfferIds.collectAsState()
    val reminders by viewModel.reminders.collectAsState()
    val filterState by viewModel.filterState.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<OfferCategory?>(null) }
    var showClearDialog by remember { mutableStateOf(false) }

    val reminderOfferIds = remember(reminders) {
        reminders.map { it.offerId }.toSet()
    }

    // Filter saved deals locally
    val filteredSavedOffers = remember(savedOffers, searchQuery, selectedCategory) {
        savedOffers.filter { offer ->
            val matchesCategory = selectedCategory == null || offer.category == selectedCategory
            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                val q = searchQuery.trim().lowercase()
                offer.title.lowercase().contains(q) ||
                        offer.merchant.lowercase().contains(q) ||
                        offer.description.lowercase().contains(q) ||
                        offer.discountHighlight.lowercase().contains(q)
            }
            matchesCategory && matchesSearch
        }
    }

    // Compute potential savings from wishlisted deals
    val potentialSavings = remember(savedOffers) {
        savedOffers.sumOf { offer ->
            if (offer.originalPrice > 0 && offer.discountedPrice > 0) {
                offer.originalPrice - offer.discountedPrice
            } else {
                offer.discountPercentage * 1.5
            }
        }
    }

    val expiringSoonCount = remember(savedOffers) {
        savedOffers.count { it.isExpiringSoon }
    }

    val currencySymbol = SupportedLocations.getCurrencySymbol(filterState.selectedCountry)

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("wishlist_screen")
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Wishlist & Saved",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            },
            actions = {
                if (savedOffers.isNotEmpty()) {
                    IconButton(
                        onClick = { showClearDialog = true },
                        modifier = Modifier.testTag("wishlist_clear_all_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear Wishlist",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Summary Header Card
            item(key = "wishlist_stats_header") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "LOCAL ROOM DATABASE BACKED",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 0.8.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${savedOffers.size} Bookmarked ${if (savedOffers.size == 1) "Deal" else "Deals"}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Savings,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Stat 1: Potential Savings
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "Potential Savings",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "$currencySymbol${String.format(Locale.US, "%.1f", potentialSavings)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = BrandTertiaryLight
                                    )
                                }
                            }

                            // Stat 2: Expiring Soon
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "Expiring Soon (<=3d)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "$expiringSoonCount deals",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (expiringSoonCount > 0) BrandSecondaryLight else MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        if (expiringSoonCount > 0) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(BrandSecondaryLight.copy(alpha = 0.12f))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = BrandSecondaryLight,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "$expiringSoonCount deals in your wishlist will expire shortly. Redeem soon!",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BrandSecondaryLight,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // Search Bar & Filter Chips within Wishlist
            if (savedOffers.isNotEmpty()) {
                item(key = "wishlist_controls") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("wishlist_search_input"),
                            placeholder = { Text("Search wishlist deals...", fontSize = 14.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                                    }
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )

                        // Category Filter Chips Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilterChip(
                                selected = selectedCategory == null,
                                onClick = { selectedCategory = null },
                                label = { Text("All (${savedOffers.size})", fontSize = 12.sp) },
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.testTag("wishlist_chip_all")
                            )

                            listOf(
                                OfferCategory.TRAVEL,
                                OfferCategory.SUPERMARKET,
                                OfferCategory.CREDIT_CARD,
                                OfferCategory.DINING,
                                OfferCategory.ELECTRONICS
                            ).forEach { cat ->
                                val count = savedOffers.count { it.category == cat }
                                if (count > 0) {
                                    FilterChip(
                                        selected = selectedCategory == cat,
                                        onClick = {
                                            selectedCategory = if (selectedCategory == cat) null else cat
                                        },
                                        label = { Text("${cat.displayName} ($count)", fontSize = 12.sp) },
                                        shape = RoundedCornerShape(20.dp),
                                        modifier = Modifier.testTag("wishlist_chip_${cat.name.lowercase()}")
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Wishlist Deals List or Empty State
            if (savedOffers.isEmpty()) {
                item(key = "wishlist_empty_view") {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp)
                            .testTag("wishlist_empty_state"),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                modifier = Modifier.size(72.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Bookmark,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Your Wishlist is Empty",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Bookmark your favorite flight discounts, supermarket circular coupons, and credit card rewards by tapping the bookmark icon. Saved deals are safely stored in your local Room database for quick offline access!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = { viewModel.navigateTo(AppScreen.HOME) },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.testTag("wishlist_explore_btn")
                            ) {
                                Icon(imageVector = Icons.Default.Explore, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Browse Trending Deals")
                            }
                        }
                    }
                }
            } else if (filteredSavedOffers.isEmpty()) {
                item(key = "wishlist_no_filter_matches") {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No Saved Deals Match Your Filter",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Try clearing the search query or switching categories.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = {
                                    searchQuery = ""
                                    selectedCategory = null
                                }
                            ) {
                                Text("Reset Wishlist Filter")
                            }
                        }
                    }
                }
            } else {
                items(
                    items = filteredSavedOffers,
                    key = { it.id }
                ) { offer ->
                    OfferCard(
                        offer = offer,
                        isSaved = true,
                        hasReminder = reminderOfferIds.contains(offer.id),
                        onCardClick = { viewModel.selectOffer(offer) },
                        onToggleSave = { viewModel.toggleSaveOffer(offer) },
                        onSetReminderClick = { viewModel.openReminderDialog(offer) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    // Confirmation Dialog to Clear Wishlist
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.BookmarkRemove,
                    contentDescription = null,
                    tint = BrandSecondaryLight
                )
            },
            title = { Text("Clear All Saved Deals?") },
            text = {
                Text("This will remove all bookmarked deals from your local database. You can always bookmark them again from Explore.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllSavedOffers()
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandSecondaryLight)
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
