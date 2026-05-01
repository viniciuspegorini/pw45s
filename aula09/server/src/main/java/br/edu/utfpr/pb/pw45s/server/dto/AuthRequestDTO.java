package br.edu.utfpr.pb.pw45s.server.dto;

import lombok.Data;

@Data
public class AuthRequestDTO {

    private String username;

    private String password;
}
