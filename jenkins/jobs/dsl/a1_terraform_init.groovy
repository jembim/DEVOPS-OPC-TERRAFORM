def createJob = freeStyleJob("${PROJECT_NAME}/Cloud_Provision/IaaS/Create-Instance/Initiate_Server_Creation")
def scmProject = "git@gitlab:${WORKSPACE_NAME}/Oracle_Tech.git"
def scmCredentialsId = "adop-jenkins-master"

Closure passwordParam(String paramName, String paramDescription, String paramDefaultValue) {
    return { project ->
        project / 'properties' / 'hudson.model.ParametersDefinitionProperty' / 'parameterDefinitions' << 'hudson.model.PasswordParameterDefinition' {
            'name'(paramName)
      		'description'(paramDescription)
        	'defaultValue'(paramDefaultValue)
        }
    }
}

folder("${PROJECT_NAME}/Cloud_Provision") {
  configure { folder ->
    folder / icon(class: 'org.example.MyFolderIcon')
  }
}

folder("${PROJECT_NAME}/Cloud_Provision/IaaS") {
  configure { folder ->
    folder / icon(class: 'org.example.MyFolderIcon')
  }
}

folder("${PROJECT_NAME}/Cloud_Provision/IaaS/Create-Instance") {
  configure { folder ->
    folder / icon(class: 'org.example.MyFolderIcon')
  }
}

createJob.with {
    description('')
    parameters {
        stringParam('OPC_USERNAME', '', 'Account\'s username in OPC (Oracle Public Cloud).')
        stringParam('DOMAIN', '580502591', 'Oracle Public Cloud Account\'s Identity Domain. (eg. a424647)')
        stringParam('ENDPOINT_URL', 'https://compute.aucom-east-1.oraclecloud.com', 'Oracle Public Cloud Endpoint URL. (eg. https://compute.aucom-east-1.oraclecloud.com)')
        stringParam('INSTANCE_NAME', '', 'Unique name for the instance. (eg. test_gft_server)')
        stringParam('SSH_KEY', 'devops_key', 'This key must be created already in OPC. (eg. oracle_key)')
		stringParam('IMAGE_LIST', '/oracle/public/OL_6.8_UEKR4_x86_64', 'Image list of the instance. (eg. /oracle/public/OL_6.8_UEKR4_x86_64)')
		stringParam('IMAGE_LIST_ENTRY', '2', 'Defines an image list entry.')
		stringParam('SHAPE', 'oc1m', 'Instance shape (OCPU and memory).')
        stringParam('BOOT_STORAGE_SIZE', '30', 'Bootable Storage size in GigaByte.')
        stringParam('NON_BOOT_STORAGE_SIZE', '30', 'Non-bootable Storage size in GigaByte.')
    }
    configure passwordParam("OPC_PASSWORD", "Account\'s password in OPC (Oracle Public Cloud)", "")

    logRotator {
        numToKeep(10)
        artifactNumToKeep(10)
    }

    concurrentBuild(true)
    label('postgres')

    scm {
        git {
            remote {
                url(scmProject)
                credentials(scmCredentialsId)
            }
            branch('*/master')
        }
    }

    wrappers {
        preBuildCleanup() 
        colorizeOutput('css')
    }

    steps {
		shell('''#!/bin/bash
echo "CUSTOM_WORKSPACE=${WORKSPACE}" > props
mv ${WORKSPACE}/Cloud_Provision/IaaS/terraform_compute/* .
find . -type d -exec rm -rf {} +

cat > credentials.tfvars <<-EOF
domain = "${DOMAIN}"
endpoint = "${ENDPOINT_URL}"
sshkey = "/Compute-${DOMAIN}/${OPC_USERNAME}/${SSH_KEY}"
instance_name = "${INSTANCE_NAME}"
image_list = "${IMAGE_LIST}"
image_list_entry = "${IMAGE_LIST_ENTRY}"
shape = "${SHAPE}"
ssh_seclist = "/Compute-${DOMAIN}/${OPC_USERNAME}/defaultSSH"
http_seclist = "/Compute-${DOMAIN}/${OPC_USERNAME}/defaultHTTP"
https_seclist = "/Compute-${DOMAIN}/${OPC_USERNAME}/defaultHTTPS"
wls_seclist = "/Compute-${DOMAIN}/${OPC_USERNAME}/wls-port"
boot_storage_size = "${BOOT_STORAGE_SIZE}"
storage_size = "${NON_BOOT_STORAGE_SIZE}"
EOF

		''')
	
        environmentVariables {
            propertiesFile('props')
        }
    }

    publishers {

        archiveArtifacts('**/*')

        downstreamParameterized {
            trigger('Execute_Server_Creation') {
                condition('SUCCESS')
                parameters {
                    currentBuild()
                    propertiesFile('props', true)
                }
            }
        }
    }

}

