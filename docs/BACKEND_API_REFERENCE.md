# AddisBuilders University Learning & Internship Platform — Backend API Reference

> **Audience:** frontend engineers and AI code generators.
> **Status:** verified against the running Spring Boot application and generated OpenAPI document.
> **Project:** Project 4 — University Learning & Internship Platform.

This document is the single source of truth for building the frontend client. It describes every externally available route, the exact request/response shapes, role rules, validation rules, error format, and the sequences the UI should follow.

---

## 1. Stack and runtime

| Item | Value |
| --- | --- |
| Backend | Spring Boot 4.1.1 / Java 25 |
| Database | MySQL 8+ (tested on MySQL 9.7) |
| Migrations | Flyway (`V1` … `V8`) |
| Auth | Stateless JWT Bearer tokens |
| API docs | springdoc-openapi 3.2.0 |
| Server port | `8080` |
| Context path | `/api` |

**Every external URL starts with `/api`.** The context path is configured in `application.properties`, so controller paths such as `/courses` are reached as `http://localhost:8080/api/courses`.

### Live documentation

| Resource | URL |
| --- | --- |
| Swagger UI | `http://localhost:8080/api/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:8080/api/v3/api-docs` |

Use the **Authorize** button in Swagger UI with the raw JWT (without the `Bearer ` prefix).

### Local startup

```powershell
# from the backend repository root
.\mvnw.cmd spring-boot:run
```

Environment variables:

| Variable | Default | Purpose |
| --- | --- | --- |
| `DB_URL` | `jdbc:mysql://localhost:3306/addisbuilders_db?...` | JDBC URL |
| `DB_USERNAME` | `root` | Database user |
| `DB_PASSWORD` | `root` | Database password |
| `JWT_SECRET` | development secret | JWT signing key (override in production) |
| `JWT_EXPIRATION` | `86400000` | Token lifetime in ms (24 h) |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000,http://localhost:5173,http://127.0.0.1:3000,http://127.0.0.1:5173` | Comma-separated browser origins |
| `JPA_SHOW_SQL` | `false` | SQL logging |

CORS allows methods `GET, POST, PUT, PATCH, DELETE, OPTIONS`, all headers, and credentials.

---

## 2. Authentication and roles

### 2.1 Token flow

1. `POST /api/auth/register` or `POST /api/auth/login`.
2. Read `token` from the flat `AuthResponse`.
3. Store it and send it on every protected request:

```http
Authorization: Bearer <token>
```

4. `GET /api/auth/me` returns the same profile shape without issuing a new token.

There is **no refresh-token endpoint and no logout endpoint**; logout is purely client-side (delete the stored token). Tokens expire after 24 hours by default. A `401` means the token is missing, invalid, expired, or the account is not `ACTIVE`.

### 2.2 Roles

| Role | Typical capability |
| --- | --- |
| `LEARNER` | Browse/publish catalogue, enroll, learn, submit assignments, save/apply to internships, view progress/dashboard/leaderboard/achievements/AI |
| `MENTOR` | Create and manage own courses, curriculum, materials, assignments; grade submissions; view mentor dashboard. **Must be `mentorStatus = APPROVED` for privileged actions.** |
| `INTERNSHIP_COORDINATOR` | Manage internship listings after **`coordinatorStatus = APPROVED`**; view/manage applications |
| `EMPLOYER` | Own organization profile, create/manage internship listings after organization verification is `APPROVED`, review applications |
| `SUPER_ADMIN` | Global administration: users, categories, organizations, approvals, audit logs, reports, all courses/internships |

Registration restrictions:

- `SUPER_ADMIN` cannot self-register.
- `MENTOR` registration requires `university`, `department`, `employeeId`, and an **HTTPS** `proofDocumentUrl`. New mentors start as `mentorStatus = PENDING` and cannot create courses until approved.
- `INTERNSHIP_COORDINATOR` registration starts as `coordinatorStatus = PENDING`.
- `EMPLOYER` registration automatically creates an organization with `verificationStatus = PENDING`.

### 2.3 Approval gates

| Action | Required state |
| --- | --- |
| Mentor creates/updates courses, modules, lessons, materials, assignments | `MENTOR` + `mentorStatus = APPROVED`, or `SUPER_ADMIN` |
| Mentor grades submissions / views mentor submissions | `MENTOR` + `mentorStatus = APPROVED`, or `SUPER_ADMIN` |
| Coordinator creates/updates/closes internship listings | `INTERNSHIP_COORDINATOR` + `coordinatorStatus = APPROVED` |
| Employer publishes a listing | Organization `verificationStatus = APPROVED` |
| Learner sees a published internship | Listing `status = PUBLISHED`, organization `APPROVED`, deadline in the future |

---

## 3. Conventions

### 3.1 Content type and encoding

- Requests and responses are `application/json; charset=UTF-8`, except multipart uploads.
- Enum values are uppercase strings with underscores, e.g. `"INTERNSHIP_COORDINATOR"`.
- Java `Long` IDs serialize as JSON **numbers**. Do not assume strings; normalize explicitly if the UI model uses strings.
- `LocalDateTime` serializes as ISO-8601 **without timezone**, e.g. `"2026-12-31T23:59:59"`.
- `BigDecimal` serializes as a JSON number, e.g. `85.50`.
- `boolean` fields whose Java record component starts with `is` serialize as `isRead`, `isActive`, `isSaved`, `hasApplied`.

### 3.2 Success status codes

| Code | Meaning |
| --- | --- |
| `200 OK` | Read or update succeeded |
| `201 Created` | Resource created (`register`, create course, material, module, lesson, assignment, internship, upload) |
| `204 No Content` | Delete, mark-read, read-all, close-with-no-body |

### 3.3 Error contract

**Validation failure (HTTP 400):**

```json
{
  "timestamp": "2026-11-02T22:31:53.818397",
  "status": 400,
  "error": "Validation failed",
  "errors": {
    "email": "Invalid email format",
    "password": "Password must be at least 6 characters long"
  }
}
```

**Business/security/not-found failure (HTTP 400/401/403/404/409/413/500):**

```json
{
  "timestamp": "2026-11-02T22:31:53.818397",
  "status": 409,
  "error": "Conflict",
  "message": "This email address is already registered. Please use another email or sign in."
}
```

| Code | When |
| --- | --- |
| `400` | Bean validation, malformed JSON, unknown enum, or a business rule (e.g. deadline passed, grade above max points, invalid status transition) |
| `401` | Missing/invalid/expired token, bad credentials, inactive account |
| `403` | Authenticated but not allowed (wrong role, not owner, unapproved mentor/coordinator, unpublished resource) |
| `404` | Entity ID does not exist |
| `409` | Duplicate email, duplicate enrollment/application, unique constraint, existing organization name |
| `413` | Upload larger than 10 MB |
| `500` | Unexpected server error |

> The security filter writes the same four-field shape (`timestamp`, `status`, `error`, `message`) for `401`/`403` generated before the controller runs.

---

## 4. Enum catalogue

Use these exact strings in TypeScript unions.

