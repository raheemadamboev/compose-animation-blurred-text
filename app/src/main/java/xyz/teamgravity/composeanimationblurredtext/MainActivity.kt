package xyz.teamgravity.composeanimationblurredtext

import android.graphics.BlurMaskFilter
import android.graphics.BlurMaskFilter.Blur
import android.graphics.Paint
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import xyz.teamgravity.composeanimationblurredtext.ui.theme.ComposeAnimationBlurredTextTheme
import kotlin.math.roundToInt

private const val ANIMATION_DURATION = 1_000
private const val TEXT = "Raheem Adamboev"

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComposeAnimationBlurredTextTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { padding ->
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .background(Color.Black)
                    ) {
                        val blurList = TEXT.mapIndexed { index, character ->
                            if (character == ' ') {
                                remember {
                                    mutableFloatStateOf(0F)
                                }
                            } else {
                                rememberInfiniteTransition(
                                    label = "transition $index"
                                ).animateFloat(
                                    initialValue = 10F,
                                    targetValue = 1F,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(
                                            durationMillis = ANIMATION_DURATION,
                                            easing = LinearEasing
                                        ),
                                        repeatMode = RepeatMode.Reverse,
                                        initialStartOffset = StartOffset(
                                            offsetMillis = (ANIMATION_DURATION / TEXT.length) * index
                                        )
                                    ),
                                    label = "blur"
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            TEXT.forEachIndexed { index, character ->
                                Text(
                                    text = character.toString(),
                                    color = Color.White,
                                    modifier = Modifier
                                        .graphicsLayer {
                                            if (character != ' ') {
                                                val blurAmount = blurList[index].value
                                                renderEffect = BlurEffect(
                                                    radiusX = blurAmount,
                                                    radiusY = blurAmount
                                                )
                                            }
                                        }
                                        .then(
                                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                                                Modifier.fullContentBlur(
                                                    blurRadius = { blurList[index].value.roundToInt() }
                                                )
                                            } else {
                                                Modifier
                                            }
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun Modifier.fullContentBlur(
    blurRadius: () -> Int,
    color: Color = Color.Black
): Modifier {
    return drawWithCache {
        val radius = blurRadius()

        val nativePaint = Paint()
        nativePaint.isAntiAlias = true
        nativePaint.color = color.toArgb()
        if (radius > 0) nativePaint.maskFilter = BlurMaskFilter(radius.toFloat(), Blur.NORMAL)

        onDrawWithContent {
            drawContent()

            drawIntoCanvas { canvas ->
                canvas.save()

                val rect = Rect(0, 0, size.width.toInt(), size.height.toInt())
                canvas.nativeCanvas.drawRect(rect, nativePaint)

                canvas.restore()
            }
        }
    }
}