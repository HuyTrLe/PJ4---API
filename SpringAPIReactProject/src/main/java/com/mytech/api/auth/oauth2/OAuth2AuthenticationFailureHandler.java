package com.mytech.api.auth.oauth2;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {
        if (exception instanceof OAuth2AuthenticationException) {
            OAuth2AuthenticationException oauth2Exception = (OAuth2AuthenticationException) exception;
            String errorMessage = oauth2Exception.getMessage();
            // Hiển thị thông báo lỗi và điều hướng lại trang sign-in
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().write("<script>alert('" + errorMessage
                    + "'); window.location.href='http://localhost:3000/auth/signin';</script>");
        } else {
            // Hiển thị thông báo lỗi và điều hướng lại trang sign-in
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().write(
                    "<script>alert('Unknown error occurred.'); window.location.href='http://localhost:3000/auth/signin';</script>");
        }
    }

}
