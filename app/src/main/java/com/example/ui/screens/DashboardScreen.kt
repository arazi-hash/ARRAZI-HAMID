package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.data.SessionLog
import com.example.viewmodel.AnchorViewModel
import kotlinx.coroutines.delay
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
    val nfcTagId by viewModel.nfcTagId.collectAsState()

    // Interactive translation and language togglers as seen in Lovable pages!
    var isEnglish by remember { mutableStateOf(true) }
    
    // Bottom Tab States: "BANK", "ANCHOR", "MOSQUE", "RESERVE", "STRICT", "SETUP"
    var selectedTab by remember { mutableStateOf("BANK") }

    // REAL-TIME SYSTEM CLOCK
    var systemTime by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        while (true) {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            systemTime = sdf.format(Date())
            delay(1000)
        }
    }

    // STATE CONTROLLERS FOR PROTOCOL FLOW (ANCHOR TAB)
    // Steps: "DURATION" -> "PRE_FLIGHT" -> "TAP_1" -> "SESSION" -> "TAP_2" -> "VERIFIED"
    var anchorStep by remember { mutableStateOf("DURATION") }
    var chosenDuration by remember { mutableStateOf(25) }
    var checklistPhoneFaceDown by remember { mutableStateOf(false) }
    var checklistWhiteboardOpen by remember { mutableStateOf(false) }
    var checklistTimerSet by remember { mutableStateOf(false) }

    // Active session bindings from ViewModel
    val isSessionActive by viewModel.isSessionActive.collectAsState()
    val timeLeftSeconds by viewModel.sessionTimeLeftSeconds.collectAsState()
    val sessionVoidedMessage by viewModel.sessionVoidedMessage.collectAsState()

    // Sync VM active session to ANCHOR Tab view states
    LaunchedEffect(isSessionActive, timeLeftSeconds, sessionVoidedMessage) {
        if (isSessionActive) {
            anchorStep = "SESSION"
        } else if (anchorStep == "SESSION" && timeLeftSeconds == 0 && isSessionActive) {
            anchorStep = "TAP_2"
        } else if (sessionVoidedMessage != null) {
            // Voided externally
            anchorStep = "DURATION"
        }
    }

    // STATE CONTROLLERS FOR MOSQUE RETREAT (MOSQUE TAB)
    var isRetreatActive by remember { mutableStateOf(false) }
    var retreatMinutesRemaining by remember { mutableStateOf(84) } // "1:24" countdown matching screenshot Page 12
    LaunchedEffect(isRetreatActive) {
        while (isRetreatActive && retreatMinutesRemaining > 0) {
            delay(1000) // accelerate speed slightly for demo
            retreatMinutesRemaining -= 1
        }
    }

    // STATE CONTROLLERS FOR SETUP PAIRING (SETUP TAB)
    var setupStep by remember { mutableStateOf(1) } // 1, 2, 3, 4

    // Main scaffold
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            // Premium dark bottom bar utilizing emojis instead of SVG icons as per constraints
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars),
                color = Color(0xFF0A0A0A),
                border = BorderStroke(1.dp, Color(0x13FFFFFF))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val tabs = listOf(
                        Triple("BANK", "🏦", if (isEnglish) "BANK" else "البنك"),
                        Triple("ANCHOR", "🌙", if (isEnglish) "ANCHOR" else "المرساة"),
                        Triple("MOSQUE", "🕌", if (isEnglish) "MOSQUE" else "المسجد"),
                        Triple("RESERVE", "💳", if (isEnglish) "RESERVE" else "الرصيد"),
                        Triple("STRICT", "🛡️", if (isEnglish) "STRICT" else "الصارم"),
                        Triple("SETUP", "⚙️", if (isEnglish) "SETUP" else "الإعداد")
                    )

                    tabs.forEach { (tabId, emoji, label) ->
                        val isSelected = selectedTab == tabId
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedTab = tabId }
                                .padding(vertical = 4.dp)
                                .testTag("tab_$tabId")
                        ) {
                            Text(
                                text = emoji,
                                fontSize = if (isSelected) 20.sp else 16.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = label,
                                color = if (isSelected) Color(0xFF2DD4A0) else Color(0xFF888888),
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 0.5.sp
                            )
                            // Elegant dot indicator
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 2.dp)
                                        .width(4.dp)
                                        .height(4.dp)
                                        .background(Color(0xFF2DD4A0), shape = RoundedCornerShape(2.dp))
                                )
                            }
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFF0A0A0A)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF0A0A0A))
        ) {
            
            // ---------------- HEADER SECTION (Matches Lovable exactly) ----------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "⚓", 
                            fontSize = 16.sp,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text(
                            text = "ANALOG ANCHOR",
                            color = Color(0xFFF5F2ED),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif,
                            letterSpacing = 1.sp
                        )
                    }
                    Text(
                        text = "PRACTICAL MINIMALISM",
                        color = Color(0xFF888888),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.5.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // English / Arabic toggler Badge
                    Row(
                        modifier = Modifier
                            .background(Color(0xFF1A1A1A), RoundedCornerShape(6.dp))
                            .border(BorderStroke(1.dp, Color(0x1AFFFFFF)), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                            .clickable { isEnglish = !isEnglish },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isEnglish) "EN" else "عربي",
                            color = Color(0xFF2DD4A0),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // System local clock + Pulse dot
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(6.dp)
                                .height(6.dp)
                                .background(Color(0xFF2DD4A0), shape = RoundedCornerShape(3.dp))
                        )
                        Text(
                            text = systemTime.ifEmpty { "18:09" },
                            color = Color(0xFFE5E5E5),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Redundant admin menu for re-testing
                    IconButton(
                        onClick = {
                            viewModel.resetFounderPanel()
                            onNavigateToFounderReset()
                            Toast.makeText(context, "All parameters system reset.", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .size(24.dp)
                            .testTag("admin_reset_icon")
                    ) {
                        Text("⚙️", fontSize = 12.sp)
                    }
                }
            }

            // Small horizontal brand divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0x0FFFFFFF))
            )

            // ---------------- CONDITIONAL VIEWS PER SELECTED TAB ----------------
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (selectedTab) {
                    "BANK" -> BankTabContent(
                        reservePoints = reservePoints,
                        isLocked = isLocked,
                        sanctuaryType = sanctuaryType,
                        sessionLogs = sessionLogs,
                        isEnglish = isEnglish,
                        onStartProtocolClick = { selectedTab = "ANCHOR" },
                        onCheckInClick = { selectedTab = "MOSQUE" }
                    )
                    "ANCHOR" -> AnchorTabContent(
                        viewModel = viewModel,
                        anchorStep = anchorStep,
                        chosenDuration = chosenDuration,
                        checklistPhoneFaceDown = checklistPhoneFaceDown,
                        checklistWhiteboardOpen = checklistWhiteboardOpen,
                        checklistTimerSet = checklistTimerSet,
                        isEnglish = isEnglish,
                        timeLeftSeconds = timeLeftSeconds,
                        onStepChange = { anchorStep = it },
                        onDurationSelect = { chosenDuration = it },
                        onChecklistPhoneToggle = { checklistPhoneFaceDown = it },
                        onChecklistWhiteboardToggle = { checklistWhiteboardOpen = it },
                        onChecklistTimerToggle = { checklistTimerSet = it },
                        onReturnToBank = {
                            selectedTab = "BANK"
                            anchorStep = "DURATION"
                        }
                    )
                    "MOSQUE" -> MosqueTabContent(
                        viewModel = viewModel,
                        isRetreatActive = isRetreatActive,
                        retreatMinutesRemaining = retreatMinutesRemaining,
                        isEnglish = isEnglish,
                        onRetreatToggle = {
                            isRetreatActive = it
                            if (it) {
                                retreatMinutesRemaining = 84 // reset to "1:24"
                            }
                        },
                        onEmergencyUnlockClick = {
                            viewModel.deductPoints(60, "Emergency Bypass Unlock", "deduction")
                            Toast.makeText(context, "Emergency unlocked. -60 pts deducted.", Toast.LENGTH_LONG).show()
                        }
                    )
                    "RESERVE" -> ReserveTabContent(
                        reservePoints = reservePoints,
                        sessionLogs = sessionLogs,
                        isEnglish = isEnglish
                    )
                    "STRICT" -> StrictTabContent(
                        isEnglish = isEnglish,
                        context = context
                    )
                    "SETUP" -> SetupTabContent(
                        nfcTagId = nfcTagId,
                        setupStep = setupStep,
                        isEnglish = isEnglish,
                        onStepChange = { setupStep = it },
                        onPairSuccess = {
                            viewModel.registerMockNfcTag()
                            Toast.makeText(context, "NFC Key securely bound to desk Anchor!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

// ==========================================
// 1. BANK TAB CONTENT
// ==========================================
@Composable
fun BankTabContent(
    reservePoints: Int,
    isLocked: Boolean,
    sanctuaryType: String,
    sessionLogs: List<SessionLog>,
    isEnglish: Boolean,
    onStartProtocolClick: () -> Unit,
    onCheckInClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        
        // 1. DayTime Access Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.dp, Color(0x1A2DD4A0)), RoundedCornerShape(8.dp))
                .background(Color(0x08FFFFFF), shape = RoundedCornerShape(8.dp))
                .padding(20.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(6.dp)
                            .height(6.dp)
                            .background(Color(0xFF2DD4A0), shape = RoundedCornerShape(3.dp))
                    )
                    Text(
                        text = if (isEnglish) "DAYTIME ACCESS SECURED" else "تم تأمين الدخول النهاري",
                        color = Color(0xFF2DD4A0),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isLocked) "The system is Locked." else "The Bank holds the line.",
                    color = Color(0xFFF5F2ED),
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isLocked) "Verification is required at your local Sanctuary to release the phone boundaries." 
                           else "Phone unlocked through standard hours. Night enforcement engages at 22:00.",
                    color = Color(0xFFBBBBBB),
                    fontSize = 15.sp,
                    lineHeight = 18.sp
                )
            }
        }

        // 2. Reserve Balance Card & Segmented Progress Block
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.dp, Color(0x13FFFFFF)), RoundedCornerShape(8.dp))
                .background(Color(0x08FFFFFF), shape = RoundedCornerShape(8.dp))
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = if (isEnglish) "RESERVE BALANCE" else "رصيد الاحتياطي",
                            color = Color(0xFF888888),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "$reservePoints",
                                color = Color(0xFFF5F2ED),
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Light
                            )
                            Text(
                                text = "PTS",
                                color = Color(0xFF888888),
                                fontSize = 15.sp,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "CEILING", color = Color(0xFF888888), fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                        Text(text = "200", color = Color(0xFFE5E5E5), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Segmented Progress Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    val activeSegments = (reservePoints / 12.5).toInt().coerceIn(1, 16)
                    for (i in 1..16) {
                        val active = i <= activeSegments
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .background(
                                    color = if (active) Color(0xFF2DD4A0) else Color(0xFF1E1E1E),
                                    shape = RoundedCornerShape(1.dp)
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("EMPTY", color = Color(0xFF888888), fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                    Text("85%", color = Color(0xFF2DD4A0), fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                    Text("FULL", color = Color(0xFF888888), fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0x0DFFFFFF)))
                Spacer(modifier = Modifier.height(14.dp))

                // Stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🔥", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("12d", color = Color(0xFFF5F2ED), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("STREAK", color = Color(0xFF888888), fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🛡️", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isLocked) "Closed" else "Open", color = Color(0xFF2DD4A0), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("ACCESS", color = Color(0xFF888888), fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🕌", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("368h", color = Color(0xFFF5F2ED), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("LAST FAJR", color = Color(0xFF888888), fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }

        // 3. Ritual Queue
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEnglish) "RITUAL QUEUE • TONIGHT" else "طابور الصلوات والطقوس الليلة",
                    color = Color(0xFF888888),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = if (isEnglish) "2 pending" else "٢ متبقي",
                    color = Color(0xFF888888),
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Ritual items
            listOf(
                DashboardRitualItem("Evening Anchor", "21:30 - 23:45", "+25", "🌙"),
                DashboardRitualItem("Fajr • Mosque check-in", "Tomorrow 04:48", "+40", "🕌")
            ).forEach { (title, time, reward, emoji) ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(BorderStroke(1.dp, Color(0x13FFFFFF)), RoundedCornerShape(8.dp))
                        .background(Color(0x05FFFFFF), shape = RoundedCornerShape(8.dp))
                        .clickable { if (emoji == "🌙") onStartProtocolClick() else onCheckInClick() }
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFF141414), RoundedCornerShape(6.dp))
                                    .border(BorderStroke(1.dp, Color(0x0FFFFFFF)), RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(emoji, fontSize = 16.sp)
                            }
                            Column {
                                Text(title, color = Color(0xFFE5E5E5), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                Text(time, color = Color(0xFF888888), fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(reward, color = Color(0xFF2DD4A0), fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Text("➔", color = Color(0xFF888888), fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        // 4. Ledger tape summary
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEnglish) "LEDGER TAPE" else "شريط المعاملات",
                    color = Color(0xFF888888),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = if (isEnglish) "VIEW LOGS" else "عرض السجل",
                    color = Color(0xFF2DD4A0),
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { /* action handles in local reserve view */ }
                )
            }

            if (sessionLogs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(Color(0x02FFFFFF), RoundedCornerShape(8.dp))
                        .border(BorderStroke(1.dp, Color(0x0FFFFFFF)), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No events logged on hardware yet.",
                        color = Color(0xFF888888),
                        fontSize = 15.sp
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    sessionLogs.take(3).forEach { log ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0x03FFFFFF), RoundedCornerShape(4.dp))
                                .padding(vertical = 12.dp, horizontal = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("📈", fontSize = 14.sp)
                                Text(log.action, color = Color(0xFFE5E5E5), fontSize = 15.sp)
                            }
                            val positive = log.pointsChange >= 0
                            Text(
                                text = if (positive) "+${log.pointsChange}" else "${log.pointsChange}",
                                color = if (positive) Color(0xFF2DD4A0) else Color(0xFFF59E0B),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 2. ANCHOR TAB CONTENT
// ==========================================
@Composable
fun AnchorTabContent(
    viewModel: AnchorViewModel,
    anchorStep: String,
    chosenDuration: Int,
    checklistPhoneFaceDown: Boolean,
    checklistWhiteboardOpen: Boolean,
    checklistTimerSet: Boolean,
    isEnglish: Boolean,
    timeLeftSeconds: Int,
    onStepChange: (String) -> Unit,
    onDurationSelect: (Int) -> Unit,
    onChecklistPhoneToggle: (Boolean) -> Unit,
    onChecklistWhiteboardToggle: (Boolean) -> Unit,
    onChecklistTimerToggle: (Boolean) -> Unit,
    onReturnToBank: () -> Unit
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        
        // Tab Header Titles
        Column {
            Text(
                text = "PILLAR I • PROTOCOL",
                color = Color(0xFF2DD4A0),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Evening Anchor",
                color = Color(0xFFF5F2ED),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Two taps. One verified session. The Reserve grows.",
                color = Color(0xFF888888),
                fontSize = 15.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Sequence map indicators (TAP 1 -> DURATION -> PRE-FLIGHT -> SESSION -> TAP 2)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val stages = listOf(
                    "DURATION" to "DURATION",
                    "PRE_FLIGHT" to "PRE-FLIGHT",
                    "TAP_1" to "TAP 1",
                    "SESSION" to "SESSION",
                    "TAP_2" to "TAP 2"
                )
                stages.forEach { (id, label) ->
                    val isCurrent = anchorStep == id
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = label,
                            color = if (isCurrent) Color(0xFF2DD4A0) else Color(0xFF444444),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .background(if (isCurrent) Color(0xFF2DD4A0) else Color(0xFF1E1E1E))
                        )
                    }
                }
            }
        }

        // Dynamic State Screen Contents
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            when (anchorStep) {
                "DURATION" -> {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "✓ ANCHOR RECOGNIZED",
                            color = Color(0xFF2DD4A0),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Choose duration",
                            color = Color(0xFFF5F2ED),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )

                        // Duration choices cards
                        val options = listOf(
                            Triple(25, "Short", "One pomodoro • planning sprint"),
                            Triple(45, "Standard", "Deep block • most days"),
                            Triple(55, "Anchor", "Full ritual • max reward")
                        )

                        options.forEach { (min, title, desc) ->
                            val isSelected = chosenDuration == min
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        BorderStroke(1.dp, if (isSelected) Color(0xFF2DD4A0) else Color(0x13FFFFFF)),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .background(if (isSelected) Color(0x0D2DD4A0) else Color(0x05FFFFFF), shape = RoundedCornerShape(8.dp))
                                    .clickable { onDurationSelect(min) }
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "$min",
                                            color = Color(0xFFF5F2ED),
                                            fontSize = 32.sp,
                                            fontWeight = FontWeight.Light,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Column {
                                            Text(title, color = Color(0xFFE5E5E5), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                            Text(desc, color = Color(0xFF888888), fontSize = 14.sp)
                                        }
                                    }
                                    Text(
                                        text = "+25 PTS",
                                        color = Color(0xFF2DD4A0),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = { onStepChange("PRE_FLIGHT") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("continue_to_preflight_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2DD4A0), contentColor = Color(0xFF0A0A0A)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("CONTINUE TO PRE-FLIGHT", fontSize = 15.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        }
                    }
                }

                "PRE_FLIGHT" -> {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "PRE-FLIGHT",
                            color = Color(0xFF888888),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = "Place phone face-down. Think on paper.",
                            color = Color(0xFFF5F2ED),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // Checklist items
                        listOf(
                            Triple("Phone face-down on the Anchor", checklistPhoneFaceDown, onChecklistPhoneToggle),
                            Triple("Whiteboard or notebook open", checklistWhiteboardOpen, onChecklistWhiteboardToggle),
                            Triple("Physical timer set", checklistTimerSet, onChecklistTimerToggle)
                        ).forEach { (label, value, toggleAction) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(BorderStroke(1.dp, Color(0x13FFFFFF)), RoundedCornerShape(8.dp))
                                    .background(Color(0x05FFFFFF), shape = RoundedCornerShape(8.dp))
                                    .clickable { toggleAction(!value) }
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = when(label) {
                                            "Physical timer set" -> "⏱️"
                                            "Whiteboard or notebook open" -> "📖"
                                            else -> "🔌"
                                        },
                                        fontSize = 14.sp
                                    )
                                    Text(label, color = Color(0xFFE5E5E5), fontSize = 15.sp)
                                }
                                Switch(
                                    checked = value,
                                    onCheckedChange = toggleAction,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color(0xFF2DD4A0),
                                        checkedTrackColor = Color(0x332DD4A0)
                                    )
                                )
                            }
                        }

                        Button(
                            onClick = { onStepChange("TAP_1") },
                            enabled = checklistPhoneFaceDown && checklistWhiteboardOpen && checklistTimerSet,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("continue_to_tap1_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2DD4A0), 
                                contentColor = Color(0xFF0A0A0A),
                                disabledContainerColor = Color(0xFF1F1F1F)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("BEGIN ${chosenDuration}-MINUTE SESSION", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                "TAP_1" -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "📡 NFC READY",
                            color = Color(0xFF2DD4A0),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.5.sp
                        )
                        
                        // Wave pulse UI
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .border(BorderStroke(1.dp, Color(0x13FFFFFF)), RoundedCornerShape(60.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .border(BorderStroke(1.dp, Color(0x332DD4A0)), RoundedCornerShape(45.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .background(Color(0x222DD4A0), RoundedCornerShape(30.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("📶", fontSize = 24.sp)
                                }
                            }
                        }

                        Text(
                            text = "Commit Session",
                            color = Color(0xFFF5F2ED),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Place your phone on the Anchor decal. The first tap opens the contract.",
                            color = Color(0xFF888888),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )

                        Button(
                            onClick = {
                                viewModel.startFocusSession(chosenDuration)
                                Toast.makeText(context, "Session Registered! Phone muted.", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("simulate_tap1_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2DD4A0), contentColor = Color(0xFF0A0A0A)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("📶 SIMULATE TAP 1", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        }

                        Text(
                            text = "PROTOTYPE SIMULATION",
                            color = Color(0xFF444444),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                "SESSION" -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "SESSION IN PROGRESS",
                            color = Color(0xFF2DD4A0),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 2.sp
                        )

                        val min = timeLeftSeconds / 60
                        val sec = timeLeftSeconds % 60
                        val formatTime = String.format("%02d:%02d", min, sec)

                        Text(
                            text = formatTime,
                            color = Color(0xFFF5F2ED),
                            fontSize = 72.sp,
                            fontWeight = FontWeight.Light,
                            fontFamily = FontFamily.Monospace
                        )

                        Text(
                            text = "SECONDS (DEMO) • LIVE SESSION WOULD BE ${chosenDuration}:00",
                            color = Color(0xFF888888),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )

                        // Fine progress line
                        val progress = if (chosenDuration > 0) timeLeftSeconds.toFloat() / (chosenDuration * 60) else 0f
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .background(Color(0xFF1E1E1E))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progress)
                                    .height(2.dp)
                                    .background(Color(0xFF2DD4A0))
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0x05FFFFFF), RoundedCornerShape(8.dp))
                                .border(BorderStroke(1.dp, Color(0x13FFFFFF)), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text("🔗", fontSize = 16.sp)
                                Column {
                                    Text("PHONE LOCKED TO ANCHOR", color = Color(0xFFE5E5E5), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text("Lifting before validation voids the session. Think on paper.", color = Color(0xFF888888), fontSize = 12.sp)
                                }
                            }
                        }

                        // Debug Reviewer controls
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = {
                                    // accelerate timer to 3 seconds remaining
                                    try {
                                        val field = viewModel::class.java.getDeclaredField("_sessionTimeLeftSeconds")
                                        field.isAccessible = true
                                        val flow = field.get(viewModel) as kotlinx.coroutines.flow.MutableStateFlow<Int>
                                        flow.value = 3
                                    } catch(e: Exception) {}
                                    Toast.makeText(context, "Accelerated", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1A1A)),
                                border = BorderStroke(1.dp, Color(0x33FFFFFF)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("ACCELERATE", fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    viewModel.failFocusSession("Session voided. Physical movement occurred")
                                    Toast.makeText(context, "Tilted!", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1A1A)),
                                border = BorderStroke(1.dp, Color(0x33FFFFFF)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("SIM TILT", fontSize = 12.sp)
                            }
                        }
                    }
                }

                "TAP_2" -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "VALIDATION WINDOW",
                            color = Color(0xFFF59E0B),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = "Tap the Anchor again.",
                            color = Color(0xFFF5F2ED),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Verify physical presence within the window or the session voids.",
                            color = Color(0xFF888888),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )

                        // 60 seconds countdown
                        Text(
                            text = "58",
                            color = Color(0xFFF5F2ED),
                            fontSize = 80.sp,
                            fontWeight = FontWeight.ExtraLight,
                            fontFamily = FontFamily.Monospace
                        )

                        Button(
                            onClick = {
                                viewModel.completeFocusSession()
                                onStepChange("VERIFIED")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("simulate_tap2_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2DD4A0), contentColor = Color(0xFF0A0A0A)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("📶 SIMULATE TAP 2", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                "VERIFIED" -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(Color(0x1A2DD4A0), RoundedCornerShape(32.dp))
                                .border(BorderStroke(2.dp, Color(0xFF2DD4A0)), RoundedCornerShape(32.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✔️", fontSize = 28.sp)
                        }

                        Text(
                            text = "SESSION VERIFIED",
                            color = Color(0xFF2DD4A0),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 2.sp
                        )

                        Text(
                            text = "+25",
                            color = Color(0xFFF5F2ED),
                            fontSize = 62.sp,
                            fontWeight = FontWeight.Light
                        )
                        Text(
                            text = "RESERVE POINTS",
                            color = Color(0xFF888888),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.5.sp
                        )

                        Text(
                            text = "Streak extended. Daytime access secured. The Bank remembers.",
                            color = Color(0xFFE5E5E5),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = onReturnToBank,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x1AFFFFFF), contentColor = Color(0xFFE5E5E5)),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0x13FFFFFF))
                        ) {
                            Text("➔ RETURN TO BANK", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. MOSQUE TAB CONTENT
// ==========================================
@Composable
fun MosqueTabContent(
    viewModel: AnchorViewModel,
    isRetreatActive: Boolean,
    retreatMinutesRemaining: Int,
    isEnglish: Boolean,
    onRetreatToggle: (Boolean) -> Unit,
    onEmergencyUnlockClick: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column {
            Text(
                text = "PILLAR II • SANCTUARY",
                color = Color(0xFF2DD4A0),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Mosque Anchor",
                color = Color(0xFFF5F2ED),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Geofenced presence. Verified prayer earns Reserve. Fajr earns the most.",
                color = Color(0xFF888888),
                fontSize = 15.sp
            )
        }

        // 1. Geofence active card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.dp, Color(0x13FFFFFF)), RoundedCornerShape(8.dp))
                .background(Color(0x05FFFFFF), shape = RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("📍", fontSize = 20.sp)
                    Column {
                        Text("Masjid Al-Rajhi • 320m", color = Color(0xFFE5E5E5), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("Within geofence • prototype simulation", color = Color(0xFF888888), fontSize = 14.sp)
                    }
                }
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(Color(0xFF2DD4A0), RoundedCornerShape(5.dp))
                )
            }
        }

        // 2. Fajr check-in verified card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.dp, Color(0xFF2DD4A0).copy(alpha = 0.3f)), RoundedCornerShape(8.dp))
                .background(Color(0x0A2DD4A0), shape = RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("🌅", fontSize = 18.sp)
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Fajr", color = Color(0xFFF5F2ED), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text("⭐", fontSize = 14.sp)
                            }
                            Text("04:48 • +40 pts", color = Color(0xFF888888), fontSize = 14.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .background(Color(0x222DD4A0), RoundedCornerShape(100.dp))
                            .border(BorderStroke(1.dp, Color(0xFF2DD4A0)), RoundedCornerShape(100.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("VERIFIED", color = Color(0xFF2DD4A0), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Highest reward • social apps stay locked until sunrise",
                    color = Color(0xFF888888),
                    fontSize = 14.sp
                )
            }
        }

        // 3. Maghrib to Isha Retreat Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.dp, Color(0x13FFFFFF)), RoundedCornerShape(8.dp))
                .background(Color(0x05FFFFFF), shape = RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("🌙", fontSize = 16.sp)
                    Text("SACRED VAULT", color = Color(0xFF2DD4A0), fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
                
                Text("Maghrib-to-Isha Retreat", color = Color(0xFFF5F2ED), fontSize = 21.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "Remain inside the mosque until Isha. Phone stays locked. Highest Reserve reward.",
                    color = Color(0xFFBBBBBB),
                    fontSize = 15.sp,
                    lineHeight = 20.sp
                )

                if (!isRetreatActive) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("18:11", color = Color(0xFFE5E5E5), fontSize = 15.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            Text("START", color = Color(0xFF888888), fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                        }
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF141414), RoundedCornerShape(100.dp))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text("+40 PTS", color = Color(0xFF2DD4A0), fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("19:42", color = Color(0xFFE5E5E5), fontSize = 15.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            Text("END", color = Color(0xFF888888), fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf(
                            Triple("📍 GEOFENCE", "Ready", Color(0xFF2DD4A0)),
                            Triple("🛡️ STRICT OFF", "Armed", Color(0xFFF59E0B)),
                            Triple("📖 PRACTICE", "Quran • Book", Color(0xFFE5E5E5))
                        ).forEach { (a, b, col) ->
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(Color(0xFF111111), RoundedCornerShape(6.dp))
                                    .padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(a, color = Color(0xFF888888), fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                                Text(b, color = col, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Button(
                        onClick = { onRetreatToggle(true) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2DD4A0), contentColor = Color(0xFF0A0A0A)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("START RETREAT", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    // ACTIVE RETREAT TIMER VIEW
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("TIME UNTIL ISHA", color = Color(0xFF888888), fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                        Text(
                            text = "1:24",
                            color = Color(0xFFF5F2ED),
                            fontSize = 54.sp,
                            fontWeight = FontWeight.Light,
                            fontFamily = FontFamily.Monospace
                        )
                        Text("Do not leave the vault. Stay present until Isha.", color = Color(0xFFBBBBBB), fontSize = 14.sp)

                        Spacer(modifier = Modifier.height(8.dp))

                        listOf(
                            "Mosque geofence" to "ACTIVE",
                            "Strict Off Mode" to "ACTIVE"
                        ).forEach { (lbl, valStr) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).background(Color(0xFF2DD4A0), RoundedCornerShape(4.dp)))
                                    Text(lbl, color = Color(0xFFE5E5E5), fontSize = 15.sp)
                                }
                                Text(valStr, color = Color(0xFF2DD4A0), fontSize = 14.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    onRetreatToggle(false)
                                    viewModel.failFocusSession("Left Mosque Geofence during Retreat")
                                    Toast.makeText(context, "Retreat broken. Locked!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f).height(44.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0x13FFFFFF), contentColor = Color(0xFF888888)),
                                border = BorderStroke(1.dp, Color(0x33FFFFFF)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("SIM • LEAVE", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    onRetreatToggle(false)
                                    viewModel.addPoints(40, "Completed Maghrib-Isha Retreat", "retreat")
                                    Toast.makeText(context, "Retreat Complete! +40 pts", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f).height(44.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2DD4A0), contentColor = Color(0xFF0A0A0A)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("COMPLETE", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // 4. Emergency Unlock
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.3f)), RoundedCornerShape(8.dp))
                .background(Color(0x05FFFFFF), shape = RoundedCornerShape(8.dp))
                .clickable { onEmergencyUnlockClick() }
                .padding(18.dp)
        ) {
            Column {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("⚠️", fontSize = 15.sp)
                    Text("EMERGENCY UNLOCK", color = Color(0xFFF59E0B), fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "If your Reserve is empty, drive to the mosque and verify presence. The system unlocks one window. The cost: -60 pts.",
                    color = Color(0xFFBBBBBB),
                    fontSize = 15.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

// ==========================================
// 4. RESERVE TAB CONTENT
// ==========================================
@Composable
fun ReserveTabContent(
    reservePoints: Int,
    sessionLogs: List<SessionLog>,
    isEnglish: Boolean
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column {
            Text(
                text = "PILLAR III • THE LEDGER",
                color = Color(0xFF2DD4A0),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Reserve Bank",
                color = Color(0xFFF5F2ED),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "You are spending discipline you already earned. A bad night does not erase a good month.",
                color = Color(0xFF888888),
                fontSize = 15.sp
            )
        }

        // 1. Large Balance Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.dp, Color(0x13FFFFFF)), RoundedCornerShape(8.dp))
                .background(Color(0x05FFFFFF), shape = RoundedCornerShape(8.dp))
                .padding(20.dp)
        ) {
            Column {
                Text("BALANCE", color = Color(0xFF888888), fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$reservePoints pts",
                    color = Color(0xFFF5F2ED),
                    fontSize = 54.sp,
                    fontWeight = FontWeight.Light
                )

                Spacer(modifier = Modifier.height(14.dp))
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0x0DFFFFFF)))
                Spacer(modifier = Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("EARNED (30D)", color = Color(0xFF888888), fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("+130", color = Color(0xFF2DD4A0), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("SPENT (30D)", color = Color(0xFF888888), fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("-95", color = Color(0xFFF59E0B), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 2. Info alert card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.dp, Color(0x332DD4A0)), RoundedCornerShape(8.dp))
                .background(Color(0x05FFFFFF), shape = RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
                Text("ℹ️", fontSize = 16.sp)
                Text(
                    text = "Reserve prevents all-or-nothing failure. Miss a session — pay the price, keep building. The ledger remembers everything.",
                    color = Color(0xFFBBBBBB),
                    fontSize = 15.sp,
                    lineHeight = 20.sp
                )
            }
        }

        // 3. Complete Ledger List
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "LEDGER",
                color = Color(0xFF888888),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.5.sp
            )

            if (sessionLogs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(Color(0x02FFFFFF), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No records found.", color = Color(0xFF888888), fontSize = 15.sp)
                }
            } else {
                sessionLogs.forEach { log ->
                    val dateStr = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(log.timestamp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0x03FFFFFF), RoundedCornerShape(4.dp))
                            .border(BorderStroke(1.dp, Color(0x05FFFFFF)), RoundedCornerShape(4.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("↗️", fontSize = 14.sp)
                                Column {
                                    Text(log.action, color = Color(0xFFE5E5E5), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                    Text(dateStr, color = Color(0xFF888888), fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                            val positive = log.pointsChange >= 0
                            Text(
                                text = if (positive) "+${log.pointsChange}" else "${log.pointsChange}",
                                color = if (positive) Color(0xFF2DD4A0) else Color(0xFFF59E0B),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. STRICT TAB CONTENT
// ==========================================
@Composable
fun StrictTabContent(
    isEnglish: Boolean,
    context: android.content.Context
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column {
            Text(
                text = "ENFORCER",
                color = Color(0xFF2DD4A0),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Strict Mode",
                color = Color(0xFFF5F2ED),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "The Anchor enforces the contract you made with yourself.",
                color = Color(0xFF888888),
                fontSize = 15.sp
            )
        }

        // 1. Slider Toggle Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.dp, Color(0x13FFFFFF)), RoundedCornerShape(8.dp))
                .background(Color(0x05FFFFFF), shape = RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("🛡️", fontSize = 18.sp)
                    Column {
                        Text("Strict Mode", color = Color(0xFFF5F2ED), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("Engaged • contract active", color = Color(0xFF2DD4A0), fontSize = 14.sp)
                    }
                }
                Switch(
                    checked = true,
                    onCheckedChange = {
                        Toast.makeText(context, "Contracts cannot be undone without hard cool-downs.", Toast.LENGTH_SHORT).show()
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF2DD4A0),
                        checkedTrackColor = Color(0x332DD4A0)
                    )
                )
            }
        }

        // 2. Contracts bullets
        listOf(
            "• Android Device Admin prevents app uninstall during active sessions.",
            "• Social apps blocked from 22:00 until after sunrise.",
            "• Disabling Strict Mode requires Anchor tap + 24-hour cool-down."
        ).forEach { rule ->
            Text(text = rule, color = Color(0xFFBBBBBB), fontSize = 15.sp, lineHeight = 22.sp, fontWeight = FontWeight.Medium)
        }

        // 3. Locked apps list
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "LOCKED APPS • 22:00 ➔ Sunrise",
                color = Color(0xFF888888),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.2.sp
            )

            val lockedApps = listOf(
                Pair("Instagram", "🔒 LOCKED"),
                Pair("TikTok", "🔒 LOCKED"),
                Pair("X (Twitter)", "🔒 LOCKED"),
                Pair("YouTube Shorts", "🔒 LOCKED"),
                Pair("Snapchat", "🔒 LOCKED")
            )

            val chunkedApps = lockedApps.chunked(2)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                chunkedApps.forEach { rowApps ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowApps.forEach { (appName, status) ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(Color(0xFF141414), RoundedCornerShape(8.dp))
                                    .border(BorderStroke(1.dp, Color(0x0FFFFFFF)), RoundedCornerShape(8.dp))
                                    .clickable { Toast.makeText(context, "$appName is hardware locked. Focus on paper.", Toast.LENGTH_SHORT).show() }
                                    .padding(14.dp)
                            ) {
                                Column {
                                    val emoji = when(appName) {
                                        "Instagram" -> "📸"
                                        "TikTok" -> "🎵"
                                        "X (Twitter)" -> "🐦"
                                        "YouTube Shorts" -> "🎥"
                                        else -> "👻"
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text(emoji, fontSize = 14.sp)
                                        Text(appName, color = Color(0xFFE5E5E5), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(status, color = Color(0xFFF59E0B), fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                        if (rowApps.size < 2) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 6. SETUP TAB CONTENT
// ==========================================
@Composable
fun SetupTabContent(
    nfcTagId: String,
    setupStep: Int,
    isEnglish: Boolean,
    onStepChange: (Int) -> Unit,
    onPairSuccess: () -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column {
            Text(
                text = "HARDWARE",
                color = Color(0xFF2DD4A0),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Pair your Anchor",
                color = Color(0xFFF5F2ED),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Four steps. One ritual. Then the Bank is live.",
                color = Color(0xFF888888),
                fontSize = 15.sp
            )
        }

        // 1. Digital graphic card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.dp, Color(0x13FFFFFF)), RoundedCornerShape(8.dp))
                .background(Color(0x05FFFFFF), shape = RoundedCornerShape(8.dp))
                .padding(18.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "NO. 0042 • EDITION ONE",
                    color = Color(0xFF2DD4A0),
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                
                // Drawing schema of hardware
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .background(Color(0xFF141414), RoundedCornerShape(8.dp))
                        .border(BorderStroke(1.dp, Color(0x13FFFFFF)), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("┌────────────────────────┐", color = Color(0xFF444444), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        Text("│      NFC TAP ZONE      │", color = Color(0xFFBBBBBB), fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        Text("└────────────────────────┘", color = Color(0xFF444444), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    listOf(
                        "📷 QR" to "Onboarding",
                        "📶 NFC" to "Daily enforcement"
                    ).forEach { (a, b) ->
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(a.split(" ")[0], fontSize = 13.sp)
                            Column {
                                Text(a.split(" ")[1], color = Color(0xFFE5E5E5), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(b, color = Color(0xFF888888), fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // 2. Interactive Steps Workflow list
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            val stepsData = listOf(
                StepInfo(1, "Place Anchor on desk", "Premium tempered glass • soft-touch base • embedded NFC + QR decal."),
                StepInfo(2, "Scan QR to install Bank", "QR is for onboarding. NFC is for enforcement."),
                StepInfo(3, "Tap NFC to pair device", "Pairing binds the Anchor to this phone only."),
                StepInfo(4, "Calibration complete", "Verified flat desktop alignment for gyroscope.")
            )

            stepsData.forEach { step ->
                val isActive = setupStep == step.number
                val isDone = setupStep > step.number
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            BorderStroke(
                                1.dp, 
                                if (isActive) Color(0xFF2DD4A0) else if (isDone) Color(0x332DD4A0) else Color(0x0FFFFFFF)
                            ), 
                            RoundedCornerShape(8.dp)
                        )
                        .background(if (isActive) Color(0x052DD4A0) else Color(0x02FFFFFF), shape = RoundedCornerShape(8.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(
                                        color = if (isActive || isDone) Color(0xFF2DD4A0).copy(alpha = 0.2f) else Color(0xFF1E1E1E),
                                        shape = RoundedCornerShape(14.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isDone) "✓" else "${step.number}",
                                    color = if (isActive || isDone) Color(0xFF2DD4A0) else Color(0xFF888888),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column {
                                Text(step.title, color = Color(0xFFE5E5E5), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text(step.subtitle, color = Color(0xFF888888), fontSize = 14.sp)
                            }
                        }

                        if (isActive) {
                            Button(
                                onClick = {
                                    if (setupStep == 3) {
                                        onPairSuccess()
                                    }
                                    if (setupStep < 4) {
                                        onStepChange(setupStep + 1)
                                    } else {
                                        onStepChange(1) // wrap around
                                    }
                                },
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2DD4A0), contentColor = Color(0xFF0A0A0A)),
                                modifier = Modifier.height(36.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp)
                            ) {
                                Text(if (step.number == 3) "PAIR" else "SIMULATE", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Help data models
private data class StepInfo(
    val number: Int,
    val title: String,
    val subtitle: String
)

private data class DashboardRitualItem(
    val title: String,
    val time: String,
    val reward: String,
    val emoji: String
)
