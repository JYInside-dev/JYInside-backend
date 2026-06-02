package org.najeungyoon.communitybackend.aop.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/* 이 어노테이션이 붙은 메서드는 kafka 실시간 인기글 파이프라인으로 전송
*   boardId와 postId를 추출해서 전송함 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TrackView {
}
