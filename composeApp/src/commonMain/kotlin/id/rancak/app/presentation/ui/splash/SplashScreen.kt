package id.rancak.app.presentation.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import rancak.composeapp.generated.resources.Res
import rancak.composeapp.generated.resources.tias_logo

// ── Palette: "luminous dark" ─────────────────────────────────────────────────
// Near-black base + electric teal + cobalt blue → terasa premium & fresh 2025
private val BgBase    = Color(0xFF07090A)   // near-black, sedikit cool
private val AuroraTeal  = Color(0xFF00E5C4)   // electric teal — bright, tech
private val AuroraBlue  = Color(0xFF1A56FF)   // cobalt blue — depth & trust
private val AuroraAmber = Color(0xFFFF8C42)   // warm amber accent — warmth
private val AuroraViolet = Color(0xFF7B3FE4)  // violet hint — luxury

@Composable
fun SplashScreen(onFinished: () -> Unit) {

    // ── Auto-navigate after 2.8 s ─────────────────────────────────────────────
    LaunchedEffect(Unit) {
        delay(2_800L)
        onFinished()
    }

    // ── Entrance: logo + text scale + fade ────────────────────────────────────
    var appeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(60L); appeared = true }

    val contentScale by animateFloatAsState(
        targetValue   = if (appeared) 1f else 0.82f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow),
        label         = "scale"
    )
    val contentAlpha by animateFloatAsState(
        targetValue   = if (appeared) 1f else 0f,
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label         = "alpha"
    )

    // ── Infinite animations ────────────────────────────────────────────────────
    val inf = rememberInfiniteTransition(label = "bg")

    // Aurora blobs — slow independent float (gives living, breathing feel)
    val blob1Y by inf.animateFloat(
        initialValue  = -28f, targetValue = 28f,
        animationSpec = infiniteRepeatable(
            tween(11_000, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ), label = "b1y"
    )
    val blob2Y by inf.animateFloat(
        initialValue  = 22f, targetValue = -22f,
        animationSpec = infiniteRepeatable(
            tween(8_500, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ), label = "b2y"
    )
    val blob3Scale by inf.animateFloat(
        initialValue  = 0.88f, targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            tween(7_000, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ), label = "b3s"
    )

    // Loading dots
    val dot1 by inf.animateFloat(0.2f, 1f, dotSpec(0),   "d1")
    val dot2 by inf.animateFloat(0.2f, 1f, dotSpec(200), "d2")
    val dot3 by inf.animateFloat(0.2f, 1f, dotSpec(400), "d3")

    // ── Root ──────────────────────────────────────────────────────────────────
    Box(
        modifier         = Modifier
            .fillMaxSize()
            .background(BgBase),            // flat near-black base
        contentAlignment = Alignment.Center
    ) {

        // ── Aurora layer ──────────────────────────────────────────────────────
        AuroraBackground(blob1Y, blob2Y, blob3Scale)

        // ── Diagonal light streak (luxury glare line) ─────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(1.dp)
                .align(Alignment.TopCenter)
                .offset(y = 140.dp)
                .graphicsLayer { rotationZ = -18f }
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.06f),
                            Color.White.copy(alpha = 0.14f),
                            Color.White.copy(alpha = 0.06f),
                            Color.Transparent
                        )
                    )
                )
        )

        // ── Content ───────────────────────────────────────────────────────────
        Column(
            modifier              = Modifier
                .graphicsLayer { scaleX = contentScale; scaleY = contentScale; alpha = contentAlpha },
            horizontalAlignment   = Alignment.CenterHorizontally,
            verticalArrangement   = Arrangement.spacedBy(0.dp)
        ) {

            // Logo (white tint)
            androidx.compose.foundation.Image(
                painter            = painterResource(Res.drawable.tias_logo),
                contentDescription = "TIAS Logo",
                contentScale       = ContentScale.Fit,
                colorFilter        = ColorFilter.tint(Color.White),
                modifier           = Modifier.size(150.dp)
            )

            Spacer(Modifier.height(32.dp))

            // App name
            Text(
                text          = "RANCAK",
                style         = MaterialTheme.typography.headlineMedium,
                fontWeight    = FontWeight.ExtraBold,
                color         = Color.White,
                letterSpacing = 6.sp
            )

            Spacer(Modifier.height(8.dp))

            // Tagline
            Text(
                text          = "Point  of  Sale",
                style         = MaterialTheme.typography.bodySmall,
                color         = Color.White.copy(alpha = 0.50f),
                textAlign     = TextAlign.Center,
                letterSpacing = 3.sp,
                fontWeight    = FontWeight.Light
            )

            Spacer(Modifier.height(52.dp))

            // Staggered loading dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                listOf(dot1, dot2, dot3).forEach { a ->
                    Box(
                        Modifier
                            .size(5.dp)
                            .graphicsLayer { alpha = a }
                            .background(Color.White, CircleShape)
                    )
                }
            }
        }
    }
}

