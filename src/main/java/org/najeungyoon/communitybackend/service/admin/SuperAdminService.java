package org.najeungyoon.communitybackend.service.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.najeungyoon.communitybackend.domain.app.Board;
import org.najeungyoon.communitybackend.domain.app.BoardAdmin;
import org.najeungyoon.communitybackend.domain.app.user.User;
import org.najeungyoon.communitybackend.domain.app.user.UserRole;
import org.najeungyoon.communitybackend.model.board.BoardCreateRequestDTO;
import org.najeungyoon.communitybackend.model.user.BoardAdminUserResponseDTO;
import org.najeungyoon.communitybackend.repository.BoardAdminRepository;
import org.najeungyoon.communitybackend.repository.BoardRepository;
import org.najeungyoon.communitybackend.repository.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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


    /* 관리자 유저 리스트 불러오기 */
    @Transactional(readOnly = true)
    public List<BoardAdminUserResponseDTO> getAllAdmins(){
        return boardAdminRepository.findAllWithUserAndBoard().stream().map(BoardAdminUserResponseDTO::from).toList();
    }


    /* 관리자 권한 박탈 */
    @Transactional
    public void revokeAdminRole(Long userId, String urlPath){

        // 1. BoardAdmin 매핑 데이터 조회 및 삭제
        BoardAdmin boardAdmin = boardAdminRepository.findByUser_IdAndBoard_UrlPath(userId, urlPath)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시판의 관리자 권한을 찾을 수 없습니다."));

        boardAdminRepository.delete(boardAdmin);

        // 2. 삭제 후, 이 유저가 관리하는 다른 게시판이 남아있는지 확인
        long remainingAdminCount = boardAdminRepository.countByUser_Id(userId);

        // 3. 더 이상 관리하는 게시판이 없다면 일반 유저로 권한 강등
        if (remainingAdminCount == 0) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

            user.changeRole(UserRole.USER); // UserRole 갱신
        }

    }



}
