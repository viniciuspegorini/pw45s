package br.edu.utfpr.pb.pw26s.server.service;

import br.edu.utfpr.pb.pw26s.server.model.Product;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface ProductService extends CrudService<Product, Long> {

    Product save(Product entity, MultipartFile file);

    void downloadFile(Long id, HttpServletResponse response);

    List<Product> findAll(Specification specification);
}
