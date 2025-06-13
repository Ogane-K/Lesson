package com.example.samuraitravel.service;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.samuraitravel.dto.ReservationDTO;
import com.example.samuraitravel.entity.House;
import com.example.samuraitravel.entity.User;
import com.example.samuraitravel.repository.HouseRepository;
import com.stripe.Stripe;
import com.stripe.exception.ApiConnectionException;
import com.stripe.exception.ApiException;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.exception.PermissionException;
import com.stripe.exception.RateLimitException;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.param.checkout.SessionCreateParams.Mode;
import com.stripe.param.checkout.SessionCreateParams.PaymentMethodType;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;

@Service
public class StripeService {

    private static final PaymentMethodType PAYMENT_METHOD_TYPE = SessionCreateParams.PaymentMethodType.CARD;

    // 通過
    private static final String CURRENCY = "jpy";

    // 数量
    private static final long QUANTITY = 1L;

    // 支払いモード
    private static final Mode MODE = SessionCreateParams.Mode.PAYMENT;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Stripeのシークレットキー
    @Value("${stripe.api-key}")
    private String stripeApiKey;

    // 決済成功時のリダイレクト先URL
    @Value("${stripe.success-url}")
    private String stripeSuccessUrl;

    // 決済キャンセル時のリダイレクト先URL
    @Value("${stripe.cancel-url}")
    private String stripeCancelUrl;

    private final HouseRepository houseRepository;

    public StripeService(HouseRepository houseRepository) {
        this.houseRepository = houseRepository;
    }

    // 依存性の注入後に一度実行されるメソッド
    @PostConstruct
    private void init() {
        // Stripeのシークレットキーを設定する
        Stripe.apiKey = stripeApiKey;
    }

    public String createStripeSession(ReservationDTO reservationDTO, User user) {
        Optional<House> optionalHouse = houseRepository.findById(reservationDTO.getHouseId());
        House house = optionalHouse.orElseThrow(() -> new EntityNotFoundException("指定されたIDの民宿が存在しません。"));

        // 商品名
        String houseName = house.getName();

        // 料金
        long unitAmount = (long) reservationDTO.getAmount();

        // メタデータ(付随情報)
        String houseId = reservationDTO.getHouseId().toString();
        String userId = user.getId().toString();
        String checkinDate = reservationDTO.getCheckinDate().format(DATE_TIME_FORMATTER);
        String checkoutDate = reservationDTO.getCheckoutDate().format(DATE_TIME_FORMATTER);
        String numberOfPeople = reservationDTO.getNumberOfPeople().toString();
        String amount = reservationDTO.getAmount().toString();

        SessionCreateParams sessionCreateParams = SessionCreateParams.builder()
                .addPaymentMethodType(PAYMENT_METHOD_TYPE)
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName(houseName)
                                                                .build())
                                                .setUnitAmount(unitAmount)
                                                .setCurrency(CURRENCY)
                                                .build())
                                .setQuantity(QUANTITY)
                                .build())
                .setMode(MODE)
                .setSuccessUrl(stripeSuccessUrl)
                .setCancelUrl(stripeCancelUrl)
                .setPaymentIntentData(
                        SessionCreateParams.PaymentIntentData.builder()
                                .putMetadata("houseId", houseId)
                                .putMetadata("userId", userId)
                                .putMetadata("checkinDate", checkinDate)
                                .putMetadata("checkoutDate", checkoutDate)
                                .putMetadata("numberOfPeople", numberOfPeople)
                                .putMetadata("amount", amount)
                                .build())
                .build();

        try {

            // stripeに送信する支払い情報をセッションとして作成する
            Session session = Session.create(sessionCreateParams);

            return session.getId();
        } catch (RateLimitException e) {
            System.out.println("短時間のうちに過剰な回数のAPIコールが行われました。");
            return "";
        } catch (InvalidRequestException e) {
            System.out.println("APIコールのパラメーターが間違っているか、状態が誤っているか、方法が無効でした。");
            return "";
        } catch (PermissionException e) {
            System.out.println("このリクエストに使用されたAPIキーには必要な権限がありません。");
            return "";
        } catch (AuthenticationException e) {
            System.out.println("Stripeは、提供された情報では認証できません。");
            return "";
        } catch (ApiConnectionException e) {
            System.out.println("お客様のサーバーとStripeの間でネットワークの問題が発生しました。");
            return "";
        } catch (ApiException e) {
            System.out.println("Stripe側で問題が発生しました。(稀な状況です)。");
            return "";
        } catch (StripeException e) {
            System.out.println("Stripeとの通信中に予期せぬエラーが発生しました。");
            return "";
        }
    }

}
