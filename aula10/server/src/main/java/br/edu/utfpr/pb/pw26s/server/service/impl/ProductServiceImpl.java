package br.edu.utfpr.pb.pw26s.server.service.impl;

import br.edu.utfpr.pb.pw26s.server.minio.payload.FileResponse;
import br.edu.utfpr.pb.pw26s.server.minio.service.MinioService;
import br.edu.utfpr.pb.pw26s.server.minio.util.FileTypeUtils;
import br.edu.utfpr.pb.pw26s.server.model.Product;
import br.edu.utfpr.pb.pw26s.server.repository.ProductRepository;
import br.edu.utfpr.pb.pw26s.server.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.List;

@Service
@Slf4j
public class ProductServiceImpl extends CrudServiceImpl<Product, Long>
    implements ProductService {

    private static final String FILE_PATH = File.separator + "uploads";

    private final ProductRepository productRepository;
    private final MinioService minioService;

    public ProductServiceImpl(ProductRepository productRepository, MinioService minioService) {
        this.productRepository = productRepository;
        this.minioService = minioService;
    }

    @Override
    protected JpaRepository<Product, Long> getRepository() {
        return this.productRepository;
    }

    public Product save(Product entity, MultipartFile file) {
        String fileType = FileTypeUtils.getFileType(file);
        if (fileType != null) {
            FileResponse fileResponse = minioService.putObject(file, "commons",
                    fileType);
            entity.setImageName(fileResponse.getFilename());
            entity.setContentType(fileResponse.getContentType());
        }
        return super.save(entity);
    }

    @Override
    public void downloadFile(Long id, HttpServletResponse response) {
        InputStream in = null;
        try {
            Product product = this.findOne(id);
            in = minioService.downloadObject("commons", product.getImageName());
            response.setHeader("Content-Disposition", "attachment;filename="
                    + URLEncoder.encode(product.getImageName(), "UTF-8"));
            response.setCharacterEncoding("UTF-8");
            // Remove bytes from InputStream Copied to the OutputStream .
            IOUtils.copy(in, response.getOutputStream());
        } catch (UnsupportedEncodingException e) {

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

    @Override
    public List<Product> findAll(Specification specification) {
        return productRepository.findAll(specification);
    }
}
