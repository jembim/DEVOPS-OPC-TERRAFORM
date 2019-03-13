def containerFolder = "${PROJECT_NAME}/Cloud_Provision/IaaS/Create-Instance"
def createJob = freeStyleJob(containerFolder + '/Execute_Server_Creation')

createJob.with {
    description('')
    parameters {
        stringParam('OPC_USERNAME', '', '')
        stringParam('OPC_PASSWORD', '', '')
        stringParam('DOMAIN', '', '')
        stringParam('ENDPOINT_URL', '', '')
        stringParam('INSTANCE_NAME', '', '')
        stringParam('SSH_KEY', '', '')
		stringParam('IMAGE_LIST', '', '')
		stringParam('IMAGE_LIST_ENTRY', '', '')
		stringParam('SHAPE', '', '')
        stringParam('BOOT_STORAGE_NAME', '', '')
        stringParam('BOOT_STORAGE_SIZE', '', '')
        stringParam('NON_BOOT_STORAGE_NAME', '', '')
        stringParam('NON_BOOT_STORAGE_SIZE', '', '')
		stringParam('SEC_LIST_NAME', '', '')
		stringParam('SEC_APP_NAME', '', '')
		stringParam('PORT', '', '')
		stringParam('IP_RESERVATION_NAME', '', '')
		stringParam('CUSTOM_WORKSPACE', '', '')
	}

    logRotator {
        numToKeep(10)
        artifactNumToKeep(10)
    }

    concurrentBuild(true)
    label('postgres')
    customWorkspace('$CUSTOM_WORKSPACE')

    wrappers {
        preBuildCleanup() 
        colorizeOutput('css')
    }

    steps {

        copyArtifacts('Initiate_Server_Creation') {
            includePatterns('**/*')
            fingerprintArtifacts(true)
            buildSelector {
                upstreamBuild(true)
                latestSuccessful(false)
            }
        }

        shell('''#!/bin/bash
terraform init
terraform plan -var "user=${OPC_USERNAME}" -var "password=${OPC_PASSWORD}" --var-file=credentials.tfvars
terraform apply -var "user=${OPC_USERNAME}" -var "password=${OPC_PASSWORD}" --var-file=credentials.tfvars

        ''')
    }
}