package design.studio.content.search.model

import org.springframework.data.annotation.Id

/**
 * @author Yasuyuki Takeo
 */
data class CMSArticle(
    @Id
    var id: Long?,
    var projectId: Long?,
    var groupId: Long?,
    var roleIds: List<Long>?,
    var title: String?,
    var content: String?,
    var title_ja: String?,
    var content_ja: String?
) {
    constructor() : this(0, 0, 0, emptyList(), "", "","","")
}