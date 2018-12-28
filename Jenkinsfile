pipeline {
    agent any
   
    tools {
        maven 'default'
    }
    
    environment {
        tmpDir = "/Users/katalon/kataplugin/${BRANCH_NAME}_${BUILD_TIMESTAMP}"
    }
    
    stages {
        stage('Prepare') {
            steps {
                script {
                    sh 'mvn clean install'
                }
            }   
        }
        
        stage('Copy plugin') {
            steps {
                dir("target") {
                    script {
                        fileOperations([
                                fileCopyOperation(
                                        excludes: '',
                                        includes: '*.jar',
                                        flattenFiles: true,
                                        targetLocation: "${env.tmpDir}")
                        ])
                    }
                }
            }
        }    
    }
}
