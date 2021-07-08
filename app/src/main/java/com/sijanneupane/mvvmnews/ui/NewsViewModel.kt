package com.sijanneupane.mvvmnews.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sijanneupane.mvvmnews.models.Article
import com.sijanneupane.mvvmnews.models.NewsResponse
import com.sijanneupane.mvvmnews.repository.NewsRepository
import com.sijanneupane.mvvmnews.utils.Resource
import kotlinx.coroutines.launch
import retrofit2.Response

class NewsViewModel(
    val newsRepository: NewsRepository //parameter
) : ViewModel(){

    //LIVEDATA OBJECT
    val breakingNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    val searchNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()

    //Pagination
    var breakingNewsPage= 1
    var searchNewsPage= 1
    var breakingNewsResponse : NewsResponse? = null
    var searchNewsResponse : NewsResponse? = null


    init {
        getBreakingNews("in")
    }

    //we cannot start the function in the coroutine so we start the it here
    /*
    viewModelScope makes the function alive only as long as the ViewModel is alive
     */
    fun getBreakingNews(countryCode: String)= viewModelScope.launch {
        breakingNews.postValue(Resource.Loading()) //init loading state before the network call

        //actual response
        val response= newsRepository.getBreakingNews(countryCode, breakingNewsPage)

        //handling response
        breakingNews.postValue(handleBreakingNewsResponse(response))
    }


    fun searchNews(searchQuery: String)= viewModelScope.launch {
        searchNews.postValue(Resource.Loading()) //init loading state before the network call

        //actual response
        val response= newsRepository.searchNews(searchQuery, searchNewsPage)

        //handling response
        searchNews.postValue(handleSearchNewsResponse(response))
    }


    private fun handleBreakingNewsResponse(response: Response<NewsResponse>): Resource<NewsResponse>{
        if (response.isSuccessful){
            response.body()?.let { resultResponse ->
                breakingNewsPage++
                if (breakingNewsResponse== null){
                    breakingNewsResponse= resultResponse //if first page save the result to the response
                }else{
                    val oldArticles= breakingNewsResponse?.articles //else, add all articles to old
                    val newArticle= resultResponse.articles //add new response to new
                    oldArticles?.addAll(newArticle) //add new articles to old articles
                }
                return  Resource.Success(breakingNewsResponse ?: resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    private fun handleSearchNewsResponse(response: Response<NewsResponse>): Resource<NewsResponse>{
        if (response.isSuccessful){
            response.body()?.let { resultResponse ->
                searchNewsPage++
                if (searchNewsResponse== null){
                    searchNewsResponse= resultResponse //if first page save the result to the response
                }else{
                    val oldArticles= searchNewsResponse?.articles //else, add all articles to old
                    val newArticle= resultResponse.articles //add new response to new
                    oldArticles?.addAll(newArticle) //add new articles to old articles
                }
                return  Resource.Success(searchNewsResponse ?: resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    /*
    function to save articles to db: coroutine
     */
    fun saveArticle(article: Article)= viewModelScope.launch {
        newsRepository.upsert(article)
    }

    /*
    function to get all saved news articles
     */
    fun getSavedArticle()= newsRepository.getSavedNews()

    /*
    function to delete article from db
     */
    fun deleteSavedArticle(article: Article)= viewModelScope.launch {
        newsRepository.deleteArticle(article)
    }

}