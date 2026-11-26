package com.example.soundvisualizer

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * 앱에는 인터넷 권한이 없다. 개인정보처리방침과 스토어 설명이 이것을 약속한다("어떤 정보도 보내지 않습니다").
 * 링크는 브라우저·메일 앱·스토어 앱이 열고, 업데이트 확인(#500)은 기기 안의 Play 스토어 앱에 묻는다.
 *
 * 매니페스트에 네트워크 권한을 적으면 실패한다. 라이브러리가 몰래 더하는 권한은 이 시험으로 막을 수 없으므로,
 * 의존성을 더할 때는 그 라이브러리의 AndroidManifest.xml 에 uses-permission 이 없는지 본다(app-update 2.1.0 은 없다).
 */
class InternetPermissionTest {

    @Test
    fun `매니페스트에 네트워크 권한이 없다`() {
        val manifest = File("src/main/AndroidManifest.xml").readText(Charsets.UTF_8)
        for (permission in listOf("android.permission.INTERNET", "android.permission.ACCESS_NETWORK_STATE", "android.permission.ACCESS_WIFI_STATE")) {
            assertTrue("$permission 을 적으면 '인터넷 권한 없음' 약속이 깨집니다", !manifest.contains(permission))
        }
    }
}
