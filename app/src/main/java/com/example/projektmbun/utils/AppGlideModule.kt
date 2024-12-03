package com.example.projektmbun.utils

import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule

@GlideModule
public final class MyAppGlideModule : AppGlideModule() {
    override fun isManifestParsingEnabled(): Boolean {
        return false // Vermeidet das Parsen von Glide-Konfigurationen aus dem Manifest
    }
}
