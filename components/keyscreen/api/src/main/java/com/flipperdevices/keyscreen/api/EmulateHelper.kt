package com.flipperdevices.keyscreen.api

import com.flipperdevices.bridge.api.manager.FlipperRequestApi
import com.flipperdevices.bridge.dao.api.model.FlipperFilePath
import com.flipperdevices.bridge.dao.api.model.FlipperKeyType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

const val SUBGHZ_DEFAULT_TIMEOUT_MS = 500L

interface EmulateHelper {
    fun getCurrentEmulatingKey(): StateFlow<FlipperFilePath?>
    suspend fun startEmulate(
        scope: CoroutineScope,
        requestApi: FlipperRequestApi,
        keyType: FlipperKeyType,
        keyPath: FlipperFilePath,
        minEmulateTime: Long = 0L
    ): Boolean

    suspend fun stopEmulate(
        scope: CoroutineScope,
        requestApi: FlipperRequestApi
    )

    suspend fun stopEmulateForce(
        requestApi: FlipperRequestApi
    )
}
