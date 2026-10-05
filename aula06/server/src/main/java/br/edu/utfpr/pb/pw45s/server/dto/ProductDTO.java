package br.edu.utfpr.pb.pw45s.server.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {

    private Long id;

    @NotNull
    private String name;

    @NotNull
    private String description;

    @NotNull
    private BigDecimal price;

    private CategoryDTO category;

    private String imageName; // Upload no Sistema de Arquivos (disco)

    private byte[] imageFile; // Upload no Banco de dados (enviado no JSON em Base64)

    private String imageFileName; // Upload no Banco de dados
}