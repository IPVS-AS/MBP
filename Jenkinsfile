pipeline {
    agent any
    tools {
        maven 'Maven 3.3.9'
        jdk 'jdk8'
    }
    environment {
        SONARQUBE_TOKEN = credentials('sonarqube-access')
    }
    stages {
        stage ('Initialize') {
            steps {
                sh '''
                    echo "PATH = ${PATH}"
                    echo "M2_HOME = ${M2_HOME}"
                '''
            }
        }

        stage ('Build') {
            steps {
                sh 'mvn -Dmaven.test.failure.ignore=true clean install' 
            }
        }
        
        stage('Static Analysis') {
            when {
                branch 'master'
            }
            steps {
                sh 'mvn sonar:sonar -Dsonar.projectKey=MBP -Dsonar.host.url=http://localhost:9000 -Dsonar.login=${SONARQUBE_TOKEN}'
            }
        }
        
        stage ('Deploy'){
            steps {
                deploy("target/MBP-0.1.war", "localhost", "deploy/${env.BRANCH_NAME}")
             }
        }
    }
}

def deploy(file, host, context) {
    withCredentials([usernamePassword(credentialsId: 'c21e88ab-24e8-406a-8667-2c0dce78de71', passwordVariable: 'password', usernameVariable: 'username')]) {
        sh "curl -v -u ${username}:${password} -T ${file} 'http://${host}:8888/manager/text/deploy?path=/${context}&update=true'"
    }
}
