package com.example.individualassignment6_q1

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.individualassignment6_q1.ui.theme.IndividualAssignment6_q1Theme
import kotlin.math.pow

class MainActivity : ComponentActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var pressureSensor: Sensor? = null

    // State variables using mutableStateOf for automatic UI recomposition
    private var currentPressure by mutableStateOf(1013.25f)  // Sea level standard pressure
    private var currentAltitude by mutableStateOf(0.0)
    private var simulatedPressure by mutableStateOf(1013.25f)  // Default to sea level for testing
    private var useSimulation by mutableStateOf(false)  // Toggle between real sensor and simulated data

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize pressure sensor for barometric readings
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)

        enableEdgeToEdge()
        setContent {
            IndividualAssignment6_q1Theme {
                // Recalculate altitude when simulation pressure changes (real-time slider updates)
                LaunchedEffect(simulatedPressure, useSimulation) {
                    if (useSimulation) {
                        currentAltitude = calculateAltitude(simulatedPressure)
                    }
                }

                AltimeterScreen(
                    altitude = currentAltitude,
                    pressure = if (useSimulation) simulatedPressure else currentPressure,
                    useSimulation = useSimulation,
                    onToggleSimulation = { useSimulation = it },
                    onPressureChange = { simulatedPressure = it }
                )
            }
        }
    }

    // Register sensor listener when app becomes visible
    override fun onResume() {
        super.onResume()
        pressureSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    // Unregister to save battery when app is not visible
    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_PRESSURE) {
                currentPressure = it.values[0]

                // Only use real sensor data when not in simulation mode
                val pressureToUse = if (useSimulation) simulatedPressure else currentPressure
                currentAltitude = calculateAltitude(pressureToUse)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed - pressure sensor accuracy changes don't affect our calculations
    }

    // Barometric formula: h = 44330 × (1 - (P/P₀)^(1/5.255))
    // P₀ = 1013.25 hPa is standard sea-level pressure
    private fun calculateAltitude(pressure: Float): Double {
        val P0 = 1013.25
        return 44330.0 * (1.0 - (pressure / P0).pow(1.0 / 5.255))
    }
}

@Composable
fun AltimeterScreen(
    altitude: Double,
    pressure: Float,
    useSimulation: Boolean,
    onToggleSimulation: (Boolean) -> Unit,
    onPressureChange: (Float) -> Unit
) {
    // Background color darkens at higher altitudes to simulate atmosphere thinning
    val backgroundColor = getAltitudeColor(altitude)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = backgroundColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start  // Left align for modern dashboard look
        ) {
            // Header with letter spacing for technical/aviation aesthetic
            Text(
                text = "ALTIMETER",
                fontSize = 28.sp,
                fontWeight = FontWeight.Light,
                color = Color.White,
                modifier = Modifier.padding(top = 32.dp, start = 8.dp),
                letterSpacing = 4.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Split layout: altitude on left, pressure gauge on right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Large altitude display --> primary information
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "ALTITUDE",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.6f),
                        letterSpacing = 2.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "%.1f".format(altitude),
                        fontSize = 64.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(start = 8.dp),
                        maxLines = 1,  // Prevent wrapping for large numbers
                        softWrap = false  // Keep all digits on one line
                    )
                    Text(
                        text = "meters",
                        fontSize = 18.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                // Circular gauge for pressure --> mimics analog altimeter design
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer ring for depth effect
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White.copy(alpha = 0.1f), CircleShape)
                    )
                    // Inner ring contains pressure reading
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .background(Color.White.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "%.1f".format(pressure),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "hPa",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            // Status cards provide quick contextual information
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Shows altitude category for quick reference
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.12f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "LEVEL",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.6f),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = getAltitudeLevel(altitude),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                // Visual indicator of data source (sensor vs simulation)
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.12f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "STATUS",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.6f),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (useSimulation) "SIM" else "LIVE",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (useSimulation) Color(0xFFFFB74D) else Color(0xFF81C784)  // Orange for sim, green for live
                        )
                    }
                }
            }

            // Testing controls --> allows manual pressure adjustment without physical movement
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color.Black.copy(alpha = 0.3f)  // Darker to separate from main content
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = "SIMULATION MODE",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Test altitude changes",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 12.sp
                            )
                        }
                        Switch(
                            checked = useSimulation,
                            onCheckedChange = onToggleSimulation,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFFFFB74D),  // Orange accent for active state
                                checkedTrackColor = Color(0xFFFFB74D).copy(alpha = 0.5f),
                                uncheckedThumbColor = Color.White.copy(alpha = 0.7f),
                                uncheckedTrackColor = Color.White.copy(alpha = 0.3f)
                            )
                        )
                    }

                    // Slider appears only when simulation is active to reduce clutter
                    if (useSimulation) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "PRESSURE CONTROL",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 12.sp,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "%.1f hPa".format(pressure),
                                    color = Color(0xFFFFB74D),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            // Range: 800-1100 hPa covers from ~2000m altitude to below sea level
                            Slider(
                                value = pressure,
                                onValueChange = onPressureChange,
                                valueRange = 800f..1100f,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFFFFB74D),
                                    activeTrackColor = Color(0xFFFFB74D).copy(alpha = 0.8f),
                                    inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                                )
                            )
                            // Show min/max values for reference
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "800",
                                    color = Color.White.copy(alpha = 0.4f),
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = "1100",
                                    color = Color.White.copy(alpha = 0.4f),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// Categorizes altitude into meaningful levels for quick reference
fun getAltitudeLevel(altitude: Double): String {
    return when {
        altitude < 0 -> "Below Sea"     // Death Valley, underwater
        altitude < 500 -> "Low"         // Most cities, plains
        altitude < 1000 -> "Medium"     // Hills, Denver
        altitude < 2000 -> "High"       // Mountain towns
        altitude < 3000 -> "Very High"  // High mountains, ski resorts
        else -> "Extreme"               // Above tree line, Everest base camp
    }
}

// Maps altitude to background color --> simulates atmospheric color changes
// Higher altitudes get lighter blues, thinner atmosphere
fun getAltitudeColor(altitude: Double): Color {
    return when {
        altitude < 0 -> Color(0xFF1A237E)      // Deep blue for below sea level
        altitude < 500 -> Color(0xFF283593)    // Dark blue - ground level
        altitude < 1000 -> Color(0xFF303F9F)   // Medium blue - low altitude
        altitude < 1500 -> Color(0xFF3949AB)   // Blue - moderate altitude
        altitude < 2000 -> Color(0xFF3F51B5)   // Light blue - high altitude
        altitude < 2500 -> Color(0xFF5C6BC0)   // Lighter blue
        altitude < 3000 -> Color(0xFF7986CB)   // Very light blue
        else -> Color(0xFF9FA8DA)              // Pale blue - extreme altitude (mountain peaks)
    }
}