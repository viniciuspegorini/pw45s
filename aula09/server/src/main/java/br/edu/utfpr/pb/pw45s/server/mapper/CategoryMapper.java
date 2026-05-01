package br.edu.utfpr.pb.pw45s.server.mapper;

import br.edu.utfpr.pb.pw45s.server.dto.CategoryDTO;
import br.edu.utfpr.pb.pw45s.server.model.Category;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CategoryMapper {

    CategoryDTO toDto(Category entity);

    Category toEntity(CategoryDTO dto);
}
