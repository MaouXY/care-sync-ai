package com.caresync.ai.context;

/**
 * 基于 ThreadLocal 实现的当前线程上下文类
 */
public class BaseContext {
    /**
     * 基于 ThreadLocal 实现的当前线程上下文类
     */
    public static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    /**
     * 存储用户角色的ThreadLocal
     */
    public static ThreadLocal<Integer> roleThreadLocal = new ThreadLocal<>();

    /**
     * 设置当前线程的用户 ID
     *
     * @param id 用户 ID
     */
    public static void setCurrentId(Long id) {
        /**
         * 设置当前线程的用户 ID
         *
         * @param id 用户 ID
         */
        threadLocal.set(id);
    }

    /**
     * 获取当前线程的用户 ID
     *
     * @return 用户 ID
     */
    public static Long getCurrentId() {
        return threadLocal.get();
    }

    /**
     * 移除当前线程的用户 ID
     */
    public static void removeCurrentId() {
        threadLocal.remove();
    }

    /**
     * 设置当前线程的用户角色
     *
     * @param role 用户角色
     */
    public static void setCurrentRole(Integer role) {
        roleThreadLocal.set(role);
    }

    /**
     * 获取当前线程的用户角色
     *
     * @return 用户角色
     */
    public static Integer getCurrentRole() {
        return roleThreadLocal.get();
    }

    /**
     * 移除当前线程的用户角色
     */
    public static void removeCurrentRole() {
        roleThreadLocal.remove();
    }

    /**
     * 清除所有线程本地变量
     */
    public static void clear() {
        threadLocal.remove();
        roleThreadLocal.remove();
    }

}
