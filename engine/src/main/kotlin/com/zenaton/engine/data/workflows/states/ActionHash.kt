package com.zenaton.engine.data.workflows.states

import com.zenaton.engine.data.types.Hash

data class ActionHash(override val hash: String) : Hash(hash)
