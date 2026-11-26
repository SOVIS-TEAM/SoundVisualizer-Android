package com.example.soundvisualizer.help

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * 도움말의 '업데이트 확인'(#500)이 기기에 묻는 것. 어느 스토어로 보낼지는 [UpdateStore] 가 정한다.
 */
internal object UpdateCheck {

    private const val TAG = "UpdateCheck"

    /** 이 앱을 설치한 앱의 패키지 이름. 스토어 밖에서 설치했거나 알 수 없으면 null. */
    fun installer(context: Context): String? = try {
        val pm = context.packageManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            pm.getInstallSourceInfo(context.packageName).installingPackageName
        } else {
            @Suppress("DEPRECATION")
            pm.getInstallerPackageName(context.packageName)
        }
    } catch (e: PackageManager.NameNotFoundException) {
        null
    } catch (e: IllegalArgumentException) {
        null
    }

    /**
     * Play 스토어 앱에 새 버전이 있는지 묻는다. 있으면 true, 최신이면 false, 묻지 못하면 null(그때는 Play 스토어 페이지를 연다).
     *
     * Play 인앱 업데이트 라이브러리는 앱이 인터넷에 접속하지 않고 기기 안의 Play 스토어 앱과 주고받는다. Play 스토어에서 설치한
     * 앱만 답을 받는다(그래서 [UpdateStore.Play] 일 때만 부른다).
     */
    suspend fun playHasUpdate(context: Context): Boolean? = suspendCancellableCoroutine { continuation ->
        try {
            AppUpdateManagerFactory.create(context.applicationContext).appUpdateInfo
                .addOnSuccessListener { info ->
                    val availability = info.updateAvailability()
                    if (continuation.isActive) {
                        continuation.resume(
                            availability == UpdateAvailability.UPDATE_AVAILABLE ||
                                availability == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
                        )
                    }
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Play 스토어에 업데이트를 묻지 못했습니다", e)
                    if (continuation.isActive) continuation.resume(null)
                }
        } catch (e: RuntimeException) {
            // Play 스토어 앱이 없거나 꺼져 있는 기기.
            Log.w(TAG, "Play 인앱 업데이트를 쓸 수 없습니다", e)
            if (continuation.isActive) continuation.resume(null)
        }
    }
}
