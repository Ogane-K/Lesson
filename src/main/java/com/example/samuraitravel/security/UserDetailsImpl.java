package com.example.samuraitravel.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.samuraitravel.entity.User;

public class UserDetailsImpl implements UserDetails {

	private final User user;
	private final Collection<GrantedAuthority> authorities;

	public UserDetailsImpl(User user, Collection<GrantedAuthority> authorities) {
		this.user = user;
		this.authorities = authorities;
	}

	public User getUser() {
		return user;
	}

	//	ロールのコレクションのゲッター
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	// ハッシュ化済みで保存されているパスワードのゲッター
	@Override
	public String getPassword() {
		return user.getPassword();
	}

	//　ログイン時の認証用ユーザーネーム(email)のゲッター
	@Override
	public String getUsername() {
		return user.getEmail();
	}

	//	アカウントがロックされていなければtrue
	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	//	アカウントがのパスワードがキレていなければtrue
	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	//	ユーザーが有効であればtrue
	@Override
	public boolean isEnabled() {
		return user.getEnabled();
	}

}
