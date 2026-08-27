package com.cabelas.smokercompanion.ui.screens.cook

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.OutdoorGrill
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cabelas.smokercompanion.data.model.CookPreset
import com.cabelas.smokercompanion.ui.theme.Charcoal700
import com.cabelas.smokercompanion.ui.theme.Charcoal800
import com.cabelas.smokercompanion.ui.theme.Charcoal900
import com.cabelas.smokercompanion.ui.theme.FlameAmber
import com.cabelas.smokercompanion.ui.theme.FlameOrange
import com.cabelas.smokercompanion.ui.theme.SmokerPrimary
import com.cabelas.smokercompanion.ui.theme.SuccessGreen
import com.cabelas.smokercompanion.ui.theme.TextGray
import com.cabelas.smokercompanion.ui.theme.TextWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookPlannerScreen(
    viewModel: CookPlannerViewModel,
    onPresetAppliedNavigateToDashboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    val presets by viewModel.presets.collectAsState()
    val selectedPreset by viewModel.selectedPreset.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "BBQ RECIPES & COOK PRESETS",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Charcoal900)
            )
        },
        containerColor = Charcoal900,
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = innerPadding.calculateBottomPadding() + 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Text(
                    text = "Select a pitmaster profile to automatically tune smoker temperature, smoke intensity, and probe target alarm.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextGray
                )
            }

            items(presets) { preset ->
                val isSelected = selectedPreset?.id == preset.id
                PresetCard(
                    preset = preset,
                    isSelected = isSelected,
                    onSelect = { viewModel.selectPreset(preset) },
                    onApply = {
                        viewModel.applyPresetToSmoker(preset)
                        onPresetAppliedNavigateToDashboard()
                    }
                )
            }
        }
    }
}

@Composable
private fun PresetCard(
    preset: CookPreset,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onApply: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Charcoal800),
        border = BorderStroke(1.dp, if (isSelected) FlameOrange else Charcoal700),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = preset.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = FlameOrange.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, FlameAmber)
                ) {
                    Text(
                        text = "${preset.recommendedGrillTempF}°F",
                        style = MaterialTheme.typography.labelMedium,
                        color = FlameAmber,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = preset.description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextGray
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Specs Row
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                SpecPill(icon = Icons.Default.Cloud, label = "Smoke ${preset.recommendedSmokeLevel}/10")
                SpecPill(icon = Icons.Default.LocalFireDepartment, label = "Pull @ ${preset.targetInternalTempF}°F")
                SpecPill(icon = Icons.Default.Schedule, label = "~${preset.estimatedDurationHours} hrs")
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Wood Recommendation
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Forest,
                    contentDescription = null,
                    tint = FlameAmber,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Wood Pairing: ${preset.woodPairing}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextWhite,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Pitmaster Tips
            if (preset.tips.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    preset.tips.forEach { tip ->
                        Row(modifier = Modifier.padding(vertical = 2.dp)) {
                            Icon(
                                imageVector = Icons.Default.TipsAndUpdates,
                                contentDescription = null,
                                tint = FlameAmber,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = tip,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextGray
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onApply,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FlameOrange,
                    contentColor = TextWhite
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.OutdoorGrill,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Apply Recipe to Smoker",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SpecPill(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Charcoal700
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = FlameAmber,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextWhite,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