```ts
type UserRole = 'LEARNER' | 'MENTOR' | 'INTERNSHIP_COORDINATOR' | 'EMPLOYER' | 'SUPER_ADMIN';

type UserStatus = 'ACTIVE' | 'INACTIVE' | 'SUSPENDED';
type ApprovalStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'SUSPENDED';
type VerificationStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

type CourseLevel = 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED' | 'ALL_LEVELS';
type CourseStatus = 'DRAFT' | 'PENDING_REVIEW' | 'PUBLISHED' | 'ARCHIVED';

type MaterialType = 'DOCUMENT' | 'VIDEO' | 'LINK' | 'SLIDE' | 'DATASET' | 'OTHER';

type ContentType = 'TEXT' | 'VIDEO_URL' | 'PDF_DOWNLOAD' | 'LINK' | 'VIDEO' | 'ARTICLE' | 'LAB';

type EnrollmentStatus = 'ACTIVE' | 'COMPLETED' | 'DROPPED';
type SubmissionStatus = 'SUBMITTED' | 'UNDER_REVIEW' | 'GRADED' | 'RETURNED';

type InternshipStatus = 'DRAFT' | 'PENDING_REVIEW' | 'PUBLISHED' | 'CLOSED' | 'REJECTED';
type WorkMode = 'ONSITE' | 'HYBRID' | 'REMOTE';

type ApplicationStatus =
  | 'SUBMITTED'
  | 'UNDER_REVIEW'
  | 'SHORTLISTED'
  | 'INTERVIEW_SCHEDULED'
  | 'ACCEPTED'
  | 'DECLINED'
  | 'REJECTED';
```

> **Important for regeneration:** older frontend mock types used `PENDING`/`REVIEWING` for applications and `VERIFIED`/`UNVERIFIED` for organizations. The backend does **not** use those values. Translate at the UI boundary if desired, but send backend values to the API.

---

## 5. Core schemas

### 5.1 `AuthResponse`

Returned by register, login, and `/auth/me`.

| Field | Type | Notes |
| --- | --- | --- |
| `id` | number | User ID |
| `token` | string \| null | JWT; `null` only for `/auth/me` |
| `type` | string | Always `"Bearer"` |
| `email` | string | |
| `fullName` | string | |
| `role` | `UserRole` | |
| `coordinatorStatus` | `ApprovalStatus` \| null | |
| `mentorStatus` | `ApprovalStatus` \| null | |
| `university` | string \| null | |
| `department` | string \| null | |
| `employeeId` | string \| null | |
| `proofDocumentUrl` | string \| null | HTTPS URL for mentors |
| `phone` | string \| null | |
| `bio` | string \| null | |
| `studentId` | string \| null | |
| `cvUrl` | string \| null | |
| `avatarUrl` | string \| null | |
| `organizationId` | number \| null | Employer/coordinator organization |
| `createdAt` | datetime | |
| `isActive` | boolean | Derived from `status === 'ACTIVE'` |

### 5.2 `UserResponse`

Returned by `/users/me`, `/admin/users`.

Fields: `id`, `email`, `fullName`, `role`, `status`, `phone`, `bio`, `university`, `department`, `studentId`, `cvUrl`, `avatarUrl`, `coordinatorStatus`, `mentorStatus`, `employeeId`, `proofDocumentUrl`, `organizationId`, `isActive`, `createdAt`.

### 5.3 `CourseResponse`

| Field | Type | Notes |
| --- | --- | --- |
| `id` | number | |
| `title` | string | |
| `description` | string \| null | |
| `shortDescription` | string \| null | |
| `level` | `CourseLevel` | |
| `status` | `CourseStatus` | |
| `thumbnailUrl` | string \| null | |
| `durationWeeks` | number \| null | |
| `learningObjectives` | string \| null | Free text, usually newline-separated |
| `mentorId` | number | |
| `instructorName` | string \| null | Derived from mentor's `fullName` |
| `categoryId` | number \| null | |
| `categoryName` | string \| null | |
| `materials` | `MaterialResponse[]` | Always present (possibly empty) |
| `createdAt` | datetime | |

### 5.4 `MaterialResponse`

| Field | Type |
| --- | --- |
| `id` | number |
| `courseId` | number |
| `title` | string |
| `description` | string \| null |
| `type` | `MaterialType` |
| `url` | string \| null |
| `order` | number |
| `createdAt` | datetime |

### 5.5 `EnrollmentResponse`

| Field | Type |
| --- | --- |
| `id` | number |
| `studentId` | number |
| `studentName` | string |
| `studentEmail` | string |
| `studentUniversity` | string \| null |
| `studentDepartment` | string \| null |
| `courseId` | number |
| `courseTitle` | string |
| `instructorId` | string (numeric ID serialized as string) |
| `status` | `EnrollmentStatus` |
| `enrolledAt` | datetime |

> Per-course lesson completion is **not** embedded here. Call `GET /api/progress/courses/{courseId}` (or the alias `GET /api/courses/{courseId}/progress`) for the learner's detailed progress.

### 5.6 `ModuleResponses` and `LessonResponse`

`ModuleResponses`: `id`, `courseId`, `title`, `moduleOrder`, `lessons: LessonResponse[]`.

`LessonResponse`: `id`, `moduleId`, `title`, `type: ContentType`, `body`, `contentUrl`, `durationMinutes`, `lessonOrder`.

### 5.7 `AssignmentResponse`

| Field | Type |
| --- | --- |
| `id` | number |
| `courseId` | number |
| `courseTitle` | string |
| `title` | string |
| `description` | string \| null |
| `dueDate` | datetime |
| `maxPoints` | number |
| `attachmentUrl` | string \| null |
| `createdAt` | datetime |

### 5.8 `SubmissionResponse`

| Field | Type |
| --- | --- |
| `id` | number |
| `assignmentId` | number |
| `assignmentTitle` | string |
| `courseId` | number |
| `courseTitle` | string |
| `learnerId` | number |
| `learnerName` | string |
| `learnerEmail` | string |
| `textAnswer` | string \| null |
| `fileUrl` | string \| null |
| `status` | `SubmissionStatus` |
| `grade` | number \| null |
| `feedback` | string \| null |
| `submittedAt` | datetime \| null |
| `gradedAt` | datetime \| null |

### 5.9 `CourseProgressResponse`

`courseId`, `courseTitle`, `totalLessons`, `completedLessons`, `completionPercentage` (0–100, 2 decimals), `completedLessonIds: number[]`.

Lesson-completion response: `{ "progressPercentage": 66.67, "completedCount": 2 }`.

### 5.10 `NotificationResponse`

`id`, `userId`, `title`, `message`, `type` (free-form string; the backend currently emits `INFO`, `SUCCESS`, `WARNING`), `link` (nullable in-app route), `isRead`, `createdAt`.

### 5.11 `InternshipListingResponse`

`id`, `organizationId`, `organizationName`, `title`, `description`, `requirements`, `workMode`, `location`, `category`, `responsibilities`, `eligibility`, `stipend`, `durationMonths`, `deadline`, `status`, `createdAt`, `postedById`, `postedByRole`, `applicantsCount`, `isSaved`, `hasApplied`.

