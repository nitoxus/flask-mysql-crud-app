def docker_registry = "sukhotin/flask-crud-app"
def docker_registry_creds = "dockerhub"
def docker_image = ""
def app_port = "8181"
def db_host = "mysql.service.consul"
def db_user = "app"
def db_user_pass = "admin"
def db_name = "crud_flask"
def with_run_params = "-e db_host=${db_host} -e db_user=${db_user} -e db_user_pass=${db_user_pass} -e db_name=${db_name} -p ${app_port}:${app_port}"
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
                    docker.image(docker_registry).withRun(with_run_params) {c ->
                        sh "curl -sSf http://localhost:${app_port}"
                        sh "docker logs ${c.id}"
                    }
                }
            }
        }
    }
}