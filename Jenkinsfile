pipeline {
    agent any
    tools {
        gradle 'Gradle'
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
                sh "gradle bootBuildImage --imageName=mbp_${env.BRANCH_NAME}:latest" 
            }
        }
    }
}