`requirements`, `responsibilities`, and `eligibility` are plain text; newline-delimited lists work well. They are **not** JSON arrays.

### 5.12 `InternshipApplicationResponse`

`id`, `internshipId`, `internshipTitle`, `learnerId`, `learnerName`, `learnerEmail`, `learnerPhone`, `university`, `department`, `coverLetter`, `resumeUri`, `organizationName`, `status`, `appliedAt`, `notes`.

### 5.13 `OrganizationResponse`

`id`, `ownerId`, `name`, `industry`, `description`, `website`, `location`, `contactEmail`, `contactPhone`, `registrationNumber`, `taxId`, `verificationStatus`, `verificationNotes`, `logoUrl`, `verificationDocumentName`, `verificationDocumentUrl`, `createdAt`.

### 5.14 Dashboard schemas

**`LearnerDashboardResponse`**

`learnerId`, `learnerName`, `enrolledCourses`, `activeCourses`, `completedCourses`, `totalLessons`, `completedLessons`, `overallCompletionPercentage`, `pendingAssignments`, `submittedAssignments`, `gradedSubmissions`, `averageGrade`, `savedInternships`, `internshipApplications`, `unreadNotifications`.

**`MentorDashboardResponse`**

`mentorId`, `mentorName`, `totalCourses`, `publishedCourses`, `draftCourses`, `totalStudents`, `activeStudents`, `completedStudents`, `totalAssignments`, `upcomingAssignments`, `pendingSubmissions`, `gradedSubmissions`, `averageGrade`, `unreadNotifications`.

- `upcomingAssignments` = assignments due within the next 7 days.
- `pendingSubmissions` = submissions in `SUBMITTED` or `UNDER_REVIEW`.
- `averageGrade` = mean of non-null submission grades, or `0.0`.

### 5.15 Leaderboard and achievements

`LeaderboardEntryResponse`: `rank`, `learnerId`, `learnerName`, `university`, `department`, `completedLessons`, `gradedSubmissions`, `averageGrade`, `points`.

Points formula: `completedLessons * 10 + gradedSubmissions * 5 + round(averageGrade)`.

`AchievementResponse`: `code`, `title`, `description`, `earned`, `progressHint`.

Achievement codes:

| Code | Earned when |
| --- | --- |
| `FIRST_STEPS` | ≥ 1 completed lesson |
| `COURSE_EXPLORER` | Enrolled in ≥ 3 courses |
| `COURSE_GRADUATE` | ≥ 1 course with enrollment status `COMPLETED` |
| `FIVE_COURSE_SCHOLAR` | ≥ 5 completed courses |
| `SUBMISSION_STARTER` | ≥ 1 assignment submission |
| `HIGH_ACHIEVER` | Average grade ≥ 85 with ≥ 1 graded submission |
| `INTERNSHIP_READY` | ≥ 1 internship application |
| `INTERNSHIP_ACCEPTED` | ≥ 1 application with status `ACCEPTED` |
| `TOP_TEN` | Rank ≤ 10 on the leaderboard |

### 5.16 AI schemas

**`StudyPlanResponse`**

`courseId`, `courseTitle`, `totalLessons`, `remainingLessons`, `estimatedWeeks`, `weeklyMinutes` (currently `300`), `items`.

Each item: `lessonId`, `lessonTitle`, `moduleId`, `moduleTitle`, `durationMinutes`, `week`, `status` (`"NOT_STARTED"`).

**`ProgressSummaryResponse`**

`learnerId`, `summary`, `enrolledCourses`, `completedLessons`, `totalLessons`, `completionPercentage`, `gradedSubmissions`, `averageGrade`, `strengths: string[]`, `recommendations: string[]`.

**`InternshipMatchResponse`**

`internshipId`, `title`, `organizationName`, `category`, `workMode`, `location`, `matchScore` (0–100), `reasons: string[]`.

**`AiAnswerResponse`**

`answer`, `sources: string[]`, `aiGenerated` (currently always `false` because the implementation is deterministic).

---

## 6. Endpoint reference

All paths below are **relative to `/api`**.

Legend: 🔓 public · 🔑 any authenticated user · 🎓 approved mentor/admin · 🧑‍🎓 learner · 🏢 employer · 🎯 approved coordinator · 🛡️ super admin.

### 6.1 Authentication — `/auth`

| Method | Path | Access | Description |
| --- | --- | --- | --- |
| POST | `/auth/register` | 🔓 | Register; returns `AuthResponse` (201) |
| POST | `/auth/login` | 🔓 | Login; returns `AuthResponse` |
| GET | `/auth/me` | 🔑 | Current profile; token in response is `null` |

**Register request**

```json
{
  "fullName": "Abebe Beso",
  "email": "abebe@example.com",
  "password": "Secret123!",
  "role": "LEARNER",
  "phone": "+251900000000",
  "university": "Addis University",
  "department": "Software Engineering",
  "studentId": "ST-1234",
  "employeeId": null,
  "proofDocumentUrl": null,
  "organizationName": null,
  "organizationDescription": null,
  "organizationWebsite": null
}
```

Mentor registration requires `university`, `department`, `employeeId`, and an HTTPS `proofDocumentUrl`. Employer registration creates an organization from `organizationName`, `organizationDescription`, and `organizationWebsite`.

**Login request:** `{ "email": "...", "password": "..." }`.

---

### 6.2 Users, profiles, and uploads

| Method | Path | Access | Description |
| --- | --- | --- | --- |
| GET | `/users/me` | 🔑 | Full profile (`UserResponse`) |
| PATCH | `/users/me` | 🔑 | Partial profile update |
| POST | `/users/me/profile-photo` | 🔑 | `multipart/form-data`, field `photo` |
| DELETE | `/users/me/profile-photo` | 🔑 | Remove avatar |
| GET | `/admin/users` | 🛡️ | List all users |
| PATCH | `/admin/users/{id}/status` | 🛡️ | Toggle `ACTIVE` ↔ `SUSPENDED` |
| POST | `/uploads` | 🔑 | `multipart/form-data`: `file`, `purpose` |
| GET | `/uploads/avatars/{id}` | 🔓 | Public avatar bytes |
| GET | `/uploads/files/{id}` | 🔑 | Private file bytes (ownership/role checked) |

`PATCH /users/me` body (`UpdateUserProfileRequest`) — all optional: `fullName`, `email`, `phone`, `bio`, `university`, `department`, `studentId`, `cvUrl`.

Upload `purpose` values:

| Purpose | Allowed roles | Allowed extensions |
| --- | --- | --- |
| `AVATAR` | any authenticated | `png`, `jpg`, `jpeg`, `webp` |
| `CV` | learner | `pdf`, `doc`, `docx`, `png`, `jpg`, `jpeg` |
| `VERIFICATION_DOC` | employer | `pdf`, `doc`, `docx`, `png`, `jpg`, `jpeg` |
| `COURSE_MATERIAL` | approved mentor | `pdf`, `doc`, `docx`, `ppt`, `pptx`, `zip`, `mp4`, `webm` |

