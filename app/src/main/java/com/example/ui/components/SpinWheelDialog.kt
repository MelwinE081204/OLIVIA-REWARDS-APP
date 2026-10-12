package com.example.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class WheelPrize(val points: Int, val label: String, val color: Color, val textColor: Color)

@Composable
fun SpinWheelDialog(
    onDismiss: () -> Unit,
    onPrizeWon: (Int) -> Unit
) {
    val context = LocalContext.current
    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator }
    val scope = rememberCoroutineScope()

    val prizes = remember {
        listOf(
            WheelPrize(50, "50 PTS", Color(0xFF0F5132), Color.White),
            WheelPrize(100, "100 PTS", Color(0xFFC59B27), Color(0xFF1B2B23)),
            WheelPrize(25, "25 PTS", Color(0xFF1C6748), Color.White),
            WheelPrize(500, "★ 500 ★", Color(0xFFE5B022), Color(0xFF1B2B23)), // Jackpot
            WheelPrize(75, "75 PTS", Color(0xFF0F5132), Color.White),
            WheelPrize(250, "250 PTS", Color(0xFFC59B27), Color(0xFF1B2B23)),
            WheelPrize(40, "40 PTS", Color(0xFF1C6748), Color.White),
            WheelPrize(150, "150 PTS", Color(0xFF26825B), Color.White)
        )
    }

    var isSpinning by remember { mutableStateOf(false) }
    var wonPrize by remember { mutableStateOf<WheelPrize?>(null) }
    val rotationAngle = remember { Animatable(0f) }

    fun triggerVibrate() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(40)
            }
        } catch (_: Exception) {}
    }

    Dialog(onDismissRequest = { if (!isSpinning) onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("spin_wheel_dialog"),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFFF0C2),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = Color(0xFFC59B27),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Spin & Win",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Daily Lucky Prize Wheel",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (!isSpinning) {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // The Wheel & Pointer Container
                Box(
                    modifier = Modifier.size(260.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer decorative golden ring
                    Canvas(modifier = Modifier.size(260.dp)) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFFFFDF9E), Color(0xFFC29014), Color(0xFF8C6400))
                            ),
                            radius = size.width / 2f
                        )
                    }

                    // Rotating Canvas Wheel
                    Canvas(
                        modifier = Modifier
                            .size(244.dp)
                            .rotate(rotationAngle.value)
                    ) {
                        val sliceAngle = 360f / prizes.size
                        val radius = size.width / 2f
                        val center = Offset(radius, radius)

                        prizes.forEachIndexed { index, prize ->
                            val startAngle = index * sliceAngle
                            // Draw slice
                            drawArc(
                                color = prize.color,
                                startAngle = startAngle,
                                sweepAngle = sliceAngle,
                                useCenter = true,
                                size = Size(size.width, size.height)
                            )

                            // Draw separator line
                            val rad = Math.toRadians(startAngle.toDouble())
                            val endX = center.x + radius * cos(rad).toFloat()
                            val endY = center.y + radius * sin(rad).toFloat()
                            drawLine(
                                color = Color(0xFFFFF0C2),
                                start = center,
                                end = Offset(endX, endY),
                                strokeWidth = 2.dp.toPx()
                            )

                            // Draw Prize Text
                            val textRad = Math.toRadians((startAngle + sliceAngle / 2f).toDouble())
                            val textDistance = radius * 0.65f
                            val textX = center.x + textDistance * cos(textRad).toFloat()
                            val textY = center.y + textDistance * sin(textRad).toFloat()

                            val paint = android.graphics.Paint().apply {
                                color = prize.textColor.hashCode()
                                textSize = 11.sp.toPx()
                                textAlign = android.graphics.Paint.Align.CENTER
                                isFakeBoldText = true
                            }

                            drawContext.canvas.nativeCanvas.save()
                            drawContext.canvas.nativeCanvas.rotate(
                                (startAngle + sliceAngle / 2f + 90f),
                                textX,
                                textY
                            )
                            drawContext.canvas.nativeCanvas.drawText(prize.label, textX, textY, paint)
                            drawContext.canvas.nativeCanvas.restore()
                        }

                        // Center inner golden hub
                        drawCircle(
                            color = Color(0xFF133F2E),
                            radius = 28.dp.toPx()
                        )
                        drawCircle(
                            color = Color(0xFFD4AF37),
                            radius = 24.dp.toPx(),
                            style = Stroke(width = 3.dp.toPx())
                        )
                    }

                    // Center Logo Emblem
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFD4AF37),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "O",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                color = Color(0xFF0F382A)
                            )
                        }
                    }

                    // Fixed Top Arrow Pointer
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .size(32.dp)
                    ) {
                        Canvas(modifier = Modifier.size(32.dp)) {
                            val path = Path().apply {
                                moveTo(size.width / 2f, size.height)
                                lineTo(0f, 0f)
                                lineTo(size.width, 0f)
                                close()
                            }
                            drawPath(path, color = Color(0xFFE53935))
                            drawPath(path, color = Color.White, style = Stroke(width = 2.dp.toPx()))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (wonPrize == null) {
                    Button(
                        onClick = {
                            if (!isSpinning) {
                                isSpinning = true
                                scope.launch {
                                    val winningIndex = Random.nextInt(prizes.size)
                                    val sliceAngle = 360f / prizes.size
                                    // Pointer is at Top (270 degrees)
                                    // Slice winningIndex center is winningIndex * sliceAngle + sliceAngle/2
                                    val targetSliceCenter = winningIndex * sliceAngle + (sliceAngle / 2f)
                                    val stopAngle = (270f - targetSliceCenter + 360f) % 360f
                                    val totalSpins = 5 + Random.nextInt(3)
                                    val targetTotalAngle = (totalSpins * 360f) + stopAngle

                                    triggerVibrate()
                                    rotationAngle.snapTo(0f)
                                    rotationAngle.animateTo(
                                        targetValue = targetTotalAngle,
                                        animationSpec = tween(
                                            durationMillis = 4200,
                                            easing = CubicBezierEasing(0.15f, 0.85f, 0.25f, 1f)
                                        )
                                    )
                                    triggerVibrate()
                                    wonPrize = prizes[winningIndex]
                                    isSpinning = false
                                }
                            }
                        },
                        enabled = !isSpinning,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("spin_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.RotateRight, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isSpinning) "Spinning..." else "SPIN FOR FREE",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                } else {
                    // Won prize announcement
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🎉 CONGRATULATIONS!",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = Color(0xFF2E7D32)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "+${wonPrize?.points} REWARD POINTS",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 24.sp
                                ),
                                color = Color(0xFF0F5132)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    wonPrize?.let { onPrizeWon(it.points) }
                                    onDismiss()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .testTag("claim_spin_prize_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F5132))
                            ) {
                                Text(
                                    text = "Claim to My Balance",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
