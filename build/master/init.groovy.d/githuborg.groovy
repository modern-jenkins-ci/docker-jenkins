#!groovy

import jenkins.model.*
import hudson.util.*
import jenkins.branch.OrganizationFolder
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.common.*
import com.cloudbees.plugins.credentials.domains.*
import com.cloudbees.plugins.credentials.impl.*
import jenkins.scm.impl.trait.*
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl
import org.jenkinsci.plugins.github_branch_source.GitHubSCMNavigator
import org.jenkinsci.plugins.github_branch_source.OriginPullRequestDiscoveryTrait
import org.jenkinsci.plugins.github_branch_source.BranchDiscoveryTrait
import org.jenkinsci.plugins.github.config.GitHubPluginConfig
import org.jenkinsci.plugins.github.config.GitHubServerConfig
import org.jenkinsci.plugins.github.config.HookSecretConfig

boolean isServerConfigsEqual(List s1, List s2) {
    s1.size() == s2.size() &&
    !(
        false in [s1, s2].transpose().collect { c1, c2 ->
            c1.name == c2.name &&
            c1.apiUrl == c2.apiUrl &&
            c1.manageHooks == c2.manageHooks &&
            c1.credentialsId == c2.credentialsId &&
            c1.clientCacheSize == c2.clientCacheSize
        }
    )
}

def configureGitHub(instance, name, apiUrl, orgCredId, manageHooks=false, hookUrl, hookSharedSecretId) {
  def server = new GitHubServerConfig(orgCredId)
  server.name = name
  server.apiUrl = apiUrl ?: 'https://api.github.com'
  server.manageHooks = manageHooks
  server.clientCacheSize = 20

  def gitHubPluginSettings = Jenkins.instance.getExtensionList(GitHubPluginConfig.class)[0]

  if(!isServerConfigsEqual(gitHubPluginSettings.configs, [server])) {
      gitHubPluginSettings.configs = [server]
  }

  if(manageHooks && hookUrl) {
    gitHubPluginSettings.overrideHookUrl = true
    gitHubPluginSettings.hookUrl = new URL(hookUrl)

    if(hookSharedSecretId) {
      if(gitHubPluginSettings.hookSecretConfig) {
        gitHubPluginSettings.hookSecretConfig = new HookSecretConfig(hookSharedSecretId)
      }
    }
  } else {
    gitHubPluginSettings.overrideHookUrl = false
    gitHubPluginSettings.hookUrl = null
  }

  gitHubPluginSettings.save()
}

def configureGitHubOrg(instance, githubOrg, folderDisplayName, orgCredential) {
  println "--> Creating [${githubOrg}] GitHub organization folder"

  // Create the top-level item if it doesn't exist already.
  def folder = instance.items.isEmpty()
    ? instance.createProject(OrganizationFolder, folderDisplayName)
    : instance.items[0]

  // Set up GitHub source.
  def navigator = new GitHubSCMNavigator(githubOrg)
  navigator.credentialsId = orgCredential.id
  navigator.traits = [
    new RegexSCMHeadFilterTrait('.*'),
    new BranchDiscoveryTrait(1), // Exclude branches that are also filed as PRs.
    new OriginPullRequestDiscoveryTrait(1) // Merging the pull request with the current target branch revision.
  ]

  folder.navigators.replace(navigator)

  println "--> Successfully created [${githubOrg}] organization folder"

  // schedule a scan
  println '--> Scheduling GitHub organization scan in 30 seconds'
  Thread.start {
    sleep 30000 // 30 seconds
    println '--> Running GitHub organization scan'
    folder.scheduleBuild()
  }
}

def apiUrl           = System.getenv('GITHUB_SETUP_API_URL')
def credentialId     = System.getenv('GITHUB_SETUP_CREDENTIAL_ID')
def hookCredentialId = System.getenv('GITHUB_SETUP_WEBHOOK_CREDENTIAL_ID')
def orgName          = System.getenv('GITHUB_SETUP_ORG_NAME')
def orgDisplayName   = System.getenv('GITHUB_SETUP_ORG_DISPLAY_NAME')

if(!orgDisplayName && orgName) {
  orgDisplayName = orgName.replaceAll(' ', '-')
}

def instance = Jenkins.getInstance()

// Verify that the credentials were actually created
def creds = CredentialsProvider.lookupCredentials(StandardUsernamePasswordCredentials, instance)
def cred = creds.findResult { it.id == credentialId ? it : null }

def textCreds = CredentialsProvider.lookupCredentials(StringCredentialsImpl, instance)
def hookCred = textCreds.findResult { it.id == hookCredentialId ? it : null }

if(hookCred) {
  def webHookUrl = System.getenv('GIHUB_SETUP_ORG_WEBHOOK_URL') ?:"${System.getenv('JENKINS_FRONTEND_URL')}github-webhook/"
  configureGitHub(instance, 'GitHub', apiUrl, hookCred.id, true, webHookUrl, hookCred.id)
} else {
  println "Could not find webhook shared secret. Could not configure GitHub"
}

if(cred) {
  if(orgName) {
    configureGitHubOrg(instance, orgName, orgDisplayName, cred)
  }
} else {
  println '[ERROR] Unable to create GitHub Org due to missing credentials'
}

instance.save()