package com.example.soundvisualizer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 탭 줄은 보고 있는 쪽의 탭을 따라 밀린다(#487). 미는 거리는 [tabRowScrollTarget] 이 탭 폭으로 정한다.
 * 값은 px 이지만, 여기서는 dp 로 잰 숫자를 그대로 넣는다. 계산은 단위와 상관없다.
 */
class TabRowScrollTest {

    /** 탭 줄의 양끝 여백과 탭 사이 간격(24dp). */
    private val gap = 24

    /**
     * 독일어 탭 폭. Startseite·Einstellungen·Geräusche·Hilfe 를 20sp Bold, 글자 간격 0.5sp 로 잰 값이다(출시 전 점검).
     * 여백까지 합치면 탭 줄은 490dp 이고, 'Hilfe' 는 418-466dp 에 있어 폭 360dp 폰에서 통째로 화면 밖이다.
     */
    private val german = listOf(93, 128, 101, 48)

    /** 영어 탭 줄(약 373dp). 'Help' 가 301-349dp 라 폭 411dp 폰에 다 들어간다. */
    private val english = listOf(60, 80, 65, 48)

    @Test
    fun `독일어 360dp 폰에서 도움말로 넘기면 탭 줄을 끝까지 민다`() {
        assertEquals("탭 줄 전체", 490, german.sum() + gap * (german.size + 1))
        val target = tabRowScrollTarget(german, selected = 3, gap = gap, viewport = 360, scroll = 0)
        assertEquals("뒤 여백까지 보이게 끝(490 - 360)까지 민다", 130, target)
        assertShown(german, selected = 3, scroll = target, viewport = 360)
    }

    @Test
    fun `이미 다 보이는 탭은 그대로 둔다`() {
        // 끝까지 민 채로 셋째 탭(Geräusche, 293-394dp)으로 돌아오면 앞뒤 여백까지 이미 보인다.
        assertEquals(130, tabRowScrollTarget(german, selected = 2, gap = gap, viewport = 360, scroll = 130))
        // 손으로 조금 밀어 둔 자리도, 고른 탭이 다 보이면 되돌리지 않는다.
        assertEquals(40, tabRowScrollTarget(german, selected = 1, gap = gap, viewport = 360, scroll = 40))
    }

    @Test
    fun `앞 탭으로 돌아오면 그 탭의 앞 여백까지 민다`() {
        // 둘째 탭(Einstellungen)은 141dp 에서 시작한다. 앞 여백 24dp 까지 보이게 117dp 로 민다.
        assertEquals(117, tabRowScrollTarget(german, selected = 1, gap = gap, viewport = 360, scroll = 130))
        assertEquals(0, tabRowScrollTarget(german, selected = 0, gap = gap, viewport = 360, scroll = 130))
    }

    @Test
    fun `탭 줄이 화면에 다 들어가면 밀지 않는다`() {
        for (selected in english.indices) {
            assertEquals("탭 $selected", 0, tabRowScrollTarget(english, selected, gap, viewport = 411, scroll = 0))
        }
    }

    @Test
    fun `탭이 보이는 폭보다 넓으면 탭의 시작 쪽을 맞춘다`() {
        // 아주 큰 글꼴에서 한 탭이 화면보다 넓을 때. 끝을 맞추면 탭 이름의 앞이 잘린다.
        val wide = listOf(100, 400, 100)
        assertEquals("둘째 탭의 앞 여백(124dp)부터 보인다", 124, tabRowScrollTarget(wide, selected = 1, gap = gap, viewport = 300, scroll = 0))
    }

    @Test
    fun `어느 자리에서 넘겨도 고른 탭이 다 보이고 밀 수 있는 끝을 넘지 않는다`() {
        val total = german.sum() + gap * (german.size + 1)
        for (viewport in listOf(320, 360, 384, 412)) {
            val maxScroll = total - viewport
            for (scroll in 0..maxScroll step 5) {
                for (selected in german.indices) {
                    val target = tabRowScrollTarget(german, selected, gap, viewport, scroll)
                    assertTrue("폭 $viewport, 민 거리 $scroll, 탭 $selected → $target", target in 0..maxScroll)
                    assertShown(german, selected, target, viewport)
                }
            }
        }
    }

    @Test
    fun `없는 탭 번호면 밀 수 있는 범위 안에서 그대로 둔다`() {
        assertEquals(130, tabRowScrollTarget(german, selected = 4, gap = gap, viewport = 360, scroll = 500))
        assertEquals(0, tabRowScrollTarget(german, selected = -1, gap = gap, viewport = 360, scroll = -10))
        assertEquals(50, tabRowScrollTarget(german, selected = 9, gap = gap, viewport = 360, scroll = 50))
    }

    /** [selected] 탭이 [scroll] 만큼 민 탭 줄의 보이는 폭([viewport]) 안에 다 들어가는지. */
    private fun assertShown(widths: List<Int>, selected: Int, scroll: Int, viewport: Int) {
        val start = gap + widths.take(selected).sumOf { it + gap }
        val end = start + widths[selected]
        assertTrue(
            "탭 $selected($start-${end}dp)이 보이는 자리($scroll-${scroll + viewport}dp) 밖이다",
            start >= scroll && end <= scroll + viewport
        )
    }
}
