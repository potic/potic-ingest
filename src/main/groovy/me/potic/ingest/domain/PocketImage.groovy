package me.potic.ingest.domain

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@EqualsAndHashCode
@ToString
class PocketImage {

    String image_id
    String item_id

    String src

    String caption
    String credit

    String height
    String width
}
