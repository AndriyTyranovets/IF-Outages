package com.github.andriytyranovets.ifoutages.services

import com.github.andriytyranovets.ifoutages.models.OutageResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

// https://svitlo.oe.if.ua/GAVTurnOff/GavGroupByAccountNumber
interface OutagesApiService {
    @FormUrlEncoded
//    @Headers(
//        "Accept: */*",
//        "User-Agent: Mozilla/5.0 (X11; Linux x86_64; rv:126.0) Gecko/20100101 Firefox/126.0"
//    )
    @POST("GavGroupByAccountNumber")
    suspend fun getOutages(
        @Field("accountNumber") accountNumber: String,
        @Field("userSearchChoice") searchChoice: String = "pob",
        @Field("address") address: String = ""
    ): OutageResponse
}