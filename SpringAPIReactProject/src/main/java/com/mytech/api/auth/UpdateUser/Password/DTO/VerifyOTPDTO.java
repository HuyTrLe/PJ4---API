package com.mytech.api.auth.UpdateUser.Password.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class VerifyOTPDTO {
    private String email;

    @NotBlank(message = "Pin cannot be blank")
    private String pin;
}
