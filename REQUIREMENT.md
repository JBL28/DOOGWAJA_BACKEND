# 두과자(DOGWAJA) 백엔드 설계 문서

> 목적: 이 문서는 **Spring Boot + MySQL + JPA** 기반 백엔드 구현을 위한 단일 기준 문서입니다.  
> 목표: 백엔드 에이전트가 임의 해석 없이 구현할 수 있도록, **도메인 규칙 / API 계약 / DB 모델 / 예외 처리 / 보안 / 유지보수 구조**를 명확히 고정합니다.

---

## 0. 문서 우선순위

백엔드 구현 시 문서 우선순위는 아래와 같습니다.

1. 이 문서
2. `PROJECT_STRUCTURE.md`
3. 개별 API 문서 (`auth.md`, `user.md`, `recommendations.md`, `recommendation-comments.md`, `bought-snacks.md`, `bought-snack-comments.md`, `bought-snack-feedback.md`, `bought-snack-status.md`)
4. `types/api.ts`
5. `requirements.md`

### 우선순위 규칙
- 상위 문서와 하위 문서가 충돌하면 **상위 문서를 기준**으로 구현한다.
- 임의로 API를 추가하거나 응답 구조를 변경하지 않는다.
- 프론트와 맞물리는 요청/응답 필드명은 **문서에 적힌 한글/영문 필드명을 그대로 유지**한다.
- 문서 간 충돌이 발견되면 구현자가 임의 판단하지 말고 **충돌 목록을 먼저 보고**해야 한다.

---

## 1. 이번 백엔드 구현에서 확정하는 해석

문서 간 혼선을 없애기 위해 아래 내용을 **최종 확정**한다.

### 1-1. 추천 게시글 피드백 규칙
- **추천 게시글 자체에는 좋아요/싫어요가 없다.**
- 추천 게시글에는 댓글만 달 수 있다.
- 추천 댓글에는 좋아요/싫어요가 가능하다.
- 따라서 `recommendation` 응답에는 `likeCount`, `dislikeCount`, `myFeedback` 필드를 포함하지 않는다.
- 추천 관련 피드백은 모두 `rc_comment` 기준으로 처리한다.

### 1-2. 구매 과자 상태 규칙
- `bought_snack.상태`는 **관리자가 관리하는 공용 상태**다.
- 일반 사용자가 `/bought-snacks/{구매_id}/status` 로 수정하는 값은 **사용자 개인 상태**다.
- 개인 상태는 `myStatus` 로 응답한다.
- 개인 상태는 `bought_snack` 테이블 컬럼으로 직접 관리하지 않고, **별도 저장소(테이블)** 에 저장한다.

### 1-3. 댓글 피드백 규칙
- 구매 과자 댓글과 추천 댓글 응답에는 `likeCount`, `dislikeCount`, `myFeedback` 필드가 존재한다.
- 하지만 현재 공개 API 문서에는 **댓글 피드백 생성/취소 엔드포인트가 정의되어 있지 않다.**
- 따라서 이번 백엔드 범위에서는:
  - 댓글 응답에 위 필드는 유지한다.
  - 실제 값은 초기에는 `0 / 0 / null` 로 고정 가능하다.
  - **댓글 피드백 API는 문서에 추가 승인되기 전까지 구현하지 않는다.**
- 프론트도 댓글 피드백 액션 호출은 하지 않는 것을 전제로 한다.

### 1-4. 관리자 판별 규칙
- 관리자 여부는 JWT 내부 클레임과 DB 사용자 역할을 함께 사용한다.
- DB 사용자 엔티티에는 `role` 필드를 둔다.
- 허용값은 `USER`, `ADMIN` 두 개만 허용한다.
- 로그인/회원가입 응답의 `user` 객체에는 `role` 을 포함하지 않는다.
- 프론트가 관리자 여부를 직접 사용해야 하면 **추가 API 없이 토큰 클레임 해석을 통해 처리하거나 추후 별도 API를 정의**한다.
- 현재 문서 범위에서는 관리자 UI가 MVP 제외이므로, 프론트 공개 영역과의 충돌은 없다.

### 1-5. 메인 페이지 댓글 노출 규칙
- 메인 페이지는 구매 과자 리스트와 추천 리스트를 한 화면에 보여준다.
- 목록 API 자체는 댓글 목록을 포함하지 않는다.
- 댓글 미리보기는 프론트가 각 카드별 댓글 목록 API를 추가 호출해 구성한다.
- 백엔드는 목록 API에 댓글 배열을 임의로 추가하지 않는다.

