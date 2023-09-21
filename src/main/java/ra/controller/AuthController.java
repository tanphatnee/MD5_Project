package ra.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import ra.exception.LoginException;
import ra.model.dto.request.FormSignInDto;
import ra.model.dto.request.FormSignUpDto;
import ra.model.dto.response.JwtResponse;
import ra.security.jwt.JwtProvider;
import ra.security.user_principle.UserPrinciple;
import ra.service.IUserService;
import ra.service.impl.MailService;


import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v4/auth")
@CrossOrigin("*")
public class AuthController {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtProvider jwtProvider;
    @Autowired
    private IUserService userService;
    @Autowired
    private MailService mailService;

    @GetMapping
    public ResponseEntity<String> home(){
        return ResponseEntity.ok("vào được rồi nè");
    }
    @PostMapping("/sign-in")
    public ResponseEntity<JwtResponse> signin(@RequestBody FormSignInDto formSignInDto) throws LoginException {
        Authentication authentication =null;

        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(formSignInDto.getUsername(), formSignInDto.getPassword())
            ); // tạo đối tương authentiction để xác thực thông qua username va password
            // tạo token và trả về cho người dùng
        }catch (AuthenticationException auth){
            throw new LoginException("Username or password is incorrect!");
        }
        UserPrinciple userPrinciple = (UserPrinciple) authentication.getPrincipal();
        if (!userPrinciple.isStatus()){
            throw new LoginException("Your account has been locked");
        }
        String token = jwtProvider.generateToken(userPrinciple);
        // lấy ra user principle
        List<String> roles = userPrinciple.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).collect(Collectors.toList());
        return ResponseEntity.ok(JwtResponse.builder().token(token)
                .name(userPrinciple.getName())
                .username(userPrinciple.getUsername())
                .roles(roles)
                        .type("Bearer")
                .status(userPrinciple.isStatus()).build());
    }
    @PostMapping("/sign-up")
    private ResponseEntity<String> signup(@RequestBody FormSignUpDto formSignUpDto){
        userService.save(formSignUpDto);
        mailService.sendEmail(formSignUpDto.getEmail(),"Register succes","ban đã đăng ký thành công");

        return new ResponseEntity("success",HttpStatus.CREATED);
    }
}
