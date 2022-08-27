package com.huyeon.apiserver.model.dto;

import com.huyeon.apiserver.model.entity.BoardStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkSpaceDto {
    private String title;
    private BoardStatus status;
    private String categoryName;
    private List<String> comments;
    private List<String> contents;
}
