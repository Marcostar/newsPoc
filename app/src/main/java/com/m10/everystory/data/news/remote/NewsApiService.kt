package com.m10.everystory.data.news.remote

import com.m10.everystory.core.network.API_KEY_HEADER
import com.m10.everystory.data.news.remote.dto.ArticlesResponseDto
import com.m10.everystory.data.news.remote.dto.SourcesResponseDto
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface NewsApiService {

    // Retrofit leaves null params out. NewsAPI refuses sources mixed with category or language.
    @GET("top-headlines")
    suspend fun topHeadlines(
        @Query("language") language: String?,
        @Query("category") category: String?,
        @Query("sources") sources: String?,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int,
    ): ArticlesResponseDto

    @GET("everything")
    suspend fun everything(
        @Query("q") query: String,
        @Query("searchIn") searchIn: String,
        @Query("language") language: String,
        @Query("sortBy") sortBy: String,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int,
    ): ArticlesResponseDto

    @GET("top-headlines/sources")
    suspend fun sources(
        @Query("category") category: String?,
        @Query("language") language: String,
    ): SourcesResponseDto

    // The cheapest real request: one story, sent with the key being tried rather than the one in use.
    @GET("top-headlines")
    suspend fun checkKey(
        @Header(API_KEY_HEADER) apiKey: String,
        @Query("language") language: String,
        @Query("pageSize") pageSize: Int = 1,
    ): ArticlesResponseDto
}
