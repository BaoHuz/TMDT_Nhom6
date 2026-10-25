package com.poolbooking.exception;

import java.time.Instant;

public record ApiError(boolean success, String message, Instant timestamp) {
}
