package com.example.soundvisualizer.help

/**
 * 도움말의 '업데이트 확인'(#500)이 어느 스토어로 보낼지.
 *
 * 앱에는 인터넷 권한이 없다. 새 버전이 있는지는 Play 스토어에서 설치한 경우에만 휴대폰의 Play 스토어 앱에 물을 수 있고,
 * 그 밖에는 스토어 페이지를 열어 거기서 업데이트하게 한다. 원스토어에서 받은 사람은 원스토어로 보낸다(원스토어가 업데이트를
 * 관리한다). GitHub 의 APK 처럼 스토어 밖에서 설치했으면 Play 스토어로 보낸다. 같은 팀 키로 서명해 Play 스토어 판이 그 위에 설치된다.
 *
 * 화면이나 Android API 없이 문자열만 다루므로 단위 테스트로 고정한다.
 */
enum class UpdateStore {
    /** Play 스토어에서 설치했다. Play 스토어 앱에 새 버전이 있는지 물을 수 있다. */
    Play,

    /** 원스토어에서 설치했다. 원스토어의 앱 페이지를 연다. */
    OneStore,

    /** 그 밖(GitHub APK, 다른 마켓, 알 수 없음). Play 스토어의 앱 페이지를 연다. */
    Other;

    companion object {
        const val PLAY_STORE_PACKAGE = "com.android.vending"

        /** 원스토어 앱과 원스토어 서비스, 예전 통신사 마켓(원스토어로 합쳐짐)의 패키지 이름. */
        val ONE_STORE_PACKAGES = setOf(
            "com.skt.skaf.A000Z00040",
            "com.skt.skaf.OA00018282",
            "com.kt.olleh.storefront",
            "com.kt.olleh.istore",
            "com.lguplus.appstore",
            "android.lgt.appstore",
        )

        /** 원스토어의 SoundVisualizer 페이지(상품 번호 0001013096). 원스토어 앱이 있으면 그 앱이 연다. */
        const val ONE_STORE_URL = "https://m.onestore.co.kr/v2/ko-kr/app/0001013096"

        /** 설치한 앱의 패키지 이름(Android 의 installingPackageName)으로 정한다. */
        fun of(installer: String?): UpdateStore = when {
            installer == PLAY_STORE_PACKAGE -> Play
            installer in ONE_STORE_PACKAGES -> OneStore
            else -> Other
        }

        /** Play 스토어 앱으로 여는 주소. */
        fun playMarketUri(packageName: String): String = "market://details?id=$packageName"

        /** Play 스토어 앱이 없을 때 브라우저로 여는 주소. */
        fun playWebUrl(packageName: String): String = "https://play.google.com/store/apps/details?id=$packageName"
    }
}

/** 업데이트 확인을 눌렀을 때 할 일. 앞의 것이 열리지 않으면 뒤의 것을 연다. */
data class UpdateLinks(val primary: String, val fallback: String?) {
    companion object {
        fun forStore(store: UpdateStore, packageName: String): UpdateLinks = when (store) {
            UpdateStore.OneStore -> UpdateLinks(UpdateStore.ONE_STORE_URL, null)
            UpdateStore.Play, UpdateStore.Other ->
                UpdateLinks(UpdateStore.playMarketUri(packageName), UpdateStore.playWebUrl(packageName))
        }
    }
}
