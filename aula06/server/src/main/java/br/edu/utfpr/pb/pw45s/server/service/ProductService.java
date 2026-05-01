package br.edu.utfpr.pb.pw45s.server.service;

import br.edu.utfpr.pb.pw45s.server.model.Product;
import org.springframework.web.multipart.MultipartFile;

public interface ProductService extends CrudService<Product, Long> {

    void saveImageFileToDisk(MultipartFile file, Product product);

    void saveImageFileToDatabase(MultipartFile file, Product product);

    String getProductImageFileFromDisk(Long id);
}
