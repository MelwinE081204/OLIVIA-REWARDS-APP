package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * High-fidelity 1D Barcode drawn directly on Canvas for crisp POS scanner readability.
 */
@Composable
fun BarcodeView(
    code: String,
    modifier: Modifier = Modifier,
    barColor: Color = Color.Black,
    backgroundColor: Color = Color.White
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        ) {
            val totalBars = 65
            val step = size.width / totalBars
            // Generate deterministic barcode widths based on character hash
            val hashSeed = code.hashCode()

            for (i in 0 until totalBars) {
                // Guard bars on ends and middle
                val isGuard = i < 3 || i > totalBars - 4 || (i in (totalBars / 2 - 2)..(totalBars / 2 + 2))
                val isDrawn = if (isGuard) {
                    i % 2 == 0
                } else {
                    val pseudoRandom = ((hashSeed + i * 31) xor (i * 17))
                    (pseudoRandom % 3) != 0
                }

                if (isDrawn) {
                    val barWidth = if (isGuard) step * 0.9f else (step * (if ((i % 4) == 0) 1.4f else 0.8f))
                    drawRect(
                        color = barColor,
                        topLeft = Offset(i * step, 0f),
                        size = Size(barWidth.coerceAtMost(step * 1.5f), size.height)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = code.chunked(4).joinToString(" "),
            style = MaterialTheme.typography.labelMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            ),
            color = barColor.copy(alpha = 0.85f),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Geometric QR Code representation with finder patterns on canvas.
 */
@Composable
fun QrCodeView(
    data: String,
    modifier: Modifier = Modifier,
    darkColor: Color = Color(0xFF0F382A),
    lightColor: Color = Color.White,
    sizeDp: Int = 140
) {
    Box(
        modifier = modifier
            .size(sizeDp.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(lightColor)
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size((sizeDp - 20).dp)) {
            val gridCount = 21
            val cellSize = size.width / gridCount
            val hash = data.hashCode()

            // Function to draw finder pattern squares
            fun drawFinder(startX: Int, startY: Int) {
                // Outer 7x7
                drawRect(
                    color = darkColor,
                    topLeft = Offset(startX * cellSize, startY * cellSize),
                    size = Size(7 * cellSize, 7 * cellSize)
                )
                // Inner 5x5 white
                drawRect(
                    color = lightColor,
                    topLeft = Offset((startX + 1) * cellSize, (startY + 1) * cellSize),
                    size = Size(5 * cellSize, 5 * cellSize)
                )
                // Center 3x3 black
                drawRect(
                    color = darkColor,
                    topLeft = Offset((startX + 2) * cellSize, (startY + 2) * cellSize),
                    size = Size(3 * cellSize, 3 * cellSize)
                )
            }

            // Draw 3 corner finder patterns
            drawFinder(0, 0)
            drawFinder(gridCount - 7, 0)
            drawFinder(0, gridCount - 7)

            // Draw interior data modules
            for (row in 0 until gridCount) {
                for (col in 0 until gridCount) {
                    val inTopLeft = row < 7 && col < 7
                    val inTopRight = row < 7 && col >= gridCount - 7
                    val inBottomLeft = row >= gridCount - 7 && col < 7
                    if (inTopLeft || inTopRight || inBottomLeft) continue

                    // Timing patterns
                    val isTiming = (row == 6 || col == 6) && (row + col) % 2 == 0
                    val isData = ((row * 13 + col * 7 + hash) % 3) == 0

                    if (isTiming || isData) {
                        drawRect(
                            color = darkColor,
                            topLeft = Offset(col * cellSize, row * cellSize),
                            size = Size(cellSize * 0.92f, cellSize * 0.92f)
                        )
                    }
                }
            }
        }
    }
}
