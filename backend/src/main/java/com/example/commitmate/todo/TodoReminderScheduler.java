package com.example.commitmate.todo;

import com.example.commitmate.user.PushSender;
import com.example.commitmate.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TodoReminderScheduler {

    private static final long REMINDER_WINDOW_MILLIS = 60 * 60 * 1000L; // 마감 1시간 이내

    private final TodoRepository todoRepository;
    private final PushSender pushSender;

    @Scheduled(fixedDelay = 5 * 60 * 1000L, initialDelay = 60 * 1000L)
    @Transactional
    public void sendDeadlineReminders() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        Timestamp threshold = new Timestamp(now.getTime() + REMINDER_WINDOW_MILLIS);

        List<Todo> targets = todoRepository.findUpcomingDeadlineTodosForReminder(now, threshold);
        for (Todo todo : targets) {
            User user = todo.getGroupMember().getUser();
            String token = user.getFcmToken();
            if (token == null || token.isBlank()) {
                continue;
            }

            try {
                long remainingMinutes = Math.max(1, (todo.getDeadline().getTime() - now.getTime()) / (60 * 1000L));
                pushSender.sendNotification(token, "마감 임박",
                        "'" + todo.getWork() + "' 마감까지 " + remainingMinutes + "분 남았습니다. 서둘러 완료해 주세요!");
                todo.setReminderSent(true);
            } catch (Exception e) {
                log.error("[리마인드 푸시] 발송 실패. todoId={}", todo.getId(), e);
            }
        }
    }

    @Scheduled(fixedDelay = 5 * 60 * 1000L, initialDelay = 90 * 1000L)
    @Transactional
    public void sendFineNotifications() {
        Timestamp now = new Timestamp(System.currentTimeMillis());

        List<Todo> targets = todoRepository.findNewlyExpiredTodosForNotification(now);
        for (Todo todo : targets) {
            todo.updateStatus();

            User user = todo.getGroupMember().getUser();
            String token = user.getFcmToken();
            int amount = todo.getFine() != null ? todo.getFine().getAmount() : 0;

            if (token == null || token.isBlank()) {
                todo.setExpiredNotified(true);
                continue;
            }

            try {
                pushSender.sendNotification(token, "벌금 발생",
                        "'" + todo.getWork() + "' 마감일이 지나 " + amount + "원의 벌금이 부과되었습니다.");
                todo.setExpiredNotified(true);
            } catch (Exception e) {
                log.error("[벌금 알림 푸시] 발송 실패. todoId={}", todo.getId(), e);
            }
        }
    }
}