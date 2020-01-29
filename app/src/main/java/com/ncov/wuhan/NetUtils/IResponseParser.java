package com.ncov.wuhan.NetUtils;


public interface IResponseParser<R> {
    R parser(String jsonStr) throws Exception;
}
