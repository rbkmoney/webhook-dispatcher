#!groovy
build('webhook-dispatcher', 'java-maven') {
    checkoutRepo()
    loadBuildUtils()

    def javaServicePipeline
    runStage('load JavaService pipeline') {
        javaServicePipeline = load("build_utils/jenkins_lib/pipeJavaService.groovy")
    }


    def buildImageTag = "fcf116dd775cc2e91bffb6a36835754e3f2d5321"
    javaLibPipeline(buildImageTag)
}