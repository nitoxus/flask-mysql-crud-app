def docker_registry = "sukhotin/flask-crud-app"
def docker_registry_creds = "dockerhub"
def docker_image = ""
def app_port = "8181"
def mysql_service_name = "mysql.service.opsschool-project.consul"
def db_host = ""
def db_username = "app"
def db_password = "admin"
def db_name = "crud_flask"
def with_run_params = ""
def deployment = "deploy-flask-crud-app.yml"
def ELB = ""
pipeline {
    agent any 
    stages 
    {
        stage("Get MySql Server IP")
        {
            steps 
            {
               script
               {
                   db_host = sh(returnStdout: true, script: "dig +short ${ mysql_service_name }").trim()
               }
            }
        }
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
                    with_run_params = "-e db_host=${ db_host } -e db_username=${ db_username } -e db_password=${ db_password } -e db_name=${ db_name } -p ${ app_port }:${ app_port }"
                    docker.image(docker_registry).withRun(with_run_params) {c ->
                        sh "sleep 5"
                        sh "curl -sf -o /dev/null http://localhost:${app_port}"
                    }
                }
            }
        }
        stage("Push the image to DockerHub") 
        { 
            steps 
            {
                script 
                {
                    withDockerRegistry(credentialsId: 'dockerhub', url: ''){
                        docker_image.push()
                    }
                }
            }
        }
        stage("Deploy app to k8s")
        {
            steps
            {
                script
                {
                    sh("kubectl apply -f ${ deployment }")
                    ELB = sh(returnStdout: true, script:"kubectl get svc flask-crud-app -o jsonpath='{.status.loadBalancer.ingress[0].hostname}'")
                }
                timeout(time: 3, unit: 'MINUTES')
                {
                    sh "until \$(curl -o /dev/null --head --fail http://${ELB}); do printf 'Wait for ${ELB}...'; sleep 20; done"
                }
            }
        }
    }
}