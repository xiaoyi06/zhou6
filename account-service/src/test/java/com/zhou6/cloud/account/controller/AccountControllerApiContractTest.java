package com.zhou6.cloud.account.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import com.zhou6.cloud.account.service.AccountService;
import com.zhou6.cloud.account.vo.AccountSummaryVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AccountControllerApiContractTest {

    private AccountService accountService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        accountService = mock(AccountService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new AccountController(accountService)).build();
    }

    @Test
    void summaryReturnsSuccessContract() throws Exception {
        AccountSummaryVO summary = new AccountSummaryVO();
        summary.setAvailable(new BigDecimal("1.0000"));
        summary.setFrozen(new BigDecimal("2.0000"));
        summary.setSettling(new BigDecimal("3.0000"));
        summary.setTotal(new BigDecimal("4.0000"));
        when(accountService.summary(10001L)).thenReturn(summary);

        mockMvc.perform(post("/api/account/summary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":10001}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("000000"))
                .andExpect(jsonPath("$.data.available").value(1.0000))
                .andExpect(jsonPath("$.data.frozen").value(2.0000))
                .andExpect(jsonPath("$.data.settling").value(3.0000))
                .andExpect(jsonPath("$.data.total").value(4.0000));
    }

    @Test
    void freezePassesRequestToService() throws Exception {
        when(accountService.freeze(10001L, new BigDecimal("1.0000"), "ORDER_PAY", "order-1")).thenReturn(true);

        mockMvc.perform(post("/api/account/freeze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userId":10001,"amount":1.0000,"bizType":"ORDER_PAY","bizId":"order-1"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("000000"))
                .andExpect(jsonPath("$.data").value(true));

        verify(accountService).freeze(eq(10001L), eq(new BigDecimal("1.0000")), eq("ORDER_PAY"), eq("order-1"));
    }
}
