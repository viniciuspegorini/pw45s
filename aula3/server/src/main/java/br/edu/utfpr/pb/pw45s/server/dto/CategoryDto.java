package br.edu.utfpr.pb.pw45s.server.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryDto {

    private Long id;

    @NotNull
    @Size(min = 2, max = 50)
    private String name;

}
