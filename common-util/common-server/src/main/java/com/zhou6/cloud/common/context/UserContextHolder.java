package com.zhou6.cloud.common.context;

public final class UserContextHolder {

    private static final ThreadLocal<CurrentLoginUser> CURRENT_USER = new ThreadLocal<>();

    private UserContextHolder() {
    }

    public static void setUserId(Long userId) {
        CURRENT_USER.set(new CurrentLoginUser(userId, null, null, null, null));
    }

    public static void setCurrentUser(CurrentLoginUser currentUser) {
        CURRENT_USER.set(currentUser);
    }

    public static CurrentLoginUser getCurrentUser() {
        return CURRENT_USER.get();
    }

    public static Long getUserId() {
        CurrentLoginUser currentUser = CURRENT_USER.get();
        return currentUser == null ? null : currentUser.getUserId();
    }

    public static void clear() {
        CURRENT_USER.remove();
    }
}
