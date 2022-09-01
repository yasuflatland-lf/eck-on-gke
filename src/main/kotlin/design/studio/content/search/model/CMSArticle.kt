package design.studio.content.search.model

/**
 * @author Yasuyuki Takeo
 */
data class CMSArticle(
    var id: String?,
    var projectId: Long?,
    var groupId: Long?,
    var roleIds: List<Long>?,
    var title: String?,
    var content: String?,
    var title_ja: String?,
    var content_ja: String?
) {
    constructor() : this("", 0, 0, emptyList(), "", "", "", "")
}