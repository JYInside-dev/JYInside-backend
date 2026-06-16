package org.najeungyoon.communitybackend.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/community")
@Slf4j
public class HealthCheckController {


    @GetMapping("/health")
    public ResponseEntity<String> healthCheck(){

        log.info("헬스체크 시작");
        return ResponseEntity.ok("OK");
    }

}
