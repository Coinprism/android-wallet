package com.coinprism.wallet;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.test.InstrumentationTestCase;

import com.coinprism.model.APIException;
import com.coinprism.model.AddressBalance;

import junit.framework.Assert;

import org.junit.Test;

import java.io.IOException;

public class APIClient extends InstrumentationTestCase
{
    @Test
    public void test_getAddressBalance_noAsset() throws Exception
    {
        IResponse get = new IResponse()
        {
            @Override
            public String getResponse(String url)
            {
                return  "{" +
                    "  \"address\": \"akMVNmYwN8bqqQknswpgPCoVCUAyiXyDxvG\",\n" +
                    "  \"asset_address\": \"akMVNmYwN8bqqQknswpgPCoVCUAyiXyDxvG\",\n" +
                    "  \"bitcoin_address\": \"1BXVXP82f7x9YWdWuCaCYwad8ZoYayyRYt\",\n" +
                    "  \"issuable_asset\": \"AcuREPQJemxHwqVZzbsutcVVqPr4HwjXBa\",\n" +
                    "  \"balance\": 500,\n" +
                    "  \"unconfirmed_balance\": 1500,\n" +
                    "  \"assets\": []\n" +
                    "}";
            }
        };

        APIClientMock client = new APIClientMock(get, null);

        AddressBalance balance = client.getAddressBalance("akMVNmYwN8bqqQknswpgPCoVCUAyiXyDxvG");

        Assert.assertEquals(new Long(2000L), balance.getSatoshiBalance());
        Assert.assertEquals(0, balance.getAssetBalances().size());
    }

    @Test
    public void test_getAddressBalance_withAsset() throws Exception
    {
        IResponse get = new IResponse()
        {
            @Override
            public String getResponse(String url)
            {
                return  "{" +
                        "  \"address\": \"akMVNmYwN8bqqQknswpgPCoVCUAyiXyDxvG\",\n" +
                        "  \"asset_address\": \"akMVNmYwN8bqqQknswpgPCoVCUAyiXyDxvG\",\n" +
                        "  \"bitcoin_address\": \"1BXVXP82f7x9YWdWuCaCYwad8ZoYayyRYt\",\n" +
                        "  \"issuable_asset\": \"AcuREPQJemxHwqVZzbsutcVVqPr4HwjXBa\",\n" +
                        "  \"balance\": 500,\n" +
                        "  \"unconfirmed_balance\": 1500,\n" +
                        "  \"assets\": [\n" +
                        "    {\n" +
                        "      \"id\": \"AN51SPP6iZBHFJ3aux1jtn6MMMD13Gh3t7\",\n" +
                        "      \"balance\": \"500\",\n" +
                        "      \"unconfirmed_balance\": \"0\"\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"id\": \"AS6tDJJ3oWrcE1Kk3T14mD8q6ycHYVzyYQ\",\n" +
                        "      \"balance\": \"200000\",\n" +
                        "      \"unconfirmed_balance\": \"0\"\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}";
            }
        };

        APIClientMock client = new APIClientMock(get, null);

        AddressBalance balance = client.getAddressBalance("akMVNmYwN8bqqQknswpgPCoVCUAyiXyDxvG");

        Assert.assertEquals(new Long(2000L), balance.getSatoshiBalance());
        Assert.assertEquals(2, balance.getAssetBalances().size());
    }

    private class APIClientMock extends com.coinprism.model.APIClient
    {
        private final IResponse get;
        private final IResponse post;

        public APIClientMock(IResponse get, IResponse post)
        {
            super("https://test/");
            this.get = get;
            this.post = post;
        }

        @Override
        protected String executeHttpPost(String url, String body) throws IOException, APIException
        {
            return post.getResponse(url);
        }

        @Override
        protected String executeHttpGet(String url) throws IOException, APIException
        {
            return get.getResponse(url);
        }
    }

    private interface IResponse
    {
        String getResponse(String url);
    }
}
