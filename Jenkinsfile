
pipeline {
    agent any

    options {
        skipDefaultCheckout(true)
        timestamps()
        timeout(time: 30, unit: 'MINUTES')
    }

    environment {
        // Azure configuration
        AZURE_TENANT_ID       = '5beb351c-3fb8-418f-b612-fe36ace96ef3'
        AZURE_SUBSCRIPTION_ID = 'ca1fd788-971e-4cfe-a25c-8e4f4af20c0c'

        RESOURCE_GROUP = 'devops-cia2-rg'
        ACR_NAME       = 'devopscia2abhi2026'
        ACR_LOGIN_SERVER = 'devopscia2abhi2026.azurecr.io'
        AKS_CLUSTER    = 'devops-cia2-aks'

        // Docker configuration
        IMAGE_REPOSITORY = 'devops-cicd-assignment'

        // Jenkins-local configuration
        AZURE_CONFIG_DIR = "${WORKSPACE}/.azure"
        KUBECONFIG       = "${WORKSPACE}/.kube/config"
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
                withCredentials([
                    usernamePassword(
                        credentialsId: 'azure-service-principal',
                        usernameVariable: 'AZURE_CLIENT_ID',
                        passwordVariable: 'AZURE_CLIENT_SECRET'
                    )
                ]) {
                    sh '''
                        set -e

                        az login \
                            --service-principal \
                            --username "$AZURE_CLIENT_ID" \
                            --password "$AZURE_CLIENT_SECRET" \
                            --tenant "$AZURE_TENANT_ID"

                        az account set --subscription "$AZURE_SUBSCRIPTION_ID"

                        az acr login --name "$ACR_NAME"

                        docker push \
                            "$ACR_LOGIN_SERVER/$IMAGE_REPOSITORY:$IMAGE_TAG"

                        az logout
                    '''
                }
            }
        }

        stage('Deploy to AKS') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'azure-service-principal',
                        usernameVariable: 'AZURE_CLIENT_ID',
                        passwordVariable: 'AZURE_CLIENT_SECRET'
                    )
                ]) {
                    sh '''
                        set -e

                        az login \
                            --service-principal \
                            --username "$AZURE_CLIENT_ID" \
                            --password "$AZURE_CLIENT_SECRET" \
                            --tenant "$AZURE_TENANT_ID"

                        az account set --subscription "$AZURE_SUBSCRIPTION_ID"

                        mkdir -p "$(dirname "$KUBECONFIG")"

                        az aks get-credentials \
                            --resource-group "$RESOURCE_GROUP" \
                            --name "$AKS_CLUSTER" \
                            --admin \
                            --file "$KUBECONFIG" \
                            --overwrite-existing

                        # Apply Kubernetes configuration
                        kubectl apply -f k8s/

                        # Update the application image
                        kubectl set image \
                            deployment/devops-cicd-app \
                            devops-cicd-app="$ACR_LOGIN_SERVER/$IMAGE_REPOSITORY:$IMAGE_TAG"

                        # Wait until the new deployment is ready
                        kubectl rollout status \
                            deployment/devops-cicd-app \
                            --timeout=180s

                        # Display deployment and service status
                        kubectl get pods
                        kubectl get services

                        az logout
                    '''
                }
            }
        }
    }

    post {
        always {
            // Publish JUnit results even if a later stage fails
            junit(
                testResults: 'target/surefire-reports/*.xml',
                allowEmptyResults: true
            )

            // Preserve JaCoCo reports
            archiveArtifacts(
                artifacts: 'target/site/jacoco/**',
                allowEmptyArchive: true
            )

            // Remove temporary Azure credentials and kubeconfig
            sh '''
                rm -rf "$AZURE_CONFIG_DIR"
                rm -rf "$(dirname "$KUBECONFIG")"
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
