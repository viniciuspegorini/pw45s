package br.edu.utfpr.pb.pw45s.server.controller;

import br.edu.utfpr.pb.pw45s.server.dto.ProductDTO;
import br.edu.utfpr.pb.pw45s.server.mapper.ProductMapper;
import br.edu.utfpr.pb.pw45s.server.model.Product;
import br.edu.utfpr.pb.pw45s.server.service.ICrudService;
import br.edu.utfpr.pb.pw45s.server.service.IProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
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

    /* Upload de arquivo salvo no sistema de arquivos
       multipart/form-data = { product: {JSON do produto}, image: arquivo }
    */
    @PostMapping(value = "upload-fs", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductDTO> saveImageFileToDisk(@RequestPart("product") @Valid ProductDTO productDTO,
                                                          @RequestPart("image") MultipartFile file) {
        Product product = toEntity(productDTO);
        productService.saveImageFileToDisk(file, product);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(product));
    }

    /* Upload de arquivo salvo no Banco de dados
       multipart/form-data = { product: {JSON do produto}, image: arquivo }
    */
    @PostMapping(value = "upload-db", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductDTO> saveImageFileToDatabase(@RequestPart("product") @Valid ProductDTO productDTO,
                                                              @RequestPart("image") MultipartFile file) {
        Product product = toEntity(productDTO);
        productService.saveImageFileToDatabase(file, product);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(product));
    }
}
