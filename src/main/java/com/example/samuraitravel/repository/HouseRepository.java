package com.example.samuraitravel.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.samuraitravel.entity.House;
import java.util.List;


public interface HouseRepository extends JpaRepository<House, Integer> {

	public Page<House> findByNameLike(String keyword, Pageable pageable);
	public House findFirstByOrderByIdDesc();
	
	public Page<House> findByNameLikeOrAddressLike(String nameKeyword, String addressKeyword, Pageable pageable);
	public Page<House> findByAddressLike(String area, Pageable pageable);
	public Page<House> findByPriceLessThanEqal(Integer price, Pageable pageable);


	
}
