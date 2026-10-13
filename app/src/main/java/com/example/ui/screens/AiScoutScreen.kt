package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CategoryBadge
import com.example.ui.components.OfferCard
import com.example.ui.viewmodel.OfferViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiScoutScreen(
    viewModel: OfferViewModel,
    modifier: Modifier = Modifier
) {
    val isAiLoading by viewModel.isAiLoading.collectAsState()
    val filterState by viewModel.filterState.collectAsState()
    val aiInsights by viewModel.aiInsights.collectAsState()
    val offers by viewModel.displayedOffers.collectAsState()
    val savedIds by viewModel.savedOfferIds.collectAsState()
    val reminders by viewModel.reminders.collectAsState()

    val reminderIds = reminders.map { it.offerId }.toSet()
    val aiDiscoveredOffers = offers.filter { it.isAiDiscovered }

    var promptInput by remember { mutableStateOf("") }

    val quickPrompts = remember(filterState.selectedCountry, filterState.selectedCity) {
        if (filterState.selectedCountry.equals("Saudi Arabia", ignoreCase = true)) {
            listOf(
                "Amazon.sa electronics & phone deals in Riyadh",
                "ClickFlyers weekly Panda & Lulu hypermarket flyers",
                "Al Rajhi & SNB bank credit card dining perks",
                "Saudia Airlines & flynas flight offers from Riyadh"
            )
        } else {
            listOf(
                "Amazon & supermarket deals in ${filterState.selectedCity}",
                "ClickFlyers weekly grocery flyers in ${filterState.selectedCountry}",
                "Top credit card dining & travel bonuses this week",
                "Latest flights and hotel flash sales from ${filterState.selectedCity}"
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("ai_scout_screen")
    ) {
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AI Deal Scout",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = { viewModel.refreshInsights() },
                    modifier = Modifier.testTag("refresh_ai_insights_btn")
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh Insights")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero AI Intro Card
            item(key = "ai_hero_card") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    text = "GEMINI 3.5 FLASH",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontSize = 10.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Live Global Scanner",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "AI Scout continuously scours regional sources for current perks, promo codes, and expiry deadlines matching your habits.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Prompt input
                        OutlinedTextField(
                            value = promptInput,
                            onValueChange = { promptInput = it },
                            placeholder = { Text("Ask AI to find specific deals or card perks...") },
                            trailingIcon = {
                                if (isAiLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    IconButton(
                                        onClick = {
                                            if (promptInput.isNotBlank()) {
                                                viewModel.discoverAiDeals(promptInput)
                                                promptInput = ""
                                            }
                                        },
                                        modifier = Modifier.testTag("send_ai_prompt_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Send,
                                            contentDescription = "Send",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("ai_prompt_input")
                        )
                    }
                }
            }

            // Quick Prompt Suggestions
            item(key = "quick_prompts") {
                Column {
                    Text(
                        text = "Suggested Deal Searches",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        quickPrompts.forEach { q ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.testTag("quick_prompt_chip")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lightbulb,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = q,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(end = 4.dp)
                                    )
                                    Button(
                                        onClick = { viewModel.discoverAiDeals(q) },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text("Scan", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Proactive AI Pattern Reminders
            item(key = "pattern_insights_title") {
                Text(
                    text = "Proactive Reminders (Based on Your Pattern)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            items(
                items = aiInsights,
                key = { it.id }
            ) { insight ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CategoryBadge(category = insight.relatedCategory)
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (insight.priority == "HIGH") MaterialTheme.colorScheme.error.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (insight.priority == "HIGH") "URGENT ALERT" else "RECOMMENDED",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (insight.priority == "HIGH") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = insight.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = insight.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = {
                                    viewModel.updateCategory(insight.relatedCategory)
                                    viewModel.navigateTo(com.example.ui.viewmodel.AppScreen.HOME)
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(insight.actionLabel)
                            }
                        }
                    }
                }
            }

            // AI Discovered Offers section
            if (aiDiscoveredOffers.isNotEmpty()) {
                item(key = "ai_discovered_header") {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Newly Discovered Live Offers (${aiDiscoveredOffers.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                items(
                    items = aiDiscoveredOffers,
                    key = { "ai_scout_" + it.id }
                ) { offer ->
                    OfferCard(
                        offer = offer,
                        isSaved = savedIds.contains(offer.id),
                        hasReminder = reminderIds.contains(offer.id),
                        onCardClick = { viewModel.selectOffer(offer) },
                        onToggleSave = { viewModel.toggleSaveOffer(offer) },
                        onSetReminderClick = { viewModel.openReminderDialog(offer) }
                    )
                }
            }
        }
    }
}
