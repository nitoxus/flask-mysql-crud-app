pipeline {
    environment {
        registry = "sukhotin/project_flask_http"
        registryCredential = "dockerhub"
        deployment = "project-flask.yml"
        dockerImage = ""
        get_dns_name_script = "aws elb describe-load-balancers --region us-east-1 --query 'LoadBalancerDescriptions[*].DNSName' --output text"      
        dns_name = ""
    }
    agent any 
    stages {
        stage("Build a docker image") {
            steps {
                script {
                    dockerImage = docker.build(registry)
                }
            }            
        }
        stage("Test the image") { 
            steps {
                script {
                    docker.image(registry).withRun('-p 8080:5000') {c ->
                        sh "until \$(curl --output /dev/null --head --fail http://localhost:8080); do printf 'Wait for responce...'; sleep 5; done"
                    }
                }
            }
        }
        stage("Push the image to DockerHub") { 
            steps {
                script {
                    withDockerRegistry(credentialsId: 'dockerhub', url: ''){
                        dockerImage.push()
                    }
                }
            }
        }
        stage("Deploy the app image to K8s") { 
            steps {
                script {
                    sh "kubectl apply -f ${deployment}"
                }
            }
        }
        stage("Wait for load balancer"){
            steps {
                script {
                   sh "sleep 10"
                   dns_name = sh(returnStdout: true, script: "${ get_dns_name_script }" )
                   sh "until \$(curl --output /dev/null --head --fail http://${ dns_name }); do printf 'Wait for responce...'; sleep 20; done"
                }
            }
        }
    }
}