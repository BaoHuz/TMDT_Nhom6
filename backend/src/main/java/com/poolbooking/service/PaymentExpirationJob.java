package com.poolbooking.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PaymentExpirationJob {
    private final BookingService bookings;

    public PaymentExpirationJob(BookingService bookings) { this.bookings = bookings; }

    @Scheduled(fixedDelay = 60000)
    public void expireUnpaidBookings() { bookings.expirePendingBookings(); }
}
