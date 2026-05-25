package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Brightness5
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: AnchorViewModel,
    onNavigateToProtocol: () -> Unit,
    onNavigateToSurvival: () -> Unit,
    onNavigateToFounderReset: () -> Unit
) {
    val context = LocalContext.current
    val reservePoints by viewModel.reservePoints.collectAsState()
    val isLocked by viewModel.isLocked.collectAsState()
    val sanctuaryType by viewModel.sanctuaryType.collectAsState()
    val sessionLogs by viewModel.sessionLogs.collectAsState()

    // Testing overrides
    var simulateFajrHour by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        
        // TOP CONTROL HEADER
        Column {
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SYSTEM STATUS",
                        color = MutedSilver,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(8.dp)
                                .height(8.dp)
                                .background(GlowGreen, shape = RoundedCornerShape(4.dp))
                        )
                        Text(
                            text = "Device Admin Active",
                            color = WarmSilverText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "AR-RAZI V1.0",
                        color = MutedSilver,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = {
                            viewModel.resetFounderPanel()
                            onNavigateToFounderReset()
                            Toast.makeText(context, "Configurations factory reset.", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("admin_reset_icon")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Reset configurations",
                            tint = MutedSilver
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // RESERVE CURRENCY BOARD (Elegant Dark custom styling)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        BorderStroke(1.dp, Color(0x1AFFFFFF)), // elegant border-white/10
                        RoundedCornerShape(12.dp)
                    )
                    .background(Color(0x08FFFFFF), shape = RoundedCornerShape(12.dp)) // bg-white/[0.03]
                    .padding(vertical = 24.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "RESERVE POINTS",
                        color = MutedSilver,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 3.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "$reservePoints",
                        color = WarmOffWhite, // #F5F2ED elegant typography
                        fontSize = 60.sp,
                        fontWeight = FontWeight.Light, // font-light from elegant dark spec
                        fontFamily = FontFamily.SansSerif,
                        letterSpacing = (-1.5).sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // Al-Murabit Rank Badge
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF1A1A1A), RoundedCornerShape(100.dp))
                            .border(BorderStroke(1.dp, Color(0x1AFFFFFF)), RoundedCornerShape(100.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Rank: Al-Murabit",
                            color = GlowGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.5.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // STATUS SECURITY DECAL CARD (Matching the Elegant Dark Access Card styling)
            val cardStrokeColor = if (isLocked) SignalAmber.copy(alpha = 0.5f) else Color(0x1AFFFFFF)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        BorderStroke(1.dp, cardStrokeColor),
                        RoundedCornerShape(12.dp)
                    )
                    .background(Color(0x08FFFFFF), shape = RoundedCornerShape(12.dp)) // bg-white/[0.03]
                    .padding(20.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (isLocked) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Your Access Is Locked",
                                    color = WarmOffWhite,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = "Verify your presence at your Sanctuary to reopen access.",
                                    color = MutedSilver,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                            Text("🚨", fontSize = 24.sp)
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color(0x0FFFFFFF)) // border-white/5
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        val sanctuaryLabel = when (sanctuaryType) {
                            "mosque" -> "Al-Fateh Grand Mosque"
                            "park" -> "Community Park Sanctuary"
                            else -> "Refuge Running Trail Sanctuary"
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("🕌", fontSize = 18.sp)
                            Column {
                                Text(
                                    text = "ACTIVE SANCTUARY",
                                    color = MutedSilver,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = sanctuaryLabel,
                                    color = SignalAmber,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Access Secured",
                                    color = WarmOffWhite,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = "Willpower is a myth. Protocol is everything.",
                                    color = MutedSilver,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                            Text("🛡️", fontSize = 24.sp)
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color(0x0FFFFFFF)) // border-white/5
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        val sanctuaryLabel = when (sanctuaryType) {
                            "mosque" -> "Al-Fateh Grand Mosque"
                            "park" -> "Community Park Sanctuary"
                            else -> "Refuge Running Trail Sanctuary"
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("🕌", fontSize = 18.sp)
                            Column {
                                Text(
                                    text = "ACTIVE SANCTUARY",
                                    color = MutedSilver,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = sanctuaryLabel,
                                    color = GlowGreen,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // MIDDLE TRIGGER SECTION
        Column {
            // "Start Evening Protocol" (Solid primary deep teal, dark text, high aesthetic)
            Button(
                onClick = { onNavigateToProtocol() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp) // h-14 height
                    .testTag("start_evening_protocol_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RefinedGreen, // #0D9488
                    contentColor = Color(0xFF0A0A0A) // Contrast dark ink
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "START EVENING PROTOCOL",
                        color = Color(0xFF0A0A0A),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // "Sanctuary Verify" (Secondary glass-charcoal button)
                Button(
                    onClick = { onNavigateToSurvival() },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp) // h-12 height
                        .testTag("sanctuary_check_in_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0x0DFFFFFF), // white/5
                        contentColor = WarmSilverText
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0x1AFFFFFF)) // border-white/10
                ) {
                    Text(
                        text = "CHECK-IN",
                        color = WarmSilverText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                }

                // "Fajr Bonus" button
                val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                val isFajrHoursActive = currentHour < 7 || simulateFajrHour
                
                Button(
                    onClick = {
                        val message = viewModel.claimFajrBonus()
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    },
                    enabled = isFajrHoursActive,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp) // h-12 height
                        .testTag("fajr_bonus_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0x19F5F2ED), // [#F5F2ED]/10
                        disabledContainerColor = Color(0x05FFFFFF),
                        contentColor = WarmOffWhite,
                        disabledContentColor = Color(0xFF444444)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isFajrHoursActive) Color(0x33F5F2ED) else Color(0x0DFFFFFF) // border-[#F5F2ED]/20
                    )
                ) {
                    Text(
                        text = "FAJR BONUS", 
                        color = if (isFajrHoursActive) WarmOffWhite else Color(0xFF666666), 
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Fajr Hour Mock Bypass
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Simulate Fajr Hour (<07:00 AM) for testing:",
                    color = MutedSilver,
                    fontSize = 10.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Switch(
                    checked = simulateFajrHour,
                    onCheckedChange = { simulateFajrHour = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = RefinedGreen,
                        checkedTrackColor = Color(0x330D9488)
                    ),
                    modifier = Modifier.testTag("simulate_fajr_switch")
                )
            }
        }

        // BOTTOM SESSION HISTORY COLUMN (Premium layout)
        Column(modifier = Modifier.weight(1f)) {
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "SURVIVAL LOG",
                color = MutedSilver,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))

            if (sessionLogs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color(0x03FFFFFF), shape = RoundedCornerShape(8.dp))
                        .border(BorderStroke(1.dp, Color(0x0FFFFFFF)), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No recorded logs. Complete a whiteboard session or visit your sanctuary to earn points.",
                        color = MutedSilver,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(1.dp) // tight arrangement as in template
                ) {
                    items(sessionLogs) { log ->
                        val dateString = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(log.timestamp))
                        
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0x05FFFFFF)) // bg-white/[0.02]
                                    .padding(vertical = 12.dp, horizontal = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = dateString, 
                                        color = Color(0xFF666666), // #666 timeline marker
                                        fontSize = 10.sp, 
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = log.action, 
                                        color = WarmSilverText, // #E5E5E5 text color
                                        fontSize = 12.sp, 
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                
                                val isPositive = log.pointsChange >= 0
                                val displayPoints = if (isPositive) "+${log.pointsChange}" else "${log.pointsChange}"
                                val displayColor = if (isPositive) GlowGreen else SignalAmber

                                Text(
                                    text = displayPoints,
                                    color = displayColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                            // Bottom separation line (border-b border-white/5)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(Color(0x0DFFFFFF)) // border-white/5
                            )
                        }
                    }
                }
            }
        }
    }
}
