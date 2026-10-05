package br.edu.utfpr.pb.pw45s.server.service.impl;

import br.edu.utfpr.pb.pw45s.server.model.Product;
import br.edu.utfpr.pb.pw45s.server.repository.ProductRepository;
import br.edu.utfpr.pb.pw45s.server.service.IProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Locale;
import java.util.Set;

@Service
@Slf4j
public class ProductServiceImpl extends CrudServiceImpl<Product, Long>
        implements IProductService {

    // Extensões de imagem aceitas no upload
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp");

    private final ProductRepository productRepository;
    // Pasta em que as imagens são armazenadas no sistema de arquivos (propriedade app.upload.directory)
    private final Path imagesDirectory;

    public ProductServiceImpl(ProductRepository productRepository,
                              @Value("${app.upload.directory}") String uploadDirectory) {
        this.productRepository = productRepository;
        this.imagesDirectory = Path.of(uploadDirectory, "images-product").toAbsolutePath().normalize();
    }

    @Override
    protected JpaRepository<Product, Long> getRepository() {
        return productRepository;
    }

    /* Salva o produto e armazena o arquivo no sistema de arquivos (disco), na pasta definida em
       app.upload.directory. O arquivo é salvo com o nome: <id do produto>.<extensão>, ex.: 1.png
       O produto e a imagem são salvos na mesma transação: se a imagem for inválida, o produto não é salvo.
     */
    @Override
    @Transactional
    public void saveImageFileToDisk(MultipartFile file, Product product) {
        String extension = getImageExtension(file);
        productRepository.save(product); // gera o id do produto, utilizado no nome do arquivo
        String fileName = product.getId() + extension;
        try {
            Files.createDirectories(imagesDirectory);
            Files.write(imagesDirectory.resolve(fileName), file.getBytes());

            product.setImageName(fileName);
            productRepository.save(product);
        } catch (IOException e) {
            log.error("Error in saveImageFileToDisk() - {}", e.getMessage());
            throw new UncheckedIOException(e);
        }
    }

    /* Salva o produto e armazena o arquivo no Banco de Dados (coluna image_file)
     */
    @Override
    @Transactional
    public void saveImageFileToDatabase(MultipartFile file, Product product) {
        String extension = getImageExtension(file);
        productRepository.save(product); // gera o id do produto, utilizado no nome do arquivo
        try {
            product.setImageFileName(product.getId() + extension);
            product.setImageFile(file.getBytes());
            productRepository.save(product);
        } catch (IOException e) {
            log.error("Error in saveImageFileToDatabase() - {}", e.getMessage());
            throw new UncheckedIOException(e);
        }
    }

    /* Retorna o arquivo armazenado em disco no formato Base64
     */
    @Override
    public String getProductImageFileFromDisk(Long id) {
        Product product = productRepository.findById(id).orElse(null);
        if (product == null || product.getImageName() == null) {
            return null;
        }
        try {
            byte[] image = Files.readAllBytes(imagesDirectory.resolve(product.getImageName()));
            return Base64.getEncoder().encodeToString(image);
        } catch (IOException e) {
            log.error("Error in getProductImageFileFromDisk() - {}", e.getMessage());
            throw new UncheckedIOException(e);
        }
    }

    /* Valida o arquivo enviado e retorna a sua extensão (ex.: ".png").
       O nome do arquivo é informado pelo cliente e não é confiável: somente a extensão é aproveitada,
       e apenas se estiver na lista de extensões permitidas. Isso impede, por exemplo, que um nome como
       "imagem.png/../../arquivo" faça o arquivo ser gravado fora da pasta de upload (path traversal).
     */
    private String getImageExtension(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Nenhuma imagem foi enviada.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("O arquivo enviado não é uma imagem.");
        }
        String originalFilename = file.getOriginalFilename();
        int dot = originalFilename == null ? -1 : originalFilename.lastIndexOf('.');
        String extension = dot < 0 ? "" : originalFilename.substring(dot).toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Extensão de imagem não permitida. Utilize: " + ALLOWED_EXTENSIONS);
        }
        return extension;
    }
}
