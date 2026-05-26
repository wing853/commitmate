package com.example.commitmate.group;

import com.example.commitmate.todo.Todo;
import com.example.commitmate.todo.TodoService;
import com.example.commitmate.user.User;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class GroupController {

    private final GroupService gs;
    private final TodoService ts;

    @GetMapping("/groups")
    public String groupListPage(Model model, HttpSession session) {
        User sessionUser = (User) session.getAttribute("sessionUser");

        List<GroupResponse.MyGroupDTO> groups = gs.getMyGroups(sessionUser.getId());
        model.addAttribute("groups", groups);
        model.addAttribute("sessionUser", sessionUser);

        return "group/group-list";
    }

    @GetMapping("/groups/save-form")
    public String groupCreateFormPage() {

        return "group/save-form";
    }

    @PostMapping("/groups/save")
    public String groupCreateProc(Model model, HttpSession session, GroupRequest.CreateDTO createDTO) {

        User userEntity = (User) session.getAttribute("sessionUser");
        createDTO.validate();
        gs.createRoom(userEntity, createDTO);

        return "redirect:/groups";
    }

    @GetMapping("/groups/{id}/update-form")
    public String groupUpdatePageForm(@PathVariable("id") Integer id,
                                      Model model) {

        GroupResponse.detailDTO group = gs.findGroupById(id);
        model.addAttribute("groups",group);

        return "group/update-form";
    }

    @PostMapping("/groups/{id}/update")
    public String groupUpdateProc(@PathVariable("id") Integer id,
                                  GroupRequest.UpdateDTO updateDTO) {

        updateDTO.validate();
        gs.updateGroup(id,updateDTO);
        return "redirect:/groups";
    }

    @PostMapping("/groups/{id}/delete")
    public String groupDeleteProc(@PathVariable("id") Integer id,
                                  HttpSession session) {
        User sessionUser = (User) session.getAttribute("sessionUser");
        gs.deleteGroup(id,sessionUser.getId());
        return "redirect:/groups";
    }

    @GetMapping("/groups/{id}")
    public String todoList(@PathVariable("id") Integer id,
                           Model model) {
        List<Todo> todoList = ts.showTodo(id);
        model.addAttribute("todoList",todoList);

        return "redirect:/groups/" + id + "/todos";
    }

    // 초대링크로 참여
    @GetMapping("/invite/{code}")
    public String joinByInviteCode(@PathVariable("code") String code, HttpSession session,
                                   GroupRequest.JoinDTO joinDTO) {
        User sessionUser = (User) session.getAttribute("sessionUser");
        gs.joinByInviteCode(code, sessionUser);
        return "redirect:/groups";
    }

    // 가입코드로 참여
    @PostMapping("/groups/join")
    public String joinByJoinCode(@ModelAttribute GroupRequest.JoinDTO joinDTO,
                                 HttpSession session) {
        User sessionUser = (User) session.getAttribute("sessionUser");
        gs.joinByJoinCode(joinDTO.getJoinCode(), sessionUser);
        return "redirect:/groups";
    }

    // 초대 정보 조회 (todo-list 모달용)
    @GetMapping("/groups/{id}/invite-info")
    @ResponseBody
    public GroupResponse.InviteInfoDTO getInviteInfo(@PathVariable("id") Integer id) {
        return gs.getInviteInfo(id);
    }

    @GetMapping("/groups/{id}/members")
    @ResponseBody
    public List<GroupResponse.MemberDTO> getMembers(@PathVariable("id") Integer id) {
        return gs.getMembers(id);
    }

    @PostMapping("/groups/{groupId}/members/{groupMemberId}/kick")
    @ResponseBody
    public ResponseEntity<Void> kickMember(@PathVariable Integer groupId,
                                           @PathVariable Integer groupMemberId,
                                           HttpSession session) {
        User sessionUser = (User) session.getAttribute("sessionUser");
        gs.kickMember(groupId, groupMemberId, sessionUser.getId());
        return ResponseEntity.ok().build();
    }

    // 그룹 나가기
    @PostMapping("/groups/{groupId}/leave")
    @ResponseBody
    public ResponseEntity<Void> leaveGroup(@PathVariable Integer groupId,
                                           HttpSession session) {
        User sessionUser = (User) session.getAttribute("sessionUser");
        gs.leaveGroup(groupId, sessionUser.getId());
        return ResponseEntity.ok().build();
    }

    // 총무 위임
    @PostMapping
    @ResponseBody
    public ResponseEntity<Void> delegateAdmin(@PathVariable Integer groupId,
                                              @PathVariable Integer groupMemberId,
                                              HttpSession session) {
        User sessionUser = (User) session.getAttribute("sessionUser");
        gs.delegateAdmin(groupId, groupMemberId, sessionUser.getId());
        return ResponseEntity.ok().build();
    }
}
