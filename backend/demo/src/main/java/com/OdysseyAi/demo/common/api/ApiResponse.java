package com.OdysseyAi.demo.common.api;



import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ApiResponse<T> {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @Builder.Default
    private final LocalDateTime timestamp=LocalDateTime.now();

    private final int status;
    private final String message;
    private final T data;



}