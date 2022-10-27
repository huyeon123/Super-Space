package com.huyeon.superspace.domain.group.service;

import com.huyeon.superspace.domain.board.repository.CategoryRepository;
import com.huyeon.superspace.domain.group.entity.GroupManager;
import com.huyeon.superspace.domain.group.entity.WorkGroup;
import com.huyeon.superspace.domain.group.repository.GroupManagerRepository;
import com.huyeon.superspace.domain.group.repository.GroupRepository;
import com.huyeon.superspace.domain.noty.dto.EmitterAdaptor;
import com.huyeon.superspace.domain.noty.service.NotyService;
import com.huyeon.superspace.domain.user.entity.User;
import com.huyeon.superspace.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.transaction.Transactional;

@SpringBootTest
public class GroupServiceTest {
    @Autowired
    UserRepository userRepository;

    @Autowired
    GroupRepository groupRepository;

    @Autowired
    GroupService groupService;

    @Autowired
    GroupManagerRepository managerRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    NotyService notyService;

    String email = "test@test.com";


    @Test
    @Transactional
    @DisplayName("그룹 삭제")
    void deleteGroup() {
        User user = userRepository.findById(email).orElseThrow();
        WorkGroup workGroup = groupRepository.findByUrlPath("test-group").orElseThrow();
        managerRepository.save(
                GroupManager.builder()
                        .manager(user)
                        .group(workGroup)
                        .build()
        );
        groupService.deleteGroup(email, "test-group");
    }


    @Test
    @DisplayName("그룹 초대")
    void inviteMember() {
        //given
        String group = "test-group";
        EmitterAdaptor ea = EmitterAdaptor.builder()
                .userEmail(email)
                .lastEventId("")
                .build();
        String notyType = "DEFAULT";

        //when
        Assertions.assertDoesNotThrow(() -> groupService.inviteMember(group, ea.getUserEmail()));

        //then
        Assertions.assertDoesNotThrow(() -> notyService.subscribe(ea, notyType));
    }
}
