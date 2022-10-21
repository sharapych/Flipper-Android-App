package com.flipperdevices.wearable.emulate.impl.viewmodel

import com.flipperdevices.core.di.SingleIn
import com.flipperdevices.core.ktx.jre.withLock
import com.flipperdevices.core.log.LogTagProvider
import com.flipperdevices.core.log.info
import com.flipperdevices.wearable.emulate.common.ipcemulate.requests.ConnectStatusOuterClass.ConnectStatus
import com.flipperdevices.wearable.emulate.impl.di.WearGraph
import com.flipperdevices.wearable.emulate.impl.model.ChannelState
import com.flipperdevices.wearable.emulate.impl.model.ConnectionTesterState
import com.flipperdevices.wearable.emulate.impl.model.WearEmulateState
import com.squareup.anvil.annotations.ContributesBinding
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex

interface WearEmulateStateMachine {
    fun getStateFlow(): StateFlow<WearEmulateState>
    suspend fun onStateUpdate(state: WearEmulateState)
    suspend fun onStatesUpdate(
        channelState: ChannelState,
        connectionTesterState: ConnectionTesterState,
        flipperConnectState: ConnectStatus
    )
}

@SingleIn(WearGraph::class)
@ContributesBinding(WearGraph::class, WearEmulateStateMachine::class)
class WearEmulateStateMachineImpl @Inject constructor(
    private val nodeFindingHelper: NodeFindingHelper,
    private val connectionChannelHelper: ConnectionChannelHelper,
    private val connectionTester: ConnectionTester
) : WearEmulateStateMachine, LogTagProvider {
    override val TAG = "WearEmulateStateMachine"

    private val wearEmulateStateFlow =
        MutableStateFlow<WearEmulateState>(WearEmulateState.NotInitialized)
    private val mutex = Mutex()

    override fun getStateFlow() = wearEmulateStateFlow

    override suspend fun onStatesUpdate(
        channelState: ChannelState,
        connectionTesterState: ConnectionTesterState,
        flipperConnectState: ConnectStatus
    ) {
        info { "Receive states update $channelState $connectionTesterState $flipperConnectState" }
        when (channelState) {
            ChannelState.DISCONNECTED -> {
                onStateUpdate(WearEmulateState.NodeFinding)
                return
            }
            ChannelState.CONNECTED -> {} // Continue
        }

        when (flipperConnectState) {
            ConnectStatus.READY -> {

            }
            ConnectStatus.UNSUPPORTED -> {
                onStateUpdate(WearEmulateState.UnsupportedFlipper)
                return
            }
            ConnectStatus.DISCONNECTED,
            ConnectStatus.CONNECTING,
            ConnectStatus.DISCONNECTING,
            ConnectStatus.UNRECOGNIZED -> {
                invalidateConnectionTesterState(connectionTesterState)
                return
            }
        }
    }

    private suspend fun invalidateConnectionTesterState(
        connectionTesterState: ConnectionTesterState
    ) {
        info { "#invalidateConnectionTesterState" }
        when (connectionTesterState) {
            ConnectionTesterState.NOT_CONNECTED -> {
                onStateUpdate(WearEmulateState.TestConnection)
            }
            ConnectionTesterState.CONNECTED -> {
                onStateUpdate(WearEmulateState.ConnectingToFlipper)
            }
        }
    }

    override suspend fun onStateUpdate(
        state: WearEmulateState
    ) = withLock(mutex, "state_update") {
        if (wearEmulateStateFlow.value == state) {
            info { "State $state already applied" }
            return@withLock
        }
        onStateUpdateInternal(state)
    }

    private suspend fun onStateUpdateInternal(state: WearEmulateState) {
        info { "#onStateUpdateInternal $state" }
        wearEmulateStateFlow.emit(state)
        when (state) {
            WearEmulateState.NodeFinding -> nodeFindingHelper.findNode()
                .also { nodeId ->
                    if (nodeId == null) {
                        onStateUpdateInternal(WearEmulateState.NotFoundNode)
                    } else onStateUpdateInternal(WearEmulateState.EstablishConnection(nodeId))
                }
            is WearEmulateState.EstablishConnection -> {
                connectionTester.resetState()
                connectionChannelHelper.establishConnection(state.nodeId)
            }
            WearEmulateState.TestConnection -> connectionTester.testConnection()
            WearEmulateState.ConnectingToFlipper -> {}
            WearEmulateState.UnsupportedFlipper,
            WearEmulateState.NotInitialized,
            is WearEmulateState.Emulating,
            is WearEmulateState.ReadyForEmulate,
            WearEmulateState.NotFoundNode -> return
        }
    }
}