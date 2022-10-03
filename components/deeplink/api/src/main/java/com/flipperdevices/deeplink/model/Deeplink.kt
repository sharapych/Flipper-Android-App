package com.flipperdevices.deeplink.model

import android.os.Parcelable
import com.flipperdevices.bridge.dao.api.model.FlipperFilePath
import kotlinx.parcelize.Parcelize

sealed class Deeplink : Parcelable {
    @Parcelize
    data class FlipperKey(
        val path: FlipperFilePath? = null,
        val content: DeeplinkContent? = null
    ) : Deeplink()

    @Parcelize
    data class WidgetOptions(
        val appWidgetId: Int
    ) : Deeplink()
}
