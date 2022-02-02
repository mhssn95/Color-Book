package io.mhssn.colorbook

import android.graphics.PointF
import android.view.MotionEvent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import kotlin.math.ceil
import kotlin.math.max

private const val approximate = 0.005f

private val colors = listOf(
    Color(0xff1abc9c),
    Color(0xff2ecc71),
    Color(0xff3498db),
    Color(0xff9b59b6),
    Color(0xff34495e),
    Color(0xfff1c40f),
    Color(0xffe67e22),
    Color(0xffe74c3c),
    Color(0xffecf0f1),
    Color(0xff95a5a6),
    Color(0xff16a085),
    Color(0xff27ae60),
    Color(0xff2980b9),
    Color(0xff8e44ad),
    Color(0xff2c3e50),
    Color(0xfff39c12),
    Color(0xffd35400),
    Color(0xffc0392b),
    Color(0xffbdc3c7),
    Color(0xff7f8c8d),
)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ColorBook() {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val width = remember {
            with(density) { maxWidth.toPx() }
        }
        val height = remember {
            with(density) { maxHeight.toPx() }
        }
        var offset by remember {
            mutableStateOf(
                Offset(
                    width, height
                )
            )
        }
        var current by remember {
            mutableStateOf(1)
        }
        var currentColor by remember {
            mutableStateOf(colors[0])
        }
        var nextColor by remember {
            mutableStateOf(colors[1])
        }
        var action by remember {
            mutableStateOf(-1)
        }
        val animation = remember(action) {
            Animatable(0f)
        }
        LaunchedEffect(action) {
            animation.animateTo(1f, tween(1500))
            if (action == 1) {
                currentColor = nextColor
                nextColor = colors[(current + 1) % colors.size]
                offset = Offset(width, height)
                current += 1
            }
            action = -1
        }
        val t = animation.value
        if (action == 0) {
            //close
            offset = Offset(offset.x + (width - offset.x) * t, offset.y + (height - offset.y) * t)
        } else if (action == 1) {
            //open
            offset = Offset(offset.x + (-width + offset.x) * t, offset.y + (-height + offset.y) * t)
        }
        Canvas(modifier = Modifier
            .fillMaxSize()
            .pointerInteropFilter {
                when (it.action) {
                    MotionEvent.ACTION_UP -> {
                        action = if (it.x < width / 2f) {
                            1
                        } else {
                            0
                        }
                    }
                    MotionEvent.ACTION_MOVE -> {
                        if (action == -1) {
                            offset = Offset(it.x, it.y)
                        }
                    }
                }
                return@pointerInteropFilter true
            }) {
            drawIntoCanvas {
                drawRect(nextColor, size = size)
                drawPath(Path().apply {
                    val point =
                        getMaxRightPoint(
                            0f,
                            size.height,
                            size.width,
                            size.height,
                            offset.x,
                            offset.y
                        )
                    moveTo(point.x, point.y)
                    lineTo(point.x + 100, 0f)
                    lineTo(0f, 0f)
                    lineTo(offset.x, offset.y)
                }, Brush.horizontalGradient(listOf(currentColor, darkenColor(currentColor, 20))))
                drawPath(Path().apply {
                    moveTo(0f, size.height)
                    quadraticBezierTo(size.width, size.height, offset.x, offset.y)
                    lineTo(offset.x + 100, 0f)
                    lineTo(0f, 0f)
                }, currentColor)
            }
        }
    }
}

private fun lerp(value0: Float, value1: Float, fraction: Float): Float {
    return (1 - fraction) * value0 + fraction * value1
}

private fun getMaxRightPoint(
    x0: Float,
    y0: Float,
    x1: Float,
    y1: Float,
    x2: Float,
    y2: Float
): PointF {
    var maxRight = 0f
    var point = PointF(0f, 0f)
    var t = 0f
    while (t <= 1f) {
        val q1 = PointF(lerp(x0, x1, t), lerp(y0, y1, t))
        val q2 = PointF(lerp(x1, x2, t), lerp(y1, y2, t))
        val c = PointF(lerp(q1.x, q2.x, t), lerp(q1.y, q2.y, t))
        if (c.x > maxRight) {
            maxRight = ceil(c.x)
            point = c
        }
        t += approximate
    }
    return point
}

private fun darkenColor(color: Color, value: Int): Color {
    return color.copy(
        color.alpha,
        max(color.red - value, 0f),
        max(color.green - value, 0f),
        max(color.blue - value, 0f)
    )
}

@Composable
@Preview
private fun BookPreview() {
    ColorBook()
}
