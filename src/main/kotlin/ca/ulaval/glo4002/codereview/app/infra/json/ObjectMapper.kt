package ca.ulaval.glo4002.codereview.app.infra.json

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

val configuredMapper = ObjectMapper().apply {
    registerKotlinModule()
    enable(SerializationFeature.INDENT_OUTPUT)
}
