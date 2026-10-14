package br.edu.utfpr.pb.pw45s.server.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentication")
@RestController
@RequestMapping
public class AuthControllerDoc {

    @Operation(
            summary = "Perform login",
            description = "Spring Security authentication endpoint. Accepts username and password and returns a JWT token.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Login successful",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(example = "{\"token\": \"jwt-token-here\"}"))),
                    @ApiResponse(responseCode = "401", description = "Invalid credentials")
            }
    )
    @PostMapping("/login")
    public void login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Login credentials",
                    required = true,
                    content = @Content(schema = @Schema(example = "{\"username\": \"user\", \"password\": \"123\"}"))
            )
            @RequestBody(required = true) Object body) {
        // This method is not actually executed.
        // Spring Security intercepts the request before it reaches this controller.
        // It is only defined here for Swagger documentation purposes.
        throw new IllegalStateException("This method is only for Swagger documentation.");
    }
}

