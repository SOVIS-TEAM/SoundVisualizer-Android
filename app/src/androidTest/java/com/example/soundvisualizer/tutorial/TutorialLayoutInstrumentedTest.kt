package com.example.soundvisualizer.tutorial

import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.DeviceConfigurationOverride
import androidx.compose.ui.test.FontScale
import androidx.compose.ui.test.ForcedSize
import androidx.compose.ui.test.Locales
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.then
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.soundvisualizer.BgColor
import com.example.soundvisualizer.R
import com.example.soundvisualizer.SettingsManager
import com.example.soundvisualizer.ui.theme.SoundVisualizerTheme
import java.util.Locale
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 작은 폰의 세로 화면에서 튜토리얼 글이 한 화면에 보이는지(#488). 폭 360dp·높이 780dp 폰(S22 급)에 상태 표시줄 29dp,
 * 3버튼 내비 48dp 를 두면 위아래 버튼 줄(184dp)을 뺀 쪽 높이가 519dp 다. 고치기 전에는 그림 칸이 쪽 높이의 절반을 차지해
 * 종류 쪽 범례의 ‘사람 소리’·‘위협음’ 줄과 진동 쪽 주의 문구의 끝줄이 화면 아래로 접혔다.
 *
 * 기기와 상관없이 같은 화면을 보도록 크기·글꼴 크기·언어(이슈에서 계산한 한국어)를 고정하고, 기기의 실제 시스템 바 대신
 * 위 높이만큼 비운다. 크기 규칙 자체는 JVM 테스트(TutorialLayoutTest)가 본다. 그림은 테스트에서 움직이지 않으므로
 * 진동 쪽에서도 폰은 울리지 않는다.
 */
@RunWith(AndroidJUnit4::class)
class TutorialLayoutInstrumentedTest {

    @get:Rule
    val rule = createComposeRule()

    private val context: Context get() = InstrumentationRegistry.getInstrumentation().targetContext

    private val korean: Context by lazy {
        context.createConfigurationContext(Configuration(context.resources.configuration).apply { setLocale(Locale.KOREAN) })
    }

    private fun text(id: Int) = korean.getString(id)

    @Before
    fun setUp() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync { SettingsManager.init(context) }
    }

    private fun showOnSmallPhone() {
        rule.setContent {
            DeviceConfigurationOverride(
                DeviceConfigurationOverride.ForcedSize(DpSize(360.dp, 780.dp))
                    .then(DeviceConfigurationOverride.FontScale(1f))
                    .then(DeviceConfigurationOverride.Locales(LocaleList("ko-KR")))
            ) {
                SoundVisualizerTheme {
                    Surface(modifier = Modifier.fillMaxSize(), color = BgColor) {
                        // 튜토리얼이 비우는 실제 시스템 바는 여기서 다 쓴 것으로 하고, 그 자리에 위 높이를 직접 비운다.
                        Box(
                            Modifier
                                .fillMaxSize()
                                .consumeWindowInsets(WindowInsets.safeDrawing)
                                .padding(top = 29.dp, bottom = 48.dp)
                        ) {
                            TutorialScreen(onClose = {})
                        }
                    }
                }
            }
        }
    }

    private fun goTo(page: TutorialPage) {
        repeat(page.ordinal) { rule.onNodeWithText(text(R.string.tutorial_next)).performClick() }
        rule.onNodeWithText(text(page.title)).assertIsDisplayed()
    }

    /**
     * [text] 가 쪽 안에 들어와 있고, 아래 흐림이 덮을 수 있는 자리(쪽 맨 아래 [SCROLL_FADE_HEIGHT])보다 위에 있는지.
     * 그러면 흐림을 그리든 그리지 않든 가려지지 않는다. 쪽은 그 글을 담은 세로 스크롤 칸이다.
     */
    private fun assertShownWithoutScrolling(text: String) {
        val bounds = rule.onNodeWithText(text, useUnmergedTree = true).getBoundsInRoot()
        val page = rule.onNode(
            SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange) and hasAnyDescendant(hasText(text)),
            useUnmergedTree = true
        ).getBoundsInRoot()
        val fadeTop = page.bottom - SCROLL_FADE_HEIGHT
        assertTrue("‘$text’ 의 위가 쪽 밖이다: 글 ${bounds.top}, 쪽 ${page.top}", bounds.top >= page.top)
        assertTrue("‘$text’ 가 쪽 아래 흐림 자리에 걸리거나 쪽 밖이다: 글 아래 ${bounds.bottom}, 흐림 위 $fadeTop", bounds.bottom <= fadeTop)
    }

    @Test
    fun allThreeTypeNamesShowOnTheTypesPage() {
        showOnSmallPhone()
        goTo(TutorialPage.Types)
        for (name in listOf(R.string.tutorial_type_ambient, R.string.tutorial_type_speech, R.string.tutorial_type_danger)) {
            assertShownWithoutScrolling(text(name))
        }
    }

    @Test
    fun aiNoteShowsOnTheVibrationPage() {
        showOnSmallPhone()
        goTo(TutorialPage.Vibration)
        assertShownWithoutScrolling(text(R.string.help_note_ai))
    }
}
