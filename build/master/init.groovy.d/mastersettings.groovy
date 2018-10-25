import jenkins.model.Jenkins
import jenkins.model.JenkinsLocationConfiguration

def instance = Jenkins.getInstance()
JenkinsLocationConfiguration location = instance.getExtensionList('jenkins.model.JenkinsLocationConfiguration')[0]

def frontendUrl = System.getenv('JENKINS_FRONTEND_URL') ?: 'http://localhost:8080'

println "Updating Jenkins URL to: ${frontendUrl}"
location.url = frontendUrl


instance.save()