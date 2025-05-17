package com.example.samuraitravel.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				//	アクセス許可範囲の設定
				.authorizeHttpRequests((requests) -> requests
						//	下記URLは全ユーザーにアクセス許可
						.requestMatchers("/css/**", "/images/**", "/js/**", "/storage/**", "/","/login","/signup")
						.permitAll()
						//	それ以外のリクエストは認証が必要
						.anyRequest().authenticated())
				//	ログイン時のURL設定
				.formLogin((form) -> form
						//	ログインページのURL
						.loginPage("/login")
						//	ログインフォームの送信先のURL
						.loginProcessingUrl("/login")
						//	ログイン成功時のリダイレクト先URL
						.defaultSuccessUrl("/?loggedIn")
						//	ログイン失敗時のリダイレクト先URL
						.failureUrl("/login?error")
						.permitAll())
				//	ログアウト時の設定
				.logout((logout) -> logout
						//	ログアウト時のリダイレクトURL					
						.logoutSuccessUrl("/?loggedOut")
						.permitAll());

		return http.build();

	}

    @Bean
    PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

}