// ── Aurora background composable ─────────────────────────────────────────────

@Composable
private fun AuroraBackground(blob1Y: Float, blob2Y: Float, blob3Scale: Float) {
    Box(Modifier.fillMaxSize()) {

        // ① Large electric teal — top-left, floats up/down
        Box(
            modifier = Modifier
                .size(500.dp)
                .offset(x = (-140).dp, y = (-160 + blob1Y).dp)
                .background(
                    Brush.radialGradient(
                        listOf(
                            AuroraTeal.copy(alpha = 0.38f),
                            AuroraTeal.copy(alpha = 0.12f),
                            Color.Transparent
                        )
                    )
                )
        )

        // ② Deep cobalt blue — bottom-right, floats opposite direction
        Box(
            modifier = Modifier
                .size(460.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 100.dp, y = (60 + blob2Y).dp)
                .background(
                    Brush.radialGradient(
                        listOf(
                            AuroraBlue.copy(alpha = 0.40f),
                            AuroraBlue.copy(alpha = 0.10f),
                            Color.Transparent
                        )
                    )
                )
        )

        // ③ Violet hint — bottom-left, slowly breathes in/out
        Box(
            modifier = Modifier
                .size(320.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-60).dp, y = 40.dp)
                .graphicsLayer { scaleX = blob3Scale; scaleY = blob3Scale }
                .background(
                    Brush.radialGradient(
                        listOf(
                            AuroraViolet.copy(alpha = 0.22f),
                            Color.Transparent
                        )
                    )
                )
        )

        // ④ Warm amber — far top-right, very subtle (adds warmth & uniqueness)
        Box(
            modifier = Modifier
                .size(280.dp)
                .align(Alignment.TopEnd)
                .offset(x = 80.dp, y = (-80 + blob2Y * 0.5f).dp)
                .background(
                    Brush.radialGradient(
                        listOf(
                            AuroraAmber.copy(alpha = 0.14f),
                            Color.Transparent
                        )
                    )
                )
        )

        // ⑤ Center radial vignette inversion — logo area slightly lighter
        //    (draws eye to center, subtle depth trick)
        Box(
            modifier = Modifier
                .size(380.dp)
                .align(Alignment.Center)
                .offset(y = (-20).dp)
                .background(
                    Brush.radialGradient(
                        listOf(
                            Color.White.copy(alpha = 0.04f),
                            Color.Transparent
                        )
                    )
                )
        )

        // ⑥ Edge vignette — dark at all four edges (depth, premium feel)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colorStops = arrayOf(
                            0.0f to Color.Transparent,
                            0.7f to Color.Transparent,
                            1.0f to Color.Black.copy(alpha = 0.55f)
                        )
                    )
                )
        )
    }
}

// ── Helper ────────────────────────────────────────────────────────────────────

private fun dotSpec(offsetMs: Int): InfiniteRepeatableSpec<Float> =
    infiniteRepeatable(
        animation          = tween(650, easing = FastOutSlowInEasing),
        repeatMode         = RepeatMode.Reverse,
        initialStartOffset = StartOffset(offsetMs)
    )