### 1-6. 댓글 정렬 규칙
- 댓글 목록 기본 정렬은 **생성일 오름차순(오래된 댓글이 위)** 으로 고정한다.
- 정렬 파라미터는 지원하지 않는다.

### 1-7. 페이지네이션 규칙
- 공통 기본값은 `page=1`, `pageSize=20` 이다.
- 다만 프론트 요구사항에 메인 화면 10개 노출 요구가 있어도, **백엔드 기본값은 문서대로 20** 으로 유지한다.
- 프론트가 메인 화면에서 `pageSize=10` 을 명시적으로 요청하는 방식으로 맞춘다.

---

## 2. 기술 스택 및 런타임 기준

## 2-1. 고정 스택
- Framework: Spring Boot
- Language: Java
- Database: MySQL
- ORM: Spring Data JPA
- Authentication: JWT
- Build: Gradle 또는 Maven 중 프로젝트에 이미 정해진 도구 사용

## 2-2. 구현 원칙
- Controller 는 HTTP 입출력과 인증 진입점만 담당한다.
- Service 는 비즈니스 규칙을 담당한다.
- Repository 는 영속성 처리만 담당한다.
- Entity 를 API 응답에 직접 노출하지 않는다.
- 모든 응답은 DTO 로 반환한다.
- 트랜잭션 경계는 Service 에 둔다.

---

## 3. 권장 프로젝트 구조

```text
backend/
├─ src/main/java/com/dogwaja/
│  ├─ global/
│  │  ├─ config/
│  │  ├─ security/
│  │  ├─ error/
│  │  ├─ response/
│  │  ├─ util/
│  │  └─ auth/
│  ├─ domain/
│  │  ├─ user/
│  │  │  ├─ controller/
│  │  │  ├─ service/
│  │  │  ├─ repository/
│  │  │  ├─ entity/
│  │  │  ├─ dto/
│  │  │  └─ mapper/
│  │  ├─ recommendation/
│  │  ├─ recommendationcomment/
│  │  ├─ boughtsnack/
│  │  ├─ boughtsnackcomment/
│  │  ├─ boughtsnackfeedback/
│  │  └─ boughtsnackstatus/
│  └─ DogwajaApplication.java
└─ src/main/resources/
   ├─ application.yml
   └─ db/
      └─ migration/
```

### 구조 원칙
- 도메인별 패키지 분리로 변경 영향 범위를 줄인다.
- 공통 예외, 공통 응답, 보안 필터, JWT 유틸은 `global` 에 둔다.
- 댓글과 게시글, 피드백, 상태를 각각 분리해 유지보수성을 높인다.
- 추천 게시글 피드백 도메인은 **이번 범위에서 만들지 않는다.**

---

## 4. 데이터 모델 설계

## 4-1. 공통 규칙
- PK 는 모두 auto increment bigint 사용 권장
- 모든 테이블은 `created_at`, `updated_at` 포함
- soft delete 는 이번 범위에서 사용하지 않는다.
- 삭제는 실제 delete 로 처리한다.
- `snake_case` 컬럼명 사용
- API 응답으로는 문서의 필드명 규칙을 맞춘 DTO 를 별도 매핑한다.

## 4-2. user

### 테이블
- `user_id` bigint PK
- `nickname` varchar(50) unique not null
- `password_h` varchar(255) not null
- `role` varchar(20) not null
- `created_at` datetime not null
- `updated_at` datetime not null

### 제약
- nickname unique
- role 은 `USER`, `ADMIN` 만 허용

## 4-3. recommendation
- `주문_id` 를 DB 컬럼으로 직접 쓰지 않는다.
- DB 컬럼은 영문 snake_case 로 저장하고 DTO 에서 문서 필드명으로 변환한다.

### 권장 컬럼
- `order_id` bigint PK
- `rc_id` bigint not null
- `user_id` bigint FK not null
- `snack_name` varchar(100) not null
- `reason` varchar(1000) not null
- `created_at` datetime not null
- `updated_at` datetime not null

### 설명
- API DTO 에서는 `주문_id`, `과자이름`, `주문이유`, `사용자Id` 로 변환한다.
- `rc_id` 의 역할이 명확하지 않더라도 문서에 존재하므로 응답에 포함한다.
- `rc_id` 가 별도 도메인 의미가 없으면 `order_id` 와 동일값 사용은 금지한다.
- `rc_id` 생성 전략은 별도 시퀀스 또는 비즈니스 키 정책으로 정한다.
- 간단 구현이 필요하면 `rc_id` 는 `order_id` 생성 직후 동일값으로 저장해도 되지만, **한 번 정하면 일관되게 유지**한다.

