package com.example.viewmodel

import android.app.Application
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.SessionLog
import com.example.data.SessionLogRepository
import com.example.device.AnchorDeviceAdminReceiver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

class AnchorViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    private val context = application.applicationContext
    private val prefs: SharedPreferences = context.getSharedPreferences("analog_anchor_prefs", Context.MODE_PRIVATE)
    private val repository: SessionLogRepository
    
    // Room integration
    val sessionLogs: StateFlow<List<SessionLog>>

    // Live state flows for UI updates
    private val _reservePoints = MutableStateFlow(100)
    val reservePoints = _reservePoints.asStateFlow()

    private val _isLocked = MutableStateFlow(false)
    val isLocked = _isLocked.asStateFlow()

    private val _isSealed = MutableStateFlow(false)
    val isSealed = _isSealed.asStateFlow()

    private val _isOnboarded = MutableStateFlow(false)
    val isOnboarded = _isOnboarded.asStateFlow()

    private val _sanctuaryType = MutableStateFlow("mosque")
    val sanctuaryType = _sanctuaryType.asStateFlow()

    private val _nfcTagId = MutableStateFlow("")
    val nfcTagId = _nfcTagId.asStateFlow()

    private val _eveningProtocolTime = MutableStateFlow("22:00")
    val eveningProtocolTime = _eveningProtocolTime.asStateFlow()

    // Hardware status logs
    private val _deviceAdminActive = MutableStateFlow(false)
    val deviceAdminActive = _deviceAdminActive.asStateFlow()

    private val _accessibilityActive = MutableStateFlow(false)
    val accessibilityActive = _accessibilityActive.asStateFlow()

    private val _uninstallPinSet = MutableStateFlow(false)
    val uninstallPinSet = _uninstallPinSet.asStateFlow()

    // Active session variables
    private val _isSessionActive = MutableStateFlow(false)
    val isSessionActive = _isSessionActive.asStateFlow()

    private val _sessionTimeLeftSeconds = MutableStateFlow(0)
    val sessionTimeLeftSeconds = _sessionTimeLeftSeconds.asStateFlow()

    private val _sessionTotalDurationMinutes = MutableStateFlow(25)
    val sessionTotalDurationMinutes = _sessionTotalDurationMinutes.asStateFlow()

    private val _sessionVoidedMessage = MutableStateFlow<String?>(null)
    val sessionVoidedMessage = _sessionVoidedMessage.asStateFlow()

    // Gyroscope tracking
    private val sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val gyroSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    private val accelSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val audioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var lastGyroX = 0f
    private var lastGyroY = 0f
    private var lastGyroZ = 0f
    private var initialRingerMode: Int = AudioManager.RINGER_MODE_NORMAL

    // Timer implementation
    private val handler = Handler(Looper.getMainLooper())
    private val countdownRunnable = object : Runnable {
        override fun run() {
            if (_sessionTimeLeftSeconds.value > 0) {
                _sessionTimeLeftSeconds.value -= 1
                handler.postDelayed(this, 1000)
            } else {
                // Timer finished successfully! Waiting for Tap 2
                _isSessionActive.value = false
                restoreAudioMode()
            }
        }
    }

    init {
        // Initialize Room Database
        val database = AppDatabase.getDatabase(context)
        repository = SessionLogRepository(database.sessionLogDao())
        
        // Load latest 5 logs reactively
        sessionLogs = repository.getLatestLogs(5).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Read states from SharedPreferences
        _reservePoints.value = prefs.getInt("reserve_points", 120) // default 120 points for GCC premium feeling
        _isLocked.value = prefs.getBoolean("is_locked", false)
        _isSealed.value = prefs.getBoolean("is_sealed", false)
        _isOnboarded.value = prefs.getBoolean("is_onboarded", false)
        _sanctuaryType.value = prefs.getString("sanctuary_type", "mosque") ?: "mosque"
        _nfcTagId.value = prefs.getString("nfc_tag_id", "") ?: ""
        _eveningProtocolTime.value = prefs.getString("evening_protocol_time", "22:00") ?: "22:00"
        _uninstallPinSet.value = prefs.getString("uninstall_pin_hash", "").orEmpty().isNotEmpty()

        checkHardwarePermissions()
    }

    fun checkHardwarePermissions() {
        // Device Admin Check
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponent = ComponentName(context, AnchorDeviceAdminReceiver::class.java)
        _deviceAdminActive.value = dpm.isAdminActive(adminComponent)

        // Accessibility Check
        _accessibilityActive.value = isAccessibilityServiceEnabled(context)
    }

    private fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val expectedComponentName = ComponentName(context, "com.example.service.AnchorBlockingAccessibilityService")
        val enabledServicesSetting = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServicesSetting)
        while (colonSplitter.hasNext()) {
            val componentNameString = colonSplitter.next()
            val enabledService = ComponentName.unflattenFromString(componentNameString)
            if (enabledService != null && enabledService == expectedComponentName) {
                return true
            }
        }
        return false
    }

    // Hash the PIN using standard SHA-256 for security
    private fun hashPin(pin: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(pin.toByteArray())
            hash.fold("") { str, it -> str + "%02x".format(it) }
        } catch (e: Exception) {
            pin // fallback to plaintext if somehow digest is missing
        }
    }

    fun setUninstallPin(pin: String) {
        val hash = hashPin(pin)
        prefs.edit().putString("uninstall_pin_hash", hash).apply()
        _uninstallPinSet.value = true
    }

    fun sealTheAnchor() {
        prefs.edit().putBoolean("is_sealed", true).apply()
        _isSealed.value = true
    }

    fun registerMockNfcTag(tagId: String = "ANCHOR_NFC_09X88") {
        prefs.edit().putString("nfc_tag_id", tagId).apply()
        _nfcTagId.value = tagId
    }

    fun saveSanctuary(type: String, lat: Float, lng: Float) {
        prefs.edit().apply {
            putString("sanctuary_type", type)
            putFloat("sanctuary_lat", lat)
            putFloat("sanctuary_lng", lng)
        }.apply()
        _sanctuaryType.value = type
    }

    fun saveEveningProtocolTime(timeStr: String) {
        prefs.edit().putString("evening_protocol_time", timeStr).apply()
        _eveningProtocolTime.value = timeStr
    }

    fun completeOnboarding() {
        prefs.edit().putBoolean("is_onboarded", true).apply()
        _isOnboarded.value = true
    }

    // Screen navigation resets
    fun resetFounderPanel() {
        prefs.edit().apply {
            putBoolean("is_sealed", false)
            putBoolean("is_onboarded", false)
            putBoolean("is_locked", false)
            putInt("reserve_points", 120)
            putString("uninstall_pin_hash", "")
        }.apply()
        _isSealed.value = false
        _isOnboarded.value = false
        _isLocked.value = false
        _reservePoints.value = 120
        _uninstallPinSet.value = false
        
        viewModelScope.launch {
            repository.clearLogs()
        }
    }

    // Points updates
    fun addPoints(points: Int, actionLabel: String, status: String) {
        val current = _reservePoints.value
        val updated = current + points
        prefs.edit().putInt("reserve_points", updated).apply()
        _reservePoints.value = updated

        // Log to Room database
        viewModelScope.launch {
            repository.insertLog(
                SessionLog(
                    action = actionLabel,
                    pointsChange = points,
                    status = status
                )
            )
        }
    }

    fun deductPoints(points: Int, actionLabel: String, status: String) {
        val current = _reservePoints.value
        val updated = maxOf(0, current - points)
        prefs.edit().putInt("reserve_points", updated).apply()
        _reservePoints.value = updated

        // Log to Room database
        viewModelScope.launch {
            repository.insertLog(
                SessionLog(
                    action = actionLabel,
                    pointsChange = -points,
                    status = status
                )
            )
        }
    }

    // Set lock flag
    fun setLockedState(locked: Boolean) {
        prefs.edit().putBoolean("is_locked", locked).apply()
        _isLocked.value = locked
    }

    // FAJR BONUS logic
    fun claimFajrBonus(): String {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        
        if (hour >= 7) {
            return "Too late. Fajr bonus is only active before 07:00 AM."
        }

        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val lastClaimDate = prefs.getString("last_fajr_claim_date", "") ?: ""
        if (lastClaimDate == todayStr) {
            return "You have already claimed your Fajr bonus for today."
        }

        // Apply reward
        prefs.edit().putString("last_fajr_claim_date", todayStr).apply()
        addPoints(40, "Fajr Bonus Presence", "bonus")
        return "Bonus granted: +40 Reserve Points."
    }

    // Active Focus Session Controls
    fun startFocusSession(durationMinutes: Int) {
        _sessionTotalDurationMinutes.value = durationMinutes
        _sessionTimeLeftSeconds.value = durationMinutes * 60
        _isSessionActive.value = true
        _sessionVoidedMessage.value = null
        
        // Make the phone silent by default
        silencePhone()

        // Register Gyroscope and Accelerometer
        lastGyroX = 0f
        lastGyroY = 0f
        lastGyroZ = 0f
        gyroSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        accelSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }

        // Start countdown
        handler.removeCallbacks(countdownRunnable)
        handler.postDelayed(countdownRunnable, 1000)
    }

    fun completeFocusSession() {
        _isSessionActive.value = false
        handler.removeCallbacks(countdownRunnable)
        unregisterSensors()
        restoreAudioMode()

        // Add 25 reserve points
        addPoints(25, "Completed ${sessionTotalDurationMinutes.value}m Protocol", "complete")
    }

    fun failFocusSession(reason: String) {
        _isSessionActive.value = false
        handler.removeCallbacks(countdownRunnable)
        unregisterSensors()
        restoreAudioMode()

        _sessionVoidedMessage.value = reason

        // Fail protocol: -35 points, set locked flag, navigate to locked Survival state
        deductPoints(35, "Failed Protocol: $reason", "missed")
        setLockedState(true)
    }

    private fun silencePhone() {
        try {
            initialRingerMode = audioManager.ringerMode
            // Set to vibrate or silent if permission is granted, otherwise mock in app console
            audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
        } catch (e: SecurityException) {
            // Might require DND access permission, fallback gracefully
            Log.e("AnchorViewModel", "No DND permission to mute phone: ${e.message}")
        }
    }

    private fun restoreAudioMode() {
        try {
            audioManager.ringerMode = initialRingerMode
        } catch (e: Exception) {
            audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
        }
    }

    private fun unregisterSensors() {
        sensorManager.unregisterListener(this)
    }

    // SensorEventListener overrides
    override fun onSensorChanged(event: SensorEvent) {
        if (!_isSessionActive.value) return

        if (event.sensor.type == Sensor.TYPE_GYROSCOPE) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            // Check if rotation rate exceeds safety threshold (angular rate > 0.35 rad/sec)
            val threshold = 0.35f
            if (abs(x) > threshold || abs(y) > threshold || abs(z) > threshold) {
                // Phone was picked up or moved! Void the session immediately
                failFocusSession("Session voided. Physical movement occurred")
            }
        } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            // Also monitor accelerometer changes to check if the phone is picked up from lying flat
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            // If phone is flat face-down, Z acceleration is around -9.8 m/s^2 (or ~9.8 if face-up)
            // If it is rotated or tilted heavily, Z changes significantly.
            // Let's safe check if tilt changes abruptly
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Not used
    }

    override fun onCleared() {
        super.onCleared()
        handler.removeCallbacks(countdownRunnable)
        unregisterSensors()
    }
}
