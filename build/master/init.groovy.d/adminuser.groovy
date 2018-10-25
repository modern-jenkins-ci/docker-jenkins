#!groovy

import jenkins.model.*
import hudson.util.*
import jenkins.install.*
import hudson.security.*

def adminUsername = System.getenv('LOCAL_ADMIN') ?: 'admin'
def adminPassword = System.getenv('LOCAL_PASSWORD') ?: 'password'

println "Creating user Account [${adminUsername}:${adminPassword}]"
def instance = Jenkins.getInstance()

def hudsonRealm = new HudsonPrivateSecurityRealm(false)
instance.setSecurityRealm(hudsonRealm)
def user = instance.getSecurityRealm().createAccount(adminUsername, adminPassword)
user.save()

def strategy = new FullControlOnceLoggedInAuthorizationStrategy()
strategy.setAllowAnonymousRead(true)
instance.setAuthorizationStrategy(strategy)

instance.getDescriptor("jenkins.CLI").get().setEnabled(false)

instance.save()