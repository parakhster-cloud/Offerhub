package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.OfferCategory

@Composable
fun CategoryFilterRow(
    selectedCategory: OfferCategory,
    onCategorySelected: (OfferCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OfferCategory.values().forEach { category ->
            val isSelected = selectedCategory == category
            val icon = when (category) {
                OfferCategory.ALL -> Icons.Default.LocalOffer
                OfferCategory.CREDIT_CARD -> Icons.Default.CreditCard
                OfferCategory.TRAVEL -> Icons.Default.Flight
                OfferCategory.SUPERMARKET -> Icons.Default.ShoppingCart
                OfferCategory.DINING -> Icons.Default.Restaurant
                OfferCategory.ELECTRONICS -> Icons.Default.Devices
                OfferCategory.FASHION -> Icons.Default.ShoppingBag
            }

            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelected(category) },
                label = { Text(text = category.displayName) },
                leadingIcon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                shape = RoundedCornerShape(20.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.testTag("category_chip_${category.name}")
            )
        }
    }
}
