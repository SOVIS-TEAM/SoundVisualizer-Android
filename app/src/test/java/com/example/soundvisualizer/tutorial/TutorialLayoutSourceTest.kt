package com.example.soundvisualizer.tutorial

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * 튜토리얼 화면이 크기 규칙(TutorialLayoutTest)을 실제로 쓰는지(#488). Compose 화면이라 JVM 에서 그릴 수 없으므로 소스에서
 * 확인한다(EasySettingsSourceTest 와 같은 방식). 작은 화면에서 글이 보이는지는 TutorialLayoutInstrumentedTest 가 본다.
 */
class TutorialLayoutSourceTest {

    private fun source(path: String) = File("src/main/java/com/example/soundvisualizer/$path").readText(Charsets.UTF_8)
        .split('\n')
        .filterNot { it.trimStart().startsWith("*") || it.trimStart().startsWith("//") || it.contains("/**") }
        .joinToString("\n")

    private val screen by lazy { source("tutorial/TutorialScreen.kt") }
    private val demo by lazy { source("tutorial/TutorialDemo.kt") }

    @Test
    fun `세로 화면의 그림 칸은 크기 규칙으로 정한다`() {
        assertTrue(screen.contains("val illustrationHeight = portraitIllustrationHeight(page, maxWidth, maxHeight, LocalDensity.current.fontScale)"))
    }

    @Test
    fun `아래 흐림은 남은 거리가 끝 빈칸보다 클 때만 그린다`() {
        val scrolling = screen.substringAfter("private fun ScrollingText(").substringBefore("\n}\n")
        assertTrue(scrolling.contains("showsBottomFade(scroll.value, scroll.maxValue, endSpacePx)"))
        assertFalse("빈칸만 가려져도 흐림을 그린다", scrolling.contains("canScrollForward"))
        // 흐림이 빼고 보는 빈칸과 실제로 두는 빈칸이 같은 값이어야 한다.
        assertTrue(scrolling.contains("Spacer(Modifier.height(endSpace))"))
        assertTrue(screen.contains("ScrollingText(Modifier.fillMaxSize(), endSpace = PAGE_END_SPACE)"))
    }

    @Test
    fun `홈 그림의 제목과 버튼 글자는 칸에 맞춘다`() {
        val phone = demo.substringAfter("private fun HomePhone(").substringBefore("\n}\n")
        assertTrue(phone.contains("fittedTitleSize(HOME_TITLE_MAX_SIZE, textRoom)"))
        assertTrue(phone.contains("fontSize = titleTextSize"))
        assertTrue(phone.contains("homeButtonLabels(startLabel, stopLabel, buttonRoom, ::widthOf)"))
        // 글자를 쓰지 않는 버튼은 막대로 그린다.
        assertTrue(phone.contains("if (startText != null) {"))
        assertTrue(phone.contains("if (stopText != null) {"))
        assertEquals("버튼마다 글자 대신 그리는 막대", 2, Regex("MockLine\\(0\\.6f, ").findAll(phone).count())
        // 두 줄은 낱말 사이에 직접 넣은 줄바꿈으로만 꺾는다(글자 단위로 꺾지 않는다).
        assertFalse("버튼 글자를 칸 폭에서 꺾는다", phone.contains("softWrap = true"))
    }
}
