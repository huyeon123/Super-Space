package com.huyeon.apiserver.service;

import com.huyeon.apiserver.exception.NotOnlyMemberException;
import com.huyeon.apiserver.model.MyEvent;
import com.huyeon.apiserver.model.dto.GroupDto;
import com.huyeon.apiserver.model.dto.NotyEventDto;
import com.huyeon.apiserver.model.entity.*;
import com.huyeon.apiserver.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class GroupService {
    private static final int IDENTIFIER_LENGTH = 10;

    private final UserGroupRepository userGroupRepository;
    private final GroupRepository groupRepository;
    private final GroupManagerRepository managerRepository;
    private final CategoryRepository categoryRepository;
    private final BoardRepository boardRepository;
    private final ApplicationEventPublisher eventPublisher;

    public GroupDto createGroup(User user, GroupDto request) {
        WorkGroup group = makeGroup(user, request);

        registerUser(user, group);

        registerUserAsManager(user, group);

        createRootCategory(group);

        return mapToDto(group);
    }

    private WorkGroup makeGroup(User user, GroupDto request) {
        WorkGroup group = WorkGroup.builder()
                .name(request.getName())
                .description(request.getDescription())
                .owner(user)
                .build();

        String urlPath = request.getUrl();

        if (exist(urlPath)) {
            urlPath = appendIdentifier(urlPath);
        }

        group.setUrlPath(urlPath);

        return groupRepository.save(group);
    }

    private void registerUser(User user, WorkGroup group) {
        UserGroup userGroup = new UserGroup(user, group);
        userGroupRepository.save(userGroup);
    }

    private void registerUserAsManager(User user, WorkGroup group) {
        GroupManager groupManager = new GroupManager(user, group);
        managerRepository.save(groupManager);
    }

    private void createRootCategory(WorkGroup group) {
        Category root = Category.builder()
                .name("==최상위 카테고리==")
                .parent(null)
                .group(group)
                .build();

        categoryRepository.save(root);
    }

    private GroupDto mapToDto(WorkGroup group) {
        return GroupDto.builder()
                .name(group.getName())
                .url(group.getUrlPath())
                .build();
    }

    public void editGroup(String groupUrl, GroupDto request) {
        WorkGroup group = getGroupByUrl(groupUrl);
        group.setName(request.getName());
        group.setDescription(request.getDescription());
        groupRepository.save(group);
    }

    public WorkGroup getGroupByUrl(String urlPath) {
        return groupRepository.findByUrlPath(urlPath).orElseThrow();
    }

    public String getGroupNameByUrl(String urlPath) {
        return groupRepository.findByUrlPath(urlPath).orElseThrow().getName();
    }

    public List<WorkGroup> getGroups(User user) {
        List<UserGroup> userGroups = userGroupRepository.findAllByUser(user);
        return userGroups.stream().map(UserGroup::getGroup).collect(Collectors.toList());
    }

    public List<User> getUsers(WorkGroup group) {
        List<UserGroup> userGroups = userGroupRepository.findAllByGroup(group);
        return userGroups.stream().map(UserGroup::getUser).collect(Collectors.toList());
    }

    private boolean exist(String urlPath) {
        return groupRepository.existsByUrlPath(urlPath);
    }

    private String appendIdentifier(String urlPath) {
        StringBuilder sb = new StringBuilder(urlPath);
        sb.append("-");
        sb.append(generateIdentifier());
        return sb.toString();
    }

    private String generateIdentifier() {
        int leftLimit = '0'; // numeral '0'
        int rightLimit = 'z'; // letter 'z'
        Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(IDENTIFIER_LENGTH)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    public void deleteGroup(User user, String groupUrl) {
        WorkGroup group = getGroupByUrl(groupUrl);

        if (notOnlyMeInGroup(group)) {
            throw new NotOnlyMemberException("다른 멤버가 존재합니다.");
        }

        if (isNotGroupOwner(user, group)) {
            throw new AccessDeniedException("그룹 소유자가 아닙니다.");
        }

        List<GroupManager> groupManager = managerRepository.findAllByGroup(group);

        categoryRepository.deleteAllByGroup(group);
        userGroupRepository.deleteAllByGroup(group);
        managerRepository.deleteAll(groupManager);
        groupRepository.delete(group);
    }

    private boolean notOnlyMeInGroup(WorkGroup group) {
        List<UserGroup> userGroups = userGroupRepository.findAllByGroup(group);
        return userGroups.size() > 1;
    }

    private boolean isNotGroupOwner(User user, WorkGroup group) {
        return !group.getOwner().equals(user);
    }

    public String checkRole(WorkGroup group, User user) {
        if (isOwner(group, user)) {
            return "그룹 소유자";
        }
        if (isManager(group, user)) {
            return "그룹 관리자";
        }

        return "일반 멤버";
    }

    private boolean isOwner(WorkGroup group, User user) {
        return group.getOwner().equals(user);
    }

    private boolean isManager(WorkGroup group, User user) {
        return managerRepository.existsByGroupAndManager(group, user);
    }

    public void inviteMember(String groupUrl, String userEmail) {
        WorkGroup group = getGroupByUrl(groupUrl);

        Noty inviteNoty = Noty.builder()
                .senderName(group.getName())
                .message("그룹에 초대합니다!\n" + group.getDescription())
                .type(NotyType.GROUP_INVITE)
                .redirectUrl(group.getUrlPath())
                .build();

        ReceivedNoty receivedNoty = ReceivedNoty.builder()
                .userEmail(userEmail)
                .noty(inviteNoty)
                .build();

        NotyEventDto notyEventDto = NotyEventDto.builder()
                .noty(inviteNoty)
                .receivers(List.of(receivedNoty))
                .build();

        MyEvent.publishEvent(eventPublisher, notyEventDto);
    }

    public void joinMember(User user, String groupUrl) {
        WorkGroup group = getGroupByUrl(groupUrl);

        UserGroup userGroup = UserGroup.builder()
                .user(user)
                .group(group)
                .build();

        userGroupRepository.save(userGroup);
    }
}
