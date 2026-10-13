package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DealSortOption
import com.example.data.model.OfferCategory
import com.example.ui.theme.CreditCardTagBg
import com.example.ui.theme.CreditCardTagText
import com.example.ui.theme.DiningTagBg
import com.example.ui.theme.DiningTagText
import com.example.ui.theme.SupermarketTagBg
import com.example.ui.theme.SupermarketTagText
import com.example.ui.theme.TechTagBg
import com.example.ui.theme.TechTagText
import com.example.ui.theme.TravelTagBg
import com.example.ui.theme.TravelTagText

/**
 * Filter UI Component in Jetpack Compose that allows users to:
 * 1. Toggle between deal categories (Travel, Supermarket, Credit Card, All, etc.)
 * 2. Sort by expiry date (Expiring Soonest, Valid Longer) or discount percentage (Highest Discount %)
 * 3. Quickly toggle expiring soon / promo codes
 * 4. Provides clear visual indicators and catchy, vibrant styling.
 */
@Composable
fun DealFilterComponent(
    selectedCategory: OfferCategory,
    onCategoryToggle: (OfferCategory) -> Unit,
    selectedSortOption: DealSortOption,
    onSortOptionSelected: (DealSortOption) -> Unit,
    modifier: Modifier = Modifier,
    onlyExpiringSoon: Boolean = false,
    onToggleExpiringSoon: (() -> Unit)? = null,
    onlyWithPromoCode: Boolean = false,
    onTogglePromoCode: (() -> Unit)? = null,
    onlyWishlisted: Boolean = false,
    onToggleWishlist: (() -> Unit)? = null,
    wishlistCount: Int = 0,
    totalDealsCount: Int? = null,
    onResetFilters: (() -> Unit)? = null
) {
    var showSortMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("deal_filter_component"),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            // ================= SECTION 1: CATEGORY TOGGLES =================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DEAL CATEGORIES",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.8.sp
                )

                if (selectedCategory != OfferCategory.ALL) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onCategoryToggle(OfferCategory.ALL) }
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Show All",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear category filter",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            // Category Toggle Pills (Highlighting Travel, Supermarket, Credit Card, and All)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Priority Categories as explicitly requested
                val priorityCategories = listOf(
                    OfferCategory.ALL,
                    OfferCategory.TRAVEL,
                    OfferCategory.SUPERMARKET,
                    OfferCategory.CREDIT_CARD,
                    OfferCategory.DINING,
                    OfferCategory.ELECTRONICS,
                    OfferCategory.FASHION
                )

                priorityCategories.forEach { category ->
                    val isSelected = selectedCategory == category
                    val (icon, activeColor, activeContainer) = getCategoryStyling(category)

                    val bgColor by animateColorAsState(
                        targetValue = if (isSelected) activeColor else MaterialTheme.colorScheme.surfaceVariant,
                        animationSpec = tween(durationMillis = 200),
                        label = "cat_bg"
                    )

                    val contentColor by animateColorAsState(
                        targetValue = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        animationSpec = tween(durationMillis = 200),
                        label = "cat_content"
                    )

                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable {
                                // Toggle behavior: if tapped when active, revert to ALL (unless already ALL)
                                if (isSelected && category != OfferCategory.ALL) {
                                    onCategoryToggle(OfferCategory.ALL)
                                } else {
                                    onCategoryToggle(category)
                                }
                            }
                            .testTag("category_toggle_${category.name.lowercase()}"),
                        shape = RoundedCornerShape(20.dp),
                        color = bgColor,
                        tonalElevation = if (isSelected) 3.dp else 0.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = contentColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = category.displayName,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                color = contentColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )

            // ================= SECTION 2: SORT BY CONTROLS =================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Sort,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "SORT DEALS BY",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.8.sp
                    )
                }

                // Dropdown trigger for full sort choices
                Box {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showSortMenu = true }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                            .testTag("sort_dropdown_button"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedSortOption.shortLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Open sort menu",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        Text(
                            text = "Sort Deals By",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                        HorizontalDivider()

                        DealSortOption.values().forEach { option ->
                            val isChosen = selectedSortOption == option
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = option.displayName,
                                            fontWeight = if (isChosen) FontWeight.ExtraBold else FontWeight.Normal,
                                            color = if (isChosen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                        if (isChosen) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    onSortOptionSelected(option)
                                    showSortMenu = false
                                },
                                modifier = Modifier.testTag("sort_menu_item_${option.name.lowercase()}")
                            )
                        }
                    }
                }
            }

            // Quick Sort Action Chips (1-tap switches for Expiry Date and Discount Percentage)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Highest Discount % Chip
                val isDiscountSort = selectedSortOption == DealSortOption.DISCOUNT_DESC
                SortPill(
                    label = "% Highest Discount",
                    icon = Icons.Default.Percent,
                    isSelected = isDiscountSort,
                    accentColor = MaterialTheme.colorScheme.secondary,
                    testTag = "sort_chip_discount",
                    onClick = {
                        if (isDiscountSort) {
                            onSortOptionSelected(DealSortOption.RECOMMENDED)
                        } else {
                            onSortOptionSelected(DealSortOption.DISCOUNT_DESC)
                        }
                    }
                )

                // 2. Expiring Soonest Chip
                val isExpirySort = selectedSortOption == DealSortOption.EXPIRY_SOONEST
                SortPill(
                    label = "⏳ Expiring Soonest",
                    icon = Icons.Default.Schedule,
                    isSelected = isExpirySort,
                    accentColor = Color(0xFFEF4444),
                    testTag = "sort_chip_expiry",
                    onClick = {
                        if (isExpirySort) {
                            onSortOptionSelected(DealSortOption.RECOMMENDED)
                        } else {
                            onSortOptionSelected(DealSortOption.EXPIRY_SOONEST)
                        }
                    }
                )

                // 3. Recommended / Featured
                val isRecommended = selectedSortOption == DealSortOption.RECOMMENDED
                SortPill(
                    label = "⭐ Recommended",
                    icon = Icons.Default.Star,
                    isSelected = isRecommended,
                    accentColor = MaterialTheme.colorScheme.primary,
                    testTag = "sort_chip_recommended",
                    onClick = {
                        onSortOptionSelected(DealSortOption.RECOMMENDED)
                    }
                )

                // 4. Latest Expiry Date (Valid Longer)
                val isLatestExpiry = selectedSortOption == DealSortOption.EXPIRY_LATEST
                SortPill(
                    label = "🗓️ Valid Longer",
                    icon = Icons.Default.Event,
                    isSelected = isLatestExpiry,
                    accentColor = MaterialTheme.colorScheme.tertiary,
                    testTag = "sort_chip_latest_expiry",
                    onClick = {
                        if (isLatestExpiry) {
                            onSortOptionSelected(DealSortOption.RECOMMENDED)
                        } else {
                            onSortOptionSelected(DealSortOption.EXPIRY_LATEST)
                        }
                    }
                )
            }

            // Quick toggles row (Urgent ≤3d, Promo Codes, Wishlist) if callbacks provided
            if (onToggleExpiringSoon != null || onTogglePromoCode != null || onToggleWishlist != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 14.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onToggleWishlist != null) {
                        FilterChip(
                            selected = onlyWishlisted,
                            onClick = onToggleWishlist,
                            label = { Text("Saved in Wishlist ($wishlistCount)", fontSize = 11.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Bookmark,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp)
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("filter_chip_wishlist")
                        )
                    }

                    if (onToggleExpiringSoon != null) {
                        FilterChip(
                            selected = onlyExpiringSoon,
                            onClick = onToggleExpiringSoon,
                            label = { Text("⚡ Urgent (≤3 days)", fontSize = 11.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp)
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("filter_chip_urgent")
                        )
                    }

                    if (onTogglePromoCode != null) {
                        FilterChip(
                            selected = onlyWithPromoCode,
                            onClick = onTogglePromoCode,
                            label = { Text("🏷️ With Promo Code", fontSize = 11.sp) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("filter_chip_promocode")
                        )
                    }

                    if (totalDealsCount != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$totalDealsCount deals",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SortPill(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    accentColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) accentColor else MaterialTheme.colorScheme.surfaceVariant,
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Returns visual styling for each category: (Icon, ActiveColor, ActiveContainer)
 */
@Composable
private fun getCategoryStyling(category: OfferCategory): Triple<ImageVector, Color, Color> {
    return when (category) {
        OfferCategory.ALL -> Triple(
            Icons.Default.LocalOffer,
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primaryContainer
        )
        OfferCategory.TRAVEL -> Triple(
            Icons.Default.Flight,
            Color(0xFF0284C7), // Sky Cyan / Blue
            TravelTagBg
        )
        OfferCategory.SUPERMARKET -> Triple(
            Icons.Default.ShoppingCart,
            Color(0xFF16A34A), // Emerald Green
            SupermarketTagBg
        )
        OfferCategory.CREDIT_CARD -> Triple(
            Icons.Default.CreditCard,
            Color(0xFF7C3AED), // Royal Violet
            CreditCardTagBg
        )
        OfferCategory.DINING -> Triple(
            Icons.Default.Restaurant,
            Color(0xFFEA580C), // Tangerine Coral
            DiningTagBg
        )
        OfferCategory.ELECTRONICS -> Triple(
            Icons.Default.Devices,
            Color(0xFF2563EB), // Electric Blue
            TechTagBg
        )
        OfferCategory.FASHION -> Triple(
            Icons.Default.ShoppingBag,
            Color(0xFFDB2777), // Vibrant Pink
            Color(0xFFFCE7F3)
        )
    }
}
