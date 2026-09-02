package com.OdysseyAi.demo.trip.controller;

import com.OdysseyAi.demo.common.api.ApiResponse;
import com.OdysseyAi.demo.common.enums.TripMemberRole;
import com.OdysseyAi.demo.trip.dto.request.AddTripMemberRequest;
import com.OdysseyAi.demo.trip.dto.response.TripMemberResponse;
import com.OdysseyAi.demo.trip.interfaces.TripMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trips/{tripId}/members")
@RequiredArgsConstructor
public class TripMemberController {

    private final TripMemberService tripMemberService;

    @PostMapping
    public ResponseEntity<ApiResponse<TripMemberResponse>> addMember(
            @PathVariable Long tripId,
            @Valid @RequestBody AddTripMemberRequest request
    ) {

        TripMemberResponse response =
                tripMemberService.addMember(tripId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.<TripMemberResponse>builder()
                                .status(HttpStatus.CREATED.value())
                                .message("Trip member added successfully.")
                                .data(response)
                                .build()
                );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TripMemberResponse>>> getMembers(
            @PathVariable Long tripId
    ) {

        List<TripMemberResponse> response =
                tripMemberService.getMembers(tripId);

        return ResponseEntity.ok(
                ApiResponse.<List<TripMemberResponse>>builder()
                        .status(HttpStatus.OK.value())
                        .message("Trip members retrieved successfully.")
                        .data(response)
                        .build()
        );
    }

    @PutMapping("/{memberId}/role")
    public ResponseEntity<ApiResponse<Void>> updateRole(
            @PathVariable Long tripId,
            @PathVariable Long memberId,
            @RequestParam TripMemberRole role
    ) {

        tripMemberService.updateMemberRole(
                tripId,
                memberId,
                role
        );

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .status(HttpStatus.OK.value())
                        .message("Trip member role updated successfully.")
                        .build()
        );
    }

    @DeleteMapping("/{memberId}")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable Long tripId,
            @PathVariable Long memberId
    ) {

        tripMemberService.removeMember(
                tripId,
                memberId
        );

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .status(HttpStatus.OK.value())
                        .message("Trip member removed successfully.")
                        .build()
        );
    }
}