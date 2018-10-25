node('docker') {
    stage('Get Latest Code') {
        cleanWs()
        checkout scm
    }

    stage('Build Master Docker Image') {
        docker.build("modern-jenkins/jenkins-master:${env.BUILD_NUMBER}", "-f Dockerfile build/master")
        
        // eventually a push to dockerhub could be done here
    }

    stage('Build Slave Docker Image') {
        docker.build("modern-jenkins/jenkins-slave:${env.BUILD_NUMBER}", "-f Dockerfile build/slave")

        // eventually a push to dockerhub could be done here
    }
}