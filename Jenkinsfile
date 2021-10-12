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
                sh "gradle bootBuildImage --imageName=mbp/${env.BRANCH_NAME}:latest" 
            }
        }
    }
}
