package io.mo.viaport.helper // package io.nekohasekai.sfa.helper
//
// import androidx.paging.PagingSource
// import androidx.paging.PagingState
//
// class DataSource(logs: List<String>): PagingSource<Int, String>(){
//     override suspend fun load(params: LoadParams<Int>): LoadResult<Int, String> {
//         return try {
//             val page = params.key ?: 1 // set page 1 as default
//             val pageSize = params.loadSize
//             val repoResponse = gitHubService.searchRepos(page, pageSize)
//             val repoItems = repoResponse.items
//             val prevKey = if (page > 1) page - 1 else null
//             val nextKey = if (repoItems.isNotEmpty()) page + 1 else null
//             LoadResult.Page(repoItems, prevKey, nextKey)
//         } catch (e: Exception) {
//             LoadResult.Error(e)
//         }
//     }
//
//     override fun getRefreshKey(state: PagingState<Int, String>): Int? {
//         return null
//     }
// }