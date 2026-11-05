pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build and Test') {
            steps {
                sh 'mvn clean verify'
            }
        }

        stage('Publish Test Results') {
            steps {
                junit 'target/surefire-reports/*.xml'
            }
        }

        stage('Archive Reports') {
            steps {
                archiveArtifacts artifacts: 'target/site/jacoco/**',
                                 allowEmptyArchive: true
            }
        }
    }
}
