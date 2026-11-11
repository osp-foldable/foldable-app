# foldable-app

갤럭시 폴드 전용 앱 모음 (오상플레이). 앱마다 패키지명·버전·스토어 등록은 따로이고, 접힘 감지 같은 공용 코드만 함께 씁니다.

| 폴더 | 내용 | 패키지 |
|---|---|---|
| `core/fold` | 접힘/펼침 감지, 진동, 공용 색 | `com.ohsang.play.fold.core` |
| `apps/pick` | 펼쳐 뽑기 - 랜덤 숫자 — 접었다 펴면 숫자 공개 | `com.ohsang.play.foldpick` |

## 빌드 (GitHub Actions)
- **main에 푸시 / PR** → 모든 앱 디버그 APK 빌드 (테스트 광고). Actions 실행 화면 하단 Artifacts에서 받아 폰에 설치
- **태그 `<앱>-v<버전>`** (예: `pick-v1.0.0`) → 그 앱만 서명된 출시 빌드.
  AAB(스토어 업로드용)와 APK가 Artifacts와 GitHub Release에 올라감
- versionCode는 Actions 실행 번호로 자동 증가

```bash
git tag pick-v1.0.0
git push origin pick-v1.0.0
```

## 출시 서명 키 등록 (앱마다 한 번)
1. 키 만들기
   ```bash
   keytool -genkeypair -v -keystore pick.jks -alias pick \
     -keyalg RSA -keysize 2048 -validity 10000
   ```
2. base64로 바꾸기: `base64 -w0 pick.jks` (macOS는 `base64 -i pick.jks`)
3. 저장소 Settings → Secrets and variables → Actions 에 추가 (앱 이름 대문자 접두사)

   | Secret | 값 |
   |---|---|
   | `PICK_KEYSTORE_BASE64` | 2번 결과 |
   | `PICK_KEYSTORE_PASSWORD` | keystore 비밀번호 |
   | `PICK_KEY_ALIAS` | `pick` |
   | `PICK_KEY_PASSWORD` | 키 비밀번호 |
   | `PICK_ADMOB_APP_ID` | AdMob 앱 ID (`ca-app-pub-…~…`) |
   | `PICK_ADMOB_BANNER_ID` | AdMob 배너 광고 단위 ID (`ca-app-pub-…/…`) |

   광고 ID 두 개가 없으면 출시 빌드도 테스트 광고로 만들어지니 꼭 넣기. 디버그 빌드는 항상 테스트 광고.

4. **`.jks` 파일과 비밀번호는 저장소에 올리지 말고 따로 안전하게 보관** (잃어버리면 업데이트 불가 — Play 앱 서명을 쓰면 업로드 키는 재설정 가능)

## 새 앱 추가하기
1. `apps/<이름>` 폴더 만들기 (`apps/pick`을 참고, 패키지 `com.ohsang.play.fold<이름>`)
2. `settings.gradle.kts`에 `include(":apps:<이름>")`
3. `.github/workflows/build.yml`의 `matrix.app`에 이름 추가
4. Secrets에 `<이름 대문자>_KEYSTORE_BASE64` 등 서명 4개 + AdMob 2개 추가

## 로컬에서 열기
Android Studio에서 저장소 폴더를 Open. Gradle 래퍼가 없으니 처음 동기화 때 Studio 안내대로 만들거나 `gradle wrapper` 한 번 실행.

에뮬레이터: Device Manager → **7.6" Fold-in with outer display** 로 접기/펴기 테스트
