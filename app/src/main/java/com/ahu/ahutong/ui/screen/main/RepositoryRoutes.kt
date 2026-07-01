package com.ahu.ahutong.ui.screen.main

import android.net.Uri
import androidx.navigation.NavHostController

const val REPOSITORY_ROUTE = "repository"
const val REPOSITORY_PATH_ARG = "path"
const val REPOSITORY_DIRECTORY_ROUTE = "repository_dir?path={path}"

fun repositoryRoute(path: String): String {
    return if (path.isBlank()) {
        REPOSITORY_ROUTE
    } else {
        "repository_dir?path=${Uri.encode(path)}"
    }
}

fun NavHostController.navigateRepository(path: String) {
    navigate(repositoryRoute(path))
}
