package com.example.commitmate.core.api;

import com.example.commitmate.core.errors.Exception401;
import com.example.commitmate.fine.Fine;
import com.example.commitmate.fine.FineResponse;
import com.example.commitmate.fine.FineService;
import com.example.commitmate.group.GroupRequest;
import com.example.commitmate.group.GroupResponse;
import com.example.commitmate.group.GroupService;
import com.example.commitmate.todo.Todo;
import com.example.commitmate.todo.TodoRequest;
import com.example.commitmate.todo.TodoService;
import com.example.commitmate.user.User;
import com.example.commitmate.user.UserRequest;
import com.example.commitmate.user.UserService;
import com.example.commitmate.user.PhoneVerificationService;
import com.example.commitmate.user.PasswordResetService;
import com.example.commitmate.user.MobileOAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/app")
@RequiredArgsConstructor
public class AppApiController {
    private final UserService userService;
    private final GroupService groupService;
    private final TodoService todoService;
    private final FineService fineService;
    private final PhoneVerificationService phoneVerificationService;
    private final PasswordResetService passwordResetService;
    private final MobileOAuthService mobileOAuthService;

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }

    @PostMapping("/phone-verifications/send")
    public Map<String, String> sendPhoneCode(@RequestBody Map<String, String> request) {
        phoneVerificationService.sendCode(request.get("phoneNumber"));
        return Map.of("message", "인증번호를 발송했습니다.");
    }

    @PostMapping("/phone-verifications/verify")
    public Map<String, String> verifyPhoneCode(@RequestBody Map<String, String> request) {
        String token = phoneVerificationService.verifyCode(request.get("phoneNumber"), request.get("code"));
        return Map.of("message", "휴대폰 인증이 완료되었습니다.", "verificationToken", token);
    }

    @PostMapping("/auth/login")
    public Map<String, Object> login(@RequestBody UserRequest.LoginDTO request, HttpSession session) {
        request.validate();
        User user = userService.login(request);
        session.setAttribute("sessionUser", user);
        return userMap(user);
    }

    @PostMapping("/auth/signup")
    public Map<String, Object> signup(@RequestBody UserRequest.SignupDTO request, HttpSession session) {
        request.validate();
        User user = userService.signup(request);
        session.setAttribute("sessionUser", user);
        return userMap(user);
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/auth/google/exchange")
    public Map<String, Object> exchangeGoogleLogin(@RequestBody Map<String, String> request,
                                                    HttpSession session) {
        User user = mobileOAuthService.redeem(request.get("code"));
        session.setAttribute("sessionUser", user);
        return userMap(user);
    }

    @PostMapping("/auth/forgot-password")
    public Map<String, String> forgotPassword(@RequestBody Map<String, String> request,
                                               HttpServletRequest servletRequest) {
        String baseUrl = servletRequest.getScheme() + "://" + servletRequest.getServerName()
                + (servletRequest.getServerPort() == 80 || servletRequest.getServerPort() == 443
                ? "" : ":" + servletRequest.getServerPort());
        passwordResetService.requestReset(request.get("email"), baseUrl);
        return Map.of("message", "비밀번호 재설정 메일을 발송했습니다.");
    }

    @GetMapping("/me")
    public Map<String, Object> me(HttpSession session) {
        return userMap(currentUser(session));
    }

    @PatchMapping("/me")
    public Map<String, Object> updateMe(@RequestBody ProfileRequest request, HttpSession session) {
        User current = currentUser(session);
        userService.updateProfile(current.getId(), request.nickname(), request.currentPassword(),
                request.newPassword(), request.newPasswordConfirm());
        User updated = userService.findById(current.getId());
        session.setAttribute("sessionUser", updated);
        return userMap(updated);
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> withdraw(@RequestBody(required = false) WithdrawRequest request,
                                         HttpSession session) {
        User current = currentUser(session);
        userService.withdraw(current.getId(), request == null ? null : request.password());
        session.invalidate();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/me/points")
    public Map<String, Object> chargePoints(@RequestBody PointRequest request, HttpSession session) {
        User current = currentUser(session);
        userService.chargePoint(current.getId(), request.amount());
        User updated = userService.findById(current.getId());
        session.setAttribute("sessionUser", updated);
        return userMap(updated);
    }

    @GetMapping("/groups")
    public List<GroupResponse.MyGroupDTO> groups(HttpSession session) {
        return groupService.getMyGroups(currentUser(session).getId());
    }

    @PostMapping("/groups")
    public GroupResponse.detailDTO createGroup(@RequestBody GroupRequest.CreateDTO request, HttpSession session) {
        request.validate();
        return new GroupResponse.detailDTO(groupService.createRoom(currentUser(session), request));
    }

    @PatchMapping("/groups/{groupId}")
    public GroupResponse.detailDTO updateGroup(@PathVariable Integer groupId,
                                                @RequestBody GroupRequest.UpdateDTO request,
                                                HttpSession session) {
        requireMember(groupId, session);
        request.validate();
        groupService.updateGroup(groupId, request);
        return groupService.findGroupById(groupId);
    }

    @DeleteMapping("/groups/{groupId}")
    public ResponseEntity<Void> deleteGroup(@PathVariable Integer groupId, HttpSession session) {
        groupService.deleteGroup(groupId, currentUser(session).getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/groups/join")
    public ResponseEntity<Void> joinGroup(@RequestBody GroupRequest.JoinDTO request, HttpSession session) {
        groupService.joinByJoinCode(request.getJoinCode(), currentUser(session));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/groups/{groupId}/members")
    public List<GroupResponse.MemberDTO> members(@PathVariable Integer groupId, HttpSession session) {
        requireMember(groupId, session);
        return groupService.getMembers(groupId);
    }

    @GetMapping("/groups/{groupId}/invite")
    public GroupResponse.InviteInfoDTO invite(@PathVariable Integer groupId, HttpSession session) {
        requireMember(groupId, session);
        return groupService.getInviteInfo(groupId);
    }

    @PostMapping("/groups/{groupId}/leave")
    public ResponseEntity<Void> leave(@PathVariable Integer groupId, HttpSession session) {
        groupService.leaveGroup(groupId, currentUser(session).getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/groups/{groupId}/members/{memberId}/kick")
    public ResponseEntity<Void> kick(@PathVariable Integer groupId, @PathVariable Integer memberId,
                                     HttpSession session) {
        groupService.kickMember(groupId, memberId, currentUser(session).getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/groups/{groupId}/members/{memberId}/delegate")
    public ResponseEntity<Void> delegate(@PathVariable Integer groupId, @PathVariable Integer memberId,
                                         HttpSession session) {
        groupService.delegateAdmin(groupId, memberId, currentUser(session).getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/groups/{groupId}/todos")
    public List<Map<String, Object>> todos(@PathVariable Integer groupId, HttpSession session) {
        requireMember(groupId, session);
        return todoService.showTodo(groupId).stream().map(this::todoMap).toList();
    }

    @PostMapping("/groups/{groupId}/todos")
    public ResponseEntity<Void> createTodo(@PathVariable Integer groupId,
                                           @RequestBody TodoRequest.AddDTO request,
                                           HttpSession session) {
        User user = currentUser(session);
        request.setGroupId(groupId);
        request.setUserId(user.getId());
        todoService.addTodo(request);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/todos/{todoId}")
    public ResponseEntity<Void> updateTodo(@PathVariable Integer todoId,
                                           @RequestBody TodoRequest.UpdateTodoDTO request,
                                           HttpSession session) {
        todoService.updateTodo(todoId, currentUser(session).getId(), request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/todos/{todoId}/toggle")
    public ResponseEntity<Void> toggleTodo(@PathVariable Integer todoId, HttpSession session) {
        todoService.toggleDone(todoId, currentUser(session).getId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/todos/{todoId}")
    public ResponseEntity<Void> deleteTodo(@PathVariable Integer todoId, HttpSession session) {
        todoService.deleteTodo(todoId, currentUser(session).getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/groups/{groupId}/fines")
    public Map<String, Object> fines(@PathVariable Integer groupId, HttpSession session) {
        requireMember(groupId, session);
        User current = currentUser(session);
        FineResponse.GroupFineInfo info = fineService.getFineInfo(groupId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalAmount", info.getTotalFinesAmount());
        result.put("unpaidCount", info.getUnpaidCount());
        result.put("paidCount", info.getPaidCount());
        result.put("unpaidAmount", info.getUnpaidAmount());
        result.put("paidAmount", info.getPaidAmount());
        result.put("items", info.getExpiredFines().stream().map(this::fineMap).toList());
        result.put("isAdmin", fineService.isAdmin(groupId, current.getId()));
        result.put("groupPoint", fineService.getGroup(groupId).getGroupPoint());

        Map<Integer, Map<String, Object>> memberSummaries = new LinkedHashMap<>();
        for (Fine fine : info.getExpiredFines()) {
            Integer ownerId = fine.getGroupMember().getUser().getId();
            Map<String, Object> summary = memberSummaries.computeIfAbsent(ownerId, id -> {
                Map<String, Object> value = new LinkedHashMap<>();
                value.put("ownerId", id);
                value.put("nickname", fine.getGroupMember().getUser().getNickname());
                value.put("unpaidCount", 0);
                value.put("unpaidAmount", 0);
                value.put("paidCount", 0);
                value.put("items", new java.util.ArrayList<Map<String, Object>>());
                return value;
            });
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) summary.get("items");
            items.add(fineMap(fine));
            if (fine.isUnpaid()) {
                summary.put("unpaidCount", (Integer) summary.get("unpaidCount") + 1);
                summary.put("unpaidAmount", (Integer) summary.get("unpaidAmount") + fine.getAmount());
            } else {
                summary.put("paidCount", (Integer) summary.get("paidCount") + 1);
            }
        }
        result.put("memberSummaries", memberSummaries.values());
        return result;
    }

    @PostMapping("/fines/{fineId}/pay")
    public Map<String, Object> payFine(@PathVariable Integer fineId, @RequestBody MemoRequest request,
                                       HttpSession session) {
        User current = currentUser(session);
        fineService.payFine(fineId, current.getId(), request.memo());
        User updated = userService.findById(current.getId());
        session.setAttribute("sessionUser", updated);
        return userMap(updated);
    }

    private User currentUser(HttpSession session) {
        Object value = session.getAttribute("sessionUser");
        if (!(value instanceof User user)) throw new Exception401("로그인이 필요합니다.");
        return userService.findById(user.getId());
    }

    private void requireMember(Integer groupId, HttpSession session) {
        User user = currentUser(session);
        boolean member = groupService.getMyGroups(user.getId()).stream().anyMatch(g -> g.getId().equals(groupId));
        if (!member) throw new Exception401("그룹 접근 권한이 없습니다.");
    }

    private Map<String, Object> userMap(User user) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", user.getId());
        map.put("email", user.getEmail());
        map.put("nickname", user.getNickname());
        map.put("phoneNumber", user.getPhoneNumber());
        map.put("point", user.getPoint() == null ? 0 : user.getPoint());
        map.put("provider", user.getProvider() == null ? "LOCAL" : user.getProvider().name());
        return map;
    }

    private Map<String, Object> todoMap(Todo todo) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", todo.getId());
        map.put("work", todo.getWork());
        map.put("status", todo.isExpired() ? "EXPIRED" : todo.getStatus().name());
        map.put("deadline", todo.getDeadline() == null ? null : todo.getDeadline().toLocalDateTime().toString());
        map.put("ownerId", todo.getGroupMember().getUser().getId());
        map.put("ownerNickname", todo.getGroupMember().getUser().getNickname());
        map.put("fineId", todo.getFine() == null ? null : todo.getFine().getId());
        map.put("fineAmount", todo.getFine() == null ? 0 : todo.getFine().getAmount());
        return map;
    }

    private Map<String, Object> fineMap(Fine fine) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", fine.getId());
        map.put("amount", fine.getAmount());
        map.put("status", fine.getStatus().name());
        map.put("memo", fine.getMemo());
        map.put("ownerId", fine.getGroupMember().getUser().getId());
        map.put("ownerNickname", fine.getGroupMember().getUser().getNickname());
        map.put("work", fine.getTodo() == null ? "탈퇴 회원 벌금" : fine.getTodo().getWork());
        return map;
    }

    public record ProfileRequest(String nickname, String currentPassword, String newPassword,
                                 String newPasswordConfirm) {}
    public record PointRequest(Integer amount) {}
    public record MemoRequest(String memo) {}
    public record WithdrawRequest(String password) {}
}
