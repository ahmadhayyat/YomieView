package com.signage.yomie.utils.anim

import android.view.View
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs


class FadeOutTransformation : ViewPager2.PageTransformer {
    override fun transformPage(page: View, position: Float) {
        page.translationX = page.width * -position

        if (position <= -1.0F || position >= 1.0F) {
            page.alpha = 0.0F
        } else if (position == 0.0F) {
            page.alpha = 1.0F
        } else {
            // position is between -1.0F & 0.0F OR 0.0F & 1.0F
            page.alpha = 1.0F - abs(position)
        }

    }
}