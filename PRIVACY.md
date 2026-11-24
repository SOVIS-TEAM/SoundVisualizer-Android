<!-- 이 파일은 SOVIS 사이트의 개인정보처리방침과 같은 내용이다. 한쪽을 고치면 다른 쪽도 함께 고친다. -->

# SoundVisualizer 개인정보처리방침

시행일: 2026년 11월 24일

SoundVisualizer(이하 "앱")는 팀 SOVIS가 만든 Android 앱입니다. 이 방침은 앱이 어떤 정보를 다루는지 설명합니다.

## 1. 수집하는 정보

앱은 **어떤 개인정보도 수집하지 않습니다.**

- 앱에는 인터넷 접속 권한이 없어, 어떤 정보도 개발자나 다른 곳으로 보내지 않습니다.
- 회원가입, 광고, 사용 통계 도구가 없습니다.

## 2. 소리를 다루는 방법

- 앱은 소리를 받아 화면 가장자리에 그리고 진동으로 알리는 데에만 씁니다.
- 받은 소리는 기기 메모리에서만 처리하고, 녹음하거나 파일로 저장하지 않습니다.
- 소리 종류를 판별하는 AI도 기기 안에서만 동작합니다.
- 다른 앱의 소리를 받으려면 Android의 화면 녹화 동의가 필요합니다. 앱은 이 동의로 소리만 받고, 화면 영상은 받지 않습니다.

## 3. 기기에 저장되는 정보

- **설정값:** 모드, 색, 크기, 진동 등 앱 설정을 기기 안에 저장합니다. 설정과 함께 기기를 구분하는 값과 설치 시각도 저장되며, 이 두 값은 백업에서 옮겨 온 설정인지 가리는 데에만 씁니다. Android 기기 백업을 켜 두었다면 설정값과 이 두 값이 사용자의 Google 계정 백업에 포함될 수 있습니다. 이 백업은 Google이 관리하며, 개발자는 볼 수 없습니다.
- **개발자 기록:** 설정의 개발자 모드에서 기록을 직접 켠 경우에만, AI가 판별한 결과(소리 종류 이름, 점수, 시각)를 앱 전용 폴더에 CSV 파일로 저장합니다. 새 기록을 시작할 때 파일이 모두 합쳐 약 200MB를 넘지 않도록 오래된 파일부터 지웁니다. 소리 자체는 저장하지 않으며, 앱을 지우면 함께 지워집니다.

## 4. 권한과 쓰임

| 권한 | 쓰임 |
| --- | --- |
| 마이크 | 휴대폰에서 재생되는 소리와, 외부 사운드 모드에서 주변 소리를 받기 위해 |
| 화면 녹화 동의 | 다른 앱이 재생하는 소리를 받기 위해 (화면 영상은 받지 않음) |
| 다른 앱 위에 표시 | 소리를 다른 앱 화면 위에 그리기 위해 |
| 알림 | 실행 중임을 알리고, 잠금 화면에서 위협음을 알리기 위해 |
| 진동 | 소리를 진동으로 알리고(처음에는 위협음만 켜져 있음), 시각화가 꺼졌을 때 알리기 위해 |
| 화면 켜짐 유지 | 잠금 화면 위협음 알림 때 화면을 잠깐 켜기 위해 |
| 포그라운드 서비스 | 다른 앱을 쓰는 동안에도 소리를 계속 받기 위해 |

## 5. 문제 제보

- 도움말 탭의 제보 버튼을 누르면, 사용자가 고른 GitHub 이슈 화면(브라우저)이나 메일 앱이 열립니다.
- 앱 버전, Android 버전, 기기 모델, 앱 언어가 미리 채워집니다.
- 보낼지는 사용자가 정합니다. 보낸 내용은 GitHub 또는 메일 서비스의 정책을 따르며, GitHub 이슈는 누구나 볼 수 있습니다.

## 6. 방침의 변경

이 방침이 바뀌면 이 페이지에 알리고 시행일을 고칩니다.

## 7. 문의

- 팀 SOVIS
- sovis.kr@gmail.com

웹에서 보기: https://sovis-team.github.io/privacy/

---

# SoundVisualizer Privacy Policy

Effective date: November 24, 2026

SoundVisualizer (the "app") is an Android app made by team SOVIS. This policy explains what information the app handles.

## 1. Information we collect

The app **does not collect any personal information.**

- The app has no internet permission, so it cannot send any information to us or anyone else.
- There are no accounts, no ads and no analytics.

## 2. How the app handles sound

- The app uses sound only to draw it at the edges of the screen and to vibrate.
- Sound is processed in the device's memory only. It is never recorded or saved to a file.
- The AI that tells the kinds of sound apart also runs only on the device.
- To capture sound from other apps, Android requires screen recording consent. The app uses this consent to capture sound only and does not capture the screen image.

## 3. Information stored on the device

- **Settings:** the app stores its settings, such as mode, colors, size and vibration, on the device. Along with the settings, it also stores a value that tells devices apart and the time the app was installed. These two values are used only to tell whether settings were restored from a backup. If Android device backup is on, the settings and these two values may be included in your Google account backup. Google manages this backup, and we cannot see it.
- **Developer log:** only if you turn on logging in developer mode in Settings, the app saves the AI's results (sound type names, scores and times) as CSV files in its own app folder. When a new log starts, the oldest files are deleted first so that all the files together stay under about 200 MB. It does not save the sound itself, and the files are deleted when the app is uninstalled.

## 4. Permissions and why they are used

| Permission | Used for |
| --- | --- |
| Microphone | Capturing sound played on the phone, and sound around you in external sound mode |
| Screen recording consent | Capturing sound played by other apps (the screen image is not captured) |
| Display over other apps | Drawing sound over other apps |
| Notifications | Showing that the app is running, and alerting danger sounds on the lock screen |
| Vibration | Vibrating for sounds (at first, only danger sounds are on), and alerting you when the visualizer turns off |
| Wake lock | Briefly turning on the screen for a lock-screen danger alert |
| Foreground service | Continuing to capture sound while you use other apps |

## 5. Reporting a problem

- When you tap the report button in the Help tab, a GitHub issue page (in your browser) or your email app opens, as you choose.
- The app version, Android version, device model and app language are filled in.
- You decide whether to send it. What you send is covered by GitHub's or your email provider's policies, and GitHub issues are public.

## 6. Changes to this policy

If this policy changes, we will update this page and its effective date.

## 7. Contact

- Team SOVIS
- sovis.kr@gmail.com

On the web: https://sovis-team.github.io/en/privacy/
