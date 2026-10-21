package com.linkedin.userservice.controller;


import com.linkedin.userservice.dto.UserResponse;
import com.linkedin.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@Slf4j
@RequiredArgsConstructor
public class UserController {


    private final UserService userService;

    /**
     * get user profile
     * X-user-ID requesting user (from gateway)
     * userid in path = target user to fetch
     * @return
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserProfile(
            @PathVariable String userId,
            @RequestHeader("X-User-Id") String requestingUserId
    ) {
        log.info("getUserProfile: {} requestingUserId: {}", userId, requestingUserId);

        return ResponseEntity.ok(userService.getUserProfile(userId));

    }


    /**
     * Update own profile
     * user can only update own profile
     * @param userId
     * @param requestingUserId
     * @return
     */


    @PutMapping("/{userId}/profile")
    public ResponseEntity<UserResponse> updateProfile(
            @PathVariable String userId,
            @RequestHeader("X-User-Id") String requestingUserId,
            @RequestBody UserResponse request


    )
    {
        if(!userId.equals(requestingUserId))
        {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(userService.updateProfile(userId ,request));
    }

    @PostMapping("/{userId}/profile-photo")
    public ResponseEntity<UserResponse> uploadProfilePhoto(
            @PathVariable String userId,
            @RequestHeader("X-User-Id") String requestingUserId,
            @RequestParam("file") MultipartFile file)
    {
        if(!userId.equals(requestingUserId))
        {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(userService.uploadProfilePhoto(userId , file));

    }

    /**
     * send connection request
     * requestor id come from header
     * @param targetUserId
     * @param requestingUserId
     * @return
     */
    @PostMapping("/{targetUserId}/connect")
    public ResponseEntity<String> sendConnectionRequest(
            @PathVariable String targetUserId,
            @RequestHeader("X-User-Id") String requestingUserId
    )
    {
        return ResponseEntity.ok(userService.sendConnectionRequest(targetUserId,requestingUserId));

    }

    @PutMapping("/connection/{connectionId}/accept")
    public ResponseEntity<String> accecptConnection(
           @PathVariable String connectionId,
           @RequestHeader("X-User-Id") String requestingUserId
    )
    {
        return ResponseEntity.ok(userService.acceptConnectionRequest(connectionId));

    }


    @GetMapping("/{userId}/connections")
    public ResponseEntity<List<UserResponse>> getConnections(
            @PathVariable String userId

    )
    {
        return ResponseEntity.ok(userService.getConnections(userId));
    }










}
