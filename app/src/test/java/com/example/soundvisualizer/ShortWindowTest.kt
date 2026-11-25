package com.example.soundvisualizer

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * 낮은 창(#487). 창 높이가 [SHORT_WINDOW_HEIGHT] 보다 낮으면 탭 줄의 위아래 여백을 줄이고, 분류 탭은 찾기 칸만 위에 붙인다.
 * 화면 배치는 Compose 라 JVM 에서 그릴 수 없으므로 판단만 여기서 보고, 배치는 TabScreenLayoutInstrumentedTest 가 본다.
 */
class ShortWindowTest {

    @Test
    fun `가로로 든 폰과 반으로 나눈 화면은 낮은 창이다`() {
        // 흔한 폰의 짧은 변(360-412dp)과, 세로 화면을 위아래로 나눈 창.
        for (height in listOf(320, 360, 393, 412, 450, 479)) {
            assertTrue("높이 ${height}dp", isShortWindow(height.dp))
        }
    }

    @Test
    fun `세로 화면과 큰 화면은 낮은 창이 아니다`() {
        // 세로 폰(640dp 이상)과 가로로 든 태블릿. 이 창들은 예전 배치 그대로다.
        for (height in listOf(480, 600, 640, 800, 915)) {
            assertFalse("높이 ${height}dp", isShortWindow(height.dp))
        }
    }

    @Test
    fun `창 높이를 아직 모르면 낮은 창으로 보지 않는다`() {
        // 처음에 줄였다가 다시 늘리면 탭 줄과 분류 탭이 한 번 튄다.
        assertFalse(isShortWindow(0.dp))
    }

    @Test
    fun `튜토리얼과 같은 기준을 쓴다`() {
        assertEquals(480.dp, SHORT_WINDOW_HEIGHT)
        // 튜토리얼이 높이를 따로 적으면 두 화면의 기준이 어긋날 수 있다.
        val tutorial = File("src/main/java/com/example/soundvisualizer/tutorial/TutorialScreen.kt").readText(Charsets.UTF_8)
        assertTrue("튜토리얼이 낮은 창 높이를 따로 정했다", tutorial.contains("private val COMPACT_HEIGHT = SHORT_WINDOW_HEIGHT"))
    }
}
