package com.example.soundvisualizer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/**
 * 백업과 기기 간 이전이 설정(프리퍼런스)만 옮기는지 (#490).
 *
 * 규칙 파일은 두 개다. Android 11 이하는 `backup_rules.xml`(fullBackupContent)을, Android 12 이상은
 * `data_extraction_rules.xml` 을 본다. 뒤의 것은 클라우드 백업과 기기 간 이전을 절마다 따로 정한다.
 * 기기 간 이전 절이 없으면 targetSdk 31 이상 앱은 backup_rules.xml 도 무시하고 포함 규칙 없이 앱 데이터 전체를
 * 옮긴다(클라우드 백업 절이 없으면 backup_rules.xml 을 쓴다). 그래서 기기 간 이전 절이 없던 동안에는 개발자
 * 기록 CSV 와 모델 사본까지 새 폰으로 갔다. 포함 규칙이 하나도 없는 절도 "전체" 다.
 *
 * 기기 없이 리소스 파일을 읽어 막는다. 유닛 테스트는 app 모듈 폴더에서 돈다(AppWindowThemeTest 와 같다).
 */
class BackupRulesTest {

    private val xmlDir = File("src/main/res/xml")

    @Test
    fun `매니페스트가 두 규칙 파일을 가리킨다`() {
        // 다른 파일을 가리키면 아래 검사가 실제 규칙을 보지 않는다.
        val application = parseRoot(File("src/main/AndroidManifest.xml"))
            .getElementsByTagName("application").item(0) as Element
        assertEquals("@xml/backup_rules", application.getAttributeNS(ANDROID_NS, "fullBackupContent"))
        assertEquals("@xml/data_extraction_rules", application.getAttributeNS(ANDROID_NS, "dataExtractionRules"))
    }

    @Test
    fun `Android 11 이하 백업과 기기 간 이전은 설정만 담는다`() {
        val root = parseRoot(File(xmlDir, "backup_rules.xml"))
        assertEquals("full-backup-content", root.tagName)
        assertOnlySettings("backup_rules.xml", root)
    }

    @Test
    fun `Android 12 이상 클라우드 백업과 기기 간 이전은 설정만 담는다`() {
        val root = parseRoot(File(xmlDir, "data_extraction_rules.xml"))
        assertEquals("data-extraction-rules", root.tagName)
        val sections = childElements(root)
        for (name in listOf("cloud-backup", "device-transfer")) {
            assertEquals(
                if (name == "device-transfer") {
                    "data_extraction_rules.xml 에 <device-transfer> 절을 하나 두세요. 없으면 기기 간 이전이 앱 데이터 전체를 옮깁니다"
                } else {
                    "data_extraction_rules.xml 에 <$name> 절을 하나 두세요. 없으면 Android 12 이상 클라우드 백업이 backup_rules.xml 로 돌아갑니다"
                },
                1,
                sections.count { it.tagName == name }
            )
        }
        // 나중에 다른 절(cross-platform-transfer 등)을 더해도 설정만 담는다.
        for (section in sections) assertOnlySettings("<${section.tagName}>", section)
    }

    /** [rules] 바로 아래에 포함 규칙이 하나 이상 있고, 모두 설정(sharedpref)인지. */
    private fun assertOnlySettings(where: String, rules: Element) {
        val includes = childElements(rules).filter { it.tagName == "include" }
        assertTrue("$where 에 include 가 없으면 앱 데이터 전체가 담깁니다", includes.isNotEmpty())
        for (include in includes) {
            assertEquals(
                "$where 는 설정만 담습니다. 개발자 기록(external)이나 모델 사본(file)을 넣지 마세요",
                "sharedpref",
                include.getAttribute("domain")
            )
        }
    }

    private fun childElements(parent: Element): List<Element> {
        val nodes = parent.childNodes
        return (0 until nodes.length).mapNotNull { nodes.item(it) as? Element }
    }

    private fun parseRoot(file: File): Element {
        if (!file.isFile) throw AssertionError("파일이 없습니다: ${file.absolutePath}")
        val factory = DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = true
            setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
        }
        return factory.newDocumentBuilder().parse(file).documentElement
    }

    private companion object {
        const val ANDROID_NS = "http://schemas.android.com/apk/res/android"
    }
}
