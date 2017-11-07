package me.potic.ingest.domain

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@EqualsAndHashCode
@ToString
class PocketVideo {

    String video_id
    String item_id

    String src

    String height
    String width

    String vid
    String type
}
