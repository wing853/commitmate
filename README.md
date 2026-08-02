# CommitMate

CommitMate는 Spring Boot API 서버와 Flutter 모바일 앱으로 구성됩니다.

## 프로젝트 구조

```text
commitmate/
├─ backend/  # Spring Boot 3 / Java 21
└─ mobile/   # Flutter Android / iOS 앱
```

## 백엔드 실행

저장소 루트에서:

```powershell
.\run-backend.ps1
```

또는 백엔드 디렉터리에서:

```powershell
cd backend
.\gradlew.bat bootRun
```

환경 변수는 `backend/.env`에서 관리합니다. 예시 파일은
`backend/.env.exaple`을 참고하세요.

## 모바일 앱 실행

저장소 루트에서:

```powershell
.\run-mobile.ps1
```

Android 에뮬레이터는 기본적으로 `http://10.0.2.2:8080/api/app`에 연결합니다.
실제 휴대폰에서는 PC와 같은 Wi-Fi에 연결하고 PC의 LAN IP를 지정합니다.

```powershell
$env:COMMITMATE_API_URL='http://192.168.0.10:8080/api/app'
.\run-mobile.ps1
```

백엔드는 기본 8080 포트를 사용하며 `SERVER_PORT` 환경변수로 변경할 수 있습니다.

또는 모바일 디렉터리에서:

```powershell
cd mobile
flutter pub get
flutter run
```

Android Studio에서 `mobile/android`를 별도 Android 프로젝트로 열 필요는
없습니다. 저장소 루트를 열고 Flutter 플러그인이 `mobile/pubspec.yaml`을
인식하도록 하거나, `mobile` 디렉터리를 Flutter 프로젝트로 열면 됩니다.

## Docker 실행

저장소 루트에서 백엔드 디렉터리를 빌드 컨텍스트로 지정합니다.

```powershell
docker build -t commitmate-backend .\backend
docker run --env-file .\backend\.env -p 8080:8080 commitmate-backend
```

Android 실행 및 빌드에는 Android Studio 또는 Android SDK가 필요합니다.
iOS 빌드는 macOS와 Xcode에서 진행해야 합니다.

## 검사

```powershell
cd mobile
flutter analyze
flutter test
```
