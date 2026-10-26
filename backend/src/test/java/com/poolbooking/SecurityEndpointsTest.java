package com.poolbooking;

import com.poolbooking.entity.TaiKhoan;
import com.poolbooking.enums.VaiTro;
import com.poolbooking.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityEndpointsTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Test
    void guestCanGetSchedules_PermitAll() throws Exception {
        mockMvc.perform(get("/api/schedules"))
            .andExpect(status().isOk());
    }

    @Test
    void guestCannotPostBooking_Unauthorized() throws Exception {
        mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"maLich\":\"L001\",\"soNguoi\":1}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void adminEndpointBlockedForCustomer_Forbidden() throws Exception {
        TaiKhoan customer = new TaiKhoan();
        customer.tenDangNhap = "customer"; // user in seed data
        customer.vaiTro = VaiTro.CUSTOMER;
        String token = jwtService.createAccessToken(customer);

        mockMvc.perform(get("/api/admin/dashboard")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isForbidden());
    }
}
