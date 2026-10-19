package com.example.ersteversion.data.model

fun countUnseenChanges(changes: List<Change>, seenIds: Set<String>): Int {
    return changes.count { it.id.toString() !in seenIds }
}