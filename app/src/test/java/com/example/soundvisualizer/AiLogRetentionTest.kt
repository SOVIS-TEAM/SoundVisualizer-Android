package com.example.soundvisualizer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files

/**
 * 개발자 기록 폴더의 전체 상한 (#490).
 *
 * 파일 하나의 상한(20MB)은 회차 하나에만 걸린다. 기록을 켜 두면 시각화를 켤 때마다 새 회차가 열려 폴더가
 * 끝없이 커졌고, 앱 안에는 지울 곳이 없었다. 새 회차를 열 때 무엇을 지우고 무엇을 남기는지 고정한다.
 */
class AiLogRetentionTest {

    /** 지금 여는 회차의 시각. 결과 기록과 지연 기록이 이름에 함께 쓴다. */
    private val stamp = "20261124-120000"

    private fun entry(name: String, bytes: Long, modifiedMs: Long) = AiLogRetention.Entry(name, bytes, modifiedMs)

    /** 상한 100 에 새 회차 자리 40 을 남기면, 나머지는 60 까지 둔다. */
    private fun pick(vararg files: AiLogRetention.Entry): List<String> =
        AiLogRetention.filesToDelete(files.toList(), stamp, folderMaxBytes = 100, sessionMaxBytes = 40)

    @Test
    fun `상한 안이면 아무것도 지우지 않는다`() {
        val picked = pick(
            entry("ai-20261120-090000.csv", 30, 1_000_000),
            entry("latency-20261120-090000-1-5.csv", 30, 1_000_500)
        )
        assertEquals(emptyList<String>(), picked)
    }

    @Test
    fun `오래된 것부터 지우고 상한 아래로 들어오면 멈춘다`() {
        val picked = pick(
            entry("ai-20261122-090000.csv", 30, 3_000_000),
            entry("ai-20261120-090000.csv", 30, 1_000_000),
            entry("latency-20261121-090000-2-7.csv", 30, 2_000_000)
        )
        assertEquals("가장 오래된 것 하나면 60 에 닿는다", listOf("ai-20261120-090000.csv"), picked)
    }

    @Test
    fun `큰 파일이 여럿이면 상한 아래로 들어올 때까지 이어서 지운다`() {
        val picked = pick(
            entry("ai-20261120-090000.csv", 50, 1_000_000),
            entry("latency-20261120-090000-1-5.csv", 50, 1_000_500),
            entry("ai-20261121-090000.csv", 50, 2_000_000),
            entry("latency-20261121-090000-2-7.csv", 10, 2_000_500)
        )
        assertEquals(
            listOf("ai-20261120-090000.csv", "latency-20261120-090000-1-5.csv"),
            picked
        )
    }

    @Test
    fun `지금 회차의 파일은 세지도 지우지도 않는다`() {
        // 지연 기록은 따로 도는 스레드가 같은 시각으로 만든다. 폴더를 훑는 사이에 생겨도 지우면 안 된다.
        val picked = pick(
            entry("ai-$stamp.csv", 500, 1),
            entry("latency-$stamp-3-77.csv", 500, 2),
            entry("ai-20261120-090000.csv", 30, 1_000_000)
        )
        assertEquals(emptyList<String>(), picked)
    }

    @Test
    fun `기록 도구가 만들지 않은 파일은 세지도 지우지도 않는다`() {
        val picked = pick(
            entry("notes.txt", 500, 1),
            entry("ai-20261119-090000.txt", 500, 2),
            entry("summary.csv", 500, 3),
            entry("ai-20261120-090000.csv", 30, 1_000_000)
        )
        assertEquals(emptyList<String>(), picked)
    }

    @Test
    fun `고친 시각이 같으면 이름 순으로 지운다`() {
        val picked = pick(
            entry("latency-20261120-090000-1-5.csv", 50, 1_000_000),
            entry("ai-20261120-090000.csv", 50, 1_000_000)
        )
        assertEquals(listOf("ai-20261120-090000.csv"), picked)
    }

    @Test
    fun `새 회차가 두 파일을 다 채울 자리를 남긴다`() {
        assertEquals(
            "개인정보처리방침이 이 숫자(약 200MB)를 적는다. 바꾸면 PRIVACY.md 와 사이트 방침도 고친다",
            200L * 1024 * 1024,
            AiLogRetention.FOLDER_MAX_BYTES
        )
        assertEquals(
            "결과 기록과 지연 기록이 파일마다 상한에서 멈춘다",
            AiDebugLogWriter.DEFAULT_MAX_BYTES + AiLatencyRecorder.MAX_BYTES,
            AiLogRetention.SESSION_MAX_BYTES
        )
        // 나머지를 상한까지 두면 이 회차가 다 쓴 뒤 폴더가 상한을 넘는다.
        val keep = AiLogRetention.FOLDER_MAX_BYTES - AiLogRetention.SESSION_MAX_BYTES
        val atLimit = listOf(entry("ai-20261120-090000.csv", keep, 1_000_000))
        assertEquals(emptyList<String>(), AiLogRetention.filesToDelete(atLimit, stamp))
        val over = listOf(entry("ai-20261120-090000.csv", keep + 1, 1_000_000))
        assertEquals(listOf("ai-20261120-090000.csv"), AiLogRetention.filesToDelete(over, stamp))
    }

    @Test
    fun `폴더를 훑어 고른 파일만 지운다`() {
        withTempDir { dir ->
            val old = file(dir, "ai-20261120-090000.csv", 30, 1_000_000)
            val middle = file(dir, "latency-20261121-090000-2-7.csv", 30, 2_000_000)
            val recent = file(dir, "ai-20261122-090000.csv", 30, 3_000_000)
            val current = file(dir, "ai-$stamp.csv", 30, 500_000)
            val stranger = file(dir, "notes.txt", 30, 100_000)
            // 이름이 기록 파일 같아도 폴더는 보지 않는다.
            val folder = File(dir, "ai-20261119-090000.csv").apply {
                assertTrue(mkdir())
                assertTrue(setLastModified(10_000))
            }

            val deleted = AiLogRetention.prune(dir, stamp, folderMaxBytes = 100, sessionMaxBytes = 40)

            assertEquals(listOf(old.name), deleted)
            assertFalse("고른 파일이 남았다", old.exists())
            for (kept in listOf(middle, recent, current, stranger, folder)) {
                assertTrue("지우지 않아야 할 것을 지웠다: ${kept.name}", kept.exists())
            }
        }
    }

    @Test
    fun `폴더가 아직 없으면 아무것도 하지 않는다`() {
        withTempDir { dir ->
            val missing = File(dir, "ai-log")
            assertEquals(emptyList<String>(), AiLogRetention.prune(missing, stamp))
            assertFalse("훑기만 해야 한다. 켜 보기만 한 기기에 폴더를 남기지 않는다", missing.exists())
        }
    }

    /** [bytes] 크기의 파일을 만들고 고친 시각을 [modifiedMs] 로 둔다. */
    private fun file(dir: File, name: String, bytes: Int, modifiedMs: Long): File =
        File(dir, name).apply {
            writeBytes(ByteArray(bytes))
            assertTrue("고친 시각을 정하지 못했다: $name", setLastModified(modifiedMs))
        }

    private fun withTempDir(block: (File) -> Unit) {
        val directory = Files.createTempDirectory("ai-log-retention").toFile()
        try {
            block(directory)
        } finally {
            directory.deleteRecursively()
        }
    }
}
