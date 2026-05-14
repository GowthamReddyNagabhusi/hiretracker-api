pipeline {
    agent any

    environment {
        JAVA_HOME = tool 'JDK17'
    }

    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out code from GitHub...'
                checkout scm
            }
        }

        stage('Build & Test') {
            steps {
                echo 'Building and testing with Maven...'
                // Use sh for Linux agents, bat for Windows — detect OS
                script {
                    if (isUnix()) {
                        sh 'chmod +x mvnw && ./mvnw clean verify -B'
                    } else {
                        bat 'mvnw.cmd clean verify -B'
                    }
                }
            }
            post {
                always {
                    // Archive test reports
                    junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Docker Build') {
            steps {
                echo 'Building Docker image...'
                script {
                    def imageTag = "hiretrack-api:${env.BUILD_NUMBER}"
                    if (isUnix()) {
                        sh "docker build -t ${imageTag} ."
                    } else {
                        bat "docker build -t ${imageTag} ."
                    }
                }
            }
        }
    }

    post {
        success {
            echo "BUILD #${env.BUILD_NUMBER} SUCCESSFUL — HireTrack API is ready!"
        }
        failure {
            echo "BUILD #${env.BUILD_NUMBER} FAILED — Check the logs above."
        }
    }
}