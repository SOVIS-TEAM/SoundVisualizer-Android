package com.example.soundvisualizer.help

/**
 * 개인정보처리방침 주소(#468).
 *
 * 처리방침은 SOVIS 사이트에 한국어와 영어 두 벌이 있다. 앱 언어가 한국어면 한국어, 그 밖의 언어는 영어 페이지를 연다.
 * 앱 스토어 등록정보도 이 주소를 가리키므로 주소를 바꾸면 스토어 쪽도 함께 고친다.
 * 앱에는 인터넷 권한이 없다. 링크는 브라우저가 열고, 앱은 주소만 넘긴다.
 */
object PrivacyLink {

    const val KOREAN_URL = "https://sovis-team.github.io/privacy/"
    const val ENGLISH_URL = "https://sovis-team.github.io/en/privacy/"

    /** [language] 는 앱이 지금 쓰는 언어의 ISO 639 코드(예: "ko", "en", "ja"). */
    fun url(language: String): String = if (language.equals("ko", ignoreCase = true)) KOREAN_URL else ENGLISH_URL

    /** 브라우저가 없을 때 안내에 적는, 앞의 https:// 를 뺀 주소. */
    fun displayUrl(language: String): String = url(language).removePrefix("https://").removeSuffix("/")
}
