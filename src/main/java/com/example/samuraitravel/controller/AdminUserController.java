package com.example.samuraitravel.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.samuraitravel.entity.User;
import com.example.samuraitravel.service.UserService;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {
    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }



    @GetMapping
    public String index(@RequestParam(name = "keyword", required = false) String keyword,
            @PageableDefault(page = 0, size = 10, sort = "id", direction = Direction.ASC) Pageable pageable,
            Model model) {
        Page<User> userPage;

        if (keyword != null && !keyword.isEmpty()) {

            userPage = userService.findUserByNameLikeOrFuriganaLike(keyword, keyword, pageable);

        } else {
            userPage = userService.findAllUsers(pageable);
        }

        model.addAttribute("userPage", userPage);
        model.addAttribute("keyword", keyword);

        return "admin/users/index";

    }

}
