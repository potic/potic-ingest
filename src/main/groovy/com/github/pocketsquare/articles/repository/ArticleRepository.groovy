package com.github.pocketsquare.articles.repository

import com.github.pocketsquare.articles.domain.Article
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.PagingAndSortingRepository

interface ArticleRepository extends PagingAndSortingRepository<Article, String> {

    Collection<Article> findByUserId(String userId, Pageable pageable)
}