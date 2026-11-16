package com.srap.wash.logic.repository

import com.google.gson.GsonBuilder
import com.srap.wash.logic.model.LatestUsedResponse
import com.srap.wash.logic.state.Code
import com.srap.wash.logic.state.CodeAdapter
import com.srap.wash.logic.state.LoadingState
import org.junit.Assert.*
import org.junit.Test

class ListResponseStateTest {
    private val gson = GsonBuilder().registerTypeAdapter(Code::class.java, CodeAdapter()).create()

    private fun response(json: String) = gson.fromJson(json, LatestUsedResponse::class.java)

    @Test fun successfulNullHistoryIsEmpty() {
        // Actual startup response observed on K70.
        val result = response("""{"code":0,"data":null,"msg":"成功"}""")
            .toLoadingState(allowNullData = true)
        assertEquals(LoadingState.Success(emptyList<LatestUsedResponse.Data>()), result)
    }

    @Test fun successfulEmptyArrayIsEmpty() {
        val result = response("""{"code":0,"data":[],"msg":"成功"}""")
            .toLoadingState(allowNullData = true)
        assertEquals(LoadingState.Success(emptyList<LatestUsedResponse.Data>()), result)
    }

    @Test fun existingMachinesArePreserved() {
        val result = response("""{"code":0,"data":[{"goodsId":42,"goodsName":"洗衣机","orgId":1,"orgName":"宿舍","workStatus":10}],"msg":"成功"}""")
            .toLoadingState(allowNullData = true) as LoadingState.Success
        assertEquals(42, result.response.single().goodsId)
        assertEquals("宿舍", result.response.single().orgName)
    }

    @Test fun loginErrorIsStillAnError() {
        val result = response("""{"code":2,"data":null,"msg":"请登录"}""")
            .toLoadingState(allowNullData = true)
        assertEquals(LoadingState.Error("请登录"), result)
    }

    @Test fun nullDataIsStillAnErrorUnlessExplicitlyAllowed() {
        val result = response("""{"code":0,"data":null,"msg":"成功"}""").toLoadingState()
        assertEquals(LoadingState.Error("异常"), result)
    }
}