Upload response: `{ "url": "http://localhost:8080/api/uploads/files/<uuid>", "filename": "resume.pdf", "sizeBytes": 12345 }`. Maximum size is 10 MB.

---

### 6.3 Course categories — `/course-categories` (alias `/categories`)

| Method | Path | Access | Description |
| --- | --- | --- | --- |
| GET | `/course-categories` | 🔓 | List categories |
| GET | `/course-categories/{id}` | 🔓 | One category |
| POST | `/course-categories` | 🛡️ | Create category (201) |
| DELETE | `/course-categories/{id}` | 🛡️ | Delete category (204) |

`CategoryResponse`: `{ "id": 1, "name": "Software Engineering", "description": "..." }`.
Create body: `{ "name": "Software Engineering", "description": "..." }`.

> The frontend should load this endpoint instead of hardcoding category IDs.

---

### 6.4 Courses — `/courses`

| Method | Path | Access | Description |
| --- | --- | --- | --- |
| GET | `/courses` | 🔓 | Catalogue, role-aware |
| GET | `/courses/{id}` | 🔓 | One course; drafts only for owner/admin |
| POST | `/courses` | 🎓 | Create course (always `DRAFT`, 201) |
| PATCH | `/courses/{id}` | 🎓 | Partial update, including `status` |
| PATCH | `/courses/{id}/publish` | 🎓 | Set status to `PUBLISHED` |
| POST | `/courses/{courseId}/enroll` | 🧑‍🎓 | Enroll; returns `CourseResponse` |

`GET /courses` query parameters: `category` (ID **or** name), `level` (`BEGINNER`/`INTERMEDIATE`/`ADVANCED`/`ALL_LEVELS`), `search` (title/description).

Role-aware listing:

- anonymous / learner → only `PUBLISHED`
- mentor → only the mentor's own courses (all statuses)
- super admin → all courses

**Create course request (`CreateCourseRequest`)**

```json
{
  "title": "Intro to Java",
  "description": "Full description",
  "shortDescription": "Card summary",
  "level": "BEGINNER",
  "durationWeeks": 6,
  "learningObjectives": "Understand classes\nWrite methods",
  "categoryId": 1,
  "thumbnailUrl": "https://..."
}
```

`title` is required (≤ 200 chars); `level` defaults to `BEGINNER`; `status` is forced to `DRAFT` on create.

**Update course request (`UpdateCourseRequest`)** — all optional: `title`, `description`, `shortDescription`, `level`, `durationWeeks`, `learningObjectives`, `thumbnailUrl`, `categoryId`, `status`.

**Enrollment behavior:** only `PUBLISHED` courses can be enrolled in. Enrolling twice is idempotent and returns the same course.

---

### 6.5 Course materials — `/courses/{courseId}/materials`

| Method | Path | Access | Description |
| --- | --- | --- | --- |
| GET | `/courses/{courseId}/materials` | 🔓 | List materials (drafts: owner/admin only) |
| POST | `/courses/{courseId}/materials` | 🎓 | Add material (201) |
| PATCH | `/courses/{courseId}/materials/{materialId}` | 🎓 | Update material |
| DELETE | `/courses/{courseId}/materials/{materialId}` | 🎓 | Delete material (204) |

Create body (`CreateMaterialRequest`):

```json
{
  "title": "Week 1 slides",
  "description": "Introduction and setup",
  "type": "SLIDE",
  "url": "https://example.com/slides.pdf",
  "order": 1
}
```

`title` required (≤ 200); `type` defaults to `LINK`; `order` defaults to the next position.

Materials are also embedded in every `CourseResponse` as `materials`, so the course detail screen does not need a second request.

---

### 6.6 Curriculum — modules and lessons

| Method | Path | Access | Description |
| --- | --- | --- | --- |
| GET | `/courses/{courseId}/modules` | 🔓 | Full curriculum with lessons |
| POST | `/courses/{courseId}/modules` | 🎓 | Add module (201) |
| PUT | `/courses/{courseId}/modules/reorder` | 🎓 | Reorder modules |
| PATCH | `/modules/{moduleId}` | 🎓 | Rename/reorder module |
| DELETE | `/modules/{moduleId}` | 🎓 | Delete module (cascades lessons) |
| POST | `/modules/{moduleId}/lessons` | 🎓 | Add lesson (201) |
| PUT | `/modules/{moduleId}/lessons/reorder` | 🎓 | Reorder lessons |
| PATCH | `/lessons/{lessonId}` | 🎓 | Update lesson |
| DELETE | `/lessons/{lessonId}` | 🎓 | Delete lesson |

Create module: `{ "title": "Module One", "moduleOrder": 1 }`.
Update module: `{ "title"?: string, "moduleOrder"?: number }`.

Create lesson:

```json
{
  "title": "Lesson One",
  "type": "VIDEO",
  "body": "Watch the video and follow along.",
  "contentUrl": "https://example.com/video",
  "durationMinutes": 30,
  "lessonOrder": 1
}
```

Update lesson — all optional: `title`, `type`, `body`, `contentUrl`, `durationMinutes`, `lessonOrder`.

Reorder body: `{ "ids": [3, 1, 2] }`. The array index becomes the new order starting at 1. The list must contain every module/lesson exactly once.

`ContentType` allowed values: `TEXT`, `VIDEO_URL`, `PDF_DOWNLOAD`, `LINK`, `VIDEO`, `ARTICLE`, `LAB`. The UI may normalize legacy values, but sending `VIDEO`, `ARTICLE`, or `LAB` is recommended.

---

### 6.7 Enrollments

| Method | Path | Access | Description |
| --- | --- | --- | --- |
| GET | `/users/me/enrollments` | 🧑‍🎓 | Current learner's enrollments |
| GET | `/instructor/students` | 🎓 | All students across the mentor's courses |
| PATCH | `/enrollments/{id}/status` | 🎓 | Change status |

Update status body: `{ "status": "COMPLETED" }` (`ACTIVE`, `COMPLETED`, `DROPPED`). Only the course mentor or a super admin may update an enrollment.

> Learner enrollment is created with `POST /courses/{courseId}/enroll`, not through an enrollment POST.

---

### 6.8 Progress tracking

| Method | Path | Access | Description |
| --- | --- | --- | --- |
| POST | `/progress/courses/{courseId}/lessons/{lessonId}/complete` | 🧑‍🎓 | Mark lesson complete |
| POST | `/courses/{courseId}/lessons/{lessonId}/complete` | 🧑‍🎓 | Alias of the above |
| GET | `/progress/courses/{courseId}` | 🧑‍🎓 | Detailed progress |
| GET | `/courses/{courseId}/progress` | 🧑‍🎓 | Alias of the above |

Requirements: the lesson must belong to the course, and the learner must have an `ACTIVE` enrollment. Completion is idempotent. `GET` accepts `ACTIVE` or `COMPLETED` enrollments.

Response for the `GET`: `CourseProgressResponse`. Response for `POST`: `{ "progressPercentage": 50.0, "completedCount": 1 }`.

---

### 6.9 Assignments

