package com.example.calendar.Controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.calendar.DTO.ParticipantDto;
import com.example.calendar.DTO.ParticipantFullDto;
import com.example.calendar.Model.ParticipantStatus;
import com.example.calendar.Repository.ParticipantRepository;
import com.example.calendar.exception.EmailNotUnique;
import com.example.calendar.exception.ParticipantNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static java.util.Arrays.stream;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/calendar")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class CalendarRestController {
    private ParticipantRepository participantRepository;
    private ModelMapper modelMapper;
    private PasswordEncoder encoder;

    @GetMapping
    public List<ParticipantDto> findAll() {
        return participantRepository.findParticipantByStatus(ParticipantStatus.ACTIVE).stream().
                map(participant -> modelMapper.map(participant, ParticipantDto.class)).toList();
    }

    @GetMapping("/refresh")
    public void refreshTokens(HttpServletRequest request, HttpServletResponse response) throws IOException {
        var authorizationHeader = request.getHeader(AUTHORIZATION);
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            try {
                var refresh_token = authorizationHeader.substring("Bearer ".length());
                var algorithm = Algorithm.HMAC256("secret".getBytes());
                var verifier = JWT.require(algorithm).build();
                var decoderJWT = verifier.verify(refresh_token);
                var username = decoderJWT.getSubject();
                var user = participantRepository.findByEmailAndStatus(username, ParticipantStatus.ACTIVE)
                        .orElseThrow(() -> new ParticipantNotFoundException(username));
                var access = JWT.create()
                        .withSubject(user.getEmail())
                        .withExpiresAt(new Date(System.currentTimeMillis() + 1 * 60 * 1000))
                        .withIssuer(request.getRequestURL().toString())
                        .withClaim("roles", List.of(user.getRole().name()))
                        .sign(algorithm);

                var tokens = new HashMap<String, String>();
                tokens.put("access_token", access);
                response.setContentType(APPLICATION_JSON_VALUE);
                new ObjectMapper().writeValue(response.getOutputStream(), tokens);
            } catch (Exception ex) {
                response.setHeader("error", ex.getMessage());
                response.setStatus(FORBIDDEN.value());
                var error = new HashMap<String, String>();
                error.put("error_message", ex.getMessage());
                response.setContentType(APPLICATION_JSON_VALUE);
                new ObjectMapper().writeValue(response.getOutputStream(), error);
            }
        } else {
            throw new RuntimeException("Refresh token is missing");
        }
    }

    @PostMapping
    public ParticipantDto addParticipant(@Valid @RequestBody ParticipantFullDto dto) {
        var participant = dto.toParticipant();
        participant.setPassword(encoder.encode(participant.getPassword()));
        if (participantRepository.findParticipantsWithEqualEmailAndNonEqualId(participant.getEmail(), 0L).size() != 0) {
            throw new EmailNotUnique(participant.getEmail());
        }
        return modelMapper.map(participantRepository.save(participant), ParticipantDto.class);
    }

}
