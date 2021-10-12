pipeline {
    agent any
    tools {
        maven 'Gradle'
    }
    stages {
        stage ('Initialize') {
            steps {
                sh '''
                    echo "PATH = ${PATH}"
                '''
            }
        }

        stage ('Build') {
            steps {
                sh "gradle bootBuildImage --imageName=mbp/${env.BRANCH_NAME}:latest" 
            }
        }
    }
}
