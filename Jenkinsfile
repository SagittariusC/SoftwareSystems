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
                // Compile the app and its dependencies
                sh './gradlew compile${'debug'}Sources'
            }
        }
        stage('Build') {
            steps {
                // Compile the app and its dependencies
                sh './gradlew assemble${BUILD_TYPE}'
                sh './gradlew generatePomFileForLibraryPublication'
            }
        }
        stage('Publish') {
            steps {
                // Archive the APKs so that they can be downloaded from Jenkins
                archiveArtifacts "**/${APP_NAME}-${'debug'}.apk"
            }
        }
    }
}
