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

def instance = Jenkins.getInstance()

def creds = CredentialsProvider.lookupCredentials(StandardUsernamePasswordCredentials, instance)
def cred = creds.findResult { it.id == 'swpc-11-2018-pat' ? it : null }

if(cred) {
  def githubOrg = 'modern-jenkins-ci'

  println "--> Creating [${githubOrg}] GitHub organization folder"
  
  // Create the top-level item if it doesn't exist already.
  def folder = instance.items.isEmpty()
    ? instance.createProject(OrganizationFolder, 'Modern Jenkins')
    : instance.items[0]
  
  // Set up GitHub source.
  def navigator = new GitHubSCMNavigator(githubOrg)
  navigator.credentialsId = cred.id // Loaded above in the GitHub section.
  navigator.traits = [
    new RegexSCMHeadFilterTrait('.*'),
    new BranchDiscoveryTrait(1), // Exclude branches that are also filed as PRs.
    new OriginPullRequestDiscoveryTrait(1) // Merging the pull request with the current target branch revision.
  ]

  folder.navigators.replace(navigator)

  println "--> Successfully created ${githubOrg} organization folder"

  println '--> Scheduling GitHub organization scan in 30 seconds'
  Thread.start {
    sleep 30000 // 30 seconds
    println '--> Running GitHub organization scan'
    folder.scheduleBuild()
  }
} else {
  println '[ERROR] Unable to create GitHub Org due to missing credentials'
}

