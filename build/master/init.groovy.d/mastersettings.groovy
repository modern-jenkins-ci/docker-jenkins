import jenkins.model.Jenkins
import jenkins.model.JenkinsLocationConfiguration

def instance = Jenkins.getInstance()
JenkinsLocationConfiguration location = instance.getExtensionList('jenkins.model.JenkinsLocationConfiguration')[0]

def frontendUrl = System.getenv('JENKINS_FRONTEND_URL') ?: 'http://localhost:8080'

println "Updating Jenkins URL to: ${frontendUrl}"
location.url = frontendUrl

if(System.getenv('http_proxy')) {
  println "--> Found http_proxy environment variable, updating master proxy information"
  def proxyUrl = new URL(System.getenv('http_proxy'))

  def pc = new hudson.ProxyConfiguration(proxyUrl.host, proxyUrl.port, '', '', 'localhost')
  instance.proxy = pc
  pc.save()
}

// Force the setup wizard not to run or other way -Djenkins.install.runSetupWizard=false
// if (!instance.installState.isSetupComplete()) {
//   println '--> Neutering SetupWizard'
//   InstallState.INITIAL_SETUP_COMPLETED.initializeState()
// }

instance.save()