## 4-4. rc_comment
- `comment_id` bigint PK
- `order_id` bigint FK not null
- `user_id` bigint FK not null
- `content` varchar(500) not null
- `created_at` datetime not null
- `updated_at` datetime not null

## 4-5. rcc_feedback
- `id` bigint PK
- `comment_id` bigint FK not null
- `order_id` bigint FK not null
- `user_id` bigint FK not null
- `reaction` varchar(20) not null
- `created_at` datetime not null

### 제약
- unique (`user_id`, `comment_id`)
- reaction 은 `LIKE`, `DISLIKE` 만 허용

## 4-6. bought_snack
- `purchase_id` bigint PK
- `snack_name` varchar(100) not null
- `status` varchar(20) not null
- `created_at` datetime not null
- `updated_at` datetime not null

### 제약
- status 는 `배송중`, `재고있음`, `재고없음` 만 허용

## 4-7. bs_comment
- `comment_key` bigint PK
- `purchase_id` bigint FK not null
- `user_id` bigint FK not null
- `content` varchar(500) not null
- `created_at` datetime not null
- `updated_at` datetime not null

## 4-8. bs_feedback
- `id` bigint PK
- `purchase_id` bigint FK not null
- `user_id` bigint FK not null
- `reaction` varchar(20) not null
- `created_at` datetime not null

### 제약
- unique (`user_id`, `purchase_id`)
- reaction 은 `LIKE`, `DISLIKE` 만 허용

## 4-9. bought_snack_user_status

> 현재 공개 ERD 7개 테이블에는 없지만, `myStatus` 와 `/my-status` 를 구현하려면 **반드시 필요한 내부 저장 테이블**이다.  
> 이 테이블은 공개 API 계약을 구현하기 위한 내부 기술 테이블이며, 프론트 계약을 깨지 않는다.

- `id` bigint PK
- `purchase_id` bigint FK not null
- `user_id` bigint FK not null
- `status` varchar(20) not null
- `created_at` datetime not null
- `updated_at` datetime not null

### 제약
- unique (`purchase_id`, `user_id`)
- status 는 `배송중`, `재고있음`, `재고없음` 만 허용

---

## 5. JPA 엔티티 설계 원칙

### 5-1. 연관관계 원칙
- `ManyToOne(fetch = LAZY)` 기본 사용
- `OneToMany` 는 꼭 필요할 때만 사용
- 컬렉션 연관관계는 남용하지 않는다.
- 응답 조합은 조회 전용 쿼리/DTO projection 으로 해결한다.

### 5-2. 엔티티 예시 규칙
- UserEntity
- RecommendationEntity
- RecommendationCommentEntity
- RecommendationCommentFeedbackEntity
- BoughtSnackEntity
- BoughtSnackCommentEntity
- BoughtSnackFeedbackEntity
- BoughtSnackUserStatusEntity

### 5-3. 금지 사항
- EAGER 로 전체 연관관계 로딩 금지
- Entity 를 Controller 에서 직접 반환 금지
- 양방향 연관관계 기본 사용 금지

---

## 6. API 응답 규약

## 6-1. 성공 응답
성공 응답은 아래 중 하나로 통일한다.

### 데이터가 있는 경우
```json
{
  "data": {},
  "message": "요청이 성공했습니다"
}
```

### 데이터가 없는 경우
```json
{
  "message": "삭제되었습니다"
}
```

### 구현 규칙
- 문서 예시에 데이터만 있더라도 실제 구현에서는 `data` 래퍼를 유지한다.
- `message` 는 선택이 아니라 **항상 포함하는 것을 권장**한다.
- 프론트 호환성을 위해 최소한 `data` 구조는 문서와 동일해야 한다.

## 6-2. 실패 응답
```json
{
  "success": false,
  "message": "에러 메시지",
  "errorCode": "ERROR_CODE"
}
```

### 고정 에러 코드
- `AUTH_REQUIRED`
- `INVALID_CREDENTIALS`
- `TOKEN_EXPIRED`
- `ACCESS_DENIED`
- `NOT_FOUND`
- `DUPLICATE_NICKNAME`
- `VALIDATION_ERROR`
- `SERVER_ERROR`

