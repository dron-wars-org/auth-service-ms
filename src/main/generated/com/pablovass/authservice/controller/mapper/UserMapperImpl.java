package com.pablovass.authservice.controller.mapper;

import com.pablovass.authservice.controller.dto.RegisterRequest;
import com.pablovass.authservice.domain.model.entity.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-01-24T14:06:59-0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.9 (JetBrains s.r.o.)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public User toEntity(RegisterRequest request) {
        if ( request == null ) {
            return null;
        }

        User.UserBuilder user = User.builder();

        user.email( request.email() );
        user.username( request.username() );

        return user.build();
    }
}
