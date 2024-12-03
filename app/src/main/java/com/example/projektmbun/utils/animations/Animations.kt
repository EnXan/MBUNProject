package com.example.projektmbun.utils.animations

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.TimeInterpolator
import android.os.Handler
import android.os.Looper
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageButton
import com.example.projektmbun.R

object Animations {

    fun animateButton(button: ImageButton) {
        // Drehanimation hin zur Check-Mark mit einer Ease-Funktion
        val rotateToCheckAnimator = ObjectAnimator.ofFloat(button, "rotation", 0f, 150f).apply {
            duration = 400
            interpolator = AccelerateDecelerateInterpolator() // Setze die Ease-Funktion
        }

        rotateToCheckAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}

            override fun onAnimationEnd(animation: Animator) {
                // Setze die Rotation korrekt, bevor das Symbol geändert wird
                button.rotation = 0f
                button.setImageResource(R.drawable.ic_check_green)

                // Setze das ursprüngliche Symbol nach 1,5 Sekunden
                Handler(Looper.getMainLooper()).postDelayed({
                    button.setImageResource(R.drawable.ic_add_black) // Ursprüngliches Icon
                }, 1500)
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })

        // Starte die Animation zur Check-Mark
        rotateToCheckAnimator.start()
    }
}
