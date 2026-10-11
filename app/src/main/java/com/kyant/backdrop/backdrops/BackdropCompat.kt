package com.kyant.backdrop.backdrops

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop

fun emptyBackdrop(): Backdrop = Backdrop()

@Composable
fun rememberLayerBackdrop(): Backdrop {
    val backdrop = remember { Backdrop() }
    val graphicsLayer = rememberGraphicsLayer()
    backdrop.layer = graphicsLayer
    return backdrop
}

/**
 * Apply to the ROOT content that should be visible "through" the glass
 * (e.g. the Box wrapping the NavHost, behind the bottom bar / top bar).
 * Records a snapshot of this content into [backdrop]'s layer on every
 * recomposition/frame, so glass surfaces elsewhere in the tree can redraw
 * a blurred copy of it. The content is still drawn normally here too -
 * this does not hide or replace anything, it only makes a copy available.
 */
fun Modifier.layerBackdrop(backdrop: Backdrop): Modifier = this
    .onGloballyPositioned { coordinates ->
        backdrop.layerBoundsInRoot = coordinates.boundsInRoot()
    }
    .drawWithContent {
        val layer = backdrop.layer
        if (layer != null) {
            layer.record { this@drawWithContent.drawContent() }
        }
        drawContent()
    }

/**
 * Apply to a glass surface (bottom bar, top bar, button, card). Redraws a
 * blurred copy of [backdrop]'s recorded content positioned behind this
 * element, then a translucent [tint] + the element's own content on top -
 * a real frosted-glass look driven by [RenderEffect] blur.
 *
 * Falls back to a flat [tint] fill with no blur on Android < 12 (API 31),
 * since [RenderEffect] blur isn't available there.
 */
fun Modifier.backdropBlur(
    backdrop: Backdrop,
    blurRadius: Dp = 24.dp,
    tint: Color = Color.White.copy(alpha = 0.18f),
): Modifier = composed {
    var selfBoundsInRoot by remember { mutableStateOf(Rect.Zero) }

    this
        .onGloballyPositioned { coordinates ->
            selfBoundsInRoot = coordinates.boundsInRoot()
        }
        .drawWithContent {
            val layer = backdrop.layer
            if (layer != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val radiusPx = blurRadius.toPx().coerceAtLeast(0.01f)
                layer.renderEffect = RenderEffect
                    .createBlurEffect(radiusPx, radiusPx, Shader.TileMode.CLAMP)
                    .asComposeRenderEffect()

                val dx = backdrop.layerBoundsInRoot.left - selfBoundsInRoot.left
                val dy = backdrop.layerBoundsInRoot.top - selfBoundsInRoot.top

                clipRect {
                    translate(dx, dy) {
                        drawLayer(layer)
                    }
                }
            }
            drawRect(tint)
            drawContent()
        }
}
