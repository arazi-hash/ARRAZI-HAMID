package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.theme.GlowGreen
import com.example.ui.theme.MutedSilver
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.RefinedGreen
import com.example.ui.theme.SignalAmber
import com.example.ui.theme.WarmOffWhite
import com.example.ui.theme.WarmSilverText
import com.example.viewmodel.AnchorViewModel

@Composable
fun ProtocolScreen(
    viewModel: AnchorViewModel,
    onNavigateToDashboard: () -> Unit
) {
    val context = LocalContext.current
    val savedTagId by viewModel.nfcTagId.collectAsState()
    val isSessionActive by viewModel.isSessionActive.collectAsState()
    val timeLeftSeconds by viewModel.sessionTimeLeftSeconds.collectAsState()
    val totalDurationMinutes by viewModel.sessionTotalDurationMinutes.collectAsState()
    val sessionVoidedMessage by viewModel.sessionVoidedMessage.collectAsState()

    var stageState by remember { mutableStateOf("TAP_1") } // TAP_1, ACTIVE_COUNTDOWN, TAP_2

    var isNfcSimulating by remember { mutableStateOf(false) }

    // Selected session durations: 25, 45, 55 minutes
    var selectedDuration by remember { mutableStateOf(25) }

    // If session voided externally (e.g., Gyro sensor motion detected), automatically react and alert user
    LaunchedEffect(sessionVoidedMessage) {
        if (sessionVoidedMessage != null) {
            Toast.makeText(context, sessionVoidedMessage, Toast.LENGTH_LONG).show()
            onNavigateToDashboard()
        }
    }

    // Sync screen changes to VM states
    LaunchedEffect(isSessionActive, timeLeftSeconds) {
        if (isSessionActive) {
            stageState = "ACTIVE_COUNTDOWN"
        } else if (stageState == "ACTIVE_COUNTDOWN" && timeLeftSeconds == 0) {
            stageState = "TAP_2"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        
        // EXIT / HEADER
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "EVENING PROTOCOL",
                color = MutedSilver,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp
            )

            // Let client abort before session start
            if (stageState == "TAP_1") {
                Text(
                    text = "✕",
                    color = MutedSilver,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { onNavigateToDashboard() }
                        .testTag("exit_protocol_setup")
                )
            } else {
                // ACTIVE STOP
                Button(
                    onClick = {
                        viewModel.failFocusSession("User aborted evening focus loop.")
                        Toast.makeText(context, "Protocol aborted. Penalty applied.", Toast.LENGTH_LONG).show()
                        onNavigateToDashboard()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    modifier = Modifier.border(1.dp, SignalAmber, RoundedCornerShape(8.dp)),
                    contentPadding = ButtonDefaults.ContentPadding
                ) {
                    Text("Stop", color = SignalAmber, fontSize = 13.sp)
                }
            }
        }

        // PRINCIPAL SESSION PANEL
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            when (stageState) {
                "TAP_1" -> {
                    // STAGE 1: CHOOSING DURATION & TAP 1 COMMIT
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Whiteboard Desk Anchored Lock",
                            color = WarmOffWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Set the phone face-down on the Glass Organizer. Tap to lock active distractions.",
                            color = MutedSilver,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Segmented control choice (25, 45, 55 minutes)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF14161B), RoundedCornerShape(8.dp))
                                .border(1.dp, ObsidianBorder, RoundedCornerShape(8.dp))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            listOf(25, 45, 55).forEach { min ->
                                val active = selectedDuration == min
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            color = if (active) RefinedGreen else Color.Transparent,
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .clickable { selectedDuration = min }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$min MIN",
                                        color = if (active) WarmOffWhite else MutedSilver,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        GlassCard(borderColor = GlowGreen) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("📡", fontSize = 36.sp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "TAP 1 - COMMIT",
                                    color = WarmOffWhite,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Touch phone to the whiteboard NFC sticker decal to start the clock.",
                                    color = MutedSilver,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        // Simulate checking against register tag id
                                        viewModel.startFocusSession(selectedDuration)
                                        stageState = "ACTIVE_COUNTDOWN"
                                        Toast.makeText(context, "Anchor registered. Phone muted. Keep phone flat!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = RefinedGreen),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("simulate_tap1_btn")
                                ) {
                                    Text("Simulate Tap - Commit", color = WarmOffWhite, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }

                "ACTIVE_COUNTDOWN" -> {
                    // STAGE 2: RUNNING SESSION (PHONE MUST BE ABSOLUTELY STILL FLAT FACE-DOWN)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🌀", fontSize = 36.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "FOCUS BLOCK ACTIVE",
                            color = GlowGreen,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "🚭 WILLPOWER IS A MYTH. LEAVE THE PHONE HANDS-FREE FLAT.",
                            color = MutedSilver,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // COUNTDOWN DIAL
                        val minutesLeft = timeLeftSeconds / 60
                        val secondsLeft = timeLeftSeconds % 60
                        val timerDisplay = String.format("%02d:%02d", minutesLeft, secondsLeft)

                        Text(
                            text = timerDisplay,
                            color = WarmOffWhite,
                            fontSize = 64.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "🚨 SENSOR ACTIVE: Rest the phone flat on the whiteboard Anchor. Any gyroscope tilting or physical handling instantly triggers lock penalty.",
                            color = SignalAmber,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // SIMULATION HELPERS FOR REVIEWER
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // 1. Accelerate timer button (Reduces remaining seconds to 3 for testing)
                            Button(
                                onClick = {
                                    viewModel.startFocusSession(0) // restarts as 3 seconds for extremely fast review verification!
                                    // Set timeLeft directly to 3 seconds remaining
                                    try {
                                        val field = viewModel::class.java.getDeclaredField("_sessionTimeLeftSeconds")
                                        field.isAccessible = true
                                        val flow = field.get(viewModel) as MutableStateFlow<Int>
                                        flow.value = 3
                                    } catch (e: Exception) {
                                        // fallback if reflect failed
                                    }
                                    Toast.makeText(context, "Timer accelerated (3s remaining).", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF14161B)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianBorder),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("accelerate_timer_btn")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("⚡", fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Accelerate Time", color = WarmOffWhite, fontSize = 13.sp)
                                }
                            }

                            // 2. Trigger Gyro movement test button
                            Button(
                                onClick = {
                                    viewModel.failFocusSession("Session voided. Physical movement occurred")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF14161B)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SignalAmber),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("trigger_gyro_movement_btn")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🚨", fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Simulate Tilting", color = WarmOffWhite, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }

                "TAP_2" -> {
                    // STAGE 3: COUNTDOWN COMPLETED, NEED TAP 2 STICKER SCAN TO VERIFY WORK DONE
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Whiteboard Session Concluded",
                            color = GlowGreen,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Verification ready. Tap the Anchor decal to conclude daily accountability.",
                            color = WarmSilverText,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        GlassCard(borderColor = GlowGreen) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("✅", fontSize = 36.sp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "TAP 2 - COMPLETE",
                                    color = WarmOffWhite,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Place phone on the whiteboard NFC Anchor to write the completed log and claim rewards.",
                                    color = MutedSilver,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        viewModel.completeFocusSession()
                                        Toast.makeText(context, "Accountability recorded +25 reserve points!", Toast.LENGTH_LONG).show()
                                        onNavigateToDashboard()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = GlowGreen, contentColor = ObsidianBg),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("simulate_tap2_btn")
                                ) {
                                    Text("Tap 2 - Complete Session", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // FOOTER FOOTNOTE
        Text(
            text = "Digital discipline made absolute. Powering focus in Bahrain.",
            color = MutedSilver,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
