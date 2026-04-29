package org.najeungyoon.communitybackend.service.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.najeungyoon.communitybackend.domain.Board;
import org.najeungyoon.communitybackend.model.admin.BoardCreateRequestDTO;
import org.najeungyoon.communitybackend.repository.BoardRepository;
import org.najeungyoon.communitybackend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class SuperAdminService {

    private final UserRepository userRepository;
    private final BoardRepository boardRepository;


    /* 새 커뮤니티(보드) 생성 */
    @Transactional
    public void createBoard(BoardCreateRequestDTO dto,Long adminId){

        /* urlPath 중복 검사 */
        if(boardRepository.existsByUrlPath(dto.urlPath())){
            throw new IllegalArgumentException("이미 존재하는 urlPath 입니다.");
        }

        log.info("[System Audit] SUPER_ADMIN(ID: {})가 새 커뮤니티 '{}' (경로: {})를 생성했습니다.",
                adminId, dto.name(), dto.urlPath());

        Board board = dto.toEntity();
        boardRepository.save(board);
    }

}
