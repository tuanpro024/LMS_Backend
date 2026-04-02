package com.lms.kanjiorigin.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.common.security.AuthPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StudySetControllerTest {

    private Object controller;
    private List<String> studySetServiceCalls;

    @BeforeEach
    void setUp() throws Exception {
        studySetServiceCalls = new CopyOnWriteArrayList<>();

        Class<?> studySetServiceClass = Class.forName("com.lms.content.common.service.StudySetService");
        Object studySetService = Proxy.newProxyInstance(
                studySetServiceClass.getClassLoader(),
                new Class<?>[] { studySetServiceClass },
                (proxy, method, args) -> {
                    studySetServiceCalls.add(method.getName());
                    Class<?> returnType = method.getReturnType();
                    if (List.class.isAssignableFrom(returnType)) {
                        return List.of();
                    }
                    if (returnType.equals(void.class)) {
                        return null;
                    }
                    return null;
                });

        Class<?> delegateClass = Class.forName("com.lms.content.common.delegate.api.StudySetApiDelegate");
        Constructor<?> delegateConstructor = delegateClass.getDeclaredConstructor(studySetServiceClass);
        delegateConstructor.setAccessible(true);
        Object delegate = delegateConstructor.newInstance(studySetService);

        Class<?> kanjiProgressServiceClass = Class.forName("com.lms.kanjiorigin.service.KanjiProgressService");
        Object kanjiProgressService = Proxy.newProxyInstance(
                kanjiProgressServiceClass.getClassLoader(),
                new Class<?>[] { kanjiProgressServiceClass },
                noopHandler());

        Class<?> controllerClass = Class.forName("com.lms.kanjiorigin.controller.StudySetController");
        Constructor<?> constructor = controllerClass.getDeclaredConstructor(delegateClass, kanjiProgressServiceClass);
        constructor.setAccessible(true);
        controller = constructor.newInstance(delegate, kanjiProgressService);
    }

    private InvocationHandler noopHandler() {
        return (proxy, method, args) -> {
            Class<?> returnType = method.getReturnType();
            if (returnType.equals(boolean.class)) {
                return false;
            }
            if (returnType.equals(int.class) || returnType.equals(long.class) || returnType.equals(short.class)
                    || returnType.equals(byte.class)) {
                return 0;
            }
            if (returnType.equals(double.class) || returnType.equals(float.class)) {
                return 0.0;
            }
            return null;
        };
    }

    @Test
    void getAllStudySets_shouldThrowForbidden_whenNonAdminRequestsOtherUser() throws Exception {
        Method method = controller.getClass().getMethod(
                "getAllStudySets", String.class, String.class, String.class, Authentication.class);
        InvocationTargetException ex = assertThrows(InvocationTargetException.class,
                () -> method.invoke(controller, "other-user", null, null, authForUser("user-1")));

        Throwable cause = ex.getCause();
        assertTrue(cause instanceof ApiException);
        assertEquals(ErrorCode.FORBIDDEN, ((ApiException) cause).getErrorCode());
        assertEquals(0, studySetServiceCalls.size());
    }

    @Test
    void findByTitleAndUserIdIgnoreCase_shouldThrowForbidden_whenNonAdminRequestsOtherUser() throws Exception {
        Method method = controller.getClass().getMethod(
                "findByTitleAndUserIdIgnoreCase", String.class, String.class, Authentication.class);
        InvocationTargetException ex = assertThrows(InvocationTargetException.class,
                () -> method.invoke(controller, "N5", "other-user", authForUser("user-1")));

        Throwable cause = ex.getCause();
        assertTrue(cause instanceof ApiException);
        assertEquals(ErrorCode.FORBIDDEN, ((ApiException) cause).getErrorCode());
        assertEquals(0, studySetServiceCalls.size());
    }

    @Test
    void getAllStudySets_shouldUseCurrentUserScope_whenNonAdminWithoutFilters() throws Exception {
        String currentUserId = "user-1";
        Method method = controller.getClass().getMethod(
                "getAllStudySets", String.class, String.class, String.class, Authentication.class);

        @SuppressWarnings("unchecked")
        ResponseEntity<ApiResponse<List<?>>> response = (ResponseEntity<ApiResponse<List<?>>>) method.invoke(
                controller, null, null, null, authForUser(currentUserId));

        assertTrue(response.getBody().success());
        assertEquals(1, countServiceCalls("getStudySetsByUserId"));
        assertEquals(0, countServiceCalls("getAllStudySets"));
    }

    @Test
    void getAllStudySets_shouldReturnAllSets_whenAdminWithoutFilters() throws Exception {
        Method method = controller.getClass().getMethod(
                "getAllStudySets", String.class, String.class, String.class, Authentication.class);

        @SuppressWarnings("unchecked")
        ResponseEntity<ApiResponse<List<?>>> response = (ResponseEntity<ApiResponse<List<?>>>) method.invoke(
                controller, null, null, null, authForAdmin("admin-1"));

        assertTrue(response.getBody().success());
        assertEquals(1, countServiceCalls("getAllStudySets"));
        assertEquals(0, countServiceCalls("getStudySetsByUserId"));
    }

    @Test
    void getAllStudySets_shouldThrowUnauthorized_whenAuthenticationMissing() throws Exception {
        Method method = controller.getClass().getMethod(
                "getAllStudySets", String.class, String.class, String.class, Authentication.class);
        InvocationTargetException ex = assertThrows(InvocationTargetException.class,
                () -> method.invoke(controller, null, null, null, null));

        Throwable cause = ex.getCause();
        assertTrue(cause instanceof ApiException);
        assertEquals(ErrorCode.UNAUTHORIZED, ((ApiException) cause).getErrorCode());
        assertEquals(0, studySetServiceCalls.size());
    }

    private Authentication authForUser(String userId) {
        AuthPrincipal principal = new AuthPrincipal(userId, userId + "@mail.com", true, Set.of("ROLE_USER"));
        return new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());
    }

    private Authentication authForAdmin(String userId) {
        AuthPrincipal principal = new AuthPrincipal(userId, userId + "@mail.com", true, Set.of("ROLE_ADMIN"));
        return new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());
    }

    private int countServiceCalls(String methodName) {
        return (int) studySetServiceCalls.stream().filter(methodName::equals).count();
    }
}