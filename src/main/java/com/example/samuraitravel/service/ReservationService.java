package com.example.samuraitravel.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import com.example.samuraitravel.dto.ReservationDTO;
import com.example.samuraitravel.entity.House;
import com.example.samuraitravel.entity.Reservation;
import com.example.samuraitravel.entity.User;
import com.example.samuraitravel.repository.HouseRepository;
import com.example.samuraitravel.repository.ReservationRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final HouseRepository houseRepository;

    public ReservationService(ReservationRepository reservationRepository, HouseRepository houseRepository) {
        this.reservationRepository = reservationRepository;
        this.houseRepository = houseRepository;
    }

    // 指定されたユーザーに紐づく予約を作品日時が新しい順に並び替え、ページングされた状態で取得
    public Page<Reservation> findReservationsByUserOrderByCreatedAtDesc(User user, Pageable pageable) {
        return reservationRepository.findByUserOrderByCreatedAtDesc(user, pageable);
    }

    // チェックイン日がチェックアウト日よりも前の日付かどうかをチェックする
    public boolean isCheckinBeforeCheckout(LocalDate checkinDate, LocalDate checkoutDate) {
        return checkinDate.isBefore(checkoutDate);
    }

    // 宿泊人数が定員以下かどうかをチェックする
    public boolean isWithCapacity(Integer numberOfPeople, Integer capacity) {
        return numberOfPeople <= capacity;
    }

    // チェックイン・チェックアウトの日の入力に不備がない場合は以前の入力値を取得する
    public String getPreviousDates(LocalDate checkinDate, LocalDate checkoutDate, BindingResult bindingResult) {
        if (checkinDate != null && checkoutDate != null
                && !bindingResult.hasFieldErrors("checkinDate")
                && !bindingResult.hasFieldErrors("checkoutDate")) {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String formattedCheckinDate = checkinDate.format(dateTimeFormatter);
            String formattedCheckoutDate = checkoutDate.format(dateTimeFormatter);

            return formattedCheckinDate + "から" + formattedCheckoutDate;
        }

        return "";
    }

    // 宿泊料金を計算する
    public Integer calculateAmount(LocalDate checkinDate, LocalDate checkoutdDate, Integer price) {
        long numberOfNights = ChronoUnit.DAYS.between(checkinDate, checkoutdDate);
        int amount = price * (int) numberOfNights;

        return amount;
    }

    // 予約のレコード数を取得する
    public long countReservations(){
        return reservationRepository.count();
    }

    // idが最も大きい予約の情報を取得する
    public Reservation findFirstReservationByOrderByIdDesc(){
        return reservationRepository.findFirstByOrderByIdDesc();
    }


    @Transactional
    public void createReservation(ReservationDTO reservationDTO, User user) {

        Reservation reservation = new Reservation();

        Optional<House> optionalHouse = houseRepository.findById(reservationDTO.getHouseId());
        House house = optionalHouse.orElseThrow(() -> new EntityNotFoundException("指定されたIDの民宿が存在しません。"));

        reservation.setHouse(house);
        reservation.setUser(user);
        reservation.setCheckinDate(reservationDTO.getCheckinDate());
        reservation.setCheckoutDate(reservationDTO.getCheckoutDate());
        reservation.setNumberOfPeople(reservationDTO.getNumberOfPeople());
        reservation.setAmount(reservationDTO.getAmount());

        reservationRepository.save(reservation);


    }

}
