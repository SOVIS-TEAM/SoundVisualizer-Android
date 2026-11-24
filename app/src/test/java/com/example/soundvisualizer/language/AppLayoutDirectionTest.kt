package com.example.soundvisualizer.language

import androidx.compose.ui.unit.LayoutDirection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/**
 * 화면의 좌우 방향(#489). 문구를 뒤집힌 배치에 맞춰 옮긴 언어(아랍어)만 오른쪽에서 왼쪽으로 그리고, 나머지는 폰 언어가 RTL
 * 이어도 왼쪽에서 오른쪽으로 그린다. 번역이 없는 RTL 언어(히브리어 등) 하나만 쓰는 폰에서 영어 문구를 뒤집어 그리면 도움말의
 * '오른쪽 아래', '왼쪽에서 네 번째'가 화면과 반대가 된다.
 *
 * 값은 res 의 bools.xml 에서 읽는다. 유닛 테스트는 app 모듈 폴더에서 돈다.
 */
class AppLayoutDirectionTest {

    private val resDir = File("src/main/res")

    @Test
    fun `기본 폴더는 RTL 배치를 쓰지 않고 아랍어만 쓴다`() {
        assertEquals("values/bools.xml 의 rtl_layout", false, rtlLayout("values"))
        assertEquals("values-ar/bools.xml 의 rtl_layout", true, rtlLayout("values-ar"))
    }

    @Test
    fun `오른쪽에서 쓰는 글자의 지원 언어만 RTL 배치를 쓴다`() {
        // 언어 이름(그 언어로 쓴 이름)의 첫 글자로 쓰는 방향을 가린다. RTL 언어를 번역해 넣으면서 bools.xml 을 빠뜨리면 그 언어도
        // 왼쪽에서 오른쪽으로 그려, 뒤집힌 배치에 맞춰 옮긴 방향 표현이 화면과 어긋난다. 값이 없는 폴더는 안드로이드처럼 기본 폴더 값을 쓴다.
        val wrong = AppLanguages.all.mapNotNull { language ->
            val rtlScript = isRtlScript(language.endonym)
            val rtl = rtlLayout(language.resourceDir) ?: rtlLayout("values")
            if (rtl == rtlScript) null else "${language.resourceDir}: rtl_layout=$rtl, 오른쪽에서 쓰는 글자=$rtlScript"
        }
        assertTrue(wrong.joinToString("\n"), wrong.isEmpty())
    }

    @Test
    fun `RTL 배치를 쓰지 않는 언어는 폰 언어가 RTL 이어도 왼쪽에서 오른쪽으로 그린다`() {
        assertEquals(LayoutDirection.Ltr, appLayoutDirection(rtlLayout = false, system = LayoutDirection.Rtl))
        assertEquals(LayoutDirection.Ltr, appLayoutDirection(rtlLayout = false, system = LayoutDirection.Ltr))
        // 아랍어는 시스템이 정한 방향 그대로다.
        assertEquals(LayoutDirection.Rtl, appLayoutDirection(rtlLayout = true, system = LayoutDirection.Rtl))
        assertEquals(LayoutDirection.Ltr, appLayoutDirection(rtlLayout = true, system = LayoutDirection.Ltr))
    }

    @Test
    fun `두 액티비티는 화면 맨 바깥에서 방향을 정한다`() {
        // 테마와 대화 상자까지 모두 같은 방향을 받으려면 setContent 의 맨 바깥이어야 한다. Compose 화면이라 소스에서 확인한다.
        for (path in listOf("MainActivity.kt", "tile/StartVisualizerActivity.kt")) {
            val source = File("src/main/java/com/example/soundvisualizer/$path").readText(Charsets.UTF_8)
            val content = source.substringAfter("setContent {", missingDelimiterValue = "")
            assertTrue("$path: setContent 를 찾지 못했다", content.isNotEmpty())
            val first = content.lines().map { it.trim() }.first { it.isNotEmpty() && !it.startsWith("//") }
            assertEquals("$path: setContent 의 맨 바깥", "ProvideAppLayoutDirection {", first)
        }
    }

    /** [dir] 의 bools.xml 에 있는 rtl_layout. 파일이나 값이 없으면 null 이다. */
    private fun rtlLayout(dir: String): Boolean? {
        val file = File(resDir, "$dir/bools.xml")
        if (!file.isFile) return null
        val factory = DocumentBuilderFactory.newInstance().apply {
            setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
        }
        val nodes = factory.newDocumentBuilder().parse(file).documentElement.getElementsByTagName("bool")
        return (0 until nodes.length).map { nodes.item(it) as Element }
            .firstOrNull { it.getAttribute("name") == "rtl_layout" }
            ?.textContent?.trim()?.toBooleanStrict()
    }

    /** 글의 첫 글자가 오른쪽에서 왼쪽으로 쓰는 글자(히브리 문자, 아랍 문자 등)인지. */
    private fun isRtlScript(text: String): Boolean {
        val first = text.firstOrNull { Character.isLetter(it) } ?: return false
        return when (Character.getDirectionality(first)) {
            Character.DIRECTIONALITY_RIGHT_TO_LEFT, Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC -> true
            else -> false
        }
    }
}
