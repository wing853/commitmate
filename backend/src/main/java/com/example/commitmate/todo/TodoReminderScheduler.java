package com.example.commitmate.todo;

import com.example.commitmate.user.SmsSender;
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
    private final SmsSender smsSender;

    @Scheduled(fixedDelay = 5 * 60 * 1000L, initialDelay = 60 * 1000L)
    @Transactional
    public void sendDeadlineReminders() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        Timestamp threshold = new Timestamp(now.getTime() + REMINDER_WINDOW_MILLIS);

        List<Todo> targets = todoRepository.findUpcomingDeadlineTodosForReminder(now, threshold);
        for (Todo todo : targets) {
            User user = todo.getGroupMember().getUser();
            String phoneNumber = user.getPhoneNumber();
            if (phoneNumber == null || phoneNumber.isBlank()) {
                continue;
            }

            try {
                smsSender.sendMessage(phoneNumber,
                        "[commitmate] '" + todo.getWork() + "' 마감이 1시간 이내입니다. 서둘러 완료해 주세요!");
                todo.setReminderSent(true);
            } catch (Exception e) {
                log.error("[리마인드 SMS] 발송 실패. todoId={}", todo.getId(), e);
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
            String phoneNumber = user.getPhoneNumber();
            int amount = todo.getFine() != null ? todo.getFine().getAmount() : 0;

            if (phoneNumber == null || phoneNumber.isBlank()) {
                todo.setExpiredNotified(true);
                continue;
            }

            try {
                smsSender.sendMessage(phoneNumber,
                        "[commitmate] '" + todo.getWork() + "' 마감을 지켜 " + amount + "원의 벌금이 부과되었습니다.");
                todo.setExpiredNotified(true);
            } catch (Exception e) {
                log.error("[벌금 알림 SMS] 발송 실패. todoId={}", todo.getId(), e);
            }
        }
    }
}