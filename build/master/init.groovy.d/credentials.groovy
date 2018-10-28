#!groovy

import jenkins.model.*
import hudson.model.*
import hudson.util.Secret
import com.cloudbees.plugins.credentials.domains.Domain
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl
import com.cloudbees.plugins.credentials.CredentialsScope
import groovy.io.FileType

def createJenkinsUsernameCredential(instance, username, password, id="${username}_credential", description="${username}_credential") {
  // Retrieve the Global credential store
  def domain = Domain.global()
  def store = instance.getExtensionList('com.cloudbees.plugins.credentials.SystemCredentialsProvider')[0].getStore()

  // Set up the local user
  def cred = new UsernamePasswordCredentialsImpl(
    CredentialsScope.GLOBAL,
    id,
    description,
    username,
    password
  )
  
  store.addCredentials(domain, cred)
  println "Created ${username} user credentials"
}

def createJenkinsTextCredential(instance, text, id, description="${id}_credential") {
  // Retrieve the Global credential store
  def domain = Domain.global()
  def store = instance.getExtensionList('com.cloudbees.plugins.credentials.SystemCredentialsProvider')[0].getStore()

  cred = new StringCredentialsImpl(
    CredentialsScope.GLOBAL,
    id,
    description,
    new Secret(text))
  
  store.addCredentials(domain, cred)
  println "Created ${id} text credentials"
}

def instance = Jenkins.getInstance()
def secretsRoot = System.getenv('JENKINS_SECRETS')

println "Importing secrets from: ${System.getenv('JENKINS_SECRETS')}"

if(secretsRoot) {
  def list = []

  def dir = new File(secretsRoot)
  dir.eachFileRecurse (FileType.FILES) { file ->
    list << file
  }

  println "Fount the following secret file(s): ${list}"

  list.each { secretFile ->
    println "--> Importing secrets from ${secretFile}"
    
    def secretData = secretFile.text.trim()

    if(secretData) {
      byte[] decoded = secretData.decodeBase64()
      def secrets = new String(decoded)
      def counter = 0
      secrets.split('\n').each { credString ->
        def credSplit = credString.split(':')
        if(credSplit.length > 2) {
          def credType = credSplit[0]
          def credId = credSplit[1]
          
          println "Creating Credential [${credId}]"

          if(credType == 'up') {
            def credUsername = credSplit[2]
            def credPassword = credSplit[3]

            createJenkinsUsernameCredential(
              instance,
              credUsername,
              credPassword,
              credId,
              credId
            )
          } else {
              def credText = credSplit[2]

              createJenkinsTextCredential(
                instance,
                credText,
                credId,
                credId
              )
          }
        }
        else {
          println "Invalid credential found on line ${counter} of file [${secretFile.path}]" 
        }
        counter++
      }
    }
  }
  
}

instance.save()