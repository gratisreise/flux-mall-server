# Git Convention

이 문서는 팀 프로젝트에서 **일관된 Git 사용 규칙**을 유지하기 위한 가이드입니다.
커밋 히스토리를 명확하게 하고, 협업과 유지보수를 쉽게 만드는 것을 목표로 합니다.

---

## 1. 기본 원칙

* **한 커밋 = 하나의 목적**
* 커밋 메시지는 **왜(What/Why)** 를 중심으로 작성
* 커밋 단위는 **되돌리기 쉬울 정도로 작게** 유지
* 커밋 메시지는 항상 **명령문 형태**로 작성

---

## 2. 브랜치 전략

### 2.1 기본 브랜치

* `main`

    * 항상 배포 가능한 상태 유지
* `develop`

    * 다음 배포를 준비하는 통합 브랜치

### 2.2 작업 브랜치

작업 브랜치는 반드시 `develop` 브랜치에서 분기합니다.

형식:

```
<type>(<description>)
```

예시:

* `feature(login-api)`
* `fix(order-validation)`
* `refactor(user-domain)`

### type 종류

* `feature` : 새로운 기능 개발
* `fix` : 버그 수정
* `hotfix` : 운영/배포 환경에서 발생한 긴급 버그 수정
* `refactor` : 리팩토링 (기능 변경 없음)
* `perf` : 성능 개선
* `style` : 코드 포맷팅 (로직 변경 없음)
* `docs` : 문서 작업
* `test` : 테스트 코드 추가/수정
* `build` : 빌드 시스템 또는 의존성 변경
* `ci` : CI 설정 변경
* `chore` : 설정, 기타 작업
* `deps` : 의존성 변경
* `config` : 애플리케이션 설정 변경
* `security` : 보안 취약점 수정
* `migration` : DB 마이그레이션

---

## 3. 커밋 메시지 규칙

### 3.1 커밋 메시지 구조

```
<type>: <subject>

<body>

<footer>
```

---

### 3.2 Type

커밋 Type은 변경의 성격을 나타내는 식별자


---

### 3.3 Subject (제목)

* 50자 이내
* 소문자로 시작
* 마침표(`.`) 사용 금지
* 명령문 형태 사용

✅ 좋은 예

```
feat: add login api
fix: prevent null pointer in order service
```

❌ 나쁜 예

```
feat: 로그인 api 추가함
fix: 버그 수정
```

---

### 3.4 Body (본문)

* 선택 사항이지만 **의미 있는 변경에는 작성 권장**
* 변경 이유와 배경 설명
* 어떻게 변경했는지보다 **왜 변경했는지** 중심

예시:

```
로그인 시 중복 요청으로 토큰이 여러 번 발급되는 문제를 해결하기 위함.
Redis 기반 락을 추가하여 동시 요청을 제어함.
```

---

### 3.5 Footer (선택)

* 이슈 트래킹 연동 시 사용

예시:

```
Closes #123
Refs #45
```

---

## 4. 커밋 예시

```
feat: add user signup api

회원 가입 시 이메일 중복 검증이 누락되어 있어
도메인 레벨에서 중복 체크 로직을 추가함.

Closes #21
```

---

## 5. Merge 규칙

* `main`, `develop` 브랜치에는 **직접 커밋 금지**
* 반드시 Pull Request(PR) 사용
* PR은 최소 1명 이상의 리뷰 승인 필요

### PR 제목 규칙

```
[type] 간단한 변경 요약
```

예시:

* `[feat] 회원 가입 기능 추가`
* `[fix] 주문 금액 계산 오류 수정`

---

## 6. Rebase & Merge 전략

* 개인 작업 브랜치 → `develop`

    * **Squash Merge 권장**
* `develop` → `main`

    * **Merge Commit 사용** (히스토리 보존)

---

## 7. 태그(Tag) 규칙

릴리즈 시 태그를 사용합니다.

형식:

```
v<MAJOR>.<MINOR>.<PATCH>
```

예시:

* `v1.0.0`
* `v1.1.2`

---

## 8. 금지 사항

* 의미 없는 커밋 메시지 (`update`, `fix bug` 등)
* 하나의 커밋에 여러 목적을 섞는 행위
* 포맷 수정 + 로직 수정 동시 커밋

---

## 9. 참고

* Conventional Commits: [https://www.conventionalcommits.org/](https://www.conventionalcommits.org/)
* Git Flow

---

> 이 컨벤션은 팀 상황에 맞게 언제든지 개선 및 수정될 수 있습니다.
