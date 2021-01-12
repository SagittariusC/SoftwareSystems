pipeline {
  agent any
  environment {
      APP_NAME = 'PhotoApp'
    }
  options {
     // Stop the build early in case of compile or test failures
     skipStagesAfterUnstable()
     }
  stages {
      stage('Clone') {
              steps {
                git(credentialsId: 'GithubLogin', url: 'https://github.com/SagittariusC/SoftwareSystems.git', branch: 'main')
              }
            }
        stage('Detect build type') {
          steps {
              script {
                    if (env.BRANCH_NAME == 'develop' || env.CHANGE_TARGET == 'develop') {
                            env.BUILD_TYPE = 'debug'
                            } else if (env.BRANCH_NAME == 'master' || env.CHANGE_TARGET == 'master') {
                                    env.BUILD_TYPE = 'release'
                                    }
                    }
              }
          }
          stage('Compile') {
            steps {
            // Compile the app and its dependencies
            sh './gradlew compile${'BUILD_TYPE'}Sources'
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
               archiveArtifacts "**/${APP_NAME}-${'release'}.apk"
            }

          }
  }
}
