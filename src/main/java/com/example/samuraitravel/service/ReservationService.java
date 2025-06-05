package com.example.samuraitravel.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.samuraitravel.entity.Reservation;
import com.example.samuraitravel.entity.User;
import com.example.samuraitravel.repository.ReservationRepository;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;

    public ReservationService(ReservationRepository reservationRepository){
        this.reservationRepository = reservationRepository;
    }

    // 指定されたユーザーに紐づく予約を作品日時が新しい順に並び替え、ページングされた状態で取得
    public Page<Reservation> findReservationsByUserOrderByCreatedAtDesc(User user,Pageable pageable){
        return reservationRepository.findByUserOrderByCreatedAtDesc(user, pageable);
    }
    

}
