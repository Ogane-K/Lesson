package com.example.samuraitravel.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;




@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles
public class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void 未ログインの場合は会員用の予約一覧ページからログインページにリダイレクトする() throws Exception{
        mockMvc.perform(get("/reservations"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    @WithUserDetails("taro.samurai@example.com")
    public void ログイン済みの場合は会員用の予約一覧ページが正しく表示される() throws Exception {
        mockMvc.perform(get("/reservations"))
        .andExpect(status().isOk())
        .andExpect(view().name("reservations/index"));
    }


}