| Method | Path | Access | Description |
| --- | --- | --- | --- |
| GET | `/assignments` | 🔑 | All visible assignments; optional `?courseId=` |
| GET | `/courses/{courseId}/assignments` | 🔑 | Assignments for one course |
| POST | `/courses/{courseId}/assignments` | 🎓 | Create assignment (201) |
| GET | `/assignments/{id}` | 🔑 | One assignment |
| PATCH | `/assignments/{id}` | 🎓 | Partial update |
| DELETE | `/assignments/{id}` | 🎓 | Delete assignment (cascades submissions, 204) |

Visibility: super admin, the owning mentor, or a learner with an `ACTIVE`/`COMPLETED` enrollment in the course.

Create body (`CreateAssignmentRequest`):

```json
{
  "title": "Build a REST endpoint",
  "description": "Implement and test the endpoint.",
  "dueDate": "2026-12-31T23:59:59",
  "maxPoints": 100,
  "attachmentUrl": "https://example.com/brief.pdf"
}
```

`title`, `dueDate` (must be in the future), and `maxPoints` are required.

Update body (`UpdateAssignmentRequest`) — all optional: `title`, `description`, `dueDate`, `maxPoints`, `attachmentUrl`. Past due dates are allowed on update so mentors can correct/extend.

---

### 6.10 Submissions and grading

| Method | Path | Access | Description |
| --- | --- | --- | --- |
| GET | `/users/me/submissions` | 🧑‍🎓 | Current learner's submissions |
| POST | `/assignments/{assignmentId}/submit` | 🧑‍🎓 | Submit or resubmit |
| GET | `/assignments/{assignmentId}/submissions` | 🎓 | Submissions for one assignment |
| GET | `/assignments/submissions` | 🎓 | All submissions in the mentor's courses |
| PATCH | `/submissions/{submissionId}/grade` | 🎓 | Grade a submission |

Submit body (`SubmitAssignmentRequest`):

```json
{
  "textAnswer": "My answer",
  "fileUrl": "http://localhost:8080/api/uploads/files/<uuid>"
}
```

Both fields are optional but at least one should be present in the UI. The learner must be enrolled and the deadline must not have passed. Resubmission resets status to `SUBMITTED`.

Grade body (`GradeSubmissionRequest`):

```json
{
  "grade": 85,
  "feedback": "Good work",
  "status": "GRADED"
}
```

`grade` is required, zero or positive, and cannot exceed the assignment's `maxPoints`. `feedback` is optional. `status` is optional and defaults to `GRADED`; allowed values are `SUBMITTED`, `UNDER_REVIEW`, `GRADED`, `RETURNED`.

---

### 6.11 Notifications

| Method | Path | Access | Description |
| --- | --- | --- | --- |
| GET | `/notifications` | 🔑 | List, newest first; `?unreadOnly=true` |
| GET | `/notifications/unread-count` | 🔑 | `{ "unreadCount": 3 }` |
| PATCH | `/notifications/{id}/read` | 🔑 | Mark one read (204) |
| POST | `/notifications/read-all` | 🔑 | Mark all read (204) |
| DELETE | `/notifications/{id}` | 🔑 | Delete one (204) |
| DELETE | `/notifications` | 🔑 | Clear all (204) |

Notification `type` is a free-form string; the backend currently emits `INFO`, `SUCCESS`, and `WARNING`. `link` may contain an in-app route such as `/learner/assignments` or `/employer/applicants`.

---

### 6.12 Internships — `/internships`

| Method | Path | Access | Description |
| --- | --- | --- | --- |
| GET | `/internships/listings` | 🔓 | Published listings |
| GET | `/internships` | 🔓 | Alias |
| GET | `/internships/listings/{id}` | 🔓 | One listing |
| POST | `/internships/listings` | 🏢 / 🎯 / 🛡️ | Create (201) |
| GET | `/internships/mine` | 🏢 / 🎯 / 🛡️ | Listings owned by caller |
| GET | `/internships/admin` | 🛡️ | All listings |
| PATCH | `/internships/listings/{id}` | owner | Update |
| DELETE | `/internships/listings/{id}` | owner | Delete (204) |
| POST | `/internships/listings/{id}/close` | owner | Set `CLOSED`, returns listing |
| POST | `/internships/listings/{id}/save` | 🧑‍🎓 | Toggle saved; `{ "isSaved": true }` |
| GET | `/internships/saved` | 🧑‍🎓 | Saved listings |
| POST | `/internships/listings/{internshipId}/apply` | 🧑‍🎓 | Apply (201) |
| GET | `/internships/applications/my` | 🧑‍🎓 | My applications |

Catalogue filters: `?category=...&workMode=ONSITE|HYBRID|REMOTE&search=...`.

Create body (`CreateInternshipListingRequest`):

```json
{
  "organizationId": 1,
  "title": "Backend Intern",
  "description": "Work on REST services.",
  "requirements": "Java\nSpring Boot",
  "workMode": "REMOTE",
  "location": "Remote",
  "category": "Software Engineering",
  "responsibilities": "Build endpoints\nWrite tests",
  "eligibility": "Software Engineering students",
  "stipend": "5000 ETB/month",
  "durationMonths": 3,
  "status": "PUBLISHED",
  "deadline": "2026-12-31T23:59:59"
}
```

`organizationId`, `title`, `description`, `workMode`, and a future `deadline` are required. Employers must have an `APPROVED` organization to publish.

Apply body (`ApplyInternshipRequest`):

```json
{
  "coverLetter": "I am a software engineering student with ... (30–5000 chars)",
  "resumeUri": "http://localhost:8080/api/uploads/files/<uuid>"
}
```

Both fields are required; the cover letter must be 30–5000 characters. Duplicate applications return `409`.

---

### 6.13 Applications — `/applications`

| Method | Path | Access | Description |
| --- | --- | --- | --- |
| GET | `/applications` | learner/manager | Role-aware list; optional `internshipId`, `applicantId` (admin only) |
| PATCH | `/applications/{id}/status` | 🏢 / 🎯 / 🛡️ | Update status |

Update body: `{ "status": "SHORTLISTED", "notes": "Interview next week" }`. Only the listing owner/coordinator/admin may update. The learner receives a notification.

---

### 6.14 Organizations — `/organizations`

| Method | Path | Access | Description |
| --- | --- | --- | --- |
| GET | `/organizations` | 🔓 | Approved public directory |
| GET | `/organizations/{id}` | 🔓 | Approved organization only |
| POST | `/organizations` | 🏢 / 🎯 / 🛡️ | Create (201) |
| GET | `/organizations/my` | 🏢 / 🎯 | Current user's organization |
| PATCH | `/organizations/my` | 🏢 | Update profile |
| POST | `/organizations/me/verification` | 🏢 | Submit verification documents |

Create body: `{ "name", "description", "website", "logoUrl", "industry", "location", "contactEmail", "contactPhone" }` (`name` required).

Verification body:

```json
{
  "registrationNumber": "REG-001",
  "taxId": "TAX-001",
  "documentName": "registration.pdf",
  "documentUrl": "http://localhost:8080/api/uploads/files/<uuid>",
  "notes": "Please verify"
}
```

Changing the organization name, registration number, or tax ID on an already-approved organization resets verification to `PENDING`.

