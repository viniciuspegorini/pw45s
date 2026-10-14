package br.edu.utfpr.pb.pw45s.server.controller;

import br.edu.utfpr.pb.pw45s.server.dto.ProductDTO;
import br.edu.utfpr.pb.pw45s.server.mapper.ProductMapper;
import br.edu.utfpr.pb.pw45s.server.model.Product;
import br.edu.utfpr.pb.pw45s.server.service.ICrudService;
import br.edu.utfpr.pb.pw45s.server.service.IProductService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("products")
public class ProductController extends CrudController<Product, ProductDTO, Long> {

    private final ProductMapper productMapper;

    public ProductController(IProductService productService, ProductMapper productMapper) {
        this.productMapper = productMapper;
        ProductController.productService = productService;
    }

    private static IProductService productService;

    @Override
    protected ICrudService<Product, Long> getService() {
        return productService;
    }

    @Override
    protected ProductDTO toDto(Product entity) {
        return productMapper.toDto(entity);
    }

    @Override
    protected Product toEntity(ProductDTO dto) {
        return productMapper.toEntity(dto);
    }

    @PostMapping(value = "upload", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_OCTET_STREAM_VALUE})
    public Product saveProduct(@RequestPart("product") @Valid Product entity,
                               @RequestPart("image") @Valid MultipartFile file) {
        return productService.save(entity, file);
    }

    @GetMapping(value = "download/{id}")
    public void downloadFile(@PathVariable Long id, HttpServletResponse response) {
        productService.downloadFile(id, response);
    }
}