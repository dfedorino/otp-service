package com.dfedorino.otp.controller.auth.context;

import com.dfedorino.otp.domain.enums.Role;

public class SecurityContext {

    private static final ThreadLocal<String> USER = new ThreadLocal<>();
    private static final ThreadLocal<Role> ROLE = new ThreadLocal<>();

    public static void set(String user, Role role) {
        USER.set(user);
        ROLE.set(role);
    }

    public static String getUser() {
        return USER.get();
    }

    public static Role getRole() {
        return ROLE.get();
    }

    public static boolean hasRole(Role role) {
        return ROLE.get() != null && ROLE.get() == role;
    }

    public static void clear() {
        USER.remove();
        ROLE.remove();
    }
}
