def docker_registry = "sukhotin/flask-crud-app"
def docker_registry_creds = "dockerhub"
def docker_image = ""
def app_port = "8181"
pipeline {
    agent any 
    stages {
        stage("Build a docker image") {
            steps 
            {
                script 
                {
                    docker_image = docker.build(docker_registry)
                }
            }            
        }
        stage("Test the image") 
        { 
            steps 
            {
                script 
                {
                    docker.image(docker_registry).withRun("-p ${app_port}:${app_port}") {c ->
                        sh "curl -sSf http://localhost:${app_port}"
                    }
                }
            }
        }
    }
}