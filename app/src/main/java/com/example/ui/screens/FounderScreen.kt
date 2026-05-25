package com.example.ui.screens

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.device.AnchorDeviceAdminReceiver
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
fun FounderScreen(
    viewModel: AnchorViewModel,
    onNavigateToOnboarding: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val context = LocalContext.current
    var inputPasscode by remember { mutableStateOf("") }
    var isPasscodeCorrect by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf(false) }

    // Core verification values
    val deviceAdminActive by viewModel.deviceAdminActive.collectAsState()
    val accessibilityActive by viewModel.accessibilityActive.collectAsState()
    val uninstallPinSet by viewModel.uninstallPinSet.collectAsState()

    var uninstallPinInput by remember { mutableStateOf("") }

    // Periodically sync permissions on resume
    LaunchedEffect(Unit) {
        viewModel.checkHardwarePermissions()
    }

    val adminIntentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.checkHardwarePermissions()
    }

    if (!isPasscodeCorrect) {
        // Portal Gateway
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ObsidianBg)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Access restricted",
                tint = MutedSilver,
                modifier = Modifier.padding(16.dp)
            )

            Text(
                text = "FOUNDER INSTALLATION PANEL",
                color = WarmOffWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Password-protected. Managed strictly by Ar-razi during white-glove home configuration.",
                color = MutedSilver,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = inputPasscode,
                onValueChange = {
                    inputPasscode = it
                    loginError = false
                },
                visualTransformation = PasswordVisualTransformation(),
                placeholder = { Text("Enter Founder Passcode", color = MutedSilver) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("founder_passcode_field"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GlowGreen,
                    unfocusedBorderColor = ObsidianBorder,
                    focusedTextColor = WarmOffWhite,
                    unfocusedTextColor = WarmOffWhite,
                    focusedContainerColor = Color(0xFF14161B),
                    unfocusedContainerColor = Color(0xFF14161B)
                ),
                shape = RoundedCornerShape(8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            if (loginError) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Incorrect credentials. Verification refused.",
                    color = SignalAmber,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    // Password threshold: "arrazi" or "1234"
                    if (inputPasscode.trim().lowercase() == "arrazi" || inputPasscode.trim() == "8888") {
                        isPasscodeCorrect = true
                    } else {
                        loginError = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("founder_login_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = RefinedGreen),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Authenticate Installation", color = WarmOffWhite, fontWeight = FontWeight.Bold)
            }
        }
    } else {
        // Authenticated Panel UI
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ObsidianBg)
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "ANALOG ANCHOR CONFIGURATION",
                    color = WarmOffWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "White-glove locking environment setup. Ensure all steps are resolved correctly.",
                    color = MutedSilver,
                    fontSize = 12.sp,
                )

                Spacer(modifier = Modifier.height(24.dp))

                // STEP 1CARD
                GlassCard(
                    borderColor = if (deviceAdminActive) GlowGreen else ObsidianBorder,
                    modifier = Modifier.padding(vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Device admin config",
                                tint = if (deviceAdminActive) GlowGreen else SignalAmber
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Step 1 - Grant Device Admin",
                                        color = WarmOffWhite,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    if (deviceAdminActive) {
                                        Text(" ✔️", fontWeight = FontWeight.Bold)
                                    }
                                }
                                Text(
                                    text = "Guarantees physical security bypass restrictions.",
                                    color = MutedSilver,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Button(
                            onClick = {
                                val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
                                val adminComponent = ComponentName(context, AnchorDeviceAdminReceiver::class.java)
                                if (!dpm.isAdminActive(adminComponent)) {
                                    val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                                        putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
                                        putExtra(
                                            DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                                            "Analog Anchor locks distraction access under evening discipline sessions."
                                        )
                                    }
                                    adminIntentLauncher.launch(intent)
                                }
                            },
                            enabled = !deviceAdminActive,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (deviceAdminActive) Color.DarkGray else RefinedGreen,
                                disabledContainerColor = Color(0xFF14161B)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("grant_device_admin_button")
                        ) {
                            Text(
                                if (deviceAdminActive) "Granted" else "Grant",
                                color = if (deviceAdminActive) MutedSilver else WarmOffWhite,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // STEP 2 CARD
                GlassCard(
                    borderColor = if (accessibilityActive) GlowGreen else ObsidianBorder,
                    modifier = Modifier.padding(vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = Icons.Default.CompassCalibration,
                                contentDescription = "Accessibility active config",
                                tint = if (accessibilityActive) GlowGreen else SignalAmber
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Step 2 - App Blocking",
                                        color = WarmOffWhite,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    if (accessibilityActive) {
                                        Text(" ✔️", fontWeight = FontWeight.Bold)
                                    }
                                }
                                Text(
                                    text = "Interceptors detect distraction application launches.",
                                    color = MutedSilver,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Button(
                            onClick = {
                                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                adminIntentLauncher.launch(intent)
                            },
                            enabled = !accessibilityActive,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (accessibilityActive) Color.DarkGray else RefinedGreen,
                                disabledContainerColor = Color(0xFF14161B)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("enable_app_blocking_button")
                        ) {
                            Text(
                                if (accessibilityActive) "Active" else "Enable",
                                color = if (accessibilityActive) MutedSilver else WarmOffWhite,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // STEP 3 CARD
                GlassCard(
                    borderColor = if (uninstallPinSet) GlowGreen else ObsidianBorder,
                    modifier = Modifier.padding(vertical = 6.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = Icons.Default.Pin,
                                    contentDescription = "Uninstall pin setup",
                                    tint = if (uninstallPinSet) GlowGreen else SignalAmber
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Step 3 - Set Uninstall PIN",
                                            color = WarmOffWhite,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        if (uninstallPinSet) {
                                            Text(" ✔️", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Text(
                                        text = "4-digit key required to disable or uninstall.",
                                        color = MutedSilver,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = uninstallPinInput,
                                onValueChange = {
                                    if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                                        uninstallPinInput = it
                                    }
                                },
                                placeholder = { Text("4-digit PIN", color = MutedSilver, fontSize = 12.sp) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GlowGreen,
                                    unfocusedBorderColor = ObsidianBorder,
                                    focusedTextColor = WarmOffWhite,
                                    unfocusedTextColor = WarmOffWhite
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("uninstall_pin_field")
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Button(
                                onClick = {
                                    if (uninstallPinInput.length == 4) {
                                        viewModel.setUninstallPin(uninstallPinInput)
                                        uninstallPinInput = ""
                                    }
                                },
                                enabled = uninstallPinInput.length == 4,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = RefinedGreen,
                                    disabledContainerColor = Color(0xFF242730)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("set_uninstall_pin_button")
                            ) {
                                Text("Save PIN", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(bottom = 16.dp)) {
                // Seal the Anchor Action
                val allStepsDone = deviceAdminActive && accessibilityActive && uninstallPinSet

                Button(
                    onClick = {
                        viewModel.sealTheAnchor()
                        onNavigateToOnboarding()
                    },
                    enabled = allStepsDone,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("seal_the_anchor_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GlowGreen,
                        disabledContainerColor = Color(0xFF14161B),
                        contentColor = ObsidianBg,
                        disabledContentColor = MutedSilver
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "Seal the Anchor",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "🚨 SEALLING LOCKS THIS PANEL FOREVER. IT TRANSITIONS TO THE CLIENT PORTAL.",
                    color = MutedSilver,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
