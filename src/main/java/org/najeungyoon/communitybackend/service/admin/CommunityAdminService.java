package org.najeungyoon.communitybackend.service.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.najeungyoon.communitybackend.domain.app.Board;
import org.najeungyoon.communitybackend.domain.app.Comment;
import org.najeungyoon.communitybackend.domain.app.NotificationType;
import org.najeungyoon.communitybackend.domain.app.user.User;
import org.najeungyoon.communitybackend.domain.app.user.UserCommunitySuspension;
import org.najeungyoon.communitybackend.domain.app.user.UserWarning;
import org.najeungyoon.communitybackend.event.NotificationEventPublisher;
import org.najeungyoon.communitybackend.repository.BoardRepository;
import org.najeungyoon.communitybackend.repository.CommentRepository;
import org.najeungyoon.communitybackend.repository.user.UserCommunitySuspensionRepository;
import org.najeungyoon.communitybackend.repository.user.UserRepository;
import org.najeungyoon.communitybackend.repository.user.UserWarningRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityAdminService {

    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final UserWarningRepository userWarningRepository;
    private final UserCommunitySuspensionRepository userCommunitySuspensionRepository;
    private final BoardRepository boardRepository;

    /* 이벤트발행 */
    private final NotificationEventPublisher notificationEventPublisher;

    /* 어드민 권한 댓글 삭제 */
    @Transactional
    public void deleteComment(Long adminId, String reason, Long commentId,
                               String urlPath, Long postId){

        Comment targetComment = commentRepository.findById(commentId)
                .orElseThrow(()-> new IllegalArgumentException("해당 ID의 댓글을 찾을 수 없습니다. "));

        User admin = userRepository.findById(adminId).orElseThrow(()-> new IllegalArgumentException("관리자가 아닙니다."));
        String targetUrl = "/board/" + urlPath + "/post/" + postId;

        /* 삭제당한 유저에게 알림 전송 */
        User author = targetComment.getUser();

        if(author!=null){
            notificationEventPublisher.publishEvent(author, admin, NotificationType.DELETED_COMMENT,
                    "관리자가 댓글을 삭제했습니다. \n 사유: "+reason,targetUrl);
        }

        commentRepository.delete(targetComment);
        log.info("관리자(ID: {})가 직권으로 댓글(ID: {})을 삭제함. 사유: {}", adminId, commentId, reason);

    }


    /* 어드민 권한 유저 경고 처리 */
    @Transactional
    public void giveWarning(Long adminId, Long userId, String urlPath, String reason){


        User admin = userRepository.findById(adminId)
                .orElseThrow(()->new IllegalArgumentException("관리자가 아닙니다."));

        User targetUser = userRepository.findById(userId)
                .orElseThrow(()-> new IllegalArgumentException("유저를 찾을 수 없습니다. "));

        Board board = boardRepository.findByUrlPath(urlPath)
                .orElseThrow(()-> new IllegalArgumentException("커뮤니티를 찾을 수 없습니다. "));

        /* ================================================================================
        * ================================================================================ */

        /* 경고내역 엔티티 생성 후 DB에 저장 */
        UserWarning userWarning = new UserWarning(targetUser,board,reason,adminId);
        userWarningRepository.save(userWarning);


        /* 정지 정보 조회 */
        UserCommunitySuspension userCommunitySuspension = userCommunitySuspensionRepository
                .findByUserAndBoard(targetUser,board)
                .orElseGet(()->new UserCommunitySuspension(targetUser,board));



        LocalDateTime lastSuspendedAt = userCommunitySuspension.getLastSuspendedAt(); // 마지막으로 정지된 시간.
        long warningCount;

        /* 아직 정지되지 않았으면 */
        if(lastSuspendedAt==null){
            warningCount=userWarningRepository.countByUserAndBoard(targetUser,board);
        }
        /* 정지이력이 있으면 */
        else {
            warningCount=userWarningRepository
                    .countByUserAndBoardAndCreatedAtAfter(targetUser,board,lastSuspendedAt);
        }

        /* 경고 누적 3회이상이면 7일 정지 */
        if(warningCount>=3){
            userCommunitySuspension.updateSuspension(7);
            userCommunitySuspensionRepository.save(userCommunitySuspension);
            log.info("{}가 7일동안 정지됨",targetUser.getNickname());
        }

        /* 알림 전송 */
        notificationEventPublisher.publishEvent(
                targetUser, admin, NotificationType.WARNING,
                "경고를 받았습니다. 사유: " + reason + " (현재 누적: " + warningCount + "회)", null
        );
    }






}
