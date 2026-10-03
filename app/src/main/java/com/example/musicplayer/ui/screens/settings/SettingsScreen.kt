package com.example.musicplayer.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.musicplayer.ui.theme.PastelBorder
import com.example.musicplayer.ui.theme.PastelCard
import com.example.musicplayer.ui.theme.PastelCardElevated
import com.example.musicplayer.ui.theme.PastelDarkBg
import com.example.musicplayer.ui.theme.PastelLavender
import com.example.musicplayer.ui.theme.PastelMint
import com.example.musicplayer.ui.theme.PastelRose
import com.example.musicplayer.ui.theme.PastelTextPrimary
import com.example.musicplayer.ui.theme.PastelTextSecondary

import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import com.example.musicplayer.util.EqualizerState

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val providers by viewModel.providersState.collectAsState()
    val activeProviderId by viewModel.activeProviderId.collectAsState()
    val eqState by viewModel.eqState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = PastelDarkBg
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 20.dp, bottom = 110.dp)
        ) {
            item {
                Text(
                    text = "Settings",
                    color = PastelTextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "Manage sound quality, audio equalizer, and music providers",
                    color = PastelTextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                )
            }

            // Audio Equalizer & Sound Enhancer Section
            item {
                EqualizerCard(
                    state = eqState,
                    onToggleEnabled = { viewModel.audioEffectManager.toggleEnabled(it) },
                    onSelectPreset = { viewModel.audioEffectManager.setPreset(it) },
                    onBandLevelChange = { index, level -> viewModel.audioEffectManager.setBandLevel(index, level) },
                    onBassBoostChange = { viewModel.audioEffectManager.setBassBoost(it) },
                    onLoudnessGainChange = { viewModel.audioEffectManager.setLoudnessGain(it) }
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Music Sources & Providers",
                    color = PastelTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            items(providers, key = { it.providerId }) { providerState ->
                val isActive = providerState.providerId == activeProviderId
                ProviderSettingCard(
                    state = providerState,
                    isActive = isActive,
                    onSelectActive = { viewModel.selectActiveProvider(providerState.providerId) },
                    onKeyChange = { viewModel.onKeyInputChanged(providerState.providerId, it) },
                    onSaveKey = { viewModel.saveKey(providerState.providerId) },
                    onDeleteKey = { viewModel.deleteKey(providerState.providerId) },
                    onTestConnection = { viewModel.testConnection(providerState.providerId) }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ProviderSettingCard(
    state: ProviderUiState,
    isActive: Boolean,
    onSelectActive: () -> Unit,
    onKeyChange: (String) -> Unit,
    onSaveKey: () -> Unit,
    onDeleteKey: () -> Unit,
    onTestConnection: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (isActive) 1.5.dp else 1.dp,
                color = if (isActive) PastelMint else PastelBorder,
                shape = RoundedCornerShape(16.dp)
            ),
        color = PastelCard
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isActive,
                        onClick = onSelectActive,
                        colors = RadioButtonDefaults.colors(
                            selectedColor = PastelMint,
                            unselectedColor = PastelTextSecondary
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = state.displayName,
                            color = PastelTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isActive) "Active Source" else "Tap radio to activate",
                            color = if (isActive) PastelMint else PastelTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))
                StatusBadge(status = state.status)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Key Input Info
            Text(
                text = if (state.requiresApiKey) "API Key / Client ID (Securely Encrypted)" else "Optional Custom Key (Free Out of the Box)",
                color = PastelTextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = state.inputKey,
                onValueChange = onKeyChange,
                placeholder = {
                    Text(
                        if (state.requiresApiKey) "Enter key..." else "Default open configuration",
                        color = PastelTextSecondary.copy(alpha = 0.6f),
                        fontSize = 13.sp
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = PastelCardElevated,
                    unfocusedContainerColor = PastelCardElevated,
                    focusedTextColor = PastelTextPrimary,
                    unfocusedTextColor = PastelTextPrimary,
                    focusedBorderColor = PastelMint,
                    unfocusedBorderColor = PastelBorder
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Buttons: Test Connection, Save, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onTestConnection,
                    enabled = !state.isTesting,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PastelTextPrimary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PastelBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    if (state.isTesting) {
                        CircularProgressIndicator(
                            color = PastelMint,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Test Connection", fontSize = 12.sp)
                    }
                }

                Button(
                    onClick = onSaveKey,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PastelMint)
                ) {
                    Icon(
                        Icons.Filled.Save,
                        contentDescription = "Save",
                        tint = Color(0xFF1A1202),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save", color = Color(0xFF1A1202), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                if (state.currentKey.isNotBlank()) {
                    IconButton(onClick = onDeleteKey) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Delete",
                            tint = PastelRose,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            if (state.testMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = state.testMessage,
                    color = if (state.status == "Active") PastelMint else PastelRose,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val (bgColor, textColor, label) = when (status) {
        "Active" -> Triple(PastelMint.copy(alpha = 0.15f), PastelMint, "Active")
        "Invalid" -> Triple(PastelRose.copy(alpha = 0.15f), PastelRose, "Invalid")
        else -> Triple(PastelCardElevated, PastelTextSecondary, "Not Configured")
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(1.dp, textColor.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun EqualizerCard(
    state: EqualizerState,
    onToggleEnabled: (Boolean) -> Unit,
    onSelectPreset: (String) -> Unit,
    onBandLevelChange: (Int, Int) -> Unit,
    onBassBoostChange: (Int) -> Unit,
    onLoudnessGainChange: (Int) -> Unit
) {
    val presets = listOf("Flat", "Bass Booster", "Treble Booster", "Vocal", "Rock", "Pop", "Jazz", "Classical")
    val frequencies = listOf("60 Hz", "230 Hz", "910 Hz", "3.6 kHz", "14 kHz")

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, PastelBorder, RoundedCornerShape(18.dp)),
        color = PastelCard
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row: Icon, Title & Enable Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(PastelMint.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Tune,
                            contentDescription = "Equalizer",
                            tint = PastelMint,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Audio Equalizer & Enhancer",
                            color = PastelTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (state.isEnabled) "Hardware DSP Active" else "Disabled",
                            color = if (state.isEnabled) PastelMint else PastelTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                Switch(
                    checked = state.isEnabled,
                    onCheckedChange = onToggleEnabled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = PastelMint,
                        uncheckedThumbColor = PastelTextSecondary,
                        uncheckedTrackColor = PastelCardElevated
                    )
                )
            }

            if (state.isEnabled) {
                Spacer(modifier = Modifier.height(18.dp))

                // Presets Chips
                Text(
                    text = "Preset Profile",
                    color = PastelTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(presets) { preset ->
                        val isSelected = state.selectedPreset == preset
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectPreset(preset) },
                            label = {
                                Text(
                                    text = preset,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PastelMint,
                                selectedLabelColor = Color(0xFFFFFFFF),
                                containerColor = PastelCardElevated,
                                labelColor = PastelTextSecondary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Bass Boost & Loudness Gain Sliders Side-by-Side
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Bass Boost Slider
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Sub Bass Boost", color = PastelTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("${state.bassBoost}%", color = PastelMint, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Slider(
                            value = state.bassBoost.toFloat(),
                            onValueChange = { onBassBoostChange(it.toInt()) },
                            valueRange = 0f..100f,
                            colors = SliderDefaults.colors(
                                thumbColor = PastelMint,
                                activeTrackColor = PastelMint,
                                inactiveTrackColor = PastelCardElevated
                            )
                        )
                    }

                    // Loudness Gain Slider
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Volume Gain", color = PastelTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("+${state.loudnessGain} dB", color = PastelLavender, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Slider(
                            value = state.loudnessGain.toFloat(),
                            onValueChange = { onLoudnessGainChange(it.toInt()) },
                            valueRange = 0f..12f,
                            colors = SliderDefaults.colors(
                                thumbColor = PastelLavender,
                                activeTrackColor = PastelLavender,
                                inactiveTrackColor = PastelCardElevated
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 5-Band Frequency Sliders
                Text(
                    text = "5-Band Equalizer Frequency Control",
                    color = PastelTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(10.dp))

                frequencies.forEachIndexed { index, freq ->
                    val level = state.bandLevels.getOrElse(index) { 0 }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = freq,
                            color = PastelTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.width(60.dp)
                        )
                        Slider(
                            value = level.toFloat(),
                            onValueChange = { onBandLevelChange(index, it.toInt()) },
                            valueRange = -15f..15f,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = PastelMint,
                                activeTrackColor = PastelMint,
                                inactiveTrackColor = PastelCardElevated
                            )
                        )
                        Text(
                            text = if (level > 0) "+$level dB" else "$level dB",
                            color = if (level != 0) PastelMint else PastelTextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(55.dp)
                        )
                    }
                }
            }
        }
    }
}
