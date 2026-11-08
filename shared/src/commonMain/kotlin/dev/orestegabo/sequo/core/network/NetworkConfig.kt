package dev.orestegabo.sequo.core.network

object NetworkConfig {
    const val ProductionBaseUrl = "https://api.sequoservice.com"
    const val BaseUrl = GeneratedSequoBuildConfig.ApiBaseUrl
    const val ReleaseId = GeneratedSequoBuildConfig.ReleaseId
    const val Environment = GeneratedSequoBuildConfig.Environment
}
