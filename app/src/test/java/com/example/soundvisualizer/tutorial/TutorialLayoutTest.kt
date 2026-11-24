package com.example.soundvisualizer.tutorial

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 튜토리얼 세로 화면의 크기 규칙(#488). 숫자는 폭 360dp·높이 780dp 폰(S22 급, 상태 표시줄 29dp·3버튼 내비 48dp)에서
 * 위아래 버튼 줄(184dp)을 뺀 쪽 높이 519dp 로 본다. 실제 화면에서 글이 보이는지는 TutorialLayoutInstrumentedTest 가 본다.
 */
class TutorialLayoutTest {

    private val pageWidth = 360.dp
    private val pageHeight = 519.dp

    /** 그림 칸의 폭. 쪽 폭에서 좌우 여백 24dp 씩을 뺀다. */
    private val illustrationWidth = 312.dp

    private fun assertDp(message: String, expected: Dp, actual: Dp, delta: Float = 0.01f) =
        assertEquals(message, expected.value, actual.value, delta)

    @Test
    fun `폭 360dp 폰의 폰 그림은 152dp 이고 진동 쪽은 양옆 표시 자리를 빼 127dp 다`() {
        assertDp("소리 쪽", 152.2.dp, demoPhoneHeight(TutorialScene.Sound, illustrationWidth), 0.1f)
        assertDp("진동 쪽", 126.8.dp, demoPhoneHeight(TutorialScene.Vibration, illustrationWidth), 0.1f)
        // 넓어도 폰 그림은 340dp 를 넘지 않는다.
        assertDp("넓은 칸", 340.dp / 2.05f, demoPhoneHeight(TutorialScene.Sound, 752.dp))
    }

    @Test
    fun `세로 화면의 그림 칸은 폰 그림 높이에 작은 여백만 더한다`() {
        // 예전에는 쪽 높이의 절반(260dp)을 그대로 잡아 그림 위아래로 100dp 넘게 비었고, 그만큼 글이 화면 밖으로 밀렸다.
        for (page in TutorialPage.entries) {
            val scene = page.scene ?: continue
            val drawing = demoPhoneHeight(scene, illustrationWidth)
            val height = portraitIllustrationHeight(page, pageWidth, pageHeight, fontScale = 1f)
            assertTrue("${page.name}: 그림($drawing)이 칸($height)에 들어가지 않는다", height >= drawing)
            assertTrue("${page.name}: 칸($height)이 그림($drawing)보다 16dp 넘게 높다", height <= drawing + 16.dp)
        }
    }

    @Test
    fun `넓은 화면을 세워 들어도 그림 칸은 그림만큼만 높다`() {
        // 태블릿을 세워 든 경우. 예전에는 쪽 높이의 절반, 최대 380dp 를 차지했다.
        val height = portraitIllustrationHeight(TutorialPage.Sound, 800.dp, 1100.dp, fontScale = 1f)
        assertTrue("칸($height)이 폰 그림 최대 높이보다 16dp 넘게 높다", height <= 340.dp / 2.05f + 16.dp)
    }

    @Test
    fun `글꼴이 크면 그림 칸은 쪽 높이의 3분의 1을 넘지 않는다`() {
        val large = portraitIllustrationHeight(TutorialPage.Types, pageWidth, pageHeight, fontScale = 1.3f)
        assertTrue("글꼴 1.3배에서 쪽 높이의 3분의 1을 넘었다: $large", large <= pageHeight * 0.34f)
        // 쪽이 낮아 그 비율이 그림보다 낮으면 그 높이가 그림을 줄인다(그림 폭 = 칸 높이 × 2.05). 140dp 밑으로는 줄이지 않는다.
        assertDp("낮은 쪽", 140.dp, portraitIllustrationHeight(TutorialPage.Types, pageWidth, 400.dp, fontScale = 1.3f))
    }

    @Test
    fun `마지막 쪽의 홈 화면 그림은 쪽 높이의 비율 그대로다`() {
        // 세워 든 폰은 칸 높이가 그림 폭을 정한다. 칸을 줄이면 그림이 같이 작아진다.
        assertDp("보통 글꼴", pageHeight * 0.34f, portraitIllustrationHeight(TutorialPage.Start, pageWidth, pageHeight, fontScale = 1f))
        assertDp("낮은 쪽", 140.dp, portraitIllustrationHeight(TutorialPage.Start, pageWidth, 400.dp, fontScale = 1f))
    }

    @Test
    fun `가려진 것이 끝 빈칸뿐이면 흐림을 그리지 않는다`() {
        // 밀도 3(폭 360dp 폰)에서 끝 빈칸 24dp 는 72px 이다.
        val space = 72
        assertFalse("넘친 것이 빈칸 일부(13dp)뿐", showsBottomFade(value = 0, maxValue = 39, endSpace = space))
        assertFalse("넘친 것이 빈칸 전부", showsBottomFade(value = 0, maxValue = 72, endSpace = space))
        assertTrue("빈칸 위의 글이 1px 가려졌다", showsBottomFade(value = 0, maxValue = 73, endSpace = space))
        assertFalse("다 들어온다", showsBottomFade(value = 0, maxValue = 0, endSpace = space))
    }

    @Test
    fun `밀어 올리는 동안에는 남은 거리로 흐림을 정한다`() {
        val space = 72
        assertTrue("가려진 글이 아직 있다", showsBottomFade(value = 50, maxValue = 141, endSpace = space))
        assertFalse("빈칸만 남았다", showsBottomFade(value = 69, maxValue = 141, endSpace = space))
        assertFalse("끝까지 밀었다", showsBottomFade(value = 141, maxValue = 141, endSpace = space))
    }

    @Test
    fun `끝 빈칸이 없는 가로 화면은 조금이라도 가려지면 흐림을 그린다`() {
        assertTrue(showsBottomFade(value = 0, maxValue = 1, endSpace = 0))
        assertFalse(showsBottomFade(value = 0, maxValue = 0, endSpace = 0))
    }
}
