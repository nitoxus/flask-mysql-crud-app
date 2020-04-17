def mysql_image = "mysql:latest"
def mysql_remote_host = "mysql.service.consul"
def mysql_remote_ip = ""
def db_user = "app"
def db_user_pass = "admin"
def db_name = "crud_flask"

pipeline 
{
    agent any
    stages {
        stage('Get MySql Server IP')
        {
            steps 
            {
               mysql_remote_ip = sh(returnStdout: true, script: "dig +short ${ mysql_remote_host }").trim()
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
                sh "mysql -h ${ mysql_remote_ip } -u${db_user} -p${db_user_pass} -e 'create database ${db_name};'"
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
                sh "mysql -h ${ mysql_remote_ip } -u${db_user} -p${db_user_pass} ${db_name} < database/${db_name}.sql"
            }
        }
    }
    post { 
        always { 
            sh "docker system prune -a -f"
        }
    }
}