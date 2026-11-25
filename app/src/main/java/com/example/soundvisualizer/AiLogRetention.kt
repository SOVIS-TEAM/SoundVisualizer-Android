package com.example.soundvisualizer

import java.io.File

/**
 * 개발자 기록 폴더(`ai-log`)의 전체 상한 (#490). 오래된 파일부터 지워 폴더 전체를 [FOLDER_MAX_BYTES] 아래로 묶는다.
 *
 * 파일 하나의 상한(20MB)은 회차 하나에만 걸린다. 기록을 켜 두면 시각화를 켤 때마다 새 회차가 열려
 * 파일 두 개(`ai-<시각>.csv`, `latency-<시각>-….csv`)가 생기므로, 그것만으로는 폴더가 끝없이 커진다.
 * 앱 안에는 파일을 지울 곳이 없어 줄이려면 앱 데이터를 지워야 했다.
 *
 * 새 회차를 열 때 [AiDebugRecorder] 의 쓰는 스레드가 [prune] 을 부른다. 그 회차의 파일은 고르지 않고,
 * 그 회차가 두 파일을 상한까지 채울 자리([SESSION_MAX_BYTES])를 남긴다. 그래서 그 회차를 다 채워도
 * 폴더 전체가 상한 안에 머문다.
 *
 * 안드로이드에 의존하지 않아 기기 없이 검사한다 ([AiLogRetentionTest]).
 */
object AiLogRetention {

    /** 폴더 전체 상한 200MB. 다 채운 회차(두 파일 40MB)로 다섯 개다. 개인정보처리방침이 이 숫자를 적는다. */
    const val FOLDER_MAX_BYTES = 200L * 1024 * 1024

    /** 한 회차가 새로 쓸 수 있는 크기. 결과 기록과 지연 기록이 파일마다 상한에서 멈춘다. */
    const val SESSION_MAX_BYTES = AiDebugLogWriter.DEFAULT_MAX_BYTES + AiLatencyRecorder.MAX_BYTES

    /** 폴더에 있는 파일 하나. [modifiedMs] 가 작을수록 오래된 것이다. */
    data class Entry(val name: String, val bytes: Long, val modifiedMs: Long)

    /**
     * 기록 도구가 만든 파일인지. 결과 기록은 `ai-<시각>.csv`, 지연 기록은 `latency-<시각>-….csv` 다.
     * 그 밖의 파일(사람이 adb 로 넣은 것 등)은 세지도 지우지도 않는다.
     */
    fun isRecordFile(name: String): Boolean =
        name.endsWith(".csv") && (name.startsWith("ai-") || name.startsWith("latency-"))

    /** [stamp] 회차의 파일인지. 두 기록이 이름에 같은 시각을 쓴다([AiDebugRecorder.start]). */
    fun isSessionFile(name: String, stamp: String): Boolean =
        name == "ai-$stamp.csv" || name.startsWith("latency-$stamp-")

    /**
     * 지울 파일 이름을 오래된 것부터 고른다. 상한 안이면 빈 목록이다.
     *
     * [stamp] 회차의 파일과 기록 도구가 만들지 않은 파일은 세지도 고르지도 않는다. 나머지를 고친 시각이 이른
     * 것부터(같으면 이름 순으로) 골라, 남는 것의 합이 [folderMaxBytes] − [sessionMaxBytes] 이하가 되면 멈춘다.
     */
    fun filesToDelete(
        files: List<Entry>,
        stamp: String,
        folderMaxBytes: Long = FOLDER_MAX_BYTES,
        sessionMaxBytes: Long = SESSION_MAX_BYTES
    ): List<String> {
        val keepBytes = (folderMaxBytes - sessionMaxBytes).coerceAtLeast(0L)
        val others = files.filter { isRecordFile(it.name) && !isSessionFile(it.name, stamp) }
        var remaining = others.sumOf { it.bytes }
        val picked = ArrayList<String>()
        for (entry in others.sortedWith(compareBy<Entry>({ it.modifiedMs }, { it.name }))) {
            if (remaining <= keepBytes) break
            picked += entry.name
            remaining -= entry.bytes
        }
        return picked
    }

    /**
     * [dir] 를 훑어 [filesToDelete] 가 고른 파일을 지우고, 지운 파일 이름을 돌려준다.
     *
     * 디스크를 만지므로 쓰는 스레드에서만 부른다. 폴더가 아직 없거나 읽지 못하면 아무것도 하지 않는다(폴더를
     * 만들지 않는다). 폴더 안의 폴더는 보지 않는다. 지우지 못한 파일은 돌려주지 않고, 다음 회차에 다시 고른다.
     */
    fun prune(
        dir: File,
        stamp: String,
        folderMaxBytes: Long = FOLDER_MAX_BYTES,
        sessionMaxBytes: Long = SESSION_MAX_BYTES
    ): List<String> {
        val entries = dir.listFiles()
            ?.filter { it.isFile }
            ?.map { Entry(it.name, it.length(), it.lastModified()) }
            ?: return emptyList()
        val deleted = ArrayList<String>()
        for (name in filesToDelete(entries, stamp, folderMaxBytes, sessionMaxBytes)) {
            if (File(dir, name).delete()) deleted += name
        }
        return deleted
    }
}
