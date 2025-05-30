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

import com.example.samuraitravel.entity.House;
import com.example.samuraitravel.service.HouseService;

@Controller
@RequestMapping("/houses")
public class HouseController {

    private final HouseService houseService;

    public HouseController(HouseService houseService) {
        this.houseService = houseService;
    }

    @GetMapping
    public String index(@RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "area", required = false) String area,
            @RequestParam(name = "price", required = false) Integer price,
            @PageableDefault(page = 0, size = 10, sort = "id", direction = Direction.ASC) Pageable pageable,
            Model model) {

        Page<House> housePage;

        if(keyword != null && !keyword.isEmpty()){
            housePage = houseService.findHousesByNameLikeOrAddressLike(keyword, keyword, null);
        }else if(area != null && !area.isEmpty()){
            housePage = houseService.findHousesByAddressLike(area, null);
        }else if(price != null){
            housePage = houseService.findHousesByPriceLessthanEqual(price, pageable);
        }else {
            housePage = houseService.findAllHouses(pageable);
        }

        model.addAttribute("housePage", housePage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("area", area);
        model.addAttribute("price", price);

        return "houses/index";
    }

}
