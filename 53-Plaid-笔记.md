---
toc: false
comments: false
title: Plaid 笔记
description: Android 经典开源项目 Plaid 阅读笔记
tags:
  - Android
id: 53
categories:
  - Android
date: 2019-4-12
---

# 数据流向

Android 经典开源项目 Plaid 阅读笔记

## StoryActivity
```kotlin
viewModel.getUiModel().observe(this,
                storyUiModel -> setupComments(storyUiModel.getComments()));
```

<!-- more -->

## StoryViewModel
```kotlin
private fun getComments() = viewModelScope.launch(dispatcherProvider.computation) {
        val result = getCommentsWithRepliesAndUsers(story.links.comments) //GetCommentsWithRepliesAndUsersUseCase 重写 invoke
        if (result is Result.Success) {
            withContext(dispatcherProvider.main) { "set 数据到 MutableLiveData"}
        }
    }
```

## GetCommentsWithRepliesAndUsersUseCase
> 组合 Repository 和别的 Repository,返回新的数据，避免 Repository 过于臃肿

```kotlin
class GetCommentsWithRepliesAndUsersUseCase @Inject constructor(
    private val getCommentsWithReplies: GetCommentsWithRepliesUseCase,
    private val userRepository: UserRepository
) {
    //获取数据
    suspend operator fun invoke(ids: List<Long>): Result<List<Comment>> {
        // Get the comments with replies
        val commentsWithRepliesResult = getCommentsWithReplies(ids)
        if (commentsWithRepliesResult is Result.Error) {
            return commentsWithRepliesResult
        }
        val commentsWithReplies = (commentsWithRepliesResult as? Result.Success)?.data.orEmpty()
        // get the ids of the users that posted comments
        val userIds = mutableSetOf<Long>()
        createUserIds(commentsWithReplies, userIds)

        // get the users
        val usersResult = userRepository.getUsers(userIds)
        val users = if (usersResult is Result.Success) {
            usersResult.data
        } else {
            emptySet()
        }
        // create the comments based on the comments with replies and users
        val comments = createComments(commentsWithReplies, users)
        return Result.Success(comments)
    }

    // 处理数据
    private fun createUserIds(comments: List<CommentWithReplies>, userIds: MutableSet<Long>) {
        comments.forEach {
            userIds.add(it.userId)
            createUserIds(it.replies, userIds)
        }
    }

    private fun createComments(
        commentsWithReplies: List<CommentWithReplies>,
        users: Set<User>
    ): List<Comment> {
        val userMapping = users.associateBy(User::id)
        return commentsWithReplies.asSequence()
                .flatMap(CommentWithReplies::flattenWithReplies)
                .map { it.toComment(userMapping[it.userId]) }
                .toList()
    }
}
```

## Repository
> 网络数据和本地数据的集合

```kotlin
class UserRepository(
    private val dataSource: UserRemoteDataSource,
    private val roomSource: DBDataSource, 
) {
    private suspend fun getAndCacheUsers(userIds: List<Long>) {
        val result = dataSource.getUsers(userIds)

        // save the new users in the cachedUsers
        if (result is Result.Success) {
            result.data.forEach { cachedUsers[it.id] = it }
        }
    }
}

```


## RemoteDataSource
> 持有 Retrofit 的 Service 对象,调用 Service 中的各个数据接口,同理 LocalDataSource 是调用本地数据库的类

```kotlin
class UserRemoteDataSource @Inject constructor(private val service: DesignerNewsService) {
    suspend fun getUsers(userIds: List<Long>) = safeApiCall(
        call = { requestGetUsers(userIds) },
        errorMessage = "Error getting user"
    )

    private suspend fun requestGetUsers(userIds: List<Long>): Result<List<User>> {
        val requestIds = userIds.joinToString(",")

        val response = service.getUsers(requestIds).await()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                return Result.Success(body)
            }
        }

        return Result.Error(
            IOException("Error getting users ${response.code()} ${response.message()}")
        )
    }
}
```


# 模块分类
## app 
Activity + ViewModel + layout + Application + 本模块相关的Dagger

## core
业务数据，业务逻辑，业务模型，比如 数据仓库 + 网络数据 + 本地数据 + Adapter + ViewHolder + ViewHolderItem布局 + util + 本模块相关的Dagger

## 业务模块
项目的某个功能或者页面的模块
UseCase + ViewModel + 页面的布局 + Activity + DataBindingAdapter + UiMOdel 