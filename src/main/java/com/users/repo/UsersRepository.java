package com.users.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.users.data.Users;

public interface UsersRepository extends JpaRepository<Users, Long> {

    Optional<Users> findByEmail(String email);
}
