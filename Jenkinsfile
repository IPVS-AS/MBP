pipeline {
    agent any
    tools {
        maven 'Maven 3.3.9'
        jdk 'jdk8'
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
                do_static_analysis("http://localhost:9000", "MBP", "MBP-Frontend")
            }
        }
        
        stage ('Deploy'){
            steps {
                do_deploy("target/MBP-0.1.war", "localhost", "deploy/${env.BRANCH_NAME}")
             }
        }
    }
}

def do_static_analysis(host, backend_project, frontend_project) {
    withCredentials([string(credentialsId: 'sonarqube-access', variable: 'sonarqube_token')]) {
        sh "mvn sonar:sonar -Dsonar.projectKey=${backend_project} -Dsonar.host.url=${host} -Dsonar.login=${sonarqube_token}"
        sh "/opt/sonarqube-scanner/bin/sonar-scanner -Dsonar.projectKey=${frontend_project} -Dsonar.sources=. -Dsonar.host.url=${host} -Dsonar.login=${sonarqube_token}"
    }
    withCredentials([string(credentialsId: 'sonarqube-access', variable: 'sonarqube_token')]) {
        sh "mvn sonar:sonar -Dsonar.projectKey=${project} -Dsonar.host.url=${host} -Dsonar.login=${sonarqube_token}"
    }
}

def do_deploy(file, host, context) {
    withCredentials([usernamePassword(credentialsId: 'c21e88ab-24e8-406a-8667-2c0dce78de71', passwordVariable: 'password', usernameVariable: 'username')]) {
        sh "curl -v -u ${username}:${password} -T ${file} 'http://${host}:8888/manager/text/deploy?path=/${context}&update=true'"
    }
}
