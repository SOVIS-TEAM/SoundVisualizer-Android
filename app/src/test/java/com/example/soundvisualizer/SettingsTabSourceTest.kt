package com.example.soundvisualizer

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * 설정 탭의 자세한 설정 화면이 정해 둔 규칙을 쓰는지. Compose 화면이라 JVM 에서 그릴 수 없으므로 소스에서 확인한다
 * (EasySettingsSourceTest 와 같은 방식).
 */
class SettingsTabSourceTest {

    private val tab = File("src/main/java/com/example/soundvisualizer/SettingsTab.kt").readText(Charsets.UTF_8)
        .split('\n')
        .filterNot { it.trimStart().startsWith("*") || it.trimStart().startsWith("//") || it.contains("/**") }
        .joinToString("\n")

    @Test
    fun `소리 종류 스위치 이름은 좁은 칸의 이름표처럼 하이픈으로 끊는다`() {
        // 스위치와 색상 버튼을 뺀 칸이라 폭 360dp 폰에서 148dp 다. 독일어 "Umgebungsgeräusche zeigen" 이 하이픈 없이
        // 낱말 중간에서 끊겼다(#489). 같은 낱말을 쓰는 쉬운 설정(#434)과 분류 탭은 이미 하이픈을 넣는다.
        assertTrue("ColorSettingRow 를 찾지 못했다", tab.contains("fun ColorSettingRow("))
        val label = tab.substringAfter("fun ColorSettingRow(").substringBefore("Switch(")
        assertTrue("스위치 앞에 이름 글자가 없다", label.contains("Text("))
        assertTrue("소리 종류 스위치 이름에 하이픈 줄바꿈이 없다", label.contains("style = wrappingLabelStyle()"))
    }
}
