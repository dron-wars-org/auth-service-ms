package com.pablovass.authservice.controller.mapper;

import com.pablovass.authservice.controller.dto.RegisterRequest;
import com.pablovass.authservice.domain.model.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "password", ignore = true) // Password se setea manualmente tras hashear
    User toEntity(RegisterRequest request);
}
