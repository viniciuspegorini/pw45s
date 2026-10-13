# Spring Framework (back-end) - Upload de arquivos utilizando RustFS

## Introdução
Utilizando o serviço de armazenamento de objetos **RustFS** será realizado o *upload* de arquivos para a API. Os arquivos armazenados serão as imagens dos produtos, sendo que para cada produto será possível realizar o *upload* de uma imagem.

O **RustFS** é um serviço de armazenamento de objetos *open source* (licença Apache 2.0), escrito em Rust e compatível com a API do **Amazon S3**. Ele substitui o **MinIO**, utilizado nas versões anteriores desta aula, devido às mudanças recentes na distribuição e no licenciamento da edição comunitária do MinIO. Como o RustFS implementa a API S3, a comunicação da aplicação com o serviço é feita com o **AWS SDK for Java 2.x**, ou seja, o mesmo código funciona com qualquer serviço compatível com S3 (RustFS, Amazon S3, Cloudflare R2, Garage, etc.), bastando alterar o *endpoint* e as credenciais.

## Criando uma instância do RustFS no Docker
Para poder fazer o upload do arquivo para o RustFS será necessário criar um contêiner **docker** para isso, do contrário seria necessário contratar um serviço de nuvem para o armazenamento. Na raiz da pasta do projeto **server** existe um arquivo chamado  **docker-compose.yml**, o qual possui as configurações necessárias para criar um contêiner **docker** com uma instância do **RustFS**, com usuário (*access key*) e senha (*secret key*): "*rustfsadmin*", conforme pode ser observado no código abaixo:

```yml
########################### SERVICES
services:
  ########################### RUSTFS
  rustfs:
    image: rustfs/rustfs:1.0.1
    container_name: rustfs
    environment:
      RUSTFS_ACCESS_KEY: "rustfsadmin"
      RUSTFS_SECRET_KEY: "rustfsadmin"
      RUSTFS_VOLUMES: "/data"
      RUSTFS_ADDRESS: "0.0.0.0:9000"
      RUSTFS_CONSOLE_ENABLE: "true"
      RUSTFS_CONSOLE_ADDRESS: "0.0.0.0:9001"
    volumes:
      - ./rustfs-data:/data
    ports:
      - 9000:9000 # API S3
      - 9001:9001 # Console web
```
As variáveis de ambiente utilizadas são:
- **RUSTFS_ACCESS_KEY** e **RUSTFS_SECRET_KEY**: usuário e senha de acesso ao serviço (equivalentes ao *access key* e *secret key* do S3).
- **RUSTFS_VOLUMES**: diretório, dentro do contêiner, onde os objetos serão armazenados. Esse diretório está mapeado para a pasta **rustfs-data** do projeto (ignorada no **.gitignore**).
- **RUSTFS_ADDRESS**: endereço da API S3 (porta **9000**).
- **RUSTFS_CONSOLE_ENABLE** e **RUSTFS_CONSOLE_ADDRESS**: habilitam o console web de administração (porta **9001**).

