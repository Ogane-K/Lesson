package com.example.samuraitravel.controller;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.samuraitravel.entity.House;
import com.example.samuraitravel.entity.Review;
import com.example.samuraitravel.entity.User;
import com.example.samuraitravel.form.ReviewEditForm;
import com.example.samuraitravel.form.ReviewRegisterForm;
import com.example.samuraitravel.security.UserDetailsImpl;
import com.example.samuraitravel.service.HouseService;
import com.example.samuraitravel.service.ReviewService;

@Controller
@RequestMapping("/houses/{houseId}/reviews")
public class ReviewController {
    private final HouseService houseService;
    private final ReviewService reviewService;

    public ReviewController(HouseService houseService, ReviewService reviewService) {
        this.houseService = houseService;
        this.reviewService = reviewService;
    }

    @GetMapping
    public String index(@PathVariable(name = "houseId") Integer houseId,
            @PageableDefault(page = 0, size = 10, sort = "id") Pageable pageable,
            RedirectAttributes redirectAttributes,
            Model model) {
        Optional<House> optionalHouse = houseService.findHousesById(houseId);

        if (optionalHouse.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "民宿が存在しません。");

            return "redirect:/houses";
        }

        House house = optionalHouse.get();
        Page<Review> reviewPage = reviewService.findReviewsByHouseOrderByCreatedAtDesc(house, pageable);

        model.addAttribute("house", house);
        model.addAttribute("reviewPage", reviewPage);

        return "reviews/index";
    }

    @GetMapping("/register")
    public String register(@PathVariable(name = "houseId") Integer houseId, RedirectAttributes redirectAttributes,
            Model model) {
        Optional<House> optionalHouse = houseService.findHousesById(houseId);

        if (optionalHouse.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "民宿が存在しません。");

            return "redirect:/houses";
        }

        House house = optionalHouse.get();

        model.addAttribute("house", house);
        model.addAttribute("reviewRegisterForm", new ReviewRegisterForm());

        return "reviews/register";
    }

    @PostMapping("/create")
    public String create(@PathVariable(name = "houseId") Integer houseId,
            @ModelAttribute @Validated ReviewRegisterForm reviewRegisterForm,
            BindingResult bindingResult,
            @AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
            RedirectAttributes redirectAttributes,
            Model model) {
        Optional<House> optionalHouse = houseService.findHousesById(houseId);

        if (optionalHouse.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "民宿が存在しません。");

            return "redirect:/houses";
        }

        House house = optionalHouse.get();

        if (bindingResult.hasErrors()) {
            model.addAttribute("house", house);
            model.addAttribute("reviewRegisterForm", reviewRegisterForm);

            return "reviews/register";
        }

        User user = userDetailsImpl.getUser();

        reviewService.createReview(reviewRegisterForm, house, user);
        redirectAttributes.addFlashAttribute("successMessage", "レビューを投稿しました。");

        return "redirect:/houses/{houseId}";
    }

    @GetMapping("/{reviewId}/edit")
    public String edit(@PathVariable(name = "houseId") Integer houseId,
            @PathVariable(name = "reviewId") Integer reviewId,
            @AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
            RedirectAttributes redirectAttributes,
            Model model) {
        Optional<House> optionalHouse = houseService.findHousesById(houseId);
        Optional<Review> optionalReview = reviewService.findReviewById(reviewId);

        if (optionalHouse.isEmpty() || optionalReview.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "指定されたページが見つかりません。");

            return "redirect:/houses";
        }

        House house = optionalHouse.get();
        Review review = optionalReview.get();
        User user = userDetailsImpl.getUser();

        if (!review.getHouse().equals(house) || !review.getUser().equals(user)) {
            redirectAttributes.addFlashAttribute("errorMessage", "不正なアクセスです。");

            return "redirect:/houses/{houseId}";
        }

        ReviewEditForm reviewEditForm = new ReviewEditForm(review.getScore(), review.getContent());

        model.addAttribute("house", house);
        model.addAttribute("review", review);
        model.addAttribute("reviewEditForm", reviewEditForm);

        return "reviews/edit";
    }

    @PostMapping("/{reviewId}/update")
    public String update(@PathVariable(name = "houseId") Integer houseId,
            @PathVariable(name = "reviewId") Integer reviewId,
            @ModelAttribute @Validated ReviewEditForm reviewEditForm,
            BindingResult bindingResult,
            @AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
            RedirectAttributes redirectAttributes,
            Model model) {
        Optional<House> optionalHouse = houseService.findHousesById(houseId);
        Optional<Review> optionalReview = reviewService.findReviewById(reviewId);

        if (optionalHouse.isEmpty() || optionalReview.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "指定されたページが見つかりません。");

            return "redirect:/houses";
        }

        House house = optionalHouse.get();
        Review review = optionalReview.get();
        User user = userDetailsImpl.getUser();

        if (!review.getHouse().equals(house) || !review.getUser().equals(user)) {
            redirectAttributes.addFlashAttribute("errorMessage", "不正なアクセスです。");

            return "redirect:/houses/{houseId}";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("house", house);
            model.addAttribute("review", review);
            model.addAttribute("reviewEditForm", reviewEditForm);

            return "reviews/edit";
        }

        reviewService.updateReview(reviewEditForm, review);
        redirectAttributes.addFlashAttribute("successMessage", "レビューを編集しました。");

        return "redirect:/houses/{houseId}";
    }

    @PostMapping("/{reviewId}/delete")
    public String delete(@PathVariable(name = "houseId") Integer houseId,
            @PathVariable(name = "reviewId") Integer reviewId,
            @AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
            RedirectAttributes redirectAttributes) {
        Optional<House> optionalHouse = houseService.findHousesById(houseId);
        Optional<Review> optionalReview = reviewService.findReviewById(reviewId);

        if (optionalHouse.isEmpty() || optionalReview.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "指定されたページが見つかりません。");

            return "redirect:/houses";
        }

        House house = optionalHouse.get();
        Review review = optionalReview.get();
        User user = userDetailsImpl.getUser();

        if (!review.getHouse().equals(house) || !review.getUser().equals(user)) {
            redirectAttributes.addFlashAttribute("errorMessage", "不正なアクセスです。");

            return "redirect:/houses/{houseId}";
        }

        reviewService.deleteReview(review);
        redirectAttributes.addFlashAttribute("successMessage", "レビューを削除しました。");

        return "redirect:/houses/{houseId}";
    }
}