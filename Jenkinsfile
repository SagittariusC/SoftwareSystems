pipeline {
    agent any
    environment {
        APP_NAME = 'PhotoApp'
    }
    stages {
        stage('Clone') {
            steps {
                git(credentialsId: 'GithubLogin', url: 'https://github.com/SagittariusC/SoftwareSystems.git', branch: 'main')
            }
        }
        stage('Compile') {
            steps {
                gradlew('clean', 'classes')
            }
        }

    }
}
