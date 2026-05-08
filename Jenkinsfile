pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out code from GitHub...'
                checkout scm
            }
        }

        stage('Build') {
            steps {
                echo 'Building with Maven...'
                bat 'mvnw.cmd clean install -DskipTests'
            }
        }

        stage('Test') {
            steps {
                echo 'Running tests...'
                bat 'mvnw.cmd test'
            }
        }

        stage('Package') {
            steps {
                echo 'Packaging JAR...'
                bat 'mvnw.cmd package -DskipTests'
            }
        }

        stage('Docker Build') {
            steps {
                echo 'Building Docker image...'
                bat 'docker build -t hiretrack-api:latest .'
            }
        }

        stage('Done') {
            steps {
                echo 'Pipeline completed successfully!'
                echo 'Image hiretrack-api:latest is ready for deployment.'
            }
        }
    }

    post {
        success {
            echo 'BUILD SUCCESSFUL - HireTrack API is ready!'
        }
        failure {
            echo 'BUILD FAILED - Check the logs above.'
        }
    }
}