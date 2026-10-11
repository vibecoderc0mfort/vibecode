package com.kyant.backdrop

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.layer.GraphicsLayer

/**
 * Holds a single recorded [GraphicsLayer] snapshot of whatever content is
 * wrapped with [com.kyant.backdrop.backdrops.layerBackdrop] (typically the
 * screen content sitting behind a bottom bar / top bar / button).
 *
 * Other composables apply [com.kyant.backdrop.backdrops.backdropBlur] to
 * redraw this layer (blurred + tinted) behind their own surface, producing
 * a real frosted-glass ("Liquid Glass") effect instead of a flat color.
 *
 * NOTE: one [Backdrop] = one shared [GraphicsLayer] = one shared blur
 * radius at a time. If you need different blur radii in different places
 * on screen simultaneously, create separate [Backdrop] instances for each.
 */
class Backdrop {
    var layer: GraphicsLayer? = null
    var layerBoundsInRoot: Rect = Rect.Zero
}
