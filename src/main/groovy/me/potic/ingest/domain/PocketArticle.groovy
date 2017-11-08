package me.potic.ingest.domain

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@EqualsAndHashCode
@ToString(includeNames = true)
class PocketArticle {

    String item_id
    String resolved_id
    Integer sort_id

    String status

    String given_url
    String resolved_url
    String amp_url

    String given_title
    String resolved_title

    Long time_added
    Long time_updated
    Long time_read
    Long time_favorited

    String favorite

    String is_article
    String excerpt
    Long word_count

    String has_image
    PocketImage image
    List<PocketImage> images

    List<PocketAuthor> authors

    String has_video
    List<PocketVideo> videos

    List<PocketTag> tags

    String is_index
}
