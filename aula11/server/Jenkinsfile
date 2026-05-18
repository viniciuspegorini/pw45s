pipeline {
    agent any

    environment {
        SPRING_PROFILES_ACTIVE="prod"
        POSTGRESQL_CRED = credentials('postgres_id')
        CLIENT_URL="https://viniciuspegorini.br/#"

        DB_JDBC_USER = "${POSTGRESQL_CRED_USR}"
        DB_JDBC_PASSWORD = "${POSTGRESQL_CRED_PSW}"
        SERVER_PORT=8080
        DATABASE_URL="jdbc:postgresql://142.93.204.246:5432/pw45s"
        DATABASE_USERNAME="${POSTGRESQL_CRED_USR}"
        DATABASE_PASSWORD="${POSTGRESQL_CRED_PSW}"

        GOOGLE_CRED = credentials('pw45s_google_client_id')
        GOOGLE_CLIENT_ID="${GOOGLE_CRED_USR}"
        GOOGLE_CLIENT_SECRET="${GOOGLE_CRED_PSW}"

        MINIO_CRED = credentials('pw45s_minio_id')
        MINIO_ENDPOINT="https://minio.viniciuspegorini.com.br"
        MINIO_PORT=443
        MINIO_ACCESS_KEY="${MINIO_CRED_USR}"
        MINIO_SECRET_KEY="${MINIO_CRED_PSW}"
        MINIO_SECURE=true
        MINIO_BUCKET_NAME="pw45s"
    }

    stages {   
        stage('Docker Compose UP') {
            steps {
                sh 'docker compose up -d --build'
            }
        }
    }
}
