//package org.najeungyoon.communitybackend.utils;
//
//
//import jakarta.servlet.http.HttpServletRequest;
//import org.springframework.util.StringUtils;
//
//import java.util.ArrayList;
//import java.util.List;
//
///* 로그인 안 했을 때 ip로 추천주작 방지 */
//public class ClientIpUtil {
//
//    /* nginx 나 로드밸런서 등을 사용해서 ip주소를 찾기 위해
//    * 여러 종류의 프록시 헤더들 뺑뺑이 돌려서 확인 */
//    public static String getClientIp(HttpServletRequest request){
//        String clientIp = null;
//        boolean isIpInHeader = false;
//
//        List<String> headerList = new ArrayList<>();
//        headerList.add("X-Forwarded-For");
//        headerList.add("HTTP_CLIENT_IP");
//        headerList.add("HTTP_X_FORWARDED_FOR");
//        headerList.add("HTTP_X_FORWARDED");
//        headerList.add("HTTP_FORWARDED_FOR");
//        headerList.add("HTTP_FORWARDED");
//        headerList.add("Proxy-Client-IP");
//        headerList.add("WL-Proxy-Client-IP");
//        headerList.add("HTTP_VIA");
//        headerList.add("IPV6_ADR");
//
//        for (String header : headerList) {
//            clientIp = request.getHeader(header);
//            if (StringUtils.hasText(clientIp) && !"unknown".equalsIgnoreCase(clientIp)) {
//                isIpInHeader = true;
//                break;
//            }
//        }
//
//        if (!isIpInHeader) {
//            clientIp = request.getRemoteAddr();
//        }
//
//        return clientIp;
//    }
//}