---

## 7. 인증 및 보안 설계

## 7-1. 비밀번호
- 비밀번호는 평문 저장 금지
- BCrypt 해시 저장
- 회원가입/비밀번호 변경 시 BCrypt 로 인코딩

## 7-2. JWT
- Access Token + Refresh Token 구조 사용
- Access Token: 짧은 만료 시간
- Refresh Token: 더 긴 만료 시간
- Access Token 에 최소한 `userId`, `nickname`, `role` 포함
- Refresh Token 저장 방식은 다음 중 하나로 고정
  - DB 저장
  - 서버 메모리/Redis 저장
  - 무상태 방식
- 유지보수성과 로그아웃 무효화를 위해 **DB 또는 Redis 저장 권장**

## 7-3. Spring Security
- `/auth/**` 공개
- 추천/구매 목록/상세 조회는 공개 허용 가능
- 댓글 작성/수정/삭제, 피드백, 개인 상태, 프로필 수정/삭제는 인증 필요
- 관리자 구매 과자 CRUD 는 `ADMIN` 만 허용

## 7-4. 권한 검증 규칙
- 추천 수정/삭제: 게시글 작성자 본인만
- 추천 댓글 수정/삭제: 댓글 작성자 본인만
- 구매 과자 댓글 수정/삭제: 댓글 작성자 본인만
- 구매 과자 생성/수정/삭제: 관리자만
- 구매 과자 개인 상태 변경: 로그인 사용자 본인 기준

---

## 8. 도메인별 비즈니스 규칙

## 8-1. 인증
### 회원가입
- nickname: 2~50자
- password: 8자 이상
- nickname 중복 시 `409 DUPLICATE_NICKNAME`
- 성공 시 accessToken, refreshToken, user 반환

### 로그인
- nickname/password 일치 시 성공
- 불일치 시 `401 INVALID_CREDENTIALS`

### 로그아웃
- refresh token 무효화
- 이미 무효화된 경우에도 성공 응답 가능

### 비밀번호 변경
- currentPassword 일치 확인
- 불일치 시 `401 INVALID_CREDENTIALS`
- newPassword 는 8자 이상

## 8-2. 사용자
### 프로필 조회
- `/users/me`
- 현재 로그인 사용자 반환

### 프로필 수정
- 수정 가능 필드: nickname 만
- nickname 중복 시 `409 DUPLICATE_NICKNAME`

### 회원 탈퇴
- 현재 사용자 삭제
- 연관 데이터 처리 규칙은 아래 중 하나로 고정
  - CASCADE DELETE
  - 삭제 금지
  - 익명 사용자 치환
- 이번 프로젝트는 단순성을 위해 **CASCADE DELETE 금지**, **탈퇴 사용자 작성 데이터도 함께 삭제** 또는 **탈퇴 차단** 중 하나를 정해야 한다.
- 권장안: **탈퇴 시 본인 작성 댓글/게시글/피드백/개인상태를 함께 삭제**

## 8-3. 추천 게시글
### 목록 조회
- 페이지네이션 적용
- 정렬: 최신순(created_at desc)
- 응답 필드:
  - 주문_id
  - rc_id
  - 사용자Id
  - 과자이름
  - 주문이유
  - author
  - commentCount
  - createdAt
  - updatedAt

### 상세 조회
- 목록과 동일 필드 반환

### 생성/수정
- 과자이름: 1~100자
- 주문이유: 최소 10자
- 수정/삭제는 작성자만 가능

## 8-4. 추천 댓글
### 목록 조회
- 기본 정렬: 오래된 순(created_at asc)
- 페이지네이션 적용
- 응답 필드:
  - 댓글_id
  - 주문_id
  - 사용자Id
  - 내용
  - author
  - likeCount
  - dislikeCount
  - myFeedback
  - createdAt
  - updatedAt

### 피드백 계산
- `rcc_feedback` 집계 결과 사용
- 비로그인 사용자는 `myFeedback = null`

## 8-5. 구매 과자
### 목록 조회
- 페이지네이션 적용
- 정렬: 최신순(created_at desc)
- `status` 파라미터가 있으면 공용 상태 기준 필터
- 응답 필드:
  - 구매_id
  - 과자이름
  - 상태
  - commentCount
  - likeCount
  - dislikeCount
  - myFeedback
  - myStatus
  - createdAt
  - updatedAt

### 상세 조회
- 목록과 동일 필드 반환

### 생성/수정/삭제
- 관리자만 가능
- 과자이름: 1~100자
- 상태 기본값: `재고있음`

## 8-6. 구매 과자 피드백
### 대상
- 구매 과자 게시글 자체

### 멱등 규칙
- 이미 LIKE 인데 LIKE 요청: 성공 처리, 변경 없음
- 이미 DISLIKE 인데 DISLIKE 요청: 성공 처리, 변경 없음
- 반대 반응 요청: 기존 반응 삭제 후 새 반응 저장
- 취소 요청인데 반응 없음: 성공 처리, 변경 없음

## 8-7. 구매 과자 개인 상태
### 대상
- 로그인 사용자 본인의 특정 구매 과자에 대한 개인 상태

### 저장 규칙
- `bought_snack_user_status` 에 upsert
- 이미 존재하면 update
- 없으면 insert

### 응답 규칙
- 목록/상세에서 로그인 상태면 `myStatus` 포함
- 비로그인 상태면 `myStatus` 생략 또는 null

---

## 9. API 상세 계약

## 9-1. 인증
- `POST /auth/login`
- `POST /auth/signup`
- `POST /auth/logout`
- `POST /auth/refresh-token`
- `PUT /auth/password`

## 9-2. 사용자
- `GET /users/me`
- `PUT /users/me`
- `DELETE /users/me`

## 9-3. 추천
- `GET /recommendations`
- `GET /recommendations/{주문_id}`
- `POST /recommendations`
- `PUT /recommendations/{주문_id}`
- `DELETE /recommendations/{주문_id}`

## 9-4. 추천 댓글
- `GET /recommendations/{주문_id}/comments`
- `GET /recommendations/{주문_id}/comments/{댓글_id}`
- `POST /recommendations/{주문_id}/comments`
- `PUT /recommendations/{주문_id}/comments/{댓글_id}`
- `DELETE /recommendations/{주문_id}/comments/{댓글_id}`

## 9-5. 구매 과자
- `GET /bought-snacks`
- `GET /bought-snacks/{구매_id}`
- `POST /bought-snacks`
- `PUT /bought-snacks/{구매_id}`
- `DELETE /bought-snacks/{구매_id}`

## 9-6. 구매 과자 댓글
- `GET /bought-snacks/{구매_id}/comments`
- `GET /bought-snacks/{구매_id}/comments/{Key}`
- `POST /bought-snacks/{구매_id}/comments`
- `PUT /bought-snacks/{구매_id}/comments/{Key}`
- `DELETE /bought-snacks/{구매_id}/comments/{Key}`

## 9-7. 구매 과자 피드백
- `POST /bought-snacks/{구매_id}/like`
- `DELETE /bought-snacks/{구매_id}/like`
- `POST /bought-snacks/{구매_id}/dislike`
- `DELETE /bought-snacks/{구매_id}/dislike`
- `GET /bought-snacks/{구매_id}/feedback-stats`

## 9-8. 구매 과자 개인 상태
- `PUT /bought-snacks/{구매_id}/status`
- `GET /bought-snacks/{구매_id}/my-status`

---

## 10. DTO 설계 원칙

## 10-1. 요청 DTO
- Controller 에서 RequestBody 검증
- Bean Validation 사용
- 예시:
  - `@NotBlank`
  - `@Size`
  - `@Pattern`

## 10-2. 응답 DTO
- Entity 그대로 반환 금지
- API 문서 필드명과 정확히 일치해야 함
- 날짜는 ISO 8601 문자열

## 10-3. DTO 명명 예시
- `LoginRequestDto`
- `LoginResponseDto`
- `RecommendationResponseDto`
- `RecommendationCommentResponseDto`
- `BoughtSnackResponseDto`
- `BoughtSnackStatusUpdateRequestDto`

---

## 11. Repository / Query 전략

## 11-1. 기본 전략
- 단건 조회: JPA Repository
- 목록 조회 + 카운트/집계 포함: QueryDSL 또는 JPQL DTO projection 권장
- 단순 count 는 별도 repository method 가능

## 11-2. N+1 방지
- 목록 응답에서 author, commentCount, likeCount, dislikeCount, myFeedback, myStatus 를 함께 구성해야 하므로 조회 전략을 분리한다.
- 게시글 목록은 다음 방식 권장:
  1. 페이지 대상 ID 목록 조회
  2. 대상 ID 기준 상세/작성자 조회
  3. 대상 ID 기준 commentCount 집계
  4. 대상 ID 기준 feedback 집계
  5. 로그인 시 대상 ID 기준 myFeedback/myStatus 조회
  6. 서비스에서 DTO 조합

### 금지
- 게시글별로 댓글 수를 반복 count 하는 방식 금지
- 게시글별로 피드백 수를 반복 count 하는 방식 금지

---

## 12. 트랜잭션 규칙

- 읽기 전용 조회: `@Transactional(readOnly = true)`
- 생성/수정/삭제/상태 변경/피드백 변경: `@Transactional`
- 피드백 변경은 조회 + 삭제 + 삽입이 한 트랜잭션 안에서 끝나야 한다.
- 개인 상태 upsert 도 한 트랜잭션에서 처리한다.

---

## 13. 예외 처리 설계

## 13-1. 전역 예외 처리
- `@RestControllerAdvice` 사용
- Validation 예외, 인증 예외, 권한 예외, 도메인 예외를 공통 처리

## 13-2. 커스텀 예외 계층
- `BusinessException`
- `NotFoundException`
- `AccessDeniedException`
- `ValidationException`
- `AuthenticationException`

## 13-3. 에러 응답 변환
- 모든 예외는 `success=false, message, errorCode` 로 변환
- 스택 트레이스는 운영 환경 응답에 노출 금지

---

## 14. 프론트 연계 안전성 체크포인트

### 반드시 지켜야 할 것
- 응답 필드명은 문서와 정확히 일치
- `pagination` 구조 유지
- `myFeedback`, `myStatus` 는 로그인 시에만 의미 있는 값 반환
- 비로그인 사용자가 조회해도 공개 조회 API는 정상 동작
- 401/403/404/409 코드와 `errorCode` 를 문서대로 반환
- `request.ts` 에 정의된 경로 외의 엔드포인트를 임의 생성 금지

### 프론트와 충돌 나기 쉬운 지점
- 추천 게시글에 피드백 필드 추가 금지
- 댓글 정렬을 최신순으로 바꾸지 말 것
- `nickname` 을 `name` 으로 바꾸지 말 것
- `구매_id`, `주문_id`, `댓글_id`, `Key` 등의 응답 필드명을 영어로 바꾸지 말 것

---

## 15. 유지보수 관점 권장 사항

- 도메인별 서비스 분리
- DTO/Entity 분리
- 전역 예외/응답 표준화
- Query 로직을 서비스에 흩뿌리지 말고 조회 전용 컴포넌트로 분리 가능
- 상태값/반응값은 enum 으로 관리
- 마이그레이션 도구(Flyway 또는 Liquibase) 사용 권장
- 통합 테스트로 API 계약을 고정

### 최소 테스트 범위
- 회원가입/로그인/토큰 갱신
- 추천 게시글 CRUD 권한
- 댓글 CRUD 권한
- 구매 과자 관리자 CRUD 권한
- 구매 과자 피드백 멱등성
- 구매 과자 개인 상태 upsert
- 페이지네이션 기본값 및 경계값
- 에러 코드 정확성

---

## 16. 구현 금지 목록

- 문서에 없는 추천 게시글 좋아요/싫어요 API 추가 금지
- 댓글 피드백 생성/취소 API 임의 추가 금지
- Entity 직접 직렬화 금지
- 무분별한 양방향 연관관계 금지
- API 응답 필드명 임의 영문화 금지
- soft delete 임의 도입 금지
- refresh token 무효화 없는 로그아웃 구현 금지

---

## 17. 백엔드 에이전트 작업 순서

1. 공통 인프라 구성
   - 예외 처리
   - JWT/Security
   - 공통 응답
2. 사용자/인증 도메인 구현
3. 추천 게시글 구현
4. 추천 댓글 구현
5. 구매 과자 구현
6. 구매 과자 피드백 구현
7. 구매 과자 개인 상태 구현
8. 통합 테스트 작성
9. API 응답 필드 검증

---

## 18. 최종 결론

이 문서 기준으로 구현하면 아래 목표를 충족해야 한다.

- 백엔드 에이전트가 임의 해석할 여지를 최소화한다.
- 프론트 요청/응답 구조와 충돌하지 않는다.
- 보안/권한/예외 처리의 기본 안전성을 확보한다.
- Spring Boot + MySQL + JPA 구조에서 유지보수 가능한 레이어드 아키텍처를 유지한다.

이 문서에 없는 기능은 구현하지 않는다.

