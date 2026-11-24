package com.example.soundvisualizer

import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.DeviceConfigurationOverride
import androidx.compose.ui.test.Locales
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.WindowSize
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.compose.ui.test.then
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.soundvisualizer.ui.theme.SoundVisualizerTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale

/**
 * 탭 화면의 배치가 언어와 창 크기에 맞는지(#487).
 *
 * - 탭 줄은 보고 있는 쪽의 탭을 따라 밀린다. 독일어 탭 줄(약 490dp)은 폭 360dp 보다 넓어 넷째 탭 'Hilfe' 가 화면 밖에 있다.
 *   예전에는 화면을 넘겨 도움말로 가도 탭 줄이 제자리라 선택 표시가 보이지 않았다.
 * - 낮은 창(가로로 든 폰)의 분류 탭은 찾기 칸만 위에 붙고, 찾는 말을 넣으면 첫 결과가 찾기 칸 바로 밑에 온다. 예전에는
 *   찾기·거르기가 함께 붙어, 자판이 뜬 가로 화면에서 결과가 보일 자리가 없었다.
 *
 * 앱의 실제 언어와 기기의 화면 크기는 건드리지 않는다. 언어와 창 크기는 시험 안에서만 정한다(DeviceConfigurationOverride).
 * 기기 창이 정한 크기보다 작으면 dp 를 줄여 맞추므로, 어느 기기에서나 같은 배치를 본다. 기대하는 글자는 리소스에서 만든다.
 * 탭 화면만 그리므로 튜토리얼 설정은 보지 않고, 진동이 울리는 칸은 누르지 않는다.
 */
@RunWith(AndroidJUnit4::class)
class TabScreenLayoutInstrumentedTest {

    @get:Rule
    val rule = createComposeRule()

    private val context: Context get() = InstrumentationRegistry.getInstrumentation().targetContext

    /** 시험 안의 밀도. 기기 창이 정한 크기보다 작으면 기기의 밀도보다 작다. */
    private lateinit var density: Density

