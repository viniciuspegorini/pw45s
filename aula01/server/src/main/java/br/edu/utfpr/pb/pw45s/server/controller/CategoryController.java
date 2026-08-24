package br.edu.utfpr.pb.pw45s.server.controller;

import br.edu.utfpr.pb.pw45s.server.dto.CategoryDTO;
import br.edu.utfpr.pb.pw45s.server.mapper.CategoryMapper;
import br.edu.utfpr.pb.pw45s.server.model.Category;
import br.edu.utfpr.pb.pw45s.server.service.ICategoryService;
import br.edu.utfpr.pb.pw45s.server.service.ICrudService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Categories")
@RestController
@RequestMapping("categories")
public class CategoryController extends CrudController<Category, CategoryDTO, Long> {

    private final CategoryMapper categoryMapper;

    public CategoryController(ICategoryService categoryService, CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
        CategoryController.categoryService = categoryService;
    }

    private static ICategoryService categoryService;

    @Override
    protected ICrudService<Category, Long> getService() {
        return categoryService;
    }

    @Override
    protected CategoryDTO toDto(Category entity) {
        return categoryMapper.toDto(entity);
    }

    @Override
    protected Category toEntity(CategoryDTO dto) {
        return categoryMapper.toEntity(dto);
    }

    @Operation(
            summary = "List all categories",
            description = "Returns all registered categories.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of categories",
                            content = @Content(mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = CategoryDTO.class))))
            }
    )
    @Override
    public ResponseEntity<List<CategoryDTO>> findAll() {
        return super.findAll();
    }

    @Operation(
            summary = "List categories with pagination",
            description = "Returns a paginated list of categories. Optional sorting by field and direction.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Paginated list of categories",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Page.class)))
            }
    )
    @Override
    public ResponseEntity<Page<CategoryDTO>> findAll(int page, int size, String order, Boolean asc) {
        return super.findAll(page, size, order, asc);
    }

    @Operation(
            summary = "Find a category by id",
            description = "Returns the category for the given id, or no content if it does not exist.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Category found",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = CategoryDTO.class))),
                    @ApiResponse(responseCode = "204", description = "Category not found")
            }
    )
    @Override
    public ResponseEntity<CategoryDTO> findOne(Long id) {
        return super.findOne(id);
    }

    @Operation(
            summary = "Create a new category",
            description = "Accepts a new category and returns the created category.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Category created",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = CategoryDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid category data")
            }
    )
    @Override
    public ResponseEntity<CategoryDTO> create(CategoryDTO entity) {
        return super.create(entity);
    }

    @Operation(
            summary = "Update a category",
            description = "Updates the category for the given id and returns the updated category.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Category updated",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = CategoryDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid category data")
            }
    )
    @Override
    public ResponseEntity<CategoryDTO> update(Long id, CategoryDTO entity) {
        return super.update(id, entity);
    }

    @Operation(
            summary = "Check if a category exists",
            description = "Returns whether a category with the given id exists.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Existence check result",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Boolean.class)))
            }
    )
    @Override
    public ResponseEntity<Boolean> exists(Long id) {
        return super.exists(id);
    }

    @Operation(
            summary = "Count categories",
            description = "Returns the total number of registered categories.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Total number of categories",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Long.class)))
            }
    )
    @Override
    public ResponseEntity<Long> count() {
        return super.count();
    }

    @Operation(
            summary = "Delete a category",
            description = "Deletes the category for the given id.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Category deleted")
            }
    )
    @Override
    public ResponseEntity<Void> delete(Long id) {
        return super.delete(id);
    }
}
