package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import com.example.viewmodel.AnchorViewModel

@Composable
fun OnboardingScreen(
    viewModel: AnchorViewModel,
    onFinishOnboarding: () -> Unit
) {
    var currentStep by remember { mutableIntStateOf(1) }
    val nfcTagId by viewModel.nfcTagId.collectAsState()
    val sanctuaryType by viewModel.sanctuaryType.collectAsState()
    val eveningProtocolTime by viewModel.eveningProtocolTime.collectAsState()

    var isNfcScanning by remember { mutableStateOf(false) }
    var locationSaved by remember { mutableStateOf(false) }

    // Sanctuary setup variables
    var selectedSanctuary by remember { mutableStateOf("mosque") }

    // Evening time values
    var selectedHour by remember { mutableIntStateOf(22) }
    var selectedMinute by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // TOP HEADER
        Column {
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CLIENT ONBOARDING",
                    color = MutedSilver,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "$currentStep of 3",
                    color = GlowGreen,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { currentStep / 3f },
                modifier = Modifier.fillMaxWidth(),
                color = GlowGreen,
                trackColor = ObsidianBorder
            )
        }

        // STEP CONTENT CARDS
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut() using SizeTransform(clip = false)
                },
                label = "step_animation"
            ) { step ->
                when (step) {
                    1 -> {
                        // Card 1: NFC Tag Registration
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📡", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "NFC Tag Registration",
                                color = WarmOffWhite,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Place your physical Anchor Tag on the back of this phone. This decal binds your phone to the physical glass whiteboard desk Anchor.",
                                color = MutedSilver,
                                fontSize = 15.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            Spacer(modifier = Modifier.height(24.dp))

                            GlassCard {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    if (nfcTagId.isEmpty()) {
                                        Text(
                                            text = if (isNfcScanning) "Listening for NFC physical tap..." else "Anchor Decal Unregistered",
                                            color = SignalAmber,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    } else {
                                        Text(
                                            text = "Anchor Bound Successfully",
                                            color = GlowGreen,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "ID: $nfcTagId",
                                            color = MutedSilver,
                                            fontSize = 14.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Button(
                                        onClick = {
                                            isNfcScanning = true
                                            // Simulate quick NFC read trigger of whiteboard NFC tag
                                            viewModel.registerMockNfcTag("ANCHOR_NFC_09X88")
                                            isNfcScanning = false
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = RefinedGreen),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("scan_nfc_onboarding_btn")
                                    ) {
                                        Text("Scan Tag", color = WarmOffWhite)
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        // Card 2: Sanctuary Setup
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📍", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Choose Your Sanctuary",
                                color = WarmOffWhite,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "This is your physical domain of digital redemption. If you face an evening lock, physically traveling to this sanctuary is the only way to restore app access.",
                                color = MutedSilver,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // Sanctuary Selection Options
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                // MOSQUE (Highlight design first)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(ObsidianSurface, RoundedCornerShape(12.dp))
                                        .border(
                                            width = 2.dp,
                                            color = if (selectedSanctuary == "mosque") GlowGreen else ObsidianBorder,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable {
                                            selectedSanctuary = "mosque"
                                            locationSaved = false
                                        }
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("🕌", fontSize = 32.sp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("The Mosque", color = WarmOffWhite, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                                        Text(
                                            "Highest redemption value. Fajr earns bonus points.",
                                            color = WarmSilverText,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                // PARK
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF14161B), RoundedCornerShape(8.dp))
                                        .border(
                                            width = 1.dp,
                                            color = if (selectedSanctuary == "park") GlowGreen else ObsidianBorder,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            selectedSanctuary = "park"
                                            locationSaved = false
                                        }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("🏞️", fontSize = 32.sp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("The Community Park", color = WarmOffWhite, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                                        Text("Standard redemption coordinates in nature.", color = WarmSilverText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }

                                // RUNNING TRAIL
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF14161B), RoundedCornerShape(8.dp))
                                        .border(
                                            width = 1.dp,
                                            color = if (selectedSanctuary == "trail") GlowGreen else ObsidianBorder,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            selectedSanctuary = "trail"
                                            locationSaved = false
                                        }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("🏃", fontSize = 32.sp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("Refuge Running Trail", color = WarmOffWhite, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                                        Text("Active physical exercise redemption space.", color = WarmSilverText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    // Save mock GPS location depending on selection
                                    val (lat, lng) = when (selectedSanctuary) {
                                        "mosque" -> Pair(26.2285f, 50.5860f) // Al Fateh Grand Mosque, Manama
                                        "park" -> Pair(26.2150f, 50.5700f) // Khalifa Park
                                        else -> Pair(26.1900f, 50.5500f) // Trail
                                    }
                                    viewModel.saveSanctuary(selectedSanctuary, lat, lng)
                                    locationSaved = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = RefinedGreen),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("set_gps_sanctuary_btn")
                            ) {
                                Text(if (locationSaved) "Sanctuary saved." else "Set GPS Location", color = WarmOffWhite)
                            }
                        }
                    }

                    3 -> {
                        // Card 3: Evening Protocol Time Selection
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("⏱️", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Evening Protocol Time",
                                color = WarmOffWhite,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Set the deadline of accountability. You must complete the Two-Tap Whiteboard session on your physical Anchor daily before this hour to bypass penalties.",
                                color = MutedSilver,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            Spacer(modifier = Modifier.height(24.dp))

                            GlassCard {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Accountability Deadline",
                                        color = MutedSilver,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    // Time display text
                                    val formattedTime = String.format("%02d:%02d PM", selectedHour, selectedMinute)
                                    Text(
                                        text = formattedTime,
                                        color = WarmOffWhite,
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    
                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Custom visual selectors for Hour & Minute
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("Hour", fontSize = 13.sp, color = MutedSilver)
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "◀",
                                                    color = GlowGreen,
                                                    fontSize = 18.sp,
                                                    modifier = Modifier.clickable {
                                                        if (selectedHour > 18) selectedHour -= 1
                                                    }
                                                )
                                                Text(
                                                    "$selectedHour",
                                                    color = WarmOffWhite,
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 8.dp)
                                                )
                                                Text(
                                                    text = "▶",
                                                    color = GlowGreen,
                                                    fontSize = 18.sp,
                                                    modifier = Modifier.clickable {
                                                        if (selectedHour < 23) selectedHour += 1
                                                    }
                                                )
                                            }
                                        }

                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("Minute", fontSize = 13.sp, color = MutedSilver)
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "◀",
                                                    color = GlowGreen,
                                                    fontSize = 18.sp,
                                                    modifier = Modifier.clickable {
                                                        if (selectedMinute >= 15) selectedMinute -= 15
                                                    }
                                                )
                                                Text(
                                                    String.format("%02d", selectedMinute),
                                                    color = WarmOffWhite,
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 8.dp)
                                                )
                                                Text(
                                                    text = "▶",
                                                    color = GlowGreen,
                                                    fontSize = 18.sp,
                                                    modifier = Modifier.clickable {
                                                        if (selectedMinute <= 30) selectedMinute += 15
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // NAVIGATION ACTIONS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (currentStep > 1) {
                Button(
                    onClick = { currentStep -= 1 },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    modifier = Modifier.border(1.dp, ObsidianBorder, RoundedCornerShape(8.dp))
                ) {
                    Text("Back", color = WarmOffWhite)
                }
            } else {
                Spacer(modifier = Modifier.width(48.dp))
            }

            if (currentStep < 3) {
                // Next step buttons
                val nextEnabled = when (currentStep) {
                    1 -> nfcTagId.isNotEmpty()
                    2 -> locationSaved
                    else -> true
                }

                Button(
                    onClick = { currentStep += 1 },
                    enabled = nextEnabled,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RefinedGreen,
                        disabledContainerColor = Color(0xFF14161B),
                        contentColor = WarmOffWhite,
                        disabledContentColor = MutedSilver
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Continue", fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("➔", color = WarmOffWhite, fontSize = 14.sp)
                                    }
                }
            } else {
                // Final begin button!
                Button(
                    onClick = {
                        val finalTimeStr = String.format("%02d:%02d", selectedHour, selectedMinute)
                        viewModel.saveEveningProtocolTime(finalTimeStr)
                        onFinishOnboarding()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GlowGreen, contentColor = ObsidianBg),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("begin_onboarding_btn")
                ) {
                    Text("Begin", fontWeight = FontWeight.Black)
                }
            }
        }
    }
}
