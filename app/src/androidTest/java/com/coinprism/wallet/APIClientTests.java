/*
 * Copyright (c) 2014 Flavien Charlon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.coinprism.wallet;

import android.test.InstrumentationTestCase;

import com.coinprism.model.APIException;
import com.coinprism.model.AddressBalance;
import com.coinprism.model.SingleAssetTransaction;

import junit.framework.Assert;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

public class APIClientTests extends InstrumentationTestCase
{
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

        Assert.assertEquals(Long.valueOf(2000L), balance.getSatoshiBalance());
        Assert.assertEquals(0, balance.getAssetBalances().size());
    }

    public void test_getAddressBalance_assetNoDefinition() throws Exception
    {
        IResponse get = new IResponse()
        {
            @Override
            public String getResponse(String url)
            {
                if (url.contains("/addresses/"))
                {
                    return "{" +
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
                else
                {
                    return "Error";
                }
            }
        };

        APIClientMock client = new APIClientMock(get, null);

        AddressBalance balance = client.getAddressBalance("akMVNmYwN8bqqQknswpgPCoVCUAyiXyDxvG");

        Assert.assertEquals(Long.valueOf(2000L), balance.getSatoshiBalance());
        Assert.assertEquals(2, balance.getAssetBalances().size());
        Assert.assertEquals(new BigInteger("500"), balance.getAssetBalances().get(0).getQuantity());
        Assert.assertEquals("AN51SPP6iZBHFJ3aux1jtn6MMMD13Gh3t7", balance.getAssetBalances().get(0).getAsset().getAssetId());
        Assert.assertEquals(null, balance.getAssetBalances().get(0).getAsset().getName());
        Assert.assertEquals(Boolean.valueOf(true), balance.getAssetBalances().get(0).getAsset().getIsUnknown());
        Assert.assertEquals(new BigInteger("200000"), balance.getAssetBalances().get(1).getQuantity());
        Assert.assertEquals("AS6tDJJ3oWrcE1Kk3T14mD8q6ycHYVzyYQ", balance.getAssetBalances().get(1).getAsset().getAssetId());
        Assert.assertEquals(null, balance.getAssetBalances().get(1).getAsset().getName());
        Assert.assertEquals(Boolean.valueOf(true), balance.getAssetBalances().get(1).getAsset().getIsUnknown());
    }

    public void test_getAddressBalance_assetWithDefinition() throws Exception
    {
        IResponse get = new IResponse()
        {
            @Override
            public String getResponse(String url)
            {
                if (url.contains("/addresses/akMVNmYwN8bqqQknswpgPCoVCUAyiXyDxvG"))
                {
                    return "{" +
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
                else if (url.contains("/assets/AN51SPP6iZBHFJ3aux1jtn6MMMD13Gh3t7"))
                {
                    return "{\n" +
                            "  \"asset_id\": \"AN51SPP6iZBHFJ3aux1jtn6MMMD13Gh3t7\",\n" +
                            "  \"metadata_url\": \"https://someurl\",\n" +
                            "  \"final_metadata_url\": \"https://someurl\",\n" +
                            "  \"verified_issuer\": false,\n" +
                            "  \"name\": \"Test Asset #4\",\n" +
                            "  \"contract_url\": null,\n" +
                            "  \"name_short\": \"TESTASSET\",\n" +
                            "  \"issuer\": null,\n" +
                            "  \"description\": \"Asset description.\",\n" +
                            "  \"description_mime\": \"text/x-markdown; charset=UTF-8\",\n" +
                            "  \"type\": \"Stock\",\n" +
                            "  \"divisibility\": 1,\n" +
                            "  \"icon_url\": \"https://coinprism.blob.core.windows.net/profile/icon/test.jpg\",\n" +
                            "  \"image_url\": \"https://coinprism.blob.core.windows.net/profile/image/test.jpg\"\n" +
                            "}";
                }
                else
                {
                    return "Error";
                }
            }
        };

        APIClientMock client = new APIClientMock(get, null);

        AddressBalance balance = client.getAddressBalance("akMVNmYwN8bqqQknswpgPCoVCUAyiXyDxvG");

        Assert.assertEquals(Long.valueOf(2000L), balance.getSatoshiBalance());
        Assert.assertEquals(2, balance.getAssetBalances().size());
        Assert.assertEquals(new BigInteger("500"), balance.getAssetBalances().get(0).getQuantity());
        Assert.assertEquals("AN51SPP6iZBHFJ3aux1jtn6MMMD13Gh3t7", balance.getAssetBalances().get(0).getAsset().getAssetId());
        Assert.assertEquals("Test Asset #4", balance.getAssetBalances().get(0).getAsset().getName());
        Assert.assertEquals("TESTASSET", balance.getAssetBalances().get(0).getAsset().getTicker());
        Assert.assertEquals(1, balance.getAssetBalances().get(0).getAsset().getDivisibility());
        Assert.assertEquals("https://coinprism.blob.core.windows.net/profile/icon/test.jpg", balance.getAssetBalances().get(0).getAsset().getIconUrl());
        Assert.assertEquals(Boolean.valueOf(false), balance.getAssetBalances().get(0).getAsset().getIsUnknown());
    }

    public void test_buildTransaction() throws Exception
    {
        IResponse post = new IResponse()
        {
            @Override
            public String getResponse(String url)
            {
                return
                    "{\n" +
                    "  \"raw\": \"0100000001c814e35a5a5f6c38d3b86ea8ebc41f517b10f1ccc145c467891683bd316f662a010000001976a9140ad8314907ce3b83344f5cd45dfa776eca6bf0a188acffffffff02400d0300000000001976a91459611e2b7ea02e81121671322108bc66911bdb9188ac1db86008000000001976a9140ad8314907ce3b83344f5cd45dfa776eca6bf0a188ac00000000\"\n" +
                    "}";
            }
        };

        APIClientMock client = new APIClientMock(null, post);

        Transaction transaction = client.buildTransaction(
            "194DnvmLR2HULRvxUsVag8mn2fm7dA3U2B",
            "1CAbqVWoFWKnpg9KZb6jSNmpy2398HRCTk",
            "150000000",
            null,
            10000);

        Assert.assertEquals(1, transaction.getInputs().size());
        Assert.assertEquals("2a666f31bd83168967c445c1ccf1107b511fc4eba86eb8d3386c5f5a5ae314c8", transaction.getInputs().get(0).getOutpoint().getHash().toString());
        Assert.assertEquals(1, transaction.getInputs().get(0).getOutpoint().getIndex());
        Assert.assertEquals(2, transaction.getOutputs().size());
        Assert.assertEquals("DUP HASH160 PUSHDATA(20)[59611e2b7ea02e81121671322108bc66911bdb91] EQUALVERIFY CHECKSIG", transaction.getOutput(0).getScriptPubKey().toString());
        Assert.assertEquals(200000, transaction.getOutput(0).getValue().value);
        Assert.assertEquals("DUP HASH160 PUSHDATA(20)[0ad8314907ce3b83344f5cd45dfa776eca6bf0a1] EQUALVERIFY CHECKSIG", transaction.getOutput(1).getScriptPubKey().toString());
        Assert.assertEquals(140556317, transaction.getOutput(1).getValue().value);
    }

    public void test_getTransactions() throws Exception
    {
        IResponse get = new IResponse()
        {
            @Override
            public String getResponse(String url)
            {
                if (url.contains("/addresses/1BXVXP82f7x9YWdWuCaCYwad8ZoYayyRYt/transactions"))
                {
                    return "[\n" +
                        "  {\n" +
                        "    \"hash\": \"eba760a81b177051b0520418b4e10596955adb98196c15367a2467ab66a19b5c\",\n" +
                        "    \"block_hash\": \"0000000000000000294fd5f69897880df65ad50c4a6aa1ee90a5f52d5dba7504\",\n" +
                        "    \"block_height\": 313677,\n" +
                        "    \"block_time\": \"2014-08-02T16:48:57.0000000Z\",\n" +
                        "    \"inputs\": [\n" +
                        "      {\n" +
                        "        \"transaction_hash\": \"eba760a81b177051b0520418b4e10596955adb98196c15367a2467ab66a19b5c\",\n" +
                        "        \"output_hash\": \"028df346bbb02469273ed286e0a1d40c965326e819f19371d04c99cf2650fddf\",\n" +
                        "        \"output_index\": 0,\n" +
                        "        \"value\": 600,\n" +
                        "        \"addresses\": [\n" +
                        "          \"1HHYPRCFijLkwQFsEPLv6iNEkYiwB4qsgs\"\n" +
                        "        ],\n" +
                        "        \"script_signature\": \"483045022100f43b7aaf8aa0fda50da3aac313c73718c2b3aa7cdcbe017a99a67a870abf5a29022033cac4d6602e72709fe58069e13391ba58f2f6e0e2c9bf446c01b1e9b045c5b401210279c1e47b36086d616523cd7c63eb592871b349236d1f0d0ffb9c94935539cfb9\",\n" +
                        "        \"asset_id\": \"AN51SPP6iZBHFJ3aux1jtn6MMMD13Gh3t7\",\n" +
                        "        \"asset_quantity\": \"2000\"\n" +
                        "      },\n" +
                        "      {\n" +
                        "        \"transaction_hash\": \"eba760a81b177051b0520418b4e10596955adb98196c15367a2467ab66a19b5c\",\n" +
                        "        \"output_hash\": \"028df346bbb02469273ed286e0a1d40c965326e819f19371d04c99cf2650fddf\",\n" +
                        "        \"output_index\": 2,\n" +
                        "        \"value\": 1947000,\n" +
                        "        \"addresses\": [\n" +
                        "          \"1HHYPRCFijLkwQFsEPLv6iNEkYiwB4qsgs\"\n" +
                        "        ],\n" +
                        "        \"script_signature\": \"48304502201768e35c93427603d4df69c4fb5e1358aa19b323879cbe972044ec0843819bc1022100cbe3156ae50c3e94d4e277b03f7d903f35d352e4f49ec64218a302fc423591de01210279c1e47b36086d616523cd7c63eb592871b349236d1f0d0ffb9c94935539cfb9\",\n" +
                        "        \"asset_id\": null,\n" +
                        "        \"asset_quantity\": null\n" +
                        "      }\n" +
                        "    ],\n" +
                        "    \"outputs\": [\n" +
                        "      {\n" +
                        "        \"transaction_hash\": \"eba760a81b177051b0520418b4e10596955adb98196c15367a2467ab66a19b5c\",\n" +
                        "        \"index\": 0,\n" +
                        "        \"value\": 0,\n" +
                        "        \"addresses\": [],\n" +
                        "        \"script\": \"6a0a4f41010002f403dc0b00\",\n" +
                        "        \"asset_id\": null,\n" +
                        "        \"asset_quantity\": null\n" +
                        "      },\n" +
                        "      {\n" +
                        "        \"transaction_hash\": \"eba760a81b177051b0520418b4e10596955adb98196c15367a2467ab66a19b5c\",\n" +
                        "        \"index\": 1,\n" +
                        "        \"value\": 600,\n" +
                        "        \"addresses\": [\n" +
                        "          \"1BXVXP82f7x9YWdWuCaCYwad8ZoYayyRYt\"\n" +
                        "        ],\n" +
                        "        \"script\": \"76a91473758c13a91699376abb8fe76931bdd9bdc04ee388ac\",\n" +
                        "        \"asset_id\": \"AN51SPP6iZBHFJ3aux1jtn6MMMD13Gh3t7\",\n" +
                        "        \"asset_quantity\": \"500\"\n" +
                        "      },\n" +
                        "      {\n" +
                        "        \"transaction_hash\": \"eba760a81b177051b0520418b4e10596955adb98196c15367a2467ab66a19b5c\",\n" +
                        "        \"index\": 2,\n" +
                        "        \"value\": 600,\n" +
                        "        \"addresses\": [\n" +
                        "          \"1HHYPRCFijLkwQFsEPLv6iNEkYiwB4qsgs\"\n" +
                        "        ],\n" +
                        "        \"script\": \"76a914b2a2de394ce286509c8e4c30dcd157df001cdf7388ac\",\n" +
                        "        \"asset_id\": \"AN51SPP6iZBHFJ3aux1jtn6MMMD13Gh3t7\",\n" +
                        "        \"asset_quantity\": \"1500\"\n" +
                        "      },\n" +
                        "      {\n" +
                        "        \"transaction_hash\": \"eba760a81b177051b0520418b4e10596955adb98196c15367a2467ab66a19b5c\",\n" +
                        "        \"index\": 3,\n" +
                        "        \"value\": 1936400,\n" +
                        "        \"addresses\": [\n" +
                        "          \"1HHYPRCFijLkwQFsEPLv6iNEkYiwB4qsgs\"\n" +
                        "        ],\n" +
                        "        \"script\": \"76a914b2a2de394ce286509c8e4c30dcd157df001cdf7388ac\",\n" +
                        "        \"asset_id\": null,\n" +
                        "        \"asset_quantity\": null\n" +
                        "      }\n" +
                        "    ],\n" +
                        "    \"amount\": 1937600,\n" +
                        "    \"fees\": 10000,\n" +
                        "    \"confirmations\": 28136\n" +
                        "  },\n" +
                        "  {\n" +
                        "    \"hash\": \"1f9f6224bee8813135aba622693c78a33b3460e4efdb340174f87fdd8c9d4148\",\n" +
                        "    \"block_hash\": \"00000000000000002395175b35b7bd31eb0c9a80b36a147978a1e53729ab2e51\",\n" +
                        "    \"block_height\": 309456,\n" +
                        "    \"block_time\": \"2014-07-06T09:52:34.0000000Z\",\n" +
                        "    \"inputs\": [\n" +
                        "      {\n" +
                        "        \"transaction_hash\": \"1f9f6224bee8813135aba622693c78a33b3460e4efdb340174f87fdd8c9d4148\",\n" +
                        "        \"output_hash\": \"11e665fb01b52bfe6de9c3c037cac0ac0a8b400a9ef7bba3f3edf677b154412f\",\n" +
                        "        \"output_index\": 0,\n" +
                        "        \"value\": 600,\n" +
                        "        \"addresses\": [\n" +
                        "          \"1D24qr4gDZ1h5D5hLXF3AbddFWiA4Vnm3d\"\n" +
                        "        ],\n" +
                        "        \"script_signature\": \"48304502210081be87c9badffbed1f178137b0948c19545321f8d006668fbd5e6f9edbe780a202204933eb3c95e88dd2f9d8080f570072d33c51e64c2e1a0dc57e89a2c9f05667de01210355da14c52fc2e527a1b2dccbbc80d49007e2d1b24881e1b16237af3c0abb4852\",\n" +
                        "        \"asset_id\": \"AS6tDJJ3oWrcE1Kk3T14mD8q6ycHYVzyYQ\",\n" +
                        "        \"asset_quantity\": \"20\"\n" +
                        "      },\n" +
                        "      {\n" +
                        "        \"transaction_hash\": \"1f9f6224bee8813135aba622693c78a33b3460e4efdb340174f87fdd8c9d4148\",\n" +
                        "        \"output_hash\": \"e01c27bcd5ab181866b5ae486638e6c3b32d8e466ab611d93daf31bb536ea6f4\",\n" +
                        "        \"output_index\": 2,\n" +
                        "        \"value\": 600,\n" +
                        "        \"addresses\": [\n" +
                        "          \"1D24qr4gDZ1h5D5hLXF3AbddFWiA4Vnm3d\"\n" +
                        "        ],\n" +
                        "        \"script_signature\": \"493046022100c0f90f7fe17d62920b60b67ea14d5a50710ed934eca683fa22ac9037f7c26b3f022100adccb1e1d4476720b8f0bf52c55fc7cac2ffa05756871a21d24a8908db5ed23801210355da14c52fc2e527a1b2dccbbc80d49007e2d1b24881e1b16237af3c0abb4852\",\n" +
                        "        \"asset_id\": \"AS6tDJJ3oWrcE1Kk3T14mD8q6ycHYVzyYQ\",\n" +
                        "        \"asset_quantity\": \"999985\"\n" +
                        "      },\n" +
                        "      {\n" +
                        "        \"transaction_hash\": \"1f9f6224bee8813135aba622693c78a33b3460e4efdb340174f87fdd8c9d4148\",\n" +
                        "        \"output_hash\": \"e01c27bcd5ab181866b5ae486638e6c3b32d8e466ab611d93daf31bb536ea6f4\",\n" +
                        "        \"output_index\": 3,\n" +
                        "        \"value\": 473200,\n" +
                        "        \"addresses\": [\n" +
                        "          \"1D24qr4gDZ1h5D5hLXF3AbddFWiA4Vnm3d\"\n" +
                        "        ],\n" +
                        "        \"script_signature\": \"48304502201352a67320c05d43514fb050715c21abe63faae4a9f3a6f58c646ef65d4d982a02210082269d440eb38c2239cd7417ab4f28524a91e9bb98495da6698e8856f839593701210355da14c52fc2e527a1b2dccbbc80d49007e2d1b24881e1b16237af3c0abb4852\",\n" +
                        "        \"asset_id\": null,\n" +
                        "        \"asset_quantity\": null\n" +
                        "      }\n" +
                        "    ],\n" +
                        "    \"outputs\": [\n" +
                        "      {\n" +
                        "        \"transaction_hash\": \"1f9f6224bee8813135aba622693c78a33b3460e4efdb340174f87fdd8c9d4148\",\n" +
                        "        \"index\": 0,\n" +
                        "        \"value\": 0,\n" +
                        "        \"addresses\": [],\n" +
                        "        \"script\": \"6a0c4f41010002c09a0c85ea3000\",\n" +
                        "        \"asset_id\": null,\n" +
                        "        \"asset_quantity\": null\n" +
                        "      },\n" +
                        "      {\n" +
                        "        \"transaction_hash\": \"1f9f6224bee8813135aba622693c78a33b3460e4efdb340174f87fdd8c9d4148\",\n" +
                        "        \"index\": 1,\n" +
                        "        \"value\": 600,\n" +
                        "        \"addresses\": [\n" +
                        "          \"1BXVXP82f7x9YWdWuCaCYwad8ZoYayyRYt\"\n" +
                        "        ],\n" +
                        "        \"script\": \"76a91473758c13a91699376abb8fe76931bdd9bdc04ee388ac\",\n" +
                        "        \"asset_id\": \"AS6tDJJ3oWrcE1Kk3T14mD8q6ycHYVzyYQ\",\n" +
                        "        \"asset_quantity\": \"200000\"\n" +
                        "      },\n" +
                        "      {\n" +
                        "        \"transaction_hash\": \"1f9f6224bee8813135aba622693c78a33b3460e4efdb340174f87fdd8c9d4148\",\n" +
                        "        \"index\": 2,\n" +
                        "        \"value\": 600,\n" +
                        "        \"addresses\": [\n" +
                        "          \"1D24qr4gDZ1h5D5hLXF3AbddFWiA4Vnm3d\"\n" +
                        "        ],\n" +
                        "        \"script\": \"76a91483d521f559808be29bcc14dbb9b8763e8bd0230f88ac\",\n" +
                        "        \"asset_id\": \"AS6tDJJ3oWrcE1Kk3T14mD8q6ycHYVzyYQ\",\n" +
                        "        \"asset_quantity\": \"800005\"\n" +
                        "      },\n" +
                        "      {\n" +
                        "        \"transaction_hash\": \"1f9f6224bee8813135aba622693c78a33b3460e4efdb340174f87fdd8c9d4148\",\n" +
                        "        \"index\": 3,\n" +
                        "        \"value\": 463200,\n" +
                        "        \"addresses\": [\n" +
                        "          \"1D24qr4gDZ1h5D5hLXF3AbddFWiA4Vnm3d\"\n" +
                        "        ],\n" +
                        "        \"script\": \"76a91483d521f559808be29bcc14dbb9b8763e8bd0230f88ac\",\n" +
                        "        \"asset_id\": null,\n" +
                        "        \"asset_quantity\": null\n" +
                        "      }\n" +
                        "    ],\n" +
                        "    \"amount\": 464400,\n" +
                        "    \"fees\": 10000,\n" +
                        "    \"confirmations\": 32357\n" +
                        "  }\n" +
                        "]  ";
                }
                else
                {
                    return "Error";
                }
            }
        };

        APIClientMock client = new APIClientMock(get, null);

        List<SingleAssetTransaction> transactions = client.getTransactions("1BXVXP82f7x9YWdWuCaCYwad8ZoYayyRYt");

        Assert.assertEquals(4, transactions.size());
        Assert.assertEquals("eba760a81b177051b0520418b4e10596955adb98196c15367a2467ab66a19b5c", transactions.get(0).getTransactionId());
        Assert.assertEquals(null, transactions.get(0).getAsset());
        Assert.assertEquals(new BigInteger("600"), transactions.get(0).getQuantity());
        Assert.assertEquals("eba760a81b177051b0520418b4e10596955adb98196c15367a2467ab66a19b5c", transactions.get(1).getTransactionId());
        Assert.assertEquals(null, transactions.get(1).getAsset());
        Assert.assertEquals(new BigInteger("500"), transactions.get(1).getQuantity());
        Assert.assertEquals("1f9f6224bee8813135aba622693c78a33b3460e4efdb340174f87fdd8c9d4148", transactions.get(2).getTransactionId());
        Assert.assertEquals(null, transactions.get(2).getAsset());
        Assert.assertEquals(new BigInteger("600"), transactions.get(2).getQuantity());
        Assert.assertEquals("1f9f6224bee8813135aba622693c78a33b3460e4efdb340174f87fdd8c9d4148", transactions.get(3).getTransactionId());
        Assert.assertEquals(null, transactions.get(3).getAsset());
        Assert.assertEquals(new BigInteger("200000"), transactions.get(3).getQuantity());
    }

    private class APIClientMock extends com.coinprism.model.APIClient
    {
        private final IResponse get;
        private final IResponse post;

        public APIClientMock(IResponse get, IResponse post)
        {
            super("https://test/", NetworkParameters.fromID(NetworkParameters.ID_MAINNET));
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
