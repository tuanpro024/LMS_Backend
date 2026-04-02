package com.lms.kanjiorigin.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.common.security.AuthPrincipal;
import com.lms.kanjiorigin.dto.request.UpdateKanjiStatusRequest;
import com.lms.kanjiorigin.dto.response.KanjiStatusResponse;
import com.lms.kanjiorigin.entity.enums.KanjiStatus;
import com.lms.kanjiorigin.service.KanjiOriginService;
import com.lms.kanjiorigin.service.KanjiProgressService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KanjiOriginControllerTest {

    @Mock
    private KanjiOriginService kanjiOriginService;

    @Mock
    private KanjiProgressService kanjiProgressService;

    private KanjiOriginController controller;

    @BeforeEach
    void setUp() {
        controller = new KanjiOriginController(kanjiOriginService, kanjiProgressService);
    }

    @Test
    void updateKanjiStatus_shouldUseAuthenticatedUserId() {
        String userId = "user-1";
        String kanjiId = "k-1";

        UpdateKanjiStatusRequest request = UpdateKanjiStatusRequest.builder()
                .status(KanjiStatus.LEARNED)
                .build();

        KanjiStatusResponse response = KanjiStatusResponse.builder()
                .kanjiId(kanjiId)
                .studySetId("set-1")
                .term("han")
                .pinyin("han")
                .status(KanjiStatus.LEARNED)
                .reviewCount(1)
                .build();

        when(kanjiProgressService.updateKanjiStatus(userId, kanjiId, request)).thenReturn(response);

        ApiResponse<KanjiStatusResponse> apiResponse = controller.updateKanjiStatus(kanjiId, request, authForUser(userId));

        assertTrue(apiResponse.success());
        assertEquals(kanjiId, apiResponse.data().getKanjiId());
        verify(kanjiProgressService).updateKanjiStatus(userId, kanjiId, request);
    }

    @Test
    void updateKanjiStatus_shouldThrowUnauthorizedWhenPrincipalInvalid() {
        Authentication authentication = new UsernamePasswordAuthenticationToken("anonymous", null,
            Collections.emptyList());

        UpdateKanjiStatusRequest request = UpdateKanjiStatusRequest.builder()
                .status(KanjiStatus.LEARNED)
                .build();

        ApiException ex = assertThrows(ApiException.class,
                () -> controller.updateKanjiStatus("k-1", request, authentication));

        assertEquals(ErrorCode.UNAUTHORIZED, ex.getErrorCode());
    }

    @Test
    void updateKanjiStatus_shouldThrowUnauthorizedWhenAuthMissing() {
        UpdateKanjiStatusRequest request = UpdateKanjiStatusRequest.builder()
                .status(KanjiStatus.LEARNING)
                .build();

        ApiException ex = assertThrows(ApiException.class,
                () -> controller.updateKanjiStatus("k-1", request, null));

        assertEquals(ErrorCode.UNAUTHORIZED, ex.getErrorCode());
    }

    private Authentication authForUser(String userId) {
        AuthPrincipal principal = new AuthPrincipal(userId, userId + "@mail.com", true, Set.of("ROLE_USER"));
        return new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());
    }
}