    @Before
    fun setUp() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync { SettingsManager.init(context) }
    }

    /**
     * 탭 화면을 [size] 창에 [start] 번째 탭으로 연다. [inGerman] 이면 앱 언어를 독일어로 둔다.
     * 보던 탭은 MainActivity 처럼 화면 밖에서 들고 있는다.
     */
    private fun showTabs(start: Int, size: DpSize, inGerman: Boolean = false) {
        val window = DeviceConfigurationOverride.WindowSize(size)
        val override = if (inGerman) DeviceConfigurationOverride.Locales(LocaleList("de")).then(window) else window
        rule.setContent {
            DeviceConfigurationOverride(override) {
                density = LocalDensity.current
                SoundVisualizerTheme {
                    Surface(modifier = Modifier.fillMaxSize(), color = BgColor) {
                        var tab by remember { mutableIntStateOf(start) }
                        LauncherApp(
                            selectedTab = tab,
                            onSelectTab = { tab = it },
                            onStart = {},
                            onStop = {},
                            onAddTile = {},
                            onOpenTutorial = {}
                        )
                    }
                }
            }
        }
        rule.waitForIdle()
    }

    /** 기기 언어와 상관없이 독일어 문구를 꺼낸다. */
    private fun german(id: Int): String {
        val config = Configuration(context.resources.configuration).apply { setLocale(Locale.GERMAN) }
        return context.createConfigurationContext(config).getString(id)
    }

    /** 탭 줄의 탭 하나. 쪽 안에 같은 글자가 있어도 탭만 고른다. */
    private fun tab(name: String) = hasText(name) and SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)

    /** 탭 화면의 페이저. 탭 화면에서 가로로 넘기는 지연 목록은 이것 하나다. */
    private val pager = SemanticsMatcher.keyIsDefined(SemanticsActions.ScrollToIndex) and
        SemanticsMatcher.keyIsDefined(SemanticsProperties.HorizontalScrollAxisRange)

    /**
     * 쪽을 하나 넘긴다(사용자처럼). 탭 줄 바로 밑, 쪽의 맨 위를 가로로 민다. 그 높이에는 어느 탭에도 가로로 끄는 조작(슬라이더)이
     * 없고, 맨 위의 누르는 칸(설정 탭의 두 칸, 도움말의 펼치기 카드)은 가로로 밀면 눌리지 않는다.
     */
    private fun swipePage(forward: Boolean) {
        rule.onNode(pager).performTouchInput {
            val y = 24.dp.toPx()
            val from = if (forward) width * 0.8f else width * 0.2f
            val to = if (forward) width * 0.2f else width * 0.8f
            swipe(Offset(from, y), Offset(to, y), durationMillis = 200)
        }
        rule.waitForIdle()
    }

    /** 탭이 탭 줄 끝에서 잘리지 않고 다 보이는지. assertIsDisplayed 는 한 점만 보여도 통과한다. */
    private fun assertFullyShown(name: String) {
        val node = rule.onNode(tab(name)).fetchSemanticsNode()
        assertEquals("탭 \"$name\" 이 화면 끝에서 잘렸다", node.size.width.toFloat(), node.boundsInRoot.width, 1f)
    }

    @Test
    fun germanTabRowFollowsSwipesToTheLastTab() {
        showTabs(start = 0, size = DpSize(360.dp, 640.dp), inGerman = true)
        val home = german(R.string.tab_home)
        val help = german(R.string.tab_help)
        rule.onNode(tab(home)).assertIsSelected().assertIsDisplayed()
        // 넷째 탭이 처음부터 보이면 이 시험은 볼 것이 없다. 독일어 탭 줄은 폭 360dp 보다 넓다.
        rule.onNode(tab(help)).assertIsNotDisplayed()

        // 홈에서 세 번 넘기면 도움말이다. 넘길 때마다 탭 줄이 따라와 고른 탭이 화면에 있다.
        repeat(3) { swipePage(forward = true) }
        rule.onNode(tab(help)).assertIsSelected().assertIsDisplayed()
        assertFullyShown(help)

        // 돌아갈 때도 따라온다.
        repeat(3) { swipePage(forward = false) }
        rule.onNode(tab(home)).assertIsSelected().assertIsDisplayed()
        assertFullyShown(home)
    }

    @Test
    fun germanTabRowShowsTheLastTabWhenOpenedOnIt() {
        // 도움말 탭(넷째, 3번)을 본 채로 화면을 돌리거나 언어를 바꾸면 그 탭으로 바로 다시 열린다. 넘기지 않아도 보여야 한다.
        showTabs(start = 3, size = DpSize(360.dp, 640.dp), inGerman = true)
        val help = german(R.string.tab_help)
        rule.onNode(tab(help)).assertIsSelected().assertIsDisplayed()
        assertFullyShown(help)
    }

    /**
     * 분류 탭의 세로 목록. 분류 탭으로 바로 열면 화면의 세로 지연 목록은 이것 하나지만, 옆 쪽을 미리 그려 둘 때와 가리려고
     * 찾기 칸이나 탭 제목을 품은 목록으로 고른다. 낮은 창에서는 처음에 탭 제목만 보이고, 밀고 나면 찾기 칸만 남는다.
     */
    private val classifyList
        get() = SemanticsMatcher.keyIsDefined(SemanticsActions.ScrollToIndex) and
            SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange) and
            (hasAnyDescendant(hasSetTextAction()) or hasAnyDescendant(hasText(context.getString(R.string.classify_title))))

    /** 거르기 줄의 '전체' 칸. */
    private val filterAll get() = hasText(context.getString(R.string.classify_filter_all)) and isSelectable()

    /**
     * 분류 탭(셋째, 2번)으로 연다. 소리 목록은 메인 스레드 밖에서 만들어지므로 목록이 그려질 때까지 기다린다
     * (ClassifyTabInstrumentedTest 와 같다).
     */
    private fun showClassify(size: DpSize) {
        showTabs(start = 2, size = size)
        rule.waitUntil(timeoutMillis = 15_000) { rule.onAllNodes(classifyList).fetchSemanticsNodes().isNotEmpty() }
        rule.waitForIdle()
    }

    private fun scrollClassifyTo(index: Int) {
        rule.onNode(classifyList).performScrollToIndex(index)
        rule.waitForIdle()
    }

    @Test
    fun shortWindowPinsOnlyTheSearchField() {
        // 가로로 든 폰(높이 360dp). 결과까지 내려가면 찾기 칸은 위에 남고, 거르기 줄은 목록과 함께 올라가 사라진다.
        showClassify(DpSize(640.dp, 360.dp))
        scrollClassifyTo(STICKY_HEADER_INDEX)
        // 거르기 줄을 제대로 찾는지 먼저 본다. 찾기 칸 바로 밑에 있다.
        rule.onNode(filterAll).assertIsDisplayed()

        scrollClassifyTo(SHORT_WINDOW_FIRST_RESULT_INDEX)
        rule.onNode(hasSetTextAction()).assertIsDisplayed()
        rule.onNode(filterAll).assertIsNotDisplayed()
    }

    @Test
    fun tallWindowKeepsTheFiltersPinned() {
        // 세로 화면은 예전 그대로 찾기 칸과 거르기 줄이 함께 붙어 있다.
        showClassify(DpSize(360.dp, 640.dp))
        scrollClassifyTo(SHORT_WINDOW_FIRST_RESULT_INDEX)
        rule.onNode(hasSetTextAction()).assertIsDisplayed()
        rule.onNode(filterAll).assertIsDisplayed()
    }

    @Test
    fun typingInAShortWindowShowsResultsRightBelowTheSearchField() {
        showClassify(DpSize(640.dp, 360.dp))
        scrollClassifyTo(STICKY_HEADER_INDEX)
        // 찾기 칸을 누르지 않고 글자만 넣는다. 자판이 뜨면 창이 그만큼 줄어 기기마다 남는 자리가 다르다.
        // 영어 이름으로도 찾으므로 기기 언어와 상관없이 사이렌 묶음이 나오고, 결과는 위협음 구역 머리부터다.
        rule.onNode(hasSetTextAction()).performSemanticsAction(SemanticsActions.SetText) { it(AnnotatedString("Siren")) }
        rule.waitForIdle()

        val field = rule.onNode(hasSetTextAction()).fetchSemanticsNode().boundsInRoot
        val heading = rule.onNode(
            hasText(context.getString(R.string.sound_type_danger)) and SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading)
        ).fetchSemanticsNode().boundsInRoot
        // 찾기 칸 아래 여백(8dp)과 구역 머리 위 여백(16dp)뿐이다. 거르기 줄과 바꾼 소리 줄이 끼면 100dp 가 넘게 벌어진다.
        val gap = heading.top - field.bottom
        val limit = with(density) { 40.dp.toPx() }
        assertTrue("첫 결과가 찾기 칸 밑으로 ${gap}px 떨어져 있다(${limit}px 이하여야 한다)", gap >= -1f && gap <= limit)
    }
}
