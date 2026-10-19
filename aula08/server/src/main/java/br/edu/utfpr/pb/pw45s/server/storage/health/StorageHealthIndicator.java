package br.edu.utfpr.pb.pw45s.server.storage.health;

import br.edu.utfpr.pb.pw45s.server.storage.config.StorageConfig;
import org.springframework.boot.health.contributor.AbstractHealthIndicator;
import org.springframework.boot.health.contributor.Health;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;

/**
 * Verifica se o RustFS está acessível e se o bucket configurado existe.
 * O resultado aparece em /actuator/health (componente "storage") e na tela de detalhes do Spring Boot Admin.
 * Se o RustFS estiver fora do ar, o status da aplicação passa a ser DOWN.
 */
@Component("storage")
public class StorageHealthIndicator extends AbstractHealthIndicator {

    private final S3Client s3Client;
    private final StorageConfig storageConfig;

    public StorageHealthIndicator(S3Client s3Client, StorageConfig storageConfig) {
        super("RustFS health check failed");
        this.s3Client = s3Client;
        this.storageConfig = storageConfig;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) {
        String bucket = storageConfig.getBucketName();
        // lança exceção se o RustFS não responder ou se o bucket não existir; o AbstractHealthIndicator converte em DOWN
        s3Client.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
        builder.up()
                .withDetail("endpoint", storageConfig.getEndpoint())
                .withDetail("bucket", bucket);
    }
}
