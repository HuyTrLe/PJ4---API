package com.mytech.api.auth.oauth2;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mytech.api.auth.jwt.JwtUtils;
import com.mytech.api.auth.payload.response.JwtResponse;
import com.mytech.api.auth.services.MyUserDetails;
import com.mytech.api.config.OAuth2.AppProperties;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private JwtUtils tokenProvider;

    private AppProperties appProperties;

    @Autowired
    OAuth2AuthenticationSuccessHandler(JwtUtils tokenProvider, AppProperties appProperties) {
        this.tokenProvider = tokenProvider;
        this.appProperties = appProperties;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        String targetUrl = determineTargetUrl(request, response, authentication);

        if (response.isCommitted()) {
            logger.debug("Response has already been committed. Unable to redirect to " + targetUrl);
            return;
        }

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) {
        try {
            // Tạo token JWT
            String token = tokenProvider.createToken(authentication);

            // Trích xuất thông tin từ Authentication
            MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            String username = oAuth2User.getAttribute("name");
            String email = oAuth2User.getAttribute("email");
            boolean isEnabled = true;

            JwtResponse jwtResponse = new JwtResponse(token, userDetails.getId(), username, email, isEnabled);

            ObjectMapper objectMapper = new ObjectMapper();
            String jwtResponseJson = objectMapper.writeValueAsString(jwtResponse);

            return "http://localhost:3000/oauth2/redirect?jwtResponse=" + jwtResponseJson;
        } catch (JsonProcessingException e) {
            // Xử lý ngoại lệ JsonProcessingException ở đây
            e.printStackTrace(); // Hoặc thực hiện xử lý khác
            return ""; // Trả về giá trị mặc định hoặc thông báo lỗi
        }
    }

}