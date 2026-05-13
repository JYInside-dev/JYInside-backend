package org.najeungyoon.communitybackend.service.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.najeungyoon.communitybackend.domain.app.Comment;
import org.najeungyoon.communitybackend.domain.app.Notification;
import org.najeungyoon.communitybackend.domain.app.user.User;
import org.najeungyoon.communitybackend.event.NotificationEventPublisher;
import org.najeungyoon.communitybackend.repository.CommentRepository;
import org.najeungyoon.communitybackend.repository.UserRepository;
import org.najeungyoon.communitybackend.repository.UserWarningRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityAdminService {

    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

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
            notificationEventPublisher.publishEvent(author, admin, Notification.NotificationType.DELETED_COMMENT,
                    "관리자가 댓글을 삭제했습니다.",targetUrl);
        }

        commentRepository.delete(targetComment);
        log.info("관리자(ID: {})가 직권으로 댓글(ID: {})을 삭제함. 사유: {}", adminId, commentId, reason);

    }

}
