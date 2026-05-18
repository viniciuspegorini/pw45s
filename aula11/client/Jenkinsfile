pipeline {
    agent any

    stages {
        stage('Docker Build') {    
            steps {
                sh 'docker build -t react-app:latest .'
            }
        }
        stage('Docker Compose UP') {    
            steps {
                sh 'docker compose up -d '
            }
        }
    }
}