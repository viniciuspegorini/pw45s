package br.edu.utfpr.pb.pw26s.server.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@Data
public class CategoryDto {

    private Long id;

    @NotNull
    @Size(min = 2, max = 50)
    private String name;

    private LocalDate testDate;
}
