package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
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
fun SurvivalScreen(
    viewModel: AnchorViewModel,
    onNavigateToDashboard: () -> Unit
) {
    val context = LocalContext.current
    val sanctuaryType by viewModel.sanctuaryType.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .padding(24.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // WARNING DECAL LOCK HEADER
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Active lock warning",
            tint = SignalAmber,
            modifier = Modifier.height(48.dp).width(48.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "YOUR ACCESS IS LOCKED",
            color = WarmOffWhite,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
        Text(
            text = "One action restores it.",
            color = SignalAmber,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        // SANCTUARY RECOVERY ACTION CARD
        val (emoji, title, lat, lng) = when (sanctuaryType) {
            "mosque" -> quadruple("🕌", "Go to the Mosque", 26.2285f, 50.5860f)
            "park" -> quadruple("🏞️", "Go to the Community Park", 26.2150f, 50.5700f)
            else -> quadruple("🏃", "Travel to Refuge Running Trail", 26.1900f, 50.5500f)
        }

        GlassCard(borderColor = SignalAmber) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(emoji, fontSize = 44.sp)
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = title,
                    color = WarmOffWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Verify your presence at your Sanctuary to restore your Reserve Points and reopen access.",
                    color = WarmSilverText,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // GPS VERIFY BUTTON
                Button(
                    onClick = {
                        // Standard coordinate mismatch alert (can prompt real Location API queries or alert fail)
                        Toast.makeText(
                            context,
                            "You are not at your Sanctuary yet. Please move closer to the physical coordinates.",
                            Toast.LENGTH_LONG
                        ).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SignalAmber),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("verify_gps_btn")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Place, contentDescription = "Query coordinates", tint = SignalAmber)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("I Am Here — Verify", color = SignalAmber, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // V0 EMULATOR SIMULATE ARRIVAL CTA
                Button(
                    onClick = {
                        viewModel.addPoints(50, "Sanctuary Alignment Restored", "redemption")
                        viewModel.setLockedState(false)
                        Toast.makeText(context, "Sanctuary verified! +50 reserve points. Access restored.", Toast.LENGTH_LONG).show()
                        onNavigateToDashboard()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RefinedGreen),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("simulate_gps_arrival_btn")
                ) {
                    Text("Simulate Sanctuary Arrival", color = WarmOffWhite, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // REPLACEMENT BEHAVIORS SEGMENT
        Text(
            text = "ANALOG ACTIONS TO TAKE NOW",
            color = MutedSilver,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Row 1: Journal Prompt
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x08FFFFFF), RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(8.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("📖", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Current Journal Prompt", color = WarmOffWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("Write one page about what you are avoiding.", color = MutedSilver, fontSize = 11.sp)
                }
            }

            // Row 2: Read physical book
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x08FFFFFF), RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(8.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("📚", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Unplugged Literary Time", color = WarmOffWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("Read one chapter of your physical book.", color = MutedSilver, fontSize = 11.sp)
                }
            }

            // Row 3: Mosque Prayer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x08FFFFFF), RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(8.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🕌", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Attend Secondary Devotions", color = WarmOffWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("Attend the next prayer at your Sanctuary.", color = MutedSilver, fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // PREMIUM SPIRITUAL NUDGE
        GlassCard(borderColor = ObsidianBorder) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = "Spiritual tip", tint = GlowGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("First-Light Focus Protocol", color = GlowGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Fajr / sitting in the mosque between Maghrib prayer and Isha prayer earns the most, but social apps stay locked until after sunrise to protect first-light focus.",
                    color = MutedSilver,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // SURVIVAL KIT LINK NUDGE
        Row(
            modifier = Modifier
                .clickable {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://get-analog-anchor.com/survivalkit"))
                    context.startActivity(intent)
                }
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Unlocking the Survival Kit Toolkit",
                color = GlowGreen,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textDecoration = TextDecoration.Underline
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.Launch,
                contentDescription = "Launch external toolkit link",
                tint = GlowGreen,
                modifier = Modifier.height(14.dp).width(14.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// Helper quadruple structure
private data class Quadruple<out A, out B, out C, out D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

private fun <A, B, C, D> quadruple(first: A, second: B, third: C, fourth: D): Quadruple<A, B, C, D> {
    return Quadruple(first, second, third, fourth)
}
