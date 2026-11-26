package com.example.soundvisualizer.help

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** 도움말의 '업데이트 확인'이 설치한 곳에 따라 어느 스토어로 보내는지(#500). */
class UpdateStoreTest {

    private val pkg = "com.sovis.soundvisualizer"

    @Test
    fun `Play 스토어에서 설치했으면 Play 다`() {
        assertEquals(UpdateStore.Play, UpdateStore.of("com.android.vending"))
    }

    @Test
    fun `원스토어에서 설치했으면 원스토어다`() {
        for (installer in listOf("com.skt.skaf.A000Z00040", "com.skt.skaf.OA00018282", "com.kt.olleh.storefront", "com.lguplus.appstore")) {
            assertEquals(installer, UpdateStore.OneStore, UpdateStore.of(installer))
        }
    }

    @Test
    fun `스토어 밖에서 설치했거나 알 수 없으면 그 밖이다`() {
        // GitHub APK 는 패키지 설치 프로그램이나 파일 관리자, 브라우저가 설치한다. adb 로 설치하면 null 이다.
        for (installer in listOf(null, "", "com.google.android.packageinstaller", "com.android.packageinstaller", "com.sec.android.app.myfiles", "com.uptodown")) {
            assertEquals(installer.toString(), UpdateStore.Other, UpdateStore.of(installer))
        }
    }

    @Test
    fun `Play 와 그 밖은 Play 스토어 앱으로 열고 없으면 브라우저로 연다`() {
        for (store in listOf(UpdateStore.Play, UpdateStore.Other)) {
            val links = UpdateLinks.forStore(store, pkg)
            assertEquals("market://details?id=$pkg", links.primary)
            assertEquals("https://play.google.com/store/apps/details?id=$pkg", links.fallback)
        }
    }

    @Test
    fun `원스토어는 원스토어의 앱 페이지만 연다`() {
        val links = UpdateLinks.forStore(UpdateStore.OneStore, pkg)
        assertEquals("https://m.onestore.co.kr/v2/ko-kr/app/0001013096", links.primary)
        // 원스토어에서 받은 사람을 다른 스토어로 보내지 않는다.
        assertNull(links.fallback)
        assertTrue(links.primary.startsWith("https://"))
    }
}
