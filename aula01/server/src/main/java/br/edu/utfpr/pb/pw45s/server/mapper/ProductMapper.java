package br.edu.utfpr.pb.pw45s.server.mapper;

import br.edu.utfpr.pb.pw45s.server.dto.ProductDTO;
import br.edu.utfpr.pb.pw45s.server.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = CategoryMapper.class)
public interface ProductMapper {

    ProductDTO toDto(Product entity);

    Product toEntity(ProductDTO dto);
}
