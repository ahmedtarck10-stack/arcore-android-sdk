package com.google.ar.core.examples.ar

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.core.exceptions.CameraNotAvailableException
import com.google.ar.core.exceptions.UnavailableApkTooOldException
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException
import com.google.ar.core.exceptions.UnavailableSdkTooOldException
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ArCoreStatus(
    val availability: ArCoreApk.Availability = ArCoreApk.Availability.UNKNOWN_CHECKING,
    val isArCoreSupported: Boolean = false,
    val isArCoreInstalled: Boolean = false,
    val isSimulationMode: Boolean = true,
    val errorMessage: String? = null
)

class ArCoreHelper(private val context: Context) {
    companion object {
        private const val TAG = "ArCoreHelper"
    }

    private val _status = MutableStateFlow(ArCoreStatus())
    val status: StateFlow<ArCoreStatus> = _status.asStateFlow()

    private var arSession: Session? = null

    // Session settings
    var isDepthEnabled: Boolean = true
    var isInstantPlacementEnabled: Boolean = true
    var isEnvironmentalHdr: Boolean = true

    fun checkArCoreAvailability() {
        try {
            val availability = ArCoreApk.getInstance().checkAvailability(context)
            if (availability.isTransient) {
                // Re-query if transient
                Handler(Looper.getMainLooper()).postDelayed({
                    checkArCoreAvailability()
                }, 200)
                return
            }

            val supported = availability.isSupported
            val installed = availability == ArCoreApk.Availability.SUPPORTED_INSTALLED
            _status.value = ArCoreStatus(
                availability = availability,
                isArCoreSupported = supported,
                isArCoreInstalled = installed,
                isSimulationMode = !installed
            )
            Log.d(TAG, "ARCore availability: $availability, supported=$supported, installed=$installed")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to query ARCore availability, falling back to simulation mode", e)
            _status.value = ArCoreStatus(
                availability = ArCoreApk.Availability.UNSUPPORTED_DEVICE_NOT_CAPABLE,
                isArCoreSupported = false,
                isArCoreInstalled = false,
                isSimulationMode = true,
                errorMessage = e.message
            )
        }
    }

    fun requestInstallArCore(activity: android.app.Activity): Boolean {
        return try {
            val installStatus = ArCoreApk.getInstance().requestInstall(activity, true)
            installStatus == ArCoreApk.InstallStatus.INSTALLED
        } catch (e: UnavailableUserDeclinedInstallationException) {
            _status.value = _status.value.copy(errorMessage = "User declined ARCore installation")
            false
        } catch (e: Exception) {
            _status.value = _status.value.copy(errorMessage = "Cannot install ARCore: ${e.message}")
            false
        }
    }

    fun tryCreateSession(): Session? {
        if (!_status.value.isArCoreInstalled) return null
        return try {
            val session = Session(context)
            val config = Config(session).apply {
                focusMode = Config.FocusMode.AUTO
                if (isDepthEnabled && session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
                    depthMode = Config.DepthMode.AUTOMATIC
                }
                if (isInstantPlacementEnabled) {
                    instantPlacementMode = Config.InstantPlacementMode.LOCAL_Y_UP
                }
                lightEstimationMode = if (isEnvironmentalHdr) {
                    Config.LightEstimationMode.ENVIRONMENTAL_HDR
                } else {
                    Config.LightEstimationMode.AMBIENT_INTENSITY
                }
            }
            session.configure(config)
            arSession = session
            session
        } catch (e: Exception) {
            Log.e(TAG, "Could not initialize ARCore session", e)
            null
        }
    }

    fun closeSession() {
        try {
            arSession?.close()
            arSession = null
        } catch (e: Exception) {
            Log.w(TAG, "Error closing AR session", e)
        }
    }
}
