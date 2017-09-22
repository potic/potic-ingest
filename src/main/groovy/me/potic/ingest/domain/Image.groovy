package me.potic.ingest.domain

import groovy.transform.builder.Builder

@Builder
class Image {

    String pocketId

    String src
    String credit
    String caption
    Integer height
    Integer width
}
