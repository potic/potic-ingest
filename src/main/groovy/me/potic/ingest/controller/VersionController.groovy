package me.potic.ingest.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
class VersionController {

    @GetMapping(path = '/version')
    @ResponseBody String version() {
        VersionController.package.implementationVersion
    }
}
