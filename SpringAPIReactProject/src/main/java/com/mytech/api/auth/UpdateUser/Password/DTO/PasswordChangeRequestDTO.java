package com.mytech.api.auth.UpdateUser.Password.DTO;

public class PasswordChangeRequestDTO {

    private PasswordDTO passwordDTO;
    private VerifyOTPDTO verifyOTPDTO;

    // getters, setters, constructors

    public PasswordDTO getPasswordDTO() {
        return passwordDTO;
    }

    public void setPasswordDTO(PasswordDTO passwordDTO) {
        this.passwordDTO = passwordDTO;
    }

    public VerifyOTPDTO getVerifyOTPDTO() {
        return verifyOTPDTO;
    }

    public void setVerifyOTPDTO(VerifyOTPDTO verifyOTPDTO) {
        this.verifyOTPDTO = verifyOTPDTO;
    }
}
