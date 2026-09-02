package com.OdysseyAi.demo.auth.mapper;

import com.OdysseyAi.demo.auth.dto.request.RegisterRequest;
import com.OdysseyAi.demo.auth.dto.response.UserDetailResponse;
import com.OdysseyAi.demo.auth.dto.response.UserProfileResponse;
import com.OdysseyAi.demo.auth.dto.response.UserResponse;
import com.OdysseyAi.demo.auth.dto.response.UserSummaryResponse;
import com.OdysseyAi.demo.auth.entity.User;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface UserMapper {
    User toEntity(RegisterRequest request);
    UserResponse toResponse(User user);
    UserProfileResponse toUserProfileResponse(User user);


    UserSummaryResponse toUserSummaryResponse(User user);

    UserDetailResponse toUserDetailResponse(User user);

    List<UserSummaryResponse> toUserSummaryResponseList(List<User> users);
}



//,unmappedTargetPolicy = ReportingPolicy.ERROR