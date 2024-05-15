package cloud.computing.auth.api.controller;


import cloud.computing.auth.api.controller.response.AuthLoginPageResponse;
import cloud.computing.auth.api.controller.response.AuthLoginResponse;
import cloud.computing.auth.api.service.auth.AuthService;
import cloud.computing.auth.api.service.oauth.OAuthService;
import cloud.computing.auth.api.service.state.LoginStateService;
import cloud.computing.auth.common.exception.ExceptionMessage;
import cloud.computing.auth.common.exception.oauth.OAuthException;
import cloud.computing.auth.common.response.JsonResult;
import cloud.computing.auth.domain.define.account.user.constant.UserPlatformType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/auth")
@RestController
public class AuthController {

    private final LoginStateService loginStateService;

    private final OAuthService oAuthService;

    private final AuthService authService;


    @GetMapping("/loginPage")
    public JsonResult<List<AuthLoginPageResponse>> loginPage() {

        String loginState = loginStateService.generateLoginState();

        // OAuth 사용하여 각 플랫폼의 로그인 페이지 URL을 가져와서 state 주입
        List<AuthLoginPageResponse> loginPages = oAuthService.loginPage(loginState);


        return JsonResult.successOf(loginPages);
    }

    @GetMapping("/{platformType}/login")
    public JsonResult<AuthLoginResponse> login(
            @PathVariable("platformType") UserPlatformType platformType,
            @RequestParam("code") String code,
            @RequestParam("state") String loginState) {

        // state 값이 유효한지 검증
        if (!loginStateService.isValidLoginState(loginState)) {
            throw new OAuthException(ExceptionMessage.LOGINSTATE_INVALID_VALUE);
        }

        AuthLoginResponse loginResponse = authService.login(platformType, code, loginState);

        return JsonResult.successOf(loginResponse);
    }
}
