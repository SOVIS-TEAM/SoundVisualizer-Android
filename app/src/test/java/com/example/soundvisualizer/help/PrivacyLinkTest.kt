package com.example.soundvisualizer.help

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** 개인정보처리방침 주소 고르기(#468). 앱 스토어 등록정보가 같은 주소를 가리킨다. */
class PrivacyLinkTest {

    @Test
    fun `한국어 앱은 한국어 처리방침을 연다`() {
        assertEquals("https://sovis-team.github.io/privacy/", PrivacyLink.url("ko"))
        assertEquals("https://sovis-team.github.io/privacy/", PrivacyLink.url("KO"))
    }

    @Test
    fun `한국어가 아닌 앱은 영어 처리방침을 연다`() {
        for (language in listOf("en", "ja", "zh", "ar", "de", "")) {
            assertEquals(language, "https://sovis-team.github.io/en/privacy/", PrivacyLink.url(language))
        }
    }

    @Test
    fun `안내에 적는 주소는 앞의 https 와 끝의 빗금을 뺀다`() {
        assertEquals("sovis-team.github.io/privacy", PrivacyLink.displayUrl("ko"))
        assertEquals("sovis-team.github.io/en/privacy", PrivacyLink.displayUrl("en"))
    }

    @Test
    fun `도움말 탭이 처리방침 주소를 앱 언어로 고른다`() {
        val help = File("src/main/java/com/example/soundvisualizer/help/HelpTab.kt").readText(Charsets.UTF_8)
        assertTrue("도움말 탭이 PrivacyLink.url 을 쓰지 않는다", help.contains("PrivacyLink.url("))
        assertTrue("도움말 탭에 처리방침 버튼 문구가 없다", help.contains("R.string.help_privacy_button"))
    }
}
