pipeline {
  agent any
  stages {
    stage('Build') {
      steps {
        git(url: 'https://github.com/SagittariusC/SoftwareSystems.git', branch: 'main', credentialsId: 'GithubLogin')
        withGradle() {
          sh '$ ./gradlew build'
        }

      }
    }

  }
  environment {
    APP_NAME = 'PhotoApp'
  }
}