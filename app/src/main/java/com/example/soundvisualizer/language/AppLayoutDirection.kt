package com.example.soundvisualizer.language

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.booleanResource
import androidx.compose.ui.unit.LayoutDirection
import com.example.soundvisualizer.R

/**
 * 앱 화면의 좌우 방향을 정해 [content] 에 준다. 액티비티의 setContent 맨 바깥에서 부른다(#489).
 *
 * 매니페스트가 supportsRtl 이라 폰 언어가 오른쪽에서 왼쪽으로 쓰는 언어(RTL)면 시스템이 화면을 뒤집는다. 그런데 RTL 번역은
 * 아랍어뿐이다. 번역이 없는 RTL 언어(히브리어·페르시아어·우르두어) 하나만 쓰는 폰에서는 문구가 영어인데 배치만 뒤집혀,
 * 영어 도움말의 '오른쪽 아래의 지구본 버튼', '왼쪽에서 네 번째 칸'이 화면과 반대가 됐다. 그래서 문구를 뒤집힌 배치에 맞춰
 * 옮긴 언어(res 의 rtl_layout 이 true)만 시스템 방향을 따르고, 나머지는 왼쪽에서 오른쪽으로 그린다.
 *
 * 대화 상자와 팝업도 이 방향을 물려받는다. 오버레이의 그림은 절대 좌표라 상관없지만, 그 위의 개발자 HUD 는
 * Compose 배치라 OverlayService 도 이것으로 감싼다.
 */
@Composable
fun ProvideAppLayoutDirection(content: @Composable () -> Unit) {
    val direction = appLayoutDirection(
        rtlLayout = booleanResource(R.bool.rtl_layout),
        system = LocalLayoutDirection.current
    )
    CompositionLocalProvider(LocalLayoutDirection provides direction) {
        content()
    }
}

/**
 * 화면을 그릴 좌우 방향. 문구가 뒤집힌 배치에 맞춰져 있으면([rtlLayout]) 시스템이 정한 방향([system])을 따르고,
 * 아니면 늘 왼쪽에서 오른쪽이다.
 */
internal fun appLayoutDirection(rtlLayout: Boolean, system: LayoutDirection): LayoutDirection =
    if (rtlLayout) system else LayoutDirection.Ltr
