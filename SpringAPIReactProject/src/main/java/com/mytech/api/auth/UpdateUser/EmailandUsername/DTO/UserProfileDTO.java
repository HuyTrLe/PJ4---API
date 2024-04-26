package com.mytech.api.auth.UpdateUser.EmailandUsername.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserProfileDTO {
    @NotBlank(message = "Username cannot be blank.")
    private String username;

    @NotBlank(message = "Email cannot be blank.")
    private String email;

    private boolean confirmNewEmail;
}