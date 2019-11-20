package com.istl.samples.faceverification.services;

import com.istl.samples.faceverification.utils.NidRequest;
import com.istl.samples.faceverification.utils.VoterInfo;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface NidService {
    @POST("person/getNidData")
    Call<VoterInfo> verifyVoter(@Body NidRequest request);
}
