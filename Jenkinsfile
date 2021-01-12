pipeline {
  agent any
  stages {
    stage('Clone') {
      steps {
        git(credentialsId: 'GithubLogin', url: 'https://github.com/SagittariusC/SoftwareSystems.git', branch: 'main')
      }
    }

  }
}