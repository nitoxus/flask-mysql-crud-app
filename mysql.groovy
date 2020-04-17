def mysql_image = "mysql:latest"
def mysql_service_name = "mysql.service.consul"
def db_host = ""
def db_username = "app"
def db_password = "admin"
def db_name = "crud_flask"

pipeline 
{
    agent any
    stages {
        stage('Get MySql Server IP')
        {
            steps 
            {
               script
               {
                   db_host = sh(returnStdout: true, script: "dig +short ${ mysql_service_name }").trim()
               }
            }
        }
        stage('Create new database') 
        {            
            agent 
            {
                docker { image mysql_image }
            }
            steps 
            {
                sh "mysql -h ${ db_host } -u${ db_username } -p${ db_password } -e 'create database ${ db_name };'"
            }
        }
        stage('Import data to new database')
        {
            agent 
            {
                docker { image mysql_image }
            }
            steps
            {
                sh "mysql -h ${ db_host } -u${ db_username } -p${ db_password } ${ db_name } < database/${db_name}.sql"
            }
        }
    }
    post { 
        always { 
            sh "docker system prune -a -f"
        }
    }
}