package com.github.pocketsquare.articles.repository

import com.github.pocketsquare.articles.domain.Article
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.PagingAndSortingRepository

interface ArticleRepository extends PagingAndSortingRepository<Article, String> {

    List<Article> findByUserIdAndRead(String userId, boolean read, Pageable pageable)

    void deleteByUserId(String userId)

    int countByUserId(String userId)

    Article findOneByUserIdAndPocketId(String userId, String pocketId)
}