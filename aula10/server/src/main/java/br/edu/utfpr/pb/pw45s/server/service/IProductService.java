package br.edu.utfpr.pb.pw45s.server.service;

import br.edu.utfpr.pb.pw45s.server.model.Product;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

public interface IProductService extends ICrudService<Product, Long> {

    Product save(Product entity, MultipartFile file);

    void downloadFile(Long id, HttpServletResponse response);
}