# Todo REST API

Spring Boot 4.x + Spring Data JPA + MySQL + Gradle + Java 25로 구현한 할 일(ToDo) REST API입니다.

> 과제의 핵심 요구사항인 `Controller · Service · Repository` 계층 분리, JPA Entity와 DTO 분리, CRUD, 완료/미완료 변경, 입력 검증, 400/404 공통 오류 응답을 모두 구현했습니다.

---

## 1. 기술 스택

| 항목 | 사용 기술 |
|---|---|
| Language | Java 25 |
| Framework | Spring Boot 4.1.1 |
| Persistence | Spring Data JPA / Hibernate |
| Database | MySQL 8.4 |
| Build | Gradle |
| Configuration | YAML |
| Test | JUnit 5 + MockMvc + H2(Test Profile) |
| Container | Docker Compose (선택 실행 방식) |

---

## 2. 가장 빠른 실행 방법

Docker가 설치되어 있다면 아래 **명령 하나**로 MySQL과 API 서버를 함께 실행할 수 있습니다.

```bash
docker compose up --build
```

실행 후:

- API 서버: `http://localhost:8080`
- API 기본 경로: `http://localhost:8080/api/todos`
- MySQL: `localhost:3306`
- MySQL Database: `todo_api`
- MySQL Username: `root`
- MySQL Password: `root`

종료:

```bash
docker compose down
```

데이터까지 삭제:

```bash
docker compose down -v
```

---

## 3. 로컬 MySQL + IntelliJ 실행

Docker를 사용하지 않고 로컬 MySQL을 사용하려면 먼저 다음 DB를 생성합니다.

```sql
CREATE DATABASE todo_api
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```

기본 설정은 다음과 같습니다.

```text
DB_URL=jdbc:mysql://localhost:3306/todo_api?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
DB_USERNAME=root
DB_PASSWORD=root
SERVER_PORT=8080
```

비밀번호가 다르면 환경변수로 덮어씁니다.

Windows PowerShell:

```powershell
$env:DB_PASSWORD="사용할_MySQL_비밀번호"
```

그 다음 Gradle 프로젝트로 IntelliJ에서 열고 `TodoApiApplication`을 실행하면 됩니다.

Gradle 명령을 직접 사용할 수 있는 환경이라면:

```bash
gradle bootRun
```

Windows에서는:

```powershell
gradlew.bat bootRun
```

> 저장소에는 Java 25 Toolchain 설정이 들어 있습니다. IntelliJ가 Java 25 JDK를 사용하도록 설정하세요.

---

## 4. 프로젝트 구조

```text
src
├── main
│   ├── java/com/example/todoapi
│   │   ├── TodoApiApplication.java
│   │   │
│   │   ├── common
│   │   │   ├── exception
│   │   │   │   ├── BusinessException.java
│   │   │   │   ├── ErrorCode.java
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   └── response
│   │   │       └── ApiErrorResponse.java
│   │   │
│   │   └── todo
│   │       ├── controller
│   │       │   └── TodoController.java
│   │       ├── dto
│   │       │   ├── TodoCreateRequest.java
│   │       │   ├── TodoUpdateRequest.java
│   │       │   ├── TodoResponse.java
│   │       │   └── TodoListResponse.java
│   │       ├── entity
│   │       │   └── Todo.java
│   │       ├── repository
│   │       │   └── TodoRepository.java
│   │       └── service
│   │           └── TodoService.java
│   │
│   └── resources
│       └── application.yaml
│
└── test
    ├── java/com/example/todoapi/todo
    │   └── TodoApiIntegrationTest.java
    └── resources
        └── application-test.yaml
```

---

## 5. 계층별 역할

### Controller

HTTP 요청과 응답을 담당합니다.

- URL 매핑
- HTTP Method 처리
- Request DTO 수신
- Response DTO 반환
- HTTP 상태 코드 결정

비즈니스 로직은 Service에 위임합니다.

### Service

애플리케이션의 비즈니스 로직을 담당합니다.

- 제목 검증
- Todo 조회
- Todo 생성/수정/삭제
- 완료/미완료 변경
- 존재하지 않는 ID 처리

### Repository

DB 접근을 담당합니다.

`JpaRepository<Todo, Long>`을 사용합니다.

### Entity

DB 테이블과 매핑되는 객체입니다.

`Todo` Entity는 Controller의 요청/응답에 직접 사용하지 않습니다.

### DTO

외부 API와 내부 Entity 사이의 계약 역할을 합니다.

- `TodoCreateRequest`: 생성 요청
- `TodoUpdateRequest`: 수정 요청
- `TodoResponse`: 단건 응답
- `TodoListResponse`: 목록 응답
- `ApiErrorResponse`: 오류 응답

