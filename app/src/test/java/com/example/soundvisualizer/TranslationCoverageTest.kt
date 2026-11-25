package com.example.soundvisualizer

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.Locale

/**
 * 영어·한국어에만 있거나 영어보다 뒤처진 번역을 막는다(#489).
 *
 * - 색 선택 창의 두 설명은 화면 읽어주기가 읽는 글이다. 영어·한국어에만 있을 때는 다른 16개 언어의 화면 읽어주기 사용자가
 *   색 사각형과 색조 막대의 설명을 영어로 들었다.
 * - 알림 권한 설명과 배터리 답은 영어에 문장이 늘 때(#47) 한국어만 따라와, 16개 언어가 옛 문장으로 남았다. 알림 권한
 *   설명에서는 직접 끄지 않았는데 꺼지면 알려 준다는 것과 거부하면 진동으로만 알린다는 것이 빠졌고, 배터리 답은 화면이
 *   꺼져도 소리를 계속 받는 것처럼 읽혔다.
 *
 * 다른 문구는 번역이 늦으면 영어로 보여도 된다(StringResourcesTest). 유닛 테스트는 app 모듈 폴더에서 돈다.
 */
class TranslationCoverageTest {

    private val resDir = File("src/main/res")

    /** strings.xml 이 있는 언어 폴더(values, values-xx). values-v31 같은 다른 폴더는 뺀다. */
    private val dirs: List<String> by lazy {
        val found = resDir.listFiles { f -> f.isDirectory && (f.name == "values" || f.name.startsWith("values-")) }
            .orEmpty()
            .filter { File(it, "strings.xml").isFile }
            .map { it.name }
            .sorted()
        assertTrue("res 폴더를 찾지 못했습니다: ${resDir.absolutePath}", found.size >= 18)
        found
    }

    private val strings: Map<String, Map<String, String>> by lazy { dirs.associateWith { SoundGroupFixtures.strings(it) } }

    @Test
    fun `색 선택 창의 화면 읽어주기 설명은 모든 언어에 있다`() {
        val missing = dirs.flatMap { dir -> COLOR_PICKER_KEYS.filter { it !in strings.getValue(dir) }.map { "$dir: $it" } }
        assertTrue("색 선택 창의 설명은 모든 언어에 넣습니다:\n${missing.joinToString("\n")}", missing.isEmpty())
    }

    @Test
    fun `문장이 늘어난 도움말의 번역은 영어보다 눈에 띄게 짧지 않다`() {
        val english = strings.getValue("values")
        val short = dirs.filter { it != "values" }.flatMap { dir ->
            val texts = strings.getValue(dir)
            // 같은 뜻을 쓰는 길이는 언어마다 다르다(중국어는 영어의 3분의 1쯤). 그 언어의 다른 도움말 문구가 영어보다 평소
            // 얼마나 긴지로 나눠, 언어와 상관없이 같은 기준으로 견준다.
            val others = texts.keys.filter { it.startsWith("help_") && it in english && it !in GROWN_HELP_KEYS }
            val usual = others.sumOf { texts.getValue(it).length }.toDouble() / others.sumOf { english.getValue(it).length }
            GROWN_HELP_KEYS.mapNotNull { key ->
                val text = texts[key] ?: return@mapNotNull null
                val ratio = text.length.toDouble() / english.getValue(key).length / usual
                if (ratio >= MIN_RATIO) null else "$dir: $key ${"%.2f".format(Locale.ROOT, ratio)}"
            }
        }
        assertTrue(
            "영어 문장이 늘었는데 번역이 옛 문장 그대로인 것 같습니다. 지금 영어의 뜻대로 다시 옮깁니다:\n${short.joinToString("\n")}",
            short.isEmpty()
        )
    }

    @Test
    fun `알림 권한 설명은 그 언어의 알림 중지 버튼과 홈 탭 이름을 그대로 쓴다`() {
        // 알림의 버튼과, 알림을 거부했을 때 무엇이 꺼졌는지 보여 주는 탭을 사용자가 그 이름으로 찾는다(CONTRIBUTING 의 번역 규칙).
        val wrong = dirs.flatMap { dir ->
            val texts = strings.getValue(dir)
            val desc = texts["help_perm_notification_desc"] ?: return@flatMap emptyList<String>()
            listOf("notification_stop", "tab_home").mapNotNull { key ->
                val name = texts.getValue(key)
                if (desc.contains(name)) null else "$dir: help_perm_notification_desc 에 '$name'($key) 가 없다"
            }
        }
        assertTrue(wrong.joinToString("\n"), wrong.isEmpty())
    }

    private companion object {
        /** 색 사각형과 색조 막대의 화면 읽어주기 설명. */
        val COLOR_PICKER_KEYS = listOf("color_picker_square_desc", "color_picker_hue_desc")

        /** 영어에 문장이 늘어 번역도 다시 옮긴 도움말 문구. */
        val GROWN_HELP_KEYS = listOf("help_perm_notification_desc", "help_faq_battery_a")

        /**
         * 그 언어의 평소 비율에 견준 길이의 하한. 옛 문장 그대로인 번역은 0.30~0.73, 다시 옮긴 번역은 0.83 이상이었다(#489).
         * 다 옮긴 다른 도움말 문구도 0.77 까지 내려가므로, 모든 도움말에 걸지 않고 문장이 늘어난 문구에만 건다.
         */
        const val MIN_RATIO = 0.75
    }
}
