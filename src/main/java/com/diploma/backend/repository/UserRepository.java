package com.diploma.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.diploma.backend.entity.User;
import java.util.*;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
