package com.revproxy.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("**")
@RequiredArgsConstructor
@Slf4j
public class RevProxyController {
    
    @GetMapping
    public ResponseEntity<Object> get(@NonNull HttpServletRequest request) {
        String message = "Hello, World!";
        return new ResponseEntity<>(message, HttpStatus.OK);
    }

}
