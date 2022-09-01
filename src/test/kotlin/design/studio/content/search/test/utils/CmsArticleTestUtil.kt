package design.studio.content.search.test.utils

import co.elastic.clients.elasticsearch.core.BulkResponse
import design.studio.content.search.model.CMSArticle
import design.studio.content.search.repositories.CMSArticleRepository
import design.studio.content.search.service.elasticsearch.connection.constants.ConnectionConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
class CmsArticleTestUtil(val cmsArticleRepository: CMSArticleRepository) {
    companion object {
    }

    suspend fun induce(fileName: String): BulkResponse = withContext(Dispatchers.Default) {
        var contents = TestContentReader.getContents(fileName)
        var bulk = contents?.map { it ->
            var entity = CMSArticle()
            entity.title_ja = it.title
            entity.content_ja = it.body
            return@map entity
        }.orEmpty()

        var res = async {
            cmsArticleRepository.createBulkIndices(ConnectionConstants.DEFAULT_INDEX, bulk)
        }

        return@withContext res.await()
    }

}