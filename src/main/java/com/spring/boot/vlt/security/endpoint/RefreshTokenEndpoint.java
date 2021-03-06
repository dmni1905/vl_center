package com.spring.boot.vlt.security.endpoint;

import com.spring.boot.vlt.config.property.TokenSettings;
import com.spring.boot.vlt.config.WebSecurityConfig;
import com.spring.boot.vlt.exceptions.InvalidJwtToken;
import com.spring.boot.vlt.mvc.model.UserContext;
import com.spring.boot.vlt.mvc.model.entity.User;
import com.spring.boot.vlt.mvc.model.token.*;
import com.spring.boot.vlt.mvc.model.token.RawAccessJwtToken;
import com.spring.boot.vlt.mvc.model.token.RefreshJwtToken;
import com.spring.boot.vlt.mvc.service.UserService;
import com.spring.boot.vlt.security.auth.token.extractor.TokenExtractor;
import com.spring.boot.vlt.security.auth.token.verifier.TokenVerifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
public class RefreshTokenEndpoint {
    @Autowired
    private JwtTokenFactory jwtTokenFactory;
    @Autowired
    private TokenSettings jwtSettings;
    @Autowired
    private UserService userService;
    @Autowired
    private TokenVerifier tokenVerifier;
    @Autowired
    @Qualifier("jwtHeaderTokenExtractor") private TokenExtractor tokenExtractor;

    @RequestMapping(value="/auth/token", method= RequestMethod.GET, produces = "application/json")
    public @ResponseBody Map<String, String> refreshToken(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        String tokenPayload = tokenExtractor.extract(request.getHeader(WebSecurityConfig.JWT_TOKEN_HEADER_PARAM));

        RawAccessJwtToken rawToken = new RawAccessJwtToken(tokenPayload);
        RefreshJwtToken refreshToken = RefreshJwtToken.create(rawToken, jwtSettings.getTokenSigningKey()).orElseThrow(() -> new InvalidJwtToken());

        String jti = refreshToken.getJti();
        if (!tokenVerifier.verify(jti)) {
            throw new InvalidJwtToken();
        }

        String subject = refreshToken.getSubject();
        User user = userService.getUserByLogin(subject);

        if (user.getRole() == null) throw new InsufficientAuthenticationException("User has no roles assigned");
        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority(user.getRole().authority())
        );

        UserContext userContext = UserContext.create(user.getLogin(), authorities);

        JwtToken accessJwtToken = jwtTokenFactory.createAccessJwtToken(userContext);
        JwtToken refreshJwtToken = jwtTokenFactory.createRefreshToken(userContext);

        Map<String, String> tokenMap = new HashMap<String, String>();
        tokenMap.put("token", accessJwtToken.getToken());
        tokenMap.put("refreshJwtToken", refreshJwtToken.getToken());

        return tokenMap;
    }
}
