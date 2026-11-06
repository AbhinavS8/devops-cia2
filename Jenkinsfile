
pipeline {
    agent any

    options {
        skipDefaultCheckout(true)
        timestamps()
        timeout(time: 30, unit: 'MINUTES')
    }

    environment {
        // Azure configuration
        AZURE_SUBSCRIPTION_ID = 'ca1fd788-971e-4cfe-a25c-8e4f4af20c0c'

        RESOURCE_GROUP = 'devops-cia2-rg'
        ACR_NAME = 'devopscia2abhi2026'
        ACR_LOGIN_SERVER = 'devopscia2abhi2026.azurecr.io'
        AKS_CLUSTER = 'devops-cia2-aks'

        // Kubernetes configuration
        K8S_NAMESPACE = 'devops-cia2'

        // Docker configuration
        IMAGE_REPOSITORY = 'devops-cicd-assignment'

        // Isolate Azure CLI credentials within the workspace
        AZURE_CONFIG_DIR = "${WORKSPACE}/.azure"
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm

                script {
                    env.IMAGE_TAG = sh(
                        script: 'git rev-parse --short=12 HEAD',
                        returnStdout: true
                    ).trim()

                    echo "Building image with tag: ${env.IMAGE_TAG}"
                }
            }
        }

        stage('Secret Scanning') {
            steps {
                sh '''
                    gitleaks git --redact --verbose .
                '''
            }
        }

        stage('Build and Test') {
            steps {
                sh 'mvn clean verify'
            }
        }

        stage('Docker Build') {
            steps {
                sh '''
                    docker build \
                        -t "$ACR_LOGIN_SERVER/$IMAGE_REPOSITORY:$IMAGE_TAG" \
                        .
                '''
            }
        }

        stage('Push to Azure Container Registry') {
            steps {
                sh '''
                    set -e

                    az login --identity

                    az account set --subscription "$AZURE_SUBSCRIPTION_ID"

                    az acr login --name "$ACR_NAME"

                    docker push \
                        "$ACR_LOGIN_SERVER/$IMAGE_REPOSITORY:$IMAGE_TAG"
                '''
            }
        }

        stage('Deploy to AKS') {
            steps {
                withCredentials([
                    file(
                        credentialsId: 'aks-kubeconfig',
                        variable: 'KUBECONFIG'
                    )
                ]) {
                    sh '''
                        set -e

                        echo "Applying Kubernetes manifests..."

                        kubectl apply \
                            -n "$K8S_NAMESPACE" \
                            -f k8s/

                        echo "Updating application image..."

                        kubectl set image \
                            deployment/devops-cicd-app \
                            devops-cicd-app="$ACR_LOGIN_SERVER/$IMAGE_REPOSITORY:$IMAGE_TAG" \
                            -n "$K8S_NAMESPACE"

                        echo "Waiting for rollout..."

                        kubectl rollout status \
                            deployment/devops-cicd-app \
                            -n "$K8S_NAMESPACE" \
                            --timeout=180s

                        echo "Deployment status:"
                        kubectl get deployments -n "$K8S_NAMESPACE"
                        kubectl get pods -n "$K8S_NAMESPACE"
                        kubectl get services -n "$K8S_NAMESPACE"
                    '''
                }
            }
        }
    }

    post {
        always {
            junit(
                testResults: 'target/surefire-reports/*.xml',
                allowEmptyResults: true
            )

            archiveArtifacts(
                artifacts: 'target/site/jacoco/**',
                allowEmptyArchive: true
            )

            sh '''
                rm -rf "$AZURE_CONFIG_DIR"
            '''
        }

        success {
            echo "CI/CD pipeline completed successfully."
            echo "Deployed image: ${env.ACR_LOGIN_SERVER}/${env.IMAGE_REPOSITORY}:${env.IMAGE_TAG}"
        }

        failure {
            echo "Pipeline failed. Check the failed stage's console output."
        }
    }
}
