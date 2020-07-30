#!groovy

properties([[$class: 'ParametersDefinitionProperty', parameterDefinitions: [
[$class: 'hudson.model.StringParameterDefinition', name: 'ECO_PIPELINE_ID', defaultValue: '0', description: 'Select an environment'],
[$class: 'hudson.model.StringParameterDefinition', name: 'PHASE', defaultValue: 'BUILD, PACKAGE, SONAR, SAST', description: 'Select an instance'],
[$class: 'hudson.model.StringParameterDefinition', name: 'TARGET_NODE', defaultValue: 'zld03318.vci.att.com', description: 'Select an environment to deploy to']
]]])

def wf = new MavenWorkflow()

wf defaultPhase:'BUILD, SONAR, SAST, DAST',
   language:'MAVEN',
   deployType: 'SWM',
   deployOptions:"swm:install -Dswm.target.node=${params.TARGET_NODE}"	