Com o **Docker** instalado no computador juntamente com o Docker compose, basta executar o comando:
```cmd
docker compose up -d
```
Com isso um contêiner do RustFS será criado e iniciado, o próximo passo é testar esse contêiner, bastando acessar a URL: [http://127.0.0.1:9001/](http://127.0.0.1:9001/) e utilizar "*rustfsadmin*" como usuário e senha. Com isso a etapa inicial de configuração do RustFS está concluída.

> **Observação:** a imagem do RustFS executa o serviço com um usuário não-root (UID 10001). Em ambientes Linux, caso o contêiner não consiga gravar na pasta **rustfs-data**, ajuste a permissão com o comando `sudo chown -R 10001:10001 rustfs-data`. No Docker Desktop (Windows/macOS) esse ajuste normalmente não é necessário.
>
> Caso esteja migrando de uma versão anterior desta aula, os dados da pasta **data** (formato interno do MinIO) não são reaproveitados pelo RustFS, por isso foi utilizada uma nova pasta (**rustfs-data**).

## Alterações na estrutura do projeto
Abaixo serão listados apenas os arquivos criados e modificados em relação ao projeto **server** da **aula7**:
- Arquivo **pom.xml**.
- Arquivo **application.yml**.
- Classe **ProductController** no pacote *controller*.
- Classe **ProductDto** no pacote *dto*.
- No pacote *storage* (nome genérico, pois as classes utilizam apenas a API S3 e funcionam com qualquer serviço compatível):
  - Classe **StorageConfig** no pacote *config*.
  - Classe **FileResponse** no pacote *payload*.
  - Classes **StorageService** e **StorageServiceImpl** nos pacotes *service* e *service.imp*, respectivamente.
  - Classe **FileTypeUtils** no pacote *util*.
  - Classe **StorageUtil** no pacote *util*.
- Classe **Product** no pacote *model*.
- Classes **ProductService** e **ProductServiceImpl** nos pacotes *service* e *service.impl*, respectivamente.

### Modificações nos arquivos pom.xml e application.yml

No arquivo **pom.xml** foi adicionado o **BOM** (*Bill of Materials*) do **AWS SDK for Java 2.x** na seção *dependencyManagement*, o qual define a versão de todos os módulos do SDK, e a dependência do módulo **s3**, utilizado para o *upload* e *download* de arquivos para o serviço de armazenamento. A dependência do **Apache commons-lang3** é utilizada para validação de *strings* e a dependência do **hutool-all** é utilizada para otimizar o trabalho com os tipos de arquivo.

```xml
<!-- ... -->
	<properties>
		<!-- ... -->
		<awssdk.version>2.55.11</awssdk.version>
	</properties>
	<dependencies>
		<!-- ... -->
		<!-- File Storage compatível com S3 (RustFS) -->
		<dependency>
			<groupId>software.amazon.awssdk</groupId>
			<artifactId>s3</artifactId>
		</dependency>
		<dependency>
			<groupId>org.apache.commons</groupId>
			<artifactId>commons-lang3</artifactId>
		</dependency>
		<dependency>
			<groupId>cn.hutool</groupId>
			<artifactId>hutool-all</artifactId>
			<version>5.8.40</version>
		</dependency>
	</dependencies>

	<dependencyManagement>
		<dependencies>
			<dependency>
				<groupId>software.amazon.awssdk</groupId>
				<artifactId>bom</artifactId>
				<version>${awssdk.version}</version>
				<type>pom</type>
				<scope>import</scope>
			</dependency>
		</dependencies>
	</dependencyManagement>
<!-- ... -->
```
No arquivo **application.yml** foram adicionadas as configurações para acesso ao serviço do **RustFS**. A propriedade **region** é obrigatória para o SDK da AWS, porém não é utilizada pelo RustFS, por isso é utilizado o valor padrão *us-east-1*.

```yml
#...
storage:
  endpoint: http://127.0.0.1:9000
  port: 9000
  accessKey:  rustfsadmin #Login Account
  secretKey:  rustfsadmin # Login Password
  secure: false
  region: us-east-1 # Region exigida pelo SDK S3 (o RustFS não a utiliza)
  bucket-name: commons # Bucket Name
  image-size: 10485760 #  Maximum size of picture file
  file-size: 104857600 #  Maximum file size
#...
```


### Modificações na classe ProductController
Nessa classe foi criado um novo método para tratar o objeto produto que está vindo do lado cliente, juntamente com o arquivo da imagem, o *saveProduct()*. E, um método para *download* do arquivo armazenado no RustFS, o *downloadFile()*.

```java
@RestController
@RequestMapping("products")
@Slf4j
public  class ProductController extends CrudController<Product, ProductDto, Long> {
	private  final  ProductService productService;
	private  final  ModelMapper modelMapper;
	public ProductController(ProductService productService, ModelMapper modelMapper) {
		super(Product.class, ProductDto.class);
		this.productService = productService;
		this.modelMapper = modelMapper;
	}
	@Override
	protected  CrudService<Product, Long> getService() {
		return  this.productService;
	}
	@Override
	protected  ModelMapper getModelMapper() {
		return  this.modelMapper;
	}

	/*
		Será realizada uma requisição a partir do client enviando um FormData.
		O corpo da requisição será um JSON com {"product": {...}, "image": ...}
		Que será tratado na classe Service.
	*/
	@PostMapping(value = "upload", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_OCTET_STREAM_VALUE})
	public  Product saveProduct(@RequestPart("product") @Valid  Product entity, @RequestPart("image") @Valid  MultipartFile file) {
		return productService.save(entity, file);
	}
	//A requisição HTTP GET irá vir com o código do produto e irá retornar a imagem no corpo da resposta.
	@GetMapping(value = "download/{id}")
	public  void downloadFile(@PathVariable("id") Long id, HttpServletResponse response) {
		productService.downloadFile(id, response);
	}
}
```

### Modificações nas classes ProductDto e Product

As únicas modificações nas classes **ProductDto** e **Product** foram a adição dos atributos **imageName** e **contentType**, para armazenar o nome da imagem gerado pela aplicação ao enviar o arquivo para o **RustFS** e o seu **content-type**.
```java
public  class ProductDto {
	//...
	private  String imageName;
	private  String contentType;
}
```

### Modificações nas classes ProductService e ProductServiceImpl

Na classe **ProductService** estão apenas as assinaturas dos métodos para salvar um novo produto e armazenar a imagem e para *download* da imagem.  No **ProductServiceImpl** está a implementação desses métodos. Essa classe precisa de uma instância de um objeto da classe **StorageService** que é onde estão as implementações de *download*, *upload* e demais operações de acesso ao serviço de armazenamento (**RustFS**).

```java
package br.edu.utfpr.pb.pw45s.server.service.impl;

import br.edu.utfpr.pb.pw45s.server.storage.payload.FileResponse;
import br.edu.utfpr.pb.pw45s.server.storage.service.StorageService;
import br.edu.utfpr.pb.pw45s.server.storage.util.FileTypeUtils;
import br.edu.utfpr.pb.pw45s.server.model.Product;
import br.edu.utfpr.pb.pw45s.server.repository.ProductRepository;
import br.edu.utfpr.pb.pw45s.server.service.IProductService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
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

    private final StorageService storageService;

    public ProductServiceImpl(ProductRepository productRepository, StorageService storageService) {
        this.productRepository = productRepository;
        this.storageService = storageService;
    }

    @Override
    protected JpaRepository<Product, Long> getRepository() {
        return productRepository;
    }

    @Override
    public Product save(Product entity, MultipartFile file) {
        String fileType = FileTypeUtils.getFileType(file);
        if (fileType != null) {
            FileResponse fileResponse = storageService.putObject(file, "commons",
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
            Product product = this.findById(id);
            in = storageService.downloadObject("commons", product.getImageName());
            response.setHeader("Content-Disposition", "attachment;filename="
                    + URLEncoder.encode(product.getImageName(), "UTF-8"));
            response.setCharacterEncoding("UTF-8");
            // Remove bytes from InputStream Copied to the OutputStream .
            in.transferTo(response.getOutputStream());
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
```
Para copiar o conteúdo do arquivo para a resposta HTTP é utilizado o método `transferTo()` da classe `InputStream` do próprio Java, dispensando a dependência do **Apache commons-io**.

### Classes do pacote *`storage`*

#### StorageConfig
A classe **StorageConfig** que está dentro do pacote *config* lê as configurações disponibilizadas no arquivo **application.yml** e instancia dois *beans* do **AWS SDK**:
- **S3Client**: cliente utilizado para as operações de *upload*, *download*, criação de *buckets*, etc.
- **S3Presigner**: utilizado para gerar URLs pré-assinadas (URLs temporárias de acesso a um arquivo privado).

Algumas configurações são necessárias para utilizar o SDK com um serviço compatível com S3 que não seja a própria AWS:
- **endpointOverride()**: endereço do RustFS (por padrão o SDK utiliza os endereços da AWS).
- **forcePathStyle(true)**: os *buckets* são acessados pelo caminho da URL (`http://127.0.0.1:9000/commons/arquivo.png`), e não por subdomínio (`http://commons.127.0.0.1:9000/arquivo.png`), que é o padrão da AWS.
- **requestChecksumCalculation()** e **responseChecksumValidation()** com o valor `WHEN_REQUIRED`: a partir da versão 2.30 o SDK envia *checksums* em todas as requisições, o que pode gerar incompatibilidade com serviços compatíveis com S3. Com essa configuração os *checksums* são enviados apenas quando a operação exige.

```java
package br.edu.utfpr.pb.pw45s.server.storage.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.checksums.RequestChecksumCalculation;
import software.amazon.awssdk.core.checksums.ResponseChecksumValidation;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Data
@Configuration
@ConfigurationProperties(prefix = "storage")
public class StorageConfig {

    /** *  It's a URL, domain name ,IPv4 perhaps IPv6 Address ") */
    private String endpoint;

    /** * //"TCP/IP Port number " */
    private Integer port;

    /** * //"accessKey Similar to user ID, Used to uniquely identify your account " */
    private String accessKey;

    /** * //"secretKey It's the password for your account " */
    private String secretKey;

    /** * //" If it is true, It uses https instead of http, The default value is true" */
    private boolean secure;

    /** * //" Region required by the S3 SDK (ignored by RustFS) " */
    private String region = "us-east-1";

    /** * //" Default bucket " */
    private String bucketName;

    /** *  The maximum size of the picture  */
    private long imageSize;

    /** *  Maximum size of other files  */
    private long fileSize;

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .endpointOverride(endpointUri())
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider())
                // RustFS (like most S3-compatible services) addresses buckets by path: http://host:9000/bucket/object
                .forcePathStyle(true)
                // Only send/validate checksums when the operation requires it (better S3 compatibility)
                .requestChecksumCalculation(RequestChecksumCalculation.WHEN_REQUIRED)
                .responseChecksumValidation(ResponseChecksumValidation.WHEN_REQUIRED)
                .build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .endpointOverride(endpointUri())
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider())
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build())
                .build();
    }

    private StaticCredentialsProvider credentialsProvider() {
        return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));
    }

    private URI endpointUri() {
        URI uri = URI.create(endpoint);
        String host = uri.getHost() != null ? uri.getHost() : endpoint;
        int endpointPort = port != null ? port : uri.getPort();
        return URI.create((secure ? "https" : "http") + "://" + host + ":" + endpointPort);
    }
}
```

#### FileResponse

A classe **FileResponse** será utilizada para o retorno do objeto adicionado no serviço de armazenamento contendo os campos que serão utilizados para armazenar no banco de dados da aplicação.

```java
package br.edu.utfpr.pb.pw45s.server.storage.payload;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public  class FileResponse {
	String filename;
	String contentType;
	Long fileSize;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "GMT")
	private  LocalDateTime createdTime;
}
```

#### StorageService e StorageServiceImpl

As classes **StorageService** e **StorageServiceImpl** contém a assinatura e implementação dos principais métodos utilizados para acesso aos serviços disponibilizados pelo serviço de armazenamento.

```java
import software.amazon.awssdk.services.s3.model.Bucket;
//...
public  interface StorageService {
	// Upload files in the bucket
	FileResponse putObject(MultipartFile multipartFile, String bucketName, String fileType);
	// Download file from bucket
	InputStream downloadObject(String bucketName, String objectName);
	//Check whether bucket already exists
	boolean bucketExists(String bucketName);
	// Create a bucket
	void makeBucket(String bucketName);
	// List all bucket names
	List<String> listBucketName();
	//List all buckets
	List<Bucket> listBuckets();
	// Delete Bucket by Name
	boolean removeBucket(String bucketName);
	// List all object names in the bucket
	List<String> listObjectNames(String bucketName);
	// Delete file in bucket
	boolean removeObject(String bucketName, String objectName);
	// Delete files in bucket
	boolean removeListObject(String bucketName, List<String> objectNameList);
	// Get file path from bucket
	String getObjectUrl(String bucketName,String objectName);
}
```
O principal método utilizado neste projeto é o ***putObject()***, que recebe o arquivo, o nome do *bucket* em que o arquivo será armazenado e o tipo do arquivo. E, utilizando os métodos da classe **StorageUtil** verifica se o *bucket* existe, caso não exista é criado e então o arquivo é enviado para o serviço do **RustFS**.
```java
package br.edu.utfpr.pb.pw45s.server.storage.service.impl;
import br.edu.utfpr.pb.pw45s.server.storage.config.StorageConfig;
import br.edu.utfpr.pb.pw45s.server.storage.payload.FileResponse;
import br.edu.utfpr.pb.pw45s.server.storage.service.StorageService;
import br.edu.utfpr.pb.pw45s.server.storage.util.StorageUtil;
import software.amazon.awssdk.services.s3.model.Bucket;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public  class StorageServiceImpl implements StorageService {
	private  final  StorageUtil storageUtil;
	private  final  StorageConfig storageProperties;
	
	public StorageServiceImpl(StorageUtil storageUtil, StorageConfig storageProperties) {
		this.storageUtil = storageUtil;
		this.storageProperties = storageProperties;
	}

	@SneakyThrows
	@Override
	public  FileResponse putObject(MultipartFile multipartFile, String bucketName, String fileType) {
		try {
			bucketName = StringUtils.isNotBlank(bucketName) ? bucketName : storageProperties.getBucketName();
			if (!this.bucketExists(bucketName)) {
				this.makeBucket(bucketName);
			}
			String fileName = multipartFile.getOriginalFilename();
			Long fileSize = multipartFile.getSize();
			String objectName = UUID.randomUUID().toString().replaceAll("-", "") + fileName.substring(fileName.lastIndexOf("."));
			LocalDateTime createdTime = LocalDateTime.now();
			storageUtil.putObject(bucketName, multipartFile, objectName,fileType);
			return FileResponse.builder().filename(objectName).fileSize(fileSize)
						.contentType(fileType).createdTime(createdTime)
						.build();
		} catch (Exception e) {
			return  null;
		}
	}
	
	@Override
	public  InputStream downloadObject(String bucketName, String objectName) {
		return storageUtil.getObject(bucketName,objectName);
	}
	
	@Override
	public  boolean bucketExists(String bucketName) {
		return storageUtil.bucketExists(bucketName);
	}
	@Override
	public  void makeBucket(String bucketName) {
		storageUtil.makeBucket(bucketName);
	}
	@Override
	public  List<String> listBucketName() {
		return storageUtil.listBucketNames();
	}
	@Override
	public  List<Bucket> listBuckets() {
		return storageUtil.listBuckets();
	}
	@Override
	public  boolean removeBucket(String bucketName) {
		return storageUtil.removeBucket(bucketName);
	}
	@Override
	public  List<String> listObjectNames(String bucketName) {
		return storageUtil.listObjectNames(bucketName);
	}
	@Override
	public  boolean removeObject(String bucketName, String objectName) {
		return storageUtil.removeObject(bucketName, objectName);
	}
	@Override
	public  boolean removeListObject(String bucketName, List<String> objectNameList) {
		return storageUtil.removeObject(bucketName,objectNameList);
	}
	@Override
	public  String getObjectUrl(String bucketName, String objectName) {
		return storageUtil.getObjectUrl(bucketName, objectName);
	}
}
```
#### FileTypeUtils e StorageUtil

A classe **FileTypeUtils** possui apenas um método o ***getFileType()*** que recebe o arquivo e retorna o tipo de arquivo do mesmo.
```java
package br.edu.utfpr.pb.pw45s.server.storage.util;
import cn.hutool.core.io.FileTypeUtil;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;

public  class FileTypeUtils {
	private  final  static  String IMAGE_TYPE = "image/";
	private  final  static  String AUDIO_TYPE = "audio/";
	private  final  static  String VIDEO_TYPE = "video/";
	private  final  static  String APPLICATION_TYPE = "application/";
	private  final  static  String TXT_TYPE = "text/";

	public  static  String getFileType(MultipartFile multipartFile) {
		InputStream inputStream = null;
		String type = null;
		try {
			inputStream = multipartFile.getInputStream();
			type = FileTypeUtil.getType(inputStream);
			if (type.equalsIgnoreCase("JPG") || type.equalsIgnoreCase("JPEG")
				|| type.equalsIgnoreCase("GIF") || type.equalsIgnoreCase("PNG")
				|| type.equalsIgnoreCase("BMP") || type.equalsIgnoreCase("PCX")
				|| type.equalsIgnoreCase("TGA") || type.equalsIgnoreCase("PSD")
				|| type.equalsIgnoreCase("TIFF")) {
				return IMAGE_TYPE+type;
			}
			if (type.equalsIgnoreCase("mp3") || type.equalsIgnoreCase("OGG")
				|| type.equalsIgnoreCase("WAV") || type.equalsIgnoreCase("REAL")
				|| type.equalsIgnoreCase("APE") || type.equalsIgnoreCase("MODULE")
				|| type.equalsIgnoreCase("MIDI") || type.equalsIgnoreCase("VQF")
				|| type.equalsIgnoreCase("CD")) {
				return AUDIO_TYPE+type;
			}
			if (type.equalsIgnoreCase("mp4") || type.equalsIgnoreCase("avi")
				|| type.equalsIgnoreCase("MPEG-1") || type.equalsIgnoreCase("RM")
				|| type.equalsIgnoreCase("ASF") || type.equalsIgnoreCase("WMV")
				|| type.equalsIgnoreCase("qlv") || type.equalsIgnoreCase("MPEG-2")
				|| type.equalsIgnoreCase("MPEG4") || type.equalsIgnoreCase("mov")
				|| type.equalsIgnoreCase("3gp")) {
				return VIDEO_TYPE+type;
			}
			if (type.equalsIgnoreCase("doc") || type.equalsIgnoreCase("docx")
				|| type.equalsIgnoreCase("ppt") || type.equalsIgnoreCase("pptx")
				|| type.equalsIgnoreCase("xls") || type.equalsIgnoreCase("xlsx")
				|| type.equalsIgnoreCase("zip")||type.equalsIgnoreCase("jar")) {
				return APPLICATION_TYPE+type;
			}
			if (type.equalsIgnoreCase("txt")) {
				return TXT_TYPE+type;
			}
		}catch (IOException e){
		}
		return  null;
	}
}
```

Já a classe **StorageUtil** é onde estão implementados os métodos de comunicação direto com a API S3 do serviço **RustFS**, por meio dos objetos **S3Client** e **S3Presigner**. Sendo o principal método o ***putObject()*** , responsável pelo upload do arquivo para o serviço de armazenamento. Cada operação do SDK recebe um objeto de requisição (`PutObjectRequest`, `GetObjectRequest`, `HeadBucketRequest`, etc.) criado com o padrão *builder*, e o conteúdo do arquivo é enviado por meio da classe `RequestBody`. No S3 o nome do arquivo dentro do *bucket* é chamado de *key*.

```java
package br.edu.utfpr.pb.pw45s.server.storage.util;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class StorageUtil {
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    // Bucket policy that allows anonymous read (GET) of the objects, used by the client to display the images
    private static final String PUBLIC_READ_POLICY = """
            {
              "Version": "2012-10-17",
              "Statement": [{
                "Effect": "Allow",
                "Principal": {"AWS": ["*"]},
                "Action": ["s3:GetObject"],
                "Resource": ["arn:aws:s3:::%s/*"]
              }]
            }""";

    // Upload Files
    @SneakyThrows
    public void putObject(String bucketName, MultipartFile multipartFile, String filename, String fileType) {
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(filename)
                        .contentType(fileType)
                        .build(),
                RequestBody.fromBytes(multipartFile.getBytes()));
    }

    // Get a file object as a stream from the specified bucket
    @SneakyThrows
    public InputStream getObject(String bucketName, String objectName) {
        boolean flag = bucketExists(bucketName);
        if (flag) {
            HeadObjectResponse statObject = statObject(bucketName, objectName);
            if (statObject != null && statObject.contentLength() > 0) {
                return s3Client.getObject(
                        GetObjectRequest.builder()
                                .bucket(bucketName)
                                .key(objectName)
                                .build());
            }
        }
        return null;
    }

    // Check if bucket name exists
    @SneakyThrows
    public boolean bucketExists(String bucketName) {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
            return true;
        } catch (NoSuchBucketException e) {
            return false;
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                return false;
            }
            throw e;
        }
    }

    // Create bucket name (with public read access to its objects)
    @SneakyThrows
    public boolean makeBucket(String bucketName) {
        boolean flag = bucketExists(bucketName);

        if (!flag) {
            s3Client.createBucket(
                    CreateBucketRequest.builder()
                            .bucket(bucketName)
                            .build());
            s3Client.putBucketPolicy(
                    PutBucketPolicyRequest.builder()
                            .bucket(bucketName)
                            .policy(PUBLIC_READ_POLICY.formatted(bucketName))
                            .build());
            return true;
        } else {
            return false;
        }
    }

    // List all buckets
    @SneakyThrows
    public List<Bucket> listBuckets() {
        return s3Client.listBuckets().buckets();
    }

    // List all bucket names
    @SneakyThrows
    public List<String> listBucketNames() {
        List<Bucket> bucketList = listBuckets();

        List<String> bucketListName = new ArrayList<>();
        for (Bucket bucket : bucketList) {
            bucketListName.add(bucket.name());
        }
        return bucketListName;
    }

    // List all objects from the specified bucket
    @SneakyThrows
    public Iterable<S3Object> listObjects(String bucketName) {
        boolean flag = bucketExists(bucketName);
        if (flag) {
            return s3Client.listObjectsV2Paginator(
                    ListObjectsV2Request.builder().bucket(bucketName).build()).contents();
        }
        return null;
    }

    // Delete Bucket by its name from the specified bucket
    @SneakyThrows
    public boolean removeBucket(String bucketName) {
        boolean flag = bucketExists(bucketName);
        if (flag) {
            Iterable<S3Object> myObjects = listObjects(bucketName);

            for (S3Object item : myObjects) {
                //  Delete failed when There are object files in bucket
                if (item.size() > 0) {
                    return false;
                }
            }

            //  Delete bucket when bucket is empty
            s3Client.deleteBucket(DeleteBucketRequest.builder().bucket(bucketName).build());
            flag = bucketExists(bucketName);

            return !flag;
        }
        return false;
    }

    // List all object names from the specified bucket
    @SneakyThrows
    public List<String> listObjectNames(String bucketName) {
        List<String> listObjectNames = new ArrayList<>();
        boolean flag = bucketExists(bucketName);

        if (flag) {
            Iterable<S3Object> myObjects = listObjects(bucketName);
            for (S3Object item : myObjects) {
                listObjectNames.add(item.key());
            }
        } else {
            listObjectNames.add("Bucket does not exist");
        }

        return listObjectNames;
    }

    // Delete object from the specified bucket
    @SneakyThrows
    public boolean removeObject(String bucketName, String objectName) {
        boolean flag = bucketExists(bucketName);
        if (flag) {
            s3Client.deleteObject(
                    DeleteObjectRequest.builder().bucket(bucketName).key(objectName).build());
            return true;
        }
        return false;
    }

    // Get file path from the specified bucket
    @SneakyThrows
    public String getObjectUrl(String bucketName, String objectName) {
        boolean flag = bucketExists(bucketName);
        String url = "";
        if (flag) {
            url = s3Presigner.presignGetObject(
                    GetObjectPresignRequest.builder()
                            .signatureDuration(Duration.ofMinutes(2))
                            .getObjectRequest(GetObjectRequest.builder()
                                    .bucket(bucketName)
                                    .key(objectName)
                                    .build())
                            .build())
                    .url().toString();
        }
        return url;
    }

    // Get metadata of the object from the specified bucket
    @SneakyThrows
    public HeadObjectResponse statObject(String bucketName, String objectName) {
        boolean flag = bucketExists(bucketName);
        if (flag) {
            return s3Client.headObject(
                    HeadObjectRequest.builder().bucket(bucketName).key(objectName).build());
        }
        return null;
    }

    // Get a file object as a stream from the specified bucket （ Breakpoint download )
    @SneakyThrows
    public InputStream getObject(String bucketName, String objectName, long offset, Long length) {
        boolean flag = bucketExists(bucketName);
        if (flag) {
            HeadObjectResponse statObject = statObject(bucketName, objectName);
            if (statObject != null && statObject.contentLength() > 0) {
                return s3Client.getObject(
                        GetObjectRequest.builder()
                                .bucket(bucketName)
                                .key(objectName)
                                .range("bytes=" + offset + "-" + (offset + length - 1))
                                .build());
            }
        }
        return null;
    }

    // Delete multiple file objects from the specified bucket
    @SneakyThrows
    public boolean removeObject(String bucketName, List<String> objectNames) {
        boolean flag = bucketExists(bucketName);
        if (flag) {
            List<ObjectIdentifier> objects = new ArrayList<>();
            for (String objectName : objectNames) {
                objects.add(ObjectIdentifier.builder().key(objectName).build());
            }
            DeleteObjectsResponse response = s3Client.deleteObjects(
                    DeleteObjectsRequest.builder()
                            .bucket(bucketName)
                            .delete(Delete.builder().objects(objects).build())
                            .build());

            return response.errors().isEmpty();
        }
        return true;
    }

    // Upload InputStream object to the specified bucket
    @SneakyThrows
    public boolean putObject(String bucketName, String objectName, InputStream inputStream, String contentType) {
        boolean flag = bucketExists(bucketName);

        if (flag) {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(objectName)
                            .contentType(contentType)
                            .build(),
                    RequestBody.fromBytes(inputStream.readAllBytes()));
            HeadObjectResponse statObject = statObject(bucketName, objectName);
            if (statObject != null && statObject.contentLength() > 0) {
                return true;
            }
        }
        return false;
    }
}
```
Com as implementações acima é possível realizar o *upload* e gerenciamento do serviço **RustFS**, na aplicação **server** será possível realizar o *download* do arquivo também.

### Acesso público aos arquivos

Por padrão os *buckets* do RustFS são privados. Como a aplicação **client** exibe as imagens dos produtos acessando diretamente a URL do arquivo no RustFS (`http://localhost:9000/commons/<nome-da-imagem>`), é necessário permitir a leitura anônima dos arquivos do *bucket*. Para isso, no método ***makeBucket()*** da classe **StorageUtil**, logo após a criação do *bucket*, é aplicada uma *bucket policy* (no mesmo formato de políticas da AWS) que permite a ação **s3:GetObject** para qualquer usuário (`"Principal": {"AWS": ["*"]}`), ou seja, qualquer portador da URL do arquivo vai poder visualizá-lo, apenas com permissão de leitura:

```json
{
  "Version": "2012-10-17",
  "Statement": [{
    "Effect": "Allow",
    "Principal": {"AWS": ["*"]},
    "Action": ["s3:GetObject"],
    "Resource": ["arn:aws:s3:::commons/*"]
  }]
}
```
Assim, ao realizar o primeiro *upload* o *bucket* **commons** é criado já com a permissão de leitura pública. Caso o *bucket* tenha sido criado manualmente, a mesma política pode ser configurada pelo console do RustFS, acessando a URL [http://127.0.0.1:9001/](http://127.0.0.1:9001/), selecionando o *bucket* **commons** e alterando a sua política de acesso para pública (somente leitura).

Outra alternativa, caso os arquivos não devam ser públicos, é manter o *bucket* privado e utilizar o *endpoint* `GET /products/download/{id}` da API ou as URLs temporárias geradas pelo método ***getObjectUrl()***.

# Referências

[1] RustFS, https://rustfs.com/. Acesso em: 05/10/2026.

[2] RustFS - Documentação, https://docs.rustfs.com/. Acesso em: 05/10/2026.

[3] AWS SDK for Java 2.x - Amazon S3, https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/examples-s3.html. Acesso em: 05/10/2026.

[4] Minio, https://min.io/. Acesso em: 19/11/2024.
