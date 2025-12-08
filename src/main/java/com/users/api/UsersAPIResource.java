package com.users.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.users.data.Users;
import com.users.service.UserReadService;
import com.users.service.UserWriteService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "APIs for managing users")
public class UsersAPIResource {

    private final UserWriteService userWriteService;
    private final UserReadService userReadService;

    public UsersAPIResource(UserWriteService userWriteService, UserReadService userReadService) {
        this.userWriteService = userWriteService;
        this.userReadService = userReadService;
    }

    // create user
    @PostMapping("/create")
    @Operation(summary = "Create user", description = "Creates a new user.")
    @ApiResponse(responseCode = "200", description = "Successfully created user")
    public Users createUser(@RequestBody Users user) {
        return userWriteService.createUser(user);
    }

    // // update user
    // @PutMapping("/update")

    // // delete user
    // @PostMapping("/delete")
    // get user details
    @GetMapping("/{userId}")
    @Operation(summary = "Get user details", description = "Fetches user details by ID.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved user details")
    public Users getUserDetails(@PathVariable Long userId) {
        return userReadService.getUserById(userId);
    }
}
