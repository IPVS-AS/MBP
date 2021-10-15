pipeline {
    agent any
    tools {
        maven 'Maven'
    }
    environment {
        TEST_RUNTIME="ci"
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
                sh 'mvn -DskipTests -B clean install' 
            }
        }
        stage ('Test with Mosquitto') {
            stages {
                stage ('Launch Mosquitto') {
                    steps {
                        sh 'docker stop mqtt-jenkins || true'
                        sh 'docker run -d --rm -p 1883:1883 -p 9001:9001 --name mqtt-jenkins eclipse-mosquitto:1.6.14'
                    }
                }
                stage ('Test') {
                    options {
                        timeout(time: 1, unit: 'HOURS')
                    }

                    stages {
                        stage('Backend') {
                            environment {
                                TEST_MODE="backend"
                            }
                            steps {
                                sh 'mvn -B clean verify'
                            }
                            post {
                                always {
                                    junit(testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true)
                                }
                            }
                        }
                        stage('Device tests') {
                            environment {
                                TEST_MODE="iotdevice"
                            }
                            steps {
                                sh 'mvn -B clean verify'
                            }
                            post {
                                always {
                                    junit(testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true)
                                }
                            }
                        }
                        stage('Frontend') {
                            steps {
                                println('IMPLEMENT ME')
                            }
                        }
                    }
                    post {
                        always {
                            sh 'docker stop mqtt-jenkins || true'
                        }
                    }
                }
            }
        }

        
        /*
        stage('Static Analysis') {
            when {
                branch 'master'
            }
            steps {
                do_static_analysis("http://localhost:9000", "MBP")
            }
        }
        
        stage ('Deploy'){
            steps {
                do_deploy(find_file("target/", "MBP-*.war"), "localhost", "deploy/${env.BRANCH_NAME}")
             }
        }*/
    }
}

def do_static_analysis(host, project) {
    withCredentials([string(credentialsId: 'sonarqube-access', variable: 'sonarqube_token')]) {
        sh "mvn sonar:sonar -Dsonar.projectKey=${project} -Dsonar.host.url=${host} -Dsonar.login=${sonarqube_token} -Dsonar.sources=src/main/java,src/main/resources/static/js,src/main/resources/static/css,src/main/webapp/WEB-INF/views"
    }
}

def do_deploy(file, host, context) {
    withCredentials([usernamePassword(credentialsId: 'c21e88ab-24e8-406a-8667-2c0dce78de71', passwordVariable: 'password', usernameVariable: 'username')]) {
        sh "curl -v -u ${username}:${password} -T ${file} 'http://${host}:8888/manager/text/deploy?path=/${context}&update=true'"
    }
}

def find_file(folder, pattern) {
    return sh(script: "find ${folder} -name '${pattern}'", returnStdout: true).trim()
}
