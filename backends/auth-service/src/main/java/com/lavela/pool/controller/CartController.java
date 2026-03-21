package com.lavela.pool.controller;

import com.lavela.pool.domain.enums.BookingStatus;
import com.lavela.pool.dto.response.ApiResponse;
import com.lavela.pool.repository.BookingRepository;
import com.lavela.pool.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "Cart", description = "Cart/Draft APIs using Booking")
@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class CartController {

    private final BookingRepository bookingRepository;

    @Operation(summary = "Get cart items count",
               description = "Returns number of bookings in IN_CART status for the authenticated user.")
    @GetMapping("/count")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Map<String, Integer>> getCartCount(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        int count = bookingRepository.countByUserIdAndStatusAndSlotStartTimeAfter(
                principal.getUserId(), BookingStatus.IN_CART, java.time.LocalDateTime.now()
        );
        return ApiResponse.ok(Map.of("count", count));
    }
}
