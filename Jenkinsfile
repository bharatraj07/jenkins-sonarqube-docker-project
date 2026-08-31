pipeline {

    agent any

    environment {
        DOCKERHUB_IMAGE = "bharatraj07/jenkins-sonarqube-docker-project"
        DOCKERHUB_CREDENTIALS = "dockerhubtoken"
    }

    tools {
        jdk "JDK21"
        maven "Maven"
    }

    stages {

        stage('Checkout') {
            steps {
                echo 'Checking out source code from SCM...'
                checkout scm
            }
        }

        stage('Build and Test') {
            steps {
                echo 'Building and testing the application...'
                bat 'mvn clean test'
            }

            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Packaging') {
            steps {
                echo 'Packaging the application...'
                bat 'mvn package -DskipTests'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                echo 'Running SonarQube analysis...'

                withSonarQubeEnv('sonarserver') {
                    bat 'mvn sonar:sonar -Dsonar.projectKey=jenkins-sonarqube-docker-project'
                }
            }
        }

        stage('Quality Gate') {
            steps {
                echo 'Waiting for SonarQube Quality Gate...'

                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Docker Build') {
            steps {
                echo 'Building Docker image...'

                bat 'docker build -t %DOCKERHUB_IMAGE%:%BUILD_NUMBER% .'
                bat 'docker tag %DOCKERHUB_IMAGE%:%BUILD_NUMBER% %DOCKERHUB_IMAGE%:latest'
            }
        }

        stage('Docker Push') {
            steps {
                echo 'Pushing image to Docker Hub...'

                withCredentials([
                    usernamePassword(
                        credentialsId: 'dockerhubtoken',
                        usernameVariable: 'DOCKER_USERNAME',
                        passwordVariable: 'DOCKER_PASSWORD'
                    )
                ]) {

                    bat '''
                        echo %DOCKER_PASSWORD% | docker login -u %DOCKER_USERNAME% --password-stdin

                        docker push %DOCKERHUB_IMAGE%:%BUILD_NUMBER%

                        docker push %DOCKERHUB_IMAGE%:latest

                        docker logout
                    '''
                }
            }
        }

        stage('Deploy Locally') {
            steps {
                echo 'Deploying Docker container locally...'

                bat '''
                    docker rm -f jenkins-sonarqube-docker-project 2>NUL || exit 0
                    docker run -d -p 8081:8081 --name jenkins-sonarqube-docker-project %DOCKERHUB_IMAGE%:%BUILD_NUMBER%
                '''
            }
        }
    }

    post {

        success {
            echo '======================================'
            echo 'CI/CD PIPELINE COMPLETED SUCCESSFULLY'
            echo '======================================'
        }

        failure {
            echo '======================================'
            echo 'CI/CD PIPELINE FAILED'
            echo '======================================'
        }
    }
}
