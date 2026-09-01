pipeline {

    agent any

    environment {
        DOCKERHUB_IMAGE = "bharatraj07/jenkins-sonarqube-docker-project"
        DOCKERHUB_CREDENTIALS = "dockerhubtoken"
        SONAR_TOKEN = credentials('sonarkey')
    }

    tools {
        jdk "java21"
        maven "maven3.9.12"
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
                echo 'Building and Testing the application...'
                sh 'mvn clean test'
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
                sh 'mvn package -DskipTests'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                echo 'Running SonarQube Analysis...'

                withSonarQubeEnv('sonarserver') {
                    sh '''
                        mvn sonar:sonar \
                        -Dsonar.projectKey=jenkins-sonarqube-docker-project \
                        -Dsonar.token=$SONAR_TOKEN
                    '''
                }
            }
        }

        stage('Quality Gate') {
            steps {
                echo 'Waiting for SonarQube Quality Gate...'

                timeout(time: 10, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Docker Build') {
            steps {
                echo 'Building Docker Image...'

                sh 'docker build -t $DOCKERHUB_IMAGE:$BUILD_NUMBER .'

                sh 'docker tag $DOCKERHUB_IMAGE:$BUILD_NUMBER $DOCKERHUB_IMAGE:latest'
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

                    sh '''
                        echo "$DOCKER_PASSWORD" | docker login \
                            -u "$DOCKER_USERNAME" \
                            --password-stdin

                        docker push $DOCKERHUB_IMAGE:$BUILD_NUMBER

                        docker push $DOCKERHUB_IMAGE:latest

                        docker logout
                    '''
                }
            }
        }

        stage('Deploy Locally') {
            steps {
                echo 'Deploying Docker container locally...'

                sh '''
                    docker stop jenkins-sonarqube-docker-project || true
                    docker rm jenkins-sonarqube-docker-project || true

                    docker run -d \
                        -p 8081:8081 \
                        --name jenkins-sonarqube-docker-project \
                        $DOCKERHUB_IMAGE:$BUILD_NUMBER
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
