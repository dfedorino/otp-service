package com.dfedorino.otp.repository;

import com.dfedorino.otp.domain.enums.Role;
import com.dfedorino.otp.domain.model.User;
import java.util.List;
import java.util.Optional;

public interface UserRepository {

    boolean save(String login, String phoneNumber, String hashedPassword, Role role);

    Optional<User> findByLogin(String login);

    Optional<User> findById(long id);

    boolean deleteById(long id);

    boolean existsAdmin();

    List<User> findAll();
}
