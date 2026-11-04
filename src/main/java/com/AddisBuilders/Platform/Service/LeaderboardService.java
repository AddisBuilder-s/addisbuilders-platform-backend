package com.AddisBuilders.Platform.Service;

import com.AddisBuilders.Platform.DTO.AchievementResponse;
import com.AddisBuilders.Platform.DTO.LeaderboardEntryResponse;
import com.AddisBuilders.Platform.Exception.ResourceNotFoundException;
import com.AddisBuilders.Platform.Model.Enrollment;
import com.AddisBuilders.Platform.Model.InternshipApplication;
import com.AddisBuilders.Platform.Model.Submission;
import com.AddisBuilders.Platform.Model.User;
import com.AddisBuilders.Platform.Model.enums.ApplicationStatus;
import com.AddisBuilders.Platform.Model.enums.EnrollmentStatus;
import com.AddisBuilders.Platform.Model.enums.UserRole;
import com.AddisBuilders.Platform.Repository.EnrollmentRepository;
import com.AddisBuilders.Platform.Repository.InternshipApplicationRepository;
import com.AddisBuilders.Platform.Repository.StudentProgressRepository;
import com.AddisBuilders.Platform.Repository.SubmissionRepository;
import com.AddisBuilders.Platform.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Computes the learner leaderboard and achievement badges on demand from the
 * existing activity tables. Nothing is persisted, so rankings always reflect
 * the latest progress data.
 */
@Service
@RequiredArgsConstructor
public class LeaderboardService {

    private final UserRepository userRepository;
    private final StudentProgressRepository progressRepository;
    private final SubmissionRepository submissionRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final InternshipApplicationRepository applicationRepository;

    @Transactional(readOnly = true)
    public List<LeaderboardEntryResponse> leaderboard(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 100));
        List<ScoredLearner> scored = scoreAllLearners();
        AtomicInteger rank = new AtomicInteger(1);
        return scored.stream()
                .limit(safeLimit)
                .map(item -> toEntry(item, rank.getAndIncrement()))
                .toList();
    }

    @Transactional(readOnly = true)
    public LeaderboardEntryResponse myEntry(User learner) {
        List<ScoredLearner> scored = scoreAllLearners();
        for (int index = 0; index < scored.size(); index++) {
            if (scored.get(index).learner().getId().equals(learner.getId())) {
                return toEntry(scored.get(index), index + 1);
            }
        }
        throw new ResourceNotFoundException("Learner not found on the leaderboard");
    }

    @Transactional(readOnly = true)
    public List<AchievementResponse> achievements(User learner) {
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(learner.getId());
        List<Submission> submissions = submissionRepository.findByLearnerId(learner.getId());
        List<InternshipApplication> applications = applicationRepository.findByLearnerId(learner.getId());
        long completedLessons = progressRepository.countByStudentIdAndCompletedTrue(learner.getId());
        long completedCourses = enrollments.stream()
                .filter(item -> item.getStatus() == EnrollmentStatus.COMPLETED)
                .count();
        double average = DashboardService.averageGrade(submissions);
        long graded = submissions.stream().filter(item -> item.getGrade() != null).count();
        boolean accepted = applications.stream()
                .anyMatch(item -> item.getStatus() == ApplicationStatus.ACCEPTED);

        int rank = 0;
        List<ScoredLearner> scored = scoreAllLearners();
        for (int index = 0; index < scored.size(); index++) {
            if (scored.get(index).learner().getId().equals(learner.getId())) {
                rank = index + 1;
                break;
            }
        }

        List<AchievementResponse> badges = new ArrayList<>();
        badges.add(badge("FIRST_STEPS", "First Steps", "Complete your first lesson",
                completedLessons >= 1, completedLessons + "/1 lesson"));
        badges.add(badge("COURSE_EXPLORER", "Course Explorer", "Enroll in at least 3 courses",
                enrollments.size() >= 3, enrollments.size() + "/3 courses"));
        badges.add(badge("COURSE_GRADUATE", "Course Graduate", "Complete at least 1 full course",
                completedCourses >= 1, completedCourses + "/1 course"));
        badges.add(badge("FIVE_COURSE_SCHOLAR", "Five-Course Scholar", "Complete at least 5 courses",
                completedCourses >= 5, completedCourses + "/5 courses"));
        badges.add(badge("SUBMISSION_STARTER", "Submission Starter", "Submit your first assignment",
                !submissions.isEmpty(), submissions.size() + "/1 submission"));
        badges.add(badge("HIGH_ACHIEVER", "High Achiever", "Hold an average grade of 85 or higher",
                graded > 0 && average >= 85, String.format("%.1f/85 average", average)));
        badges.add(badge("INTERNSHIP_READY", "Internship Ready", "Apply for your first internship",
                !applications.isEmpty(), applications.size() + "/1 application"));
        badges.add(badge("INTERNSHIP_ACCEPTED", "Internship Accepted", "Receive an accepted internship application",
                accepted, accepted ? "earned" : "no accepted application yet"));
        badges.add(badge("TOP_TEN", "Top Ten", "Reach the top 10 of the learner leaderboard",
                rank > 0 && rank <= 10, rank == 0 ? "not ranked" : "current rank " + rank));
        return badges;
    }

    private List<ScoredLearner> scoreAllLearners() {
        List<User> learners = userRepository.findByRole(UserRole.LEARNER);
        List<ScoredLearner> scored = new ArrayList<>();
        for (User learner : learners) {
            long completedLessons = progressRepository.countByStudentIdAndCompletedTrue(learner.getId());
            List<Submission> submissions = submissionRepository.findByLearnerId(learner.getId());
            long graded = submissions.stream().filter(item -> item.getGrade() != null).count();
            double average = DashboardService.averageGrade(submissions);
            long points = completedLessons * 10 + graded * 5 + Math.round(average);
            scored.add(new ScoredLearner(learner, completedLessons, graded, average, points));
        }
        scored.sort(Comparator
                .comparingLong(ScoredLearner::points).reversed()
                .thenComparing(ScoredLearner::averageGrade, Comparator.reverseOrder())
                .thenComparing(item -> item.learner().getFullName()));
        return scored;
    }

    private LeaderboardEntryResponse toEntry(ScoredLearner item, int rank) {
        return LeaderboardEntryResponse.builder()
                .rank(rank)
                .learnerId(item.learner().getId())
                .learnerName(item.learner().getFullName())
                .university(item.learner().getUniversity())
                .department(item.learner().getDepartment())
                .completedLessons(item.completedLessons())
                .gradedSubmissions(item.gradedSubmissions())
                .averageGrade(item.averageGrade())
                .points(item.points())
                .build();
    }

    private AchievementResponse badge(String code, String title, String description,
                                      boolean earned, String hint) {
        return AchievementResponse.builder()
                .code(code)
                .title(title)
                .description(description)
                .earned(earned)
                .progressHint(hint)
                .build();
    }

    private record ScoredLearner(
            User learner,
            long completedLessons,
            long gradedSubmissions,
            double averageGrade,
            long points
    ) {}
}
