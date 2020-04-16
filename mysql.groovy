def mysql_image = "mysql:latest"
def mysql_remote_host = "10.0.2.31"
def db_user = "app"
def db_user_pass = "admin"
def db_name = "crud_flask"

pipeline 
{
    agent any
    stages {
        stage('Create new database') 
        {            
            agent 
            {
                docker { image mysql_image }
            }
            steps 
            {
                sh "mysql -h ${ mysql_remote_host } -u${db_user} -p${db_user_pass} -e 'create database ${db_name};'"
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
                sh "mysql -h ${ mysql_remote_host } -u${db_user} -p${db_user_pass} -e 'show databases;'"
            }
        }
    }
    post { 
        always { 
            sh "docker system prune -a -f"
        }
    }
}