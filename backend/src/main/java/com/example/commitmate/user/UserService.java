package com.example.commitmate.user;


import com.example.commitmate.core.errors.ExceptionFine;
import com.example.commitmate.core.errors.ExceptionInput;
import com.example.commitmate.core.errors.ExceptionNoInfo;
import com.example.commitmate.fine.Fine;
import com.example.commitmate.fine.FineRepository;
import com.example.commitmate.fine.FineStatus;
import com.example.commitmate.groupmember.GroupMember;
import com.example.commitmate.groupmember.GroupMemberRepository;
import com.example.commitmate.groupmember.GroupRole;
import com.example.commitmate.todo.Todo;
import com.example.commitmate.todo.TodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository ur;
    private final PasswordEncoder passwordEncoder;
    private final PhoneVerificationService phoneVerificationService;
    private final GroupMemberRepository gmr;
    private final TodoRepository tr;
    private final FineRepository fr;

    public User findById(Integer id) {
        return ur.findById(id).orElseThrow(
                () -> new ExceptionNoInfo("사용자를 찾을 수 없습니다.")
        );
    }

    public User login(UserRequest.LoginDTO loginDTO) {
        User userEntity = ur.findByEmail(loginDTO.getEmail()).orElseThrow(
                () -> new ExceptionInput("이메일 혹은 비밀번호를 잘못 입력했습니다.")
        );

        if(!passwordEncoder.matches(loginDTO.getPassword(),userEntity.getPassword())){
            throw new ExceptionInput("이메일 혹은 비밀번호를 잘못 입력했습니다.");
        }

        userEntity.setProvider(AuthProvider.LOCAL);
        return userEntity;
    }

    public User findOrCreateSocialUser(AuthProvider provider, String providerId,
                                       String email, String nickname) {
        String randomPassword = passwordEncoder.encode(UUID.randomUUID().toString());
        return ur.findByProviderAndProviderId(provider, providerId)
                .orElseGet(() -> ur.save(User.builder()
                        .email(email)
                        .nickname(nickname)
                        .password(randomPassword)
                        .provider(provider)
                        .providerId(providerId)
                        .build()));
    }

    @Transactional
    public User signup(UserRequest.SignupDTO signupDTO) {
        User userEntity = signupDTO.toEntity();

        if(ur.findByEmail(signupDTO.getEmail()).isPresent()) {
            throw new ExceptionInput("이미 사용 중인 이메일입니다.");
        }

        if(ur.findByNickname(signupDTO.getNickname()).isPresent()) {
            throw new ExceptionInput("이미 사용 중인 닉네임입니다.");
        }

        String phoneNumber = signupDTO.getPhoneNumber().replaceAll("[^0-9]", "");
        if(ur.findByPhoneNumber(phoneNumber).isPresent()) {
            throw new ExceptionInput("이미 가입된 휴대폰 번호입니다.");
        }

        phoneVerificationService.consumeVerification(phoneNumber, signupDTO.getPhoneVerificationToken());

        userEntity.setProvider(AuthProvider.LOCAL);
        userEntity.setPassword(passwordEncoder.encode(signupDTO.getPassword()));
        return ur.save(userEntity);
    }

    // 유저 정보 수정
    @Transactional
    public void updateProfile(Integer userId, String nickname,
                              String currentPassword, String newPassword, String newPasswordConfirm) {
        User user = ur.findById(userId).orElseThrow(
                () -> new ExceptionNoInfo("사용자를 찾을 수 없습니다.")
        );

        // 닉네임 변경
        if (nickname != null && !nickname.trim().isEmpty()) {
            if (ur.findByNickname(nickname).isPresent()) {
                throw new ExceptionInput("이미 사용 중인 닉네임입니다.");
            }
            user.setNickname(nickname);
        }

        // 비밀번호 변경 (새 비밀번호 입력한 경우만)
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            if (currentPassword == null || currentPassword.trim().isEmpty()) {
                throw new ExceptionInput("현재 비밀번호를 입력하세요.");
            }
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                throw new ExceptionInput("현재 비밀번호가 일치하지 않습니다.");
            }
            if (!newPassword.equals(newPasswordConfirm)) {
                throw new ExceptionInput("새 비밀번호가 일치하지 않습니다.");
            }
            user.setPassword(passwordEncoder.encode(newPassword));
        }
    }

    @Transactional
    public void chargePoint(Integer userId, Integer amount) {
        User user = ur.findById(userId).orElseThrow(
                () -> new ExceptionNoInfo("사용자를 찾을 수 없습니다.")
        );
        user.setPoint(user.getPoint() + amount);
    }

    @Transactional
    public void withdraw(Integer userId, String password) {
        User user = ur.findById(userId).orElseThrow(
                () -> new ExceptionNoInfo("사용자를 찾을 수 없습니다.")
        );

        if (user.isLocalUser()) {
            if (password == null || password.trim().isEmpty()
                    || !passwordEncoder.matches(password, user.getPassword())) {
                throw new ExceptionInput("비밀번호가 일치하지 않습니다.");
            }
        }

        List<GroupMember> memberships = gmr.findByUserIdAndIsActiveTrue(userId);
        for (GroupMember member : memberships) {
            if (member.getRole() == GroupRole.ADMIN) {
                throw new ExceptionInput("총무로 있는 그룹이 있습니다. 총무를 위임하거나 그룹을 삭제한 후 탈퇴해 주세요.");
            }

            boolean hasUnpaidFine = fr.findByGroupId(member.getGroup().getId()).stream()
                    .filter(f -> f.getGroupMember() != null && f.getGroupMember().getId().equals(member.getId()))
                    .filter(Fine::isExpired)
                    .anyMatch(f -> f.getStatus() == FineStatus.UNPAID);
            if (hasUnpaidFine) {
                throw new ExceptionFine("미납 벌금이 있어 탈퇴할 수 없습니다.");
            }
        }

        for (GroupMember member : memberships) {
            List<Todo> todos = tr.findByGroupMemberId(member.getId());
            todos.forEach(todo -> {
                if (todo.getFine() != null) {
                    todo.getFine().setTodo(null);
                    todo.setFine(null);
                }
            });
            tr.detachFineByGroupMemberId(member.getId());
            tr.deleteByGroupMemberId(member.getId());
            member.setActive(false);
        }

        user.setNickname("탈퇴한사용자" + user.getId());
        user.setEmail("withdrawn_" + user.getId() + "@commitmate.local");
        user.setPhoneNumber(null);
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setProviderId(null);
        user.setWithdrawnAt(new Timestamp(System.currentTimeMillis()));
    }

}
