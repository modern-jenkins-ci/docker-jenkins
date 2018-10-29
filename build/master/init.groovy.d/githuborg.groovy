#!groovy

import jenkins.model.*
import hudson.util.*
import jenkins.branch.OrganizationFolder
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.common.*
import com.cloudbees.plugins.credentials.domains.*
import com.cloudbees.plugins.credentials.impl.*
import jenkins.scm.impl.trait.*
import org.jenkinsci.plugins.github_branch_source.GitHubSCMNavigator
import org.jenkinsci.plugins.github_branch_source.OriginPullRequestDiscoveryTrait
import org.jenkinsci.plugins.github_branch_source.BranchDiscoveryTrait
import org.jenkinsci.plugins.github.config.GitHubPluginConfig
import org.jenkinsci.plugins.github.config.GitHubServerConfig
import org.jenkinsci.plugins.github.config.HookSecretConfig

def configureGitHub(name, apiUrl, orgCredId, manageHooks=false, hookUrl, hookSharedSecretId) {
  def server = new GitHubServerConfig(orgCredId)
  server.name = name
  server.apiUrl = apiUrl ?: 'https://api.github.com'
  server.manageHooks = manageHooks
  server.clientCacheSize = 20

  def gitHubPluginSettings = Jenkins.instance.getExtensionList(GitHubPluginConfig.class)[0]

  if(!gitHubPluginSettings.configs) {
    gitHubPluginSettings.configs = [server]
  }
  else {
    gitHubPluginSettings.configs << server
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

def configureGitHubOrg(githubOrg, folderDisplayName, orgCredential) {
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
  // println '--> Scheduling GitHub organization scan in 30 seconds'
  // Thread.start {
  //   sleep 30000 // 30 seconds
  //   println '--> Running GitHub organization scan'
  //   folder.scheduleBuild()
  // }
}

def instance = Jenkins.getInstance()

def creds = CredentialsProvider.lookupCredentials(StandardUsernamePasswordCredentials, instance)
def cred = creds.findResult { it.id == 'swpc-11-2018-pat' ? it : null }
def hookCred = creds.findResult { it.id == 'swpc-11-2018-pat-text' ? it : null }

if(cred) {
  if(hookCred) {
    def webHookUrl = "${System.getenv('JENKINS_FRONTEND_URL')}/github-webook"
    configureGitHub('GitHub', 'https://api.github.com', cred.id, true, webHookUrl, hookCred.id)
  }

  configureGitHubOrg('modern-jenkins-ci', 'Modern-Jenkins', cred)
} else {
  println '[ERROR] Unable to create GitHub Org due to missing credentials'
}