---

## 6. API 명세

### 6.1 할 일 생성

`POST /api/todos`

Request Body:

```json
{
  "title": "Spring 공부하기"
}
```

성공: `201 Created`

Response:

```json
{
  "id": 1,
  "title": "Spring 공부하기",
  "completed": false,
  "createdAt": "2026-09-23T15:30:00"
}
```

응답 Header에는 다음 Location이 포함됩니다.

```text
Location: /api/todos/1
```

---

### 6.2 할 일 목록 조회

`GET /api/todos`

기본값:

```text
page=0
size=20
```

예:

```text
GET /api/todos?page=0&size=20
```

완료 여부 필터도 사용할 수 있습니다.

```text
GET /api/todos?completed=true
GET /api/todos?completed=false
```

Response:

```json
{
  "content": [
    {
      "id": 1,
      "title": "Spring 공부하기",
      "completed": false,
      "createdAt": "2026-09-23T15:30:00"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

---

### 6.3 할 일 단건 조회

`GET /api/todos/{id}`

예:

```text
GET /api/todos/1
```

성공: `200 OK`

---

### 6.4 할 일 수정 / 완료 여부 변경

`PATCH /api/todos/{id}`

제목 수정:

```json
{
  "title": "Spring Boot 공부하기"
}
```

완료 처리:

```json
{
  "completed": true
}
```

둘을 한 번에 수정:

```json
{
  "title": "Spring Boot 프로젝트 완료",
  "completed": true
}
```

성공: `200 OK`

별도 완료 API를 만들지 않고 수정 API에서 완료/미완료 상태를 변경하도록 설계했습니다.

---

### 6.5 할 일 삭제

`DELETE /api/todos/{id}`

예:

```text
DELETE /api/todos/1
```

성공: `204 No Content`

---

## 7. 입력 검증

제목은 다음 규칙을 적용합니다.

1. 빈 문자열 금지
2. 공백만 있는 문자열 금지
3. 최대 200자
4. 저장 시 앞뒤 공백 제거

예를 들어 다음 요청은 거절됩니다.

```json
{
  "title": "   "
}
```

또는 201자 이상의 제목도 거절됩니다.

실패 상태 코드는 `400 Bad Request`입니다.

---

## 8. 오류 응답 형식

400, 404 등 클라이언트 오류는 동일한 JSON 구조를 사용합니다.

```json
{
  "timestamp": "2026-09-23T15:30:00",
  "status": 404,
  "code": "TODO_NOT_FOUND",
  "message": "해당 할 일을 찾을 수 없습니다.",
  "path": "/api/todos/999"
}
```

### 400 예시

```json
{
  "timestamp": "2026-09-23T15:30:00",
  "status": 400,
  "code": "INVALID_REQUEST",
  "message": "title: 제목은 비어 있을 수 없습니다.",
  "path": "/api/todos"
}
```

### 404 예시

```json
{
  "timestamp": "2026-09-23T15:30:00",
  "status": 404,
  "code": "TODO_NOT_FOUND",
  "message": "해당 할 일을 찾을 수 없습니다.",
  "path": "/api/todos/999"
}
```

오류 응답은 `GlobalExceptionHandler`에서 한 곳으로 모아 일관된 모양을 유지합니다.

---

## 9. DB 설계

테이블명: `todos`

| 컬럼 | 타입 | 제약 | 설명 |
|---|---|---|---|
| id | BIGINT | PK, AUTO_INCREMENT | 서버가 생성하는 식별자 |
| title | VARCHAR(200) | NOT NULL | 할 일 제목 |
| completed | BOOLEAN | NOT NULL | 완료 여부 |
| created_at | DATETIME | NOT NULL | 생성 시각 |

### 설계 이유

- `id`: 클라이언트가 직접 생성하지 않고 DB가 생성합니다.
- `title`: 과제에서 길이 상한을 직접 정하도록 되어 있어 200자로 정했습니다.
- `completed`: 처음 생성할 때 `false`입니다.
- `created_at`: 할 일이 생성된 시점을 기록합니다.

---

## 10. 테스트

전체 테스트:

```bash
gradle test
```

Windows:

```powershell
gradlew.bat test
```

테스트 파일:

```text
src/test/java/com/example/todoapi/todo/TodoApiIntegrationTest.java
```

테스트 프로파일은 H2를 사용해 테스트마다 DB를 초기화합니다. 운영/실행 DB는 MySQL입니다.

### 필수 요구사항 테스트 목록

| 테스트 | 검증 내용 | 예상 결과 |
|---|---|---|
| `createAndReadTodo` | 생성 → 단건 조회 | POST 201, GET 200 |
| `listAndFilterTodos` | 목록 조회 + 완료 필터 | GET 200 |
| `updateTitleAndCompletion` | 제목/완료 상태 수정 | PATCH 200 |
| `deleteTodo` | 삭제 후 조회 | DELETE 204, 이후 GET 404 |
| `blankTitleReturns400WithUnifiedErrorShape` | 빈 제목/공백 제목 | 400 + 공통 오류 구조 |
| `overlongTitleReturns400` | 201자 제목 | 400 |
| `missingIdReturns404ForReadUpdateDelete` | 없는 ID 조회/수정/삭제 | 모두 404 + 동일 오류 구조 |
| `emptyUpdateReturns400` | 수정 내용 없음 | 400 |

---

## 11. curl로 직접 검증하기

### 1) 생성

```bash
curl -i -X POST http://localhost:8080/api/todos \
  -H "Content-Type: application/json" \
  -d '{"title":"Spring Boot 공부"}'
```

예상:

```text
HTTP/1.1 201 Created
Location: /api/todos/1
```

```json
{
  "id": 1,
  "title": "Spring Boot 공부",
  "completed": false,
  "createdAt": "..."
}
```

### 2) 목록

```bash
curl -i http://localhost:8080/api/todos
```

예상: `200 OK`

### 3) 단건 조회

```bash
curl -i http://localhost:8080/api/todos/1
```

예상: `200 OK`

### 4) 완료 처리

```bash
curl -i -X PATCH http://localhost:8080/api/todos/1 \
  -H "Content-Type: application/json" \
  -d '{"completed":true}'
```

예상: `200 OK` + `completed: true`

### 5) 제목 수정

```bash
curl -i -X PATCH http://localhost:8080/api/todos/1 \
  -H "Content-Type: application/json" \
  -d '{"title":"수정된 할 일"}'
```

예상: `200 OK`

### 6) 삭제

```bash
curl -i -X DELETE http://localhost:8080/api/todos/1
```

예상: `204 No Content`

### 7) 없는 ID 조회

```bash
curl -i http://localhost:8080/api/todos/999999
```

예상: `404 Not Found`

```json
{
  "timestamp": "...",
  "status": 404,
  "code": "TODO_NOT_FOUND",
  "message": "해당 할 일을 찾을 수 없습니다.",
  "path": "/api/todos/999999"
}
```

### 8) 잘못된 제목

```bash
curl -i -X POST http://localhost:8080/api/todos \
  -H "Content-Type: application/json" \
  -d '{"title":"   "}'
```

예상: `400 Bad Request`

---

## 12. 요구사항 체크리스트

- [x] Spring Boot 4.x
- [x] Spring Data JPA
- [x] Java 25
- [x] MySQL
- [x] 회원가입/로그인 미구현
- [x] 할 일 생성
- [x] 할 일 목록 조회
- [x] 할 일 단건 조회
- [x] 할 일 수정
- [x] 할 일 삭제
- [x] 완료/미완료 변경
- [x] 빈 제목 거절
- [x] 공백만 있는 제목 거절
- [x] 제목 길이 상한 설정(200자)
- [x] 잘못된 입력은 400
- [x] 없는 ID 조회/수정/삭제는 404
- [x] 500 응답이 발생하도록 예외를 무분별하게 삼키지 않음
- [x] Controller · Service · Repository 계층 분리
- [x] 요청 DTO와 응답 DTO 사용
- [x] JPA Entity를 Controller API에 직접 노출하지 않음
- [x] 오류 응답 공통 구조
- [x] README에 실행 방법 작성
- [x] README에 엔드포인트별 API 명세 작성
- [x] README에 설계 이유 작성
- [x] README에 정상/400/404 호출 예시 작성
- [x] 목록 페이지 나누기
- [x] 완료 여부 목록 필터
- [x] MockMvc 통합 테스트
- [ ] OpenAPI(Swagger) 문서 — 선택 사항이므로 필수 구현에서 제외

---

## 13. 과제 제출 전 확인

```text
1. MySQL 접속 정보 확인
2. 서버 실행
3. POST 생성
4. GET 목록
5. GET 단건
6. PATCH 완료 처리
7. PATCH 제목 수정
8. DELETE 삭제
9. 빈 제목 400
10. 201자 제목 400
11. 없는 ID GET/PATCH/DELETE 모두 404
12. 테스트 실행
13. build/, .gradle/ 등 빌드 산출물 제거 후 제출
```

회원가입/로그인은 요구사항에 없으므로 구현하지 않았습니다.