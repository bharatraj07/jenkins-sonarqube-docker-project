pipeline {

	agent any

	environment {
		DOCKERHUB_IMAGE = "bharatraj07/jenkins-sonarqube-docker-project"
		DOCKERHUB_CREDENTIALS = "dockerhubtoken"
		SONAR_TOKEN = credentials('sonarkey')
	}
	
	tools {
		jdk "java21"
		maven "maven3.9.9"
	}

	stages {

		stage('Checkout') {
			steps {
				echo 'Checking our source code from scm...'
				checkout scm
			}
		}
		stage('Build and Test') {
			steps {
				echo 'Building and Testing the application...'
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
				echo 'Running SonarQube Analysis...'
				withSonarQubeEnv('sonarserver'){
					bat 'mvn sonar:sonar -Dsonar.projectKey=jenkins-sonarqube-docker-project -Dsonar.token=%SONAR_TOKEN%'
				}
			}
		}
		stage('Quality Gate') {
			steps {
			   	echo 'Waiting for SonarQube Quality Gate...' 
				timeout(time:5, unit:'MINUTES') {
					waitForQualityGate abortPipeline: true
				}
			}
		}
		stage('Docker Build') {
			steps {
				echo 'Building Docker Image...'
				bat 'docker build -t %DOCKERHUB_IMAGE%:%BUILD_NUMBER% .'
				bat 'docker tag %DOCKERHUB_IMAGE%:%BUILD_NUMBER% %DOCKERHUB_IMAGE%:latest'
			}
		}
		stage('Docker Push') {
			steps {
				echo 'Pushing image to docker hub...'
				withCredentials([
					usernamePassword(
						credentialsId: 'dockerhubtoken',
						usernameVariable: 'DOCKER_USERNAME',
						passwordVariable: 'DOCKER_PASSWORD'
					)
				]) {
					 bat '''
						docker login -u %DOCKER_USERNAME% -p %DOCKER_PASSWORD%
						docker push %DOCKERHUB_IMAGE%:%BUILD_NUMBER%
						docker push %DOCKERHUB_IMAGE%:latest
						docker logout
					'''
                        	}
			}
		}
		stage('Deploy Locally') {
			steps {
				echo 'Deploying Docker container loacally...'
				bat 'docker run -d -p 8081:8081 --name jenkins-sonarqube-docker-project %DOCKERHUB_IMAGE%:%BUILD_NUMBER%'

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
		
