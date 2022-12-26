package com.huyeon.superspace.domain.group.dto;

import com.huyeon.superspace.domain.group.entity.WorkGroup;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GroupDto {
    private String name;
    private String url;
    private String description;

    @Builder
    public GroupDto(WorkGroup group) {
        name = group.getName();
        url = group.getUrlPath();
        description = group.getDescription();
    }
}
