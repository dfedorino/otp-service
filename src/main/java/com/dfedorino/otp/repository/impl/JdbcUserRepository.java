package com.dfedorino.otp.repository.impl;

import com.dfedorino.otp.domain.enums.Role;
import com.dfedorino.otp.domain.model.User;
import com.dfedorino.otp.repository.UserRepository;
import com.dfedorino.otp.repository.utils.Queries;
import com.dfedorino.otp.repository.utils.ResultSetMapper;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JdbcUserRepository implements UserRepository {

    private static final String INSERT_USER = "INSERT INTO users (login, password, role) VALUES (?, ?, ?)";
    private static final String SELECT_BY_LOGIN = "SELECT id, login, password, role FROM users WHERE login = ?";
    private static final String SELECT_BY_ID = "SELECT id, login, password, role FROM users WHERE id = ?";
    private static final String DELETE_BY_ID = "DELETE FROM users WHERE id = ?";

    private static final ResultSetMapper<User> USER_RESULT_SET_MAPPER = rs -> new User(
        rs.getLong("id"),
        rs.getString("login"),
        rs.getString("password"),
        Role.valueOf(rs.getString("role"))
    );

    @Override
    public boolean save(String login, String hashedPassword, Role role) {
        log.debug("Creating user with login: {}, hashedPassword: {}, role: {}",
            login, hashedPassword, role);
        return Queries.update(INSERT_USER, login, hashedPassword, role.name()) > 0;
    }

    @Override
    public Optional<User> findByLogin(String login) {
        log.debug("Looking for user with login: {}", login);
        return Queries.query(SELECT_BY_LOGIN, USER_RESULT_SET_MAPPER, login).stream().findAny();
    }

    @Override
    public Optional<User> findById(long id) {
        log.debug("Looking for user with id: {}", id);
        return Queries.query(SELECT_BY_ID, USER_RESULT_SET_MAPPER, id).stream().findAny();
    }

    @Override
    public boolean deleteById(long id) {
        log.debug("Delete user by id: {}", id);
        return Queries.update(DELETE_BY_ID, id) > 0;
    }
}