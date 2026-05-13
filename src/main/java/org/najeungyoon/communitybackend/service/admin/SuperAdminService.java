package org.najeungyoon.communitybackend.service.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.najeungyoon.communitybackend.domain.app.Board;
import org.najeungyoon.communitybackend.domain.app.BoardAdmin;
import org.najeungyoon.communitybackend.domain.app.user.User;
import org.najeungyoon.communitybackend.domain.app.user.UserRole;
import org.najeungyoon.communitybackend.model.board.BoardCreateRequestDTO;
import org.najeungyoon.communitybackend.repository.BoardAdminRepository;
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
    private final BoardAdminRepository boardAdminRepository;


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

    /* 유저에게 커뮤니티 관리자 권한 부여*/
    @Transactional
    public void grantCommunityAdmin(Long userId, String boardUrlPath){

        /* 예외처리 */
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
        Board board = boardRepository.findByUrlPath(boardUrlPath)
                .orElseThrow(()->new IllegalArgumentException("존재하지 않는 커뮤니티입니다."));

        boolean isAlreadyAdmin = boardAdminRepository.existsByUserAndBoard(user,board);

        if(isAlreadyAdmin){
            throw new IllegalArgumentException("이미 커뮤니티 관리자 입니다.");
        }

        /* 권한 업데이트 로직 */
        if(user.getRole()== UserRole.USER){
           user.changeRole(UserRole.COMMUNITY_ADMIN);
        }

        BoardAdmin boardAdmin = new BoardAdmin(user,board);
        boardAdminRepository.save(boardAdmin);
        log.info("{}가 {}커뮤니티 관리자로 임명 되었습니다.",user.getNickname(),board.getName());

    }

}
