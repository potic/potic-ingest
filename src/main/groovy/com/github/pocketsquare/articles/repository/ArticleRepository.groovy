package com.github.pocketsquare.articles.repository

import com.github.pocketsquare.articles.domain.Article
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource(collectionResourceRel = 'article', path = 'article')
interface ArticleRepository extends PagingAndSortingRepository<Article, String> {

    Collection<Article> findByUserId(String userId)

//    Collection<Article> findByUserIdAndReadIsTrue(String userId)
//
//    Collection<Article> findByUserIdAndReadIsFalse(String userId)
}