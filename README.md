# Create: Furnace Engine

Minecraft 1.21.1용 Create 애드온입니다. Create 구버전의 Furnace Engine을 Create 6.0.1 환경에 맞춰 복원합니다.

증기 보일러 대신 작동 중인 화로를 열원으로 사용하며, Create Steam Engine 방식의 헤드·크랭크 애니메이션과 Shaft 출력을 제공합니다.

## 요구 사항

- Minecraft 1.21.1
- NeoForge 21.1.248 이상
- Create 6.0.1

## 설치

1. Minecraft 1.21.1용 NeoForge를 설치합니다.
2. Create 6.0.1과 이 모드의 JAR 파일을 `mods` 폴더에 넣습니다.
3. 게임을 실행합니다.

## 사용 방법

1. 화로, 용광로 또는 훈연기 옆면이나 윗면에 Furnace Engine을 설치합니다.
2. 엔진에서 바깥쪽으로 두 블록 떨어진 위치에 Create Shaft를 설치합니다.
3. Shaft를 든 채 엔진을 바라보면 Steam Engine과 같은 설치 가이드가 표시됩니다.
4. 화로가 연료를 소비하며 작동하면 엔진이 동력을 생산합니다.

```text
[Furnace] [Furnace Engine] [빈 공간] [Shaft]
```

### 설치 제한

- 화로 하나에는 Furnace Engine을 하나만 연결할 수 있습니다.
- 화로의 전면에는 엔진을 설치할 수 없습니다.
- Shaft의 축은 엔진 진행 방향과 나란할 수 없습니다.

## 기능

- Create Steam Engine 기반의 Head·Piston·Linkage·Crank 애니메이션
- Shaft 설치 가이드
- 고글과 마우스 휠을 이용한 회전 방향 설정
- 화로 작동 상태에 따른 자동 시동 및 정지
- 점진적인 RPM 워밍업
- 설정 가능한 히트싱크 블록 및 출력
- Industrial Iron 스타일의 독립적인 엔진 Head 텍스처

## 워밍업

화로가 작동하기 시작하면 엔진은 1 RPM으로 시동합니다. 이후 5틱마다 1 RPM씩 증가하며 설정된 목표 RPM에 도달하면 가속을 멈춥니다.

화로가 꺼지면 엔진도 정지하고 워밍업 상태가 초기화됩니다. 다음 시동에서는 다시 1 RPM부터 시작합니다.

## 히트싱크

화로에 직접 맞닿은 6개 위치 중 하나 이상에 설정된 히트싱크 블록이 있으면 히트싱크용 출력 설정이 적용됩니다.

기본 히트싱크는 `minecraft:copper_block`입니다.

## 설정

최초 실행 후 `config/createfurnaceengine-common.toml` 파일이 생성됩니다.

| 설정 | 기본값 | 설명 |
| --- | ---: | --- |
| `baseRpm` | `40` | 히트싱크가 없을 때 목표 RPM |
| `baseSuPerRpm` | `32` | 히트싱크가 없을 때 RPM당 응력 용량 |
| `heatSinkBlock` | `minecraft:copper_block` | 히트싱크로 인식할 블록 ID |
| `heatSinkRpm` | `32` | 히트싱크가 있을 때 목표 RPM |
| `heatSinkSuPerRpm` | `32` | 히트싱크가 있을 때 RPM당 응력 용량 |

총 응력 용량은 `현재 RPM × SU/RPM`으로 계산됩니다. 기본 설정에서 워밍업이 끝난 일반 출력은 1,280 SU이고, 히트싱크 출력은 1,024 SU입니다.

## 조합법

Furnace Engine은 Create의 Mechanical Crafter로 제작합니다.

```text
황동 판  황동 판    황동 주괴
황동 판  황동 케이싱  피스톤
황동 판  황동 판    황동 주괴
```

조합법은 좌우 반전을 지원합니다.

## 개발 및 빌드

Windows:

```powershell
.\gradlew.bat build
```

Linux/macOS:

```bash
./gradlew build
```

빌드된 JAR은 `build/libs/`에 생성됩니다.

## 자산 출처

Furnace Engine의 Head 및 Crank 모델과 텍스처는 Create 6.0.1 자산을 기반으로 이 모드의 네임스페이스에 맞게 수정되었습니다. 자세한 내용은 [THIRD_PARTY_ASSETS.md](THIRD_PARTY_ASSETS.md)를 참고하세요.

## 라이선스

이 프로젝트는 MIT License로 배포됩니다. 포함된 Create 파생 자산에도 원본 Create 프로젝트의 MIT License가 적용됩니다.
