package com.OdysseyAi.demo.mapper;

import com.OdysseyAi.demo.dto.request.RegisterRequest;
import com.OdysseyAi.demo.dto.response.UserResponse;
import com.OdysseyAi.demo.enitity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toEntity(RegisterRequest request);
    UserResponse toResponse(User user);
}



//,unmappedTargetPolicy = ReportingPolicy.ERROR