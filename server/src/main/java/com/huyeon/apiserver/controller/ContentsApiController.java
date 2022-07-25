package com.huyeon.apiserver.controller;

import com.huyeon.apiserver.model.UserDetailsImpl;
import com.huyeon.apiserver.model.dto.ContentsReq;
import com.huyeon.apiserver.model.entity.Board;
import com.huyeon.apiserver.model.entity.ContentBlock;
import com.huyeon.apiserver.service.BoardService;
import com.huyeon.apiserver.service.ContentBlockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/contents")
@RequiredArgsConstructor
public class ContentsApiController {
    private final BoardService boardService;
    private final ContentBlockService blockService;

    @PutMapping("/{boardId}")
    public ResponseEntity<?> editContents(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long boardId, @RequestBody ContentsReq request) {

        //boardId 게시글이 해당 유저의 것이 맞는지 확인
        if (!boardService.getBoard(boardId)
                .orElse(new Board())
                .getUserEmail()
                .equals(userDetails.getUsername())) {
            return new ResponseEntity<>("접근할 수 없는 게시글입니다.", HttpStatus.OK);
        }
        //맞다면 request로 내용 덮어쓰기
        List<ContentBlock> contents = request.getContents().stream()
                .map(content -> ContentBlock.builder().content(content).build())
                .collect(Collectors.toList());
        blockService.writeContents(boardId, contents);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
