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
				withSonarQubeENV('sonarserver')
				bat 'mvn sonar:sonar -Dsonar.projectkey=jenkins-sonarqube-docker-project'
			}
		}
		stage('Quality Gate') {
			steps {
			   	echo 'Waiting for SonarQube Quality Gate...' 
				timeout(time:5, unit 'MINUTES') {
					waitForQualityGate abortpipeline: true
				}
			}
		}
		stage('Docker Build') {
			steps {
				echo 'Building Docker Image...'
				bat 'docker build -t %DOCKER_IMAGE%:%BUILD_IMAGE%'
				bat 'docker tag %DOCKER_IMAGE%:%BUILD_IMAGE% %DOCKER_IMAGE%:latest'
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
						echo %DOCKER_PASSWORD% | docker login -u %DOCKER_USERNAME% --password --stdin
						docker push %DOCKER_IMAGE%:%BUILD_IMAGE%
						docker push %DOCKER_IMAGE%:latest
					'''
                        	}
			}
		}
		stage('Deploy Locally')
			steps {
				echo 'Deploying Docker container loacally...'
				bat 'docker run -d -p 8081:8081 --name jenkins-sonarqube-docker-project %DOCKER_IMAGE%:%BUILD_IMAGE%'	

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
