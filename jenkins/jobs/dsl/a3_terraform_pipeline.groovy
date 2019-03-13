def containerFolder = "${PROJECT_NAME}/Cloud_Provision/IaaS/Create-Instance"

buildPipelineView(containerFolder + '/Server_Creation_Pipeline') {
    title('Provision Servers')
    displayedBuilds(10)
    selectedJob('Initiate_Server_Creation')
	showPipelineDefinitionHeader()
    showPipelineParameters()
	consoleOutputLinkStyle(OutputStyle.NewWindow)
    refreshFrequency(3)
}