---

### 6.15 Dashboards

| Method | Path | Access | Description |
| --- | --- | --- | --- |
| GET | `/dashboard/learner` | 🧑‍🎓 | `LearnerDashboardResponse` |
| GET | `/dashboard/mentor` | 🎓 | `MentorDashboardResponse` |

These are aggregate summaries. Use the list endpoints for drill-down tables.

---

### 6.16 Rankings and achievements

| Method | Path | Access | Description |
| --- | --- | --- | --- |
| GET | `/leaderboard?limit=10` | 🔑 | Ranked learners (limit 1–100) |
| GET | `/leaderboard/me` | 🧑‍🎓 | Current learner's rank entry |
| GET | `/achievements/me` | 🧑‍🎓 | Badge list with `earned` flags |

---

### 6.17 AI support (optional features)

| Method | Path | Access | Description |
| --- | --- | --- | --- |
| GET | `/ai/study-plan?courseId={id}` | 🧑‍🎓 | Remaining lessons grouped into weeks |
| GET | `/ai/progress-summary` | 🧑‍🎓 | Natural-language progress summary + recommendations |
| GET | `/ai/internship-matches?limit=5` | 🧑‍🎓 | Heuristic internship matching |
| POST | `/ai/ask` | 🧑‍🎓 | Course/progress Q&A |

Ask body: `{ "question": "What does this course cover?", "courseId": 1 }`. `courseId` is optional; omit it for a progress-based answer.

These endpoints are deterministic and do **not** call an external LLM. `aiGenerated` is therefore `false`. The UI can still label them as an “assistant” / “smart suggestions”.

---

### 6.18 Administration and approvals

| Method | Path | Access | Description |
| --- | --- | --- | --- |
| GET | `/admin/reports/overview` | 🛡️ | Platform statistics |
| GET | `/admin/audit-logs` | 🛡️ | Recent 500 audit entries |
| GET | `/admin/organizations` | 🛡️ | All organizations |
| POST | `/admin/organizations/{id}/verification-decision` | 🛡️ | Approve/reject organization |
| GET | `/admin/mentor-requests` | 🛡️ | Pending mentor applications |
| POST | `/admin/mentor-requests/{id}/decision` | 🛡️ | Approve/reject mentor |
| GET | `/admin/coordinator-requests` | 🛡️ | Pending coordinator applications |
| POST | `/admin/coordinator-requests/{id}/decision` | 🛡️ | Approve/reject coordinator |
| GET | `/mentors/me/status` | mentor | `{ "status", "notes" }` |
| GET | `/coordinators/me/status` | coordinator | `{ "status", "notes" }` |

Decision body: `{ "status": "APPROVED", "notes": "Verified" }` (`PENDING`, `APPROVED`, `REJECTED`, `SUSPENDED`).

Organization verification decision body: `{ "status": "APPROVED", "notes": "..." }` (`PENDING` is rejected as a decision).

`AdminReportResponse`: `totalLearners`, `totalMentors`, `totalCourses`, `totalInternships`, `totalApplications`, `totalOrganizations`, `verifiedOrganizations`, `pendingApprovals`, `pendingVerifications`, `publishedCourses`, `activeEnrollments`, `completionRate`, `submissionsCount`, `gradesPending`.

`AuditLogResponse`: `id`, `actorId`, `actorName`, `actorRole`, `action`, `targetType`, `targetId`, `details`, `timestamp`, `ipAddress`.

---

## 7. Complete route matrix

| # | Method | Path | Access |
| --- | --- | --- | --- |
| 1 | POST | `/auth/register` | public |
| 2 | POST | `/auth/login` | public |
| 3 | GET | `/auth/me` | authenticated |
| 4 | GET | `/users/me` | authenticated |
| 5 | PATCH | `/users/me` | authenticated |
| 6 | POST | `/users/me/profile-photo` | authenticated |
| 7 | DELETE | `/users/me/profile-photo` | authenticated |
| 8 | GET | `/admin/users` | super admin |
| 9 | PATCH | `/admin/users/{id}/status` | super admin |
| 10 | POST | `/uploads` | authenticated |
| 11 | GET | `/uploads/avatars/{id}` | public |
| 12 | GET | `/uploads/files/{id}` | authenticated |
| 13 | GET | `/course-categories` / `/categories` | public |
| 14 | GET | `/course-categories/{id}` / `/categories/{id}` | public |
| 15 | POST | `/course-categories` / `/categories` | super admin |
| 16 | DELETE | `/course-categories/{id}` / `/categories/{id}` | super admin |
| 17 | GET | `/courses` | public |
| 18 | GET | `/courses/{id}` | public |
| 19 | POST | `/courses` | approved mentor/admin |
| 20 | PATCH | `/courses/{id}` | approved mentor/admin |
| 21 | PATCH | `/courses/{id}/publish` | approved mentor/admin |
| 22 | POST | `/courses/{courseId}/enroll` | learner |
| 23 | GET | `/courses/{courseId}/materials` | public (drafts owner/admin) |
| 24 | POST | `/courses/{courseId}/materials` | approved mentor/admin |
| 25 | PATCH | `/courses/{courseId}/materials/{materialId}` | approved mentor/admin |
| 26 | DELETE | `/courses/{courseId}/materials/{materialId}` | approved mentor/admin |
| 27 | GET | `/courses/{courseId}/modules` | public (drafts owner/admin) |
| 28 | POST | `/courses/{courseId}/modules` | approved mentor/admin |
| 29 | PUT | `/courses/{courseId}/modules/reorder` | approved mentor/admin |
| 30 | PATCH | `/modules/{moduleId}` | approved mentor/admin |
| 31 | DELETE | `/modules/{moduleId}` | approved mentor/admin |
| 32 | POST | `/modules/{moduleId}/lessons` | approved mentor/admin |
| 33 | PUT | `/modules/{moduleId}/lessons/reorder` | approved mentor/admin |
| 34 | PATCH | `/lessons/{lessonId}` | approved mentor/admin |
| 35 | DELETE | `/lessons/{lessonId}` | approved mentor/admin |
| 36 | GET | `/users/me/enrollments` | learner |
| 37 | GET | `/instructor/students` | approved mentor/admin |
| 38 | PATCH | `/enrollments/{id}/status` | approved mentor/admin |
| 39 | POST | `/progress/courses/{courseId}/lessons/{lessonId}/complete` | learner |
| 40 | POST | `/courses/{courseId}/lessons/{lessonId}/complete` | learner |
| 41 | GET | `/progress/courses/{courseId}` | learner |
| 42 | GET | `/courses/{courseId}/progress` | learner |
| 43 | GET | `/assignments` | authenticated |
| 44 | GET | `/courses/{courseId}/assignments` | authenticated |
| 45 | POST | `/courses/{courseId}/assignments` | approved mentor/admin |
| 46 | GET | `/assignments/{id}` | authenticated |
| 47 | PATCH | `/assignments/{id}` | approved mentor/admin |
| 48 | DELETE | `/assignments/{id}` | approved mentor/admin |
| 49 | GET | `/users/me/submissions` | learner |
| 50 | POST | `/assignments/{assignmentId}/submit` | learner |
| 51 | GET | `/assignments/{assignmentId}/submissions` | approved mentor/admin |
| 52 | GET | `/assignments/submissions` | approved mentor/admin |
| 53 | PATCH | `/submissions/{submissionId}/grade` | approved mentor/admin |
| 54 | GET | `/notifications` | authenticated |
| 55 | GET | `/notifications/unread-count` | authenticated |
| 56 | PATCH | `/notifications/{id}/read` | authenticated |
| 57 | POST | `/notifications/read-all` | authenticated |
| 58 | DELETE | `/notifications/{id}` | authenticated |
| 59 | DELETE | `/notifications` | authenticated |
| 60 | GET | `/internships/listings` | public |
| 61 | GET | `/internships/listings/{id}` | public |
| 62 | POST | `/internships/listings` | employer/coordinator/admin |
| 63 | GET | `/internships/mine` | employer/coordinator/admin |
| 64 | GET | `/internships/admin` | super admin |
| 65 | PATCH | `/internships/listings/{id}` | owner |
| 66 | DELETE | `/internships/listings/{id}` | owner |
| 67 | POST | `/internships/listings/{id}/close` | owner |
| 68 | POST | `/internships/listings/{id}/save` | learner |
| 69 | GET | `/internships/saved` | learner |
| 70 | POST | `/internships/listings/{internshipId}/apply` | learner |
| 71 | GET | `/internships/applications/my` | learner |
| 72 | GET | `/applications` | learner/manager |
| 73 | PATCH | `/applications/{id}/status` | employer/coordinator/admin |
| 74 | GET | `/organizations` | public |
| 75 | GET | `/organizations/{id}` | public (approved) |
| 76 | POST | `/organizations` | employer/coordinator/admin |
| 77 | GET | `/organizations/my` | employer/coordinator |
| 78 | PATCH | `/organizations/my` | employer |
| 79 | POST | `/organizations/me/verification` | employer |
| 80 | GET | `/dashboard/learner` | learner |
| 81 | GET | `/dashboard/mentor` | approved mentor/admin |
| 82 | GET | `/leaderboard` | authenticated |
| 83 | GET | `/leaderboard/me` | learner |
| 84 | GET | `/achievements/me` | learner |
| 85 | GET | `/ai/study-plan` | learner |
| 86 | GET | `/ai/progress-summary` | learner |
| 87 | GET | `/ai/internship-matches` | learner |
| 88 | POST | `/ai/ask` | learner |
| 89 | GET | `/admin/reports/overview` | super admin |
| 90 | GET | `/admin/audit-logs` | super admin |
| 91 | GET | `/admin/organizations` | super admin |
| 92 | POST | `/admin/organizations/{id}/verification-decision` | super admin |
| 93 | GET | `/admin/mentor-requests` | super admin |
| 94 | POST | `/admin/mentor-requests/{id}/decision` | super admin |
| 95 | GET | `/admin/coordinator-requests` | super admin |
| 96 | POST | `/admin/coordinator-requests/{id}/decision` | super admin |
| 97 | GET | `/mentors/me/status` | mentor |
| 98 | GET | `/coordinators/me/status` | coordinator |

Aliases that also work: `GET /internships` (same as `/internships/listings`), `GET /internships/{id}`, `PATCH /internships/{id}`, `DELETE /internships/{id}`, `POST /internships/{id}/close`, `POST /internships/{id}/save`, `POST /internships/{internshipId}/applications`, `GET /organizations/me`, `PATCH /organizations/me`.

---

## 8. Business rules the frontend must respect

1. **Course visibility**
   - Only `PUBLISHED` courses appear to anonymous users and learners.
   - Mentors see their own drafts; admins see everything.
   - `GET /courses/{id}` and `GET /courses/{id}/modules` return `403` for unpublished courses unless the caller is the owner or admin.

2. **Ownership**
   - Mentors can modify only their own courses, modules, lessons, materials, assignments, and submissions.
   - Employers/coordinators can modify only listings they created.
   - Super admins bypass ownership checks.

3. **Approval gates**
   - A mentor with `mentorStatus = PENDING` can log in and browse, but course/assignment mutations return `403`.
   - A coordinator with `coordinatorStatus = PENDING` cannot manage listings.
   - An employer can create a `DRAFT` listing while unverified, but publishing requires `organization.verificationStatus = APPROVED`.

4. **Enrollment**
   - Only learners can enroll.
   - Only published courses can be enrolled in.
   - Duplicate enrollment is idempotent.

5. **Progress**
   - Progress is per lesson and idempotent.
   - The learner must be enrolled in the course.
   - `completionPercentage` is `completed / total * 100`, rounded to 2 decimals.

6. **Assignments/submissions**
   - Learners must be enrolled to view or submit.
   - Submissions are rejected after the due date.
   - Grades cannot exceed `maxPoints`.
   - One submission per learner per assignment; resubmission updates the existing row.

7. **Internships**
   - Public listing requires `PUBLISHED` + approved organization + future deadline.
   - Applications are one per learner per listing.
   - Cover letter 30–5000 characters; resume URI required.
   - Closing sets status to `CLOSED`; deleting removes the listing and (via DB cascade) its applications.

8. **Organizations**
   - The public directory only returns `APPROVED` organizations.
   - Changing legal identity fields on an approved organization resets it to `PENDING`.

---

## 9. Frontend integration guide

### 9.1 Recommended Axios client

```ts
import axios from 'axios';

const ORIGIN = (import.meta.env.VITE_API_URL ?? 'http://localhost:8080')
  .replace(/\/+$/, '')
  .replace(/\/api$/i, '');

export const api = axios.create({
  baseURL: ORIGIN,
  headers: { 'Content-Type': 'application/json' },
  timeout: 15000,
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login?session_expired=true';
    }
    return Promise.reject(error);
  }
);
```

Every service must call paths with the `/api` prefix, e.g. `api.get('/api/courses')`.

### 9.2 ID normalization

Backend IDs are JSON numbers. If the UI uses `string` IDs, normalize at the service boundary:

```ts
const toId = (value: unknown) => (value == null ? '' : String(value));
```

### 9.3 Error extraction

```ts
type ApiError = {
  timestamp?: string;
  status?: number;
  error?: string;
  message?: string;
  errors?: Record<string, string>;
};

export function apiErrorMessage(error: unknown, fallback: string): string {
  if (axios.isAxiosError(error)) {
    const data = error.response?.data as ApiError | undefined;
    const firstValidation = data?.errors && Object.values(data.errors)[0];
    return firstValidation ?? data?.message ?? data?.error ?? fallback;
  }
  return fallback;
}
```

Note: Spring validation errors are a **flat** `Record<string, string>`, not `Record<string, string[]>`.

### 9.4 Suggested screen → endpoint mapping

| Screen | Endpoints |
| --- | --- |
| Login / Register | `POST /auth/login`, `POST /auth/register`, `GET /auth/me` |
| Course catalogue | `GET /courses`, `GET /course-categories` |
| Course detail | `GET /courses/{id}` (includes `materials`), `GET /courses/{id}/modules`, `GET /courses/{id}/progress` |
| My learning | `GET /users/me/enrollments`, `GET /dashboard/learner`, `GET /progress/courses/{courseId}` |
| Lesson player | `POST /courses/{courseId}/lessons/{lessonId}/complete`, `GET /courses/{courseId}/progress` |
| Assignments (learner) | `GET /assignments`, `GET /assignments/{id}`, `POST /assignments/{id}/submit`, `GET /users/me/submissions` |
| Grading (mentor) | `GET /assignments/submissions`, `GET /assignments/{id}/submissions`, `PATCH /submissions/{id}/grade` |
| Course builder | `POST /courses`, `PATCH /courses/{id}`, module/lesson CRUD, `PATCH /courses/{id}/publish` |
| Materials manager | `POST/PATCH/DELETE /courses/{courseId}/materials` |
| Mentor dashboard | `GET /dashboard/mentor`, `GET /instructor/students`, `GET /courses` |
| Internship catalogue | `GET /internships/listings`, `GET /internships/listings/{id}` |
| Saved internships | `GET /internships/saved`, `POST /internships/listings/{id}/save` |
| My applications | `GET /internships/applications/my`, `GET /applications` |
| Employer applicants | `GET /applications?internshipId=...`, `PATCH /applications/{id}/status` |
| Employer listings | `GET /internships/mine`, `POST/PATCH/DELETE /internships/listings...` |
| Organization profile | `GET/PATCH /organizations/my`, `POST /organizations/me/verification` |
| Notifications | `GET /notifications`, `GET /notifications/unread-count`, `PATCH /notifications/{id}/read`, `POST /notifications/read-all` |
| Leaderboard | `GET /leaderboard`, `GET /leaderboard/me`, `GET /achievements/me` |
| AI assistant | `GET /ai/study-plan`, `GET /ai/progress-summary`, `GET /ai/internship-matches`, `POST /ai/ask` |
| Admin | `GET /admin/reports/overview`, `/admin/users`, `/admin/organizations`, approval endpoints, `/admin/audit-logs` |

### 9.5 Recommended TypeScript course/material contract

```ts
export interface CourseResponse {
  id: number;
  title: string;
  description: string | null;
  shortDescription: string | null;
  level: CourseLevel;
  status: CourseStatus;
  thumbnailUrl: string | null;
  durationWeeks: number | null;
  learningObjectives: string | null;
  mentorId: number;
  instructorName: string | null;
  categoryId: number | null;
  categoryName: string | null;
  materials: MaterialResponse[];
  createdAt: string;
}

export interface MaterialResponse {
  id: number;
  courseId: number;
  title: string;
  description: string | null;
  type: MaterialType;
  url: string | null;
  order: number;
  createdAt: string;
}
```

### 9.6 Typical flows

**Learner enrollment + first lesson**

1. `GET /courses?category=1&level=BEGINNER&search=java`
2. `GET /courses/{id}` → show `materials` and metadata
3. `POST /courses/{id}/enroll`
4. `GET /courses/{id}/modules`
5. `POST /courses/{id}/lessons/{lessonId}/complete`
6. `GET /courses/{id}/progress`

**Mentor publish flow**

1. `POST /courses` → `DRAFT`
2. `POST /courses/{id}/modules`
3. `POST /modules/{moduleId}/lessons`
4. `POST /courses/{id}/materials`
5. `POST /courses/{id}/assignments`
6. `PATCH /courses/{id}/publish`

**Assignment grading**

1. Mentor: `GET /assignments/submissions`
2. Mentor: `PATCH /submissions/{id}/grade` with `{ grade, feedback }`
3. Learner notification appears; learner: `GET /users/me/submissions`

**Internship application**

1. Learner: `GET /internships/listings`
2. Learner: `POST /internships/listings/{id}/save` (optional)
3. Learner: `POST /uploads` with `purpose=CV`
4. Learner: `POST /internships/listings/{id}/apply` with `coverLetter` + `resumeUri`
5. Employer: `GET /applications?internshipId={id}`
6. Employer: `PATCH /applications/{id}/status`

---

## 10. Notes, limitations, and frontend gotchas

1. **No pagination.** List endpoints return complete arrays. Use client-side filtering/paging for large screens, or ask the backend team to add pagination later.
2. **No refresh tokens / logout / password reset.** The UI should expire the session on `401` and support contact-support text for password recovery.
3. **No public user profiles.** Only `/users/me` and the admin user list exist.
4. **Enrollment progress is not embedded in `EnrollmentResponse`.** Use the progress endpoint per course.
5. **Requirement lists are plain text.** Internship `requirements`, `responsibilities`, and `eligibility` may contain newline-separated items; split on `\n` for display.
6. **Material type values differ from old mock data.** Use `DOCUMENT | VIDEO | LINK | SLIDE | DATASET | OTHER`; there is no `PDF` material type.
7. **Application statuses differ from old mock data.** Use backend values (`SUBMITTED`, `UNDER_REVIEW`, `SHORTLISTED`, `INTERVIEW_SCHEDULED`, `ACCEPTED`, `DECLINED`, `REJECTED`). Translate to UI labels only at the presentation layer.
8. **Organization statuses differ from old mock data.** Use `PENDING | APPROVED | REJECTED`; there is no `VERIFIED`/`UNVERIFIED`.
9. **`/auth/me` does not return a token.** Keep the token already stored from login/register.
10. **AI answers are deterministic.** Do not promise streaming or generative chat; the endpoints return complete JSON responses.
11. **Uploads return absolute URLs** built from the current request host, including the `/api` context path. Store and send them back unchanged.
12. **Deleting a course is not exposed.** Courses can be archived via `PATCH /courses/{id}` with `{ "status": "ARCHIVED" }`.
13. **No websocket/SSE notifications.** Poll `/notifications/unread-count` or refresh on navigation.

---

## 11. Quick verification checklist

- [ ] `GET /api/course-categories` returns categories (no hardcoded IDs).
- [ ] Course payload includes `materials`.
- [ ] `POST /api/courses/{id}/materials` works for an approved mentor.
- [ ] `GET /api/assignments/{id}`, `PATCH`, and `DELETE` work.
- [ ] `PATCH /api/submissions/{id}/grade` accepts `{ grade, feedback }` without `status`.
- [ ] `POST /api/courses/{courseId}/lessons/{lessonId}/complete` and `GET /api/courses/{courseId}/progress` work.
- [ ] `GET /api/dashboard/learner` and `/dashboard/mentor` return aggregates.
- [ ] `GET /api/leaderboard`, `/leaderboard/me`, `/achievements/me` work.
- [ ] `GET /api/notifications/unread-count` and delete routes work.
- [ ] `DELETE /api/internships/listings/{id}` and `POST .../close` work.
- [ ] Swagger UI opens at `/api/swagger-ui.html`; `/api/v3/api-docs` lists 86 unique paths (aliases are merged under their operation).
- [ ] `mvn test` passes and Flyway is at version `V8`.

---

*Generated from the verified backend implementation. If a route or field differs from this document, the running OpenAPI contract at `/api/v3/api-docs` is the final authority.*
