package whq.projects.sd.entities

import retrofit2.Call
import whq.projects.entities.ContentAndOP
import whq.projects.entities.ContentPublishSimple
import whq.projects.utilities.*

data class ArticleIdList(var article_id_key: String, val articles_list: List<ContentPublishSimple>) {
    private var idx_current: Int? = null
    fun nextArticle(next: Boolean): Call<ContentAndOP>? {
        idx_current = if (idx_current == null)
            articles_list.indexOfFirst { it._id == article_id_key }
        else if (next) {
            (idx_current!! + 1) % articles_list.size
        } else {
            (idx_current!! - 1 + articles_list.size) % articles_list.size
        }
        return idx_current?.run {
            WhqService.serviceInstance().getArticleById(articles_list[this]._id)
        }
    }
}

