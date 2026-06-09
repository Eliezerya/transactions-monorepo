package com.transaction.wallet.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDto {
    private Long userId;
    private String email;
    private String username;
    private String role;
    private boolean valid;
}
