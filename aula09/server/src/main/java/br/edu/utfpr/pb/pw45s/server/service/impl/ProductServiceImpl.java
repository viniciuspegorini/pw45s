package br.edu.utfpr.pb.pw45s.server.service.impl;

//import br.edu.utfpr.pb.pw45s.server.minio.payload.FileResponse;
//import br.edu.utfpr.pb.pw45s.server.minio.service.MinioService;
//import br.edu.utfpr.pb.pw45s.server.minio.util.FileTypeUtils;
import br.edu.utfpr.pb.pw45s.server.model.Product;
import br.edu.utfpr.pb.pw45s.server.repository.ProductRepository;
import br.edu.utfpr.pb.pw45s.server.service.IProductService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.io.IOUtils;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;

@Service
@Slf4j
public class ProductServiceImpl extends CrudServiceImpl<Product, Long>
        implements IProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    protected JpaRepository<Product, Long> getRepository() {
        return productRepository;
    }

    @Override
    public Product save(Product entity, MultipartFile file) {
//        String fileType = FileTypeUtils.getFileType(file);
//        if (fileType != null) {
//            FileResponse fileResponse = minioService.putObject(file, "commons",
//                    fileType);
//            entity.setImageName(fileResponse.getFilename());
//            entity.setContentType(fileResponse.getContentType());
//        }
        return super.save(entity);
    }

    @Override
    public void downloadFile(Long id, HttpServletResponse response) {
        InputStream in = null;
        try {
            Product product = this.findById(id);
            // in = minioService.downloadObject("commons", product.getImageName());
            response.setHeader("Content-Disposition", "attachment;filename="
                    + URLEncoder.encode(product.getImageName(), "UTF-8"));
            response.setCharacterEncoding("UTF-8");
            // Remove bytes from InputStream Copied to the OutputStream .
            // IOUtils.copy(in, response.getOutputStream());
        } catch (IOException e) {
            log.error(e.getMessage());
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    log.error(e.getMessage());
                }
            }
        }
    }
}
