package com.transport.api.user.repository;

import com.transport.api.user.entity.User;
import com.transport.api.user.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByEmailAndCompanyId(String email, Long companyId);

    Optional<User> findByIdAndCompanyId(Long id, Long companyId);

    List<User> findByRole(Role role);
}