package com.clouddeadline.BlockChain;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class BlockChainTest {

    @Test
    public void testBlockValidationHappy(){
        BlockChain blockChainHelper = new BlockChain();
        JSONObject blockchain = blockChainHelper.decode("eJyFk9tum0AQht9lr7mYw85heZWqigBDbClxqxg1lSK_ewcCBddJuldoF3a--f7hLXXH5nRO9be31Dw9PYy_58eXvvuVai-SRd3Ji1WpeR5TLbCsKo2n5z7VCcVJCDJsK1Xp2FyOcZhbMnOFdP1epZ8_XmNrOj0dUg1_XwLrilu2OBibl8d-nG5N1-qG6HJ6TDUKcRCwBVepVkpGViRAp9gb-oCiDL4AI6lnL1nvgKc7TAiLaVS-9OfDvx0vfB1k46Y9zEjvNZUwxx1MbOU_ZhCA0OMGl4KbmaYVHfDgOzMEtLjBzQ2IDAeUvRv4WE60D0ScobCtbgqIFQgHBVc35kiyMGc3LYT3xIWyUg4g2dTcaF7z1VZx8H4GmimoCDmia5kieKdAKOY5JiSMrRiwIDgbSfF7BlSPtDFagq_jabXDFmHYxXPb9lyH5JN4FBhIVQ2Ut3haMAn1sosH13Rolw65N-w36TTzN-f-9WHbg2m45639NEfpQgaCRKsqFVcjN2bgxRQL69IEZ1K2fCdLWZkMgxf168BwaLN3hjtZH_zln9bJYjm7ksI0uNc_g4cQeA==");
        assertTrue(blockChainHelper.isChainValid(blockchain));
    }

    @Test
    public void testBlock1IncorrectBlockHash(){
        BlockChain blockChainHelper = new BlockChain();
        JSONObject blockchain = blockChainHelper.decode("eJyFk9tum0AQht9lr7mYw85heZWqigBDbClxqxg1lSK_ewcCBddJuldoF3a--f7hLXXH5nRO9be31Dw9PYy_58eXvvuVai-SRd3Ji1WpeR5TLbCsKo2n5z7VCcVJCDJsK1Xp2FyOcZhbMnOFdP1epZ8_XmNrOj0dUg1_XwLrilu2OBibl8d-nG5N1-qG6HJ6TDUKcRCwBVepVkpGViRAp9gb-oCiDL4AI6lnL1nvgKc7TAiLaVS-9OfDvx0vfB1k46Y9zEjvNZUwxx1MbOU_ZhCA0OMGl4KbmaYVHfDgOzMEtLjBzQ2IDAeUvRv4WE60D0ScobCtbgqIFQgHBVc35kiyMGc3LYT3xIWyUg4g2dTcaF7z1VZx8H4GmimoCDmia5kieKdAKOY5JiSMrRiwIDgbSfF7BlSPtDFagq_jabXDFmHYxXPb9lyH5JN4FBhIVQ2Ut3haMAn1sosH13Rolw65N-w36TTzN-f-9WHbg2m45639NEfpQgaCRKsqFVcjN2bgxRQL69IEZ1K2fCdLWZkMgxf168BwaLN3hjtZH_zln9bJYjm7ksI0uNc_g4cQeA==");
        // modify block hash to an incorrect one
        ((JSONObject)((JSONArray) blockchain.get("chain")).get(0)).put("hash","07c98742");
        assertFalse(blockChainHelper.isChainValid(blockchain));
    }

    @Test
    public void testBlock2Transaction1IncorrectSignature(){
        BlockChain blockChainHelper = new BlockChain();
        JSONObject blockchain = blockChainHelper.decode("eJyFk9tum0AQht9lr7mYw85heZWqigBDbClxqxg1lSK_ewcCBddJuldoF3a--f7hLXXH5nRO9be31Dw9PYy_58eXvvuVai-SRd3Ji1WpeR5TLbCsKo2n5z7VCcVJCDJsK1Xp2FyOcZhbMnOFdP1epZ8_XmNrOj0dUg1_XwLrilu2OBibl8d-nG5N1-qG6HJ6TDUKcRCwBVepVkpGViRAp9gb-oCiDL4AI6lnL1nvgKc7TAiLaVS-9OfDvx0vfB1k46Y9zEjvNZUwxx1MbOU_ZhCA0OMGl4KbmaYVHfDgOzMEtLjBzQ2IDAeUvRv4WE60D0ScobCtbgqIFQgHBVc35kiyMGc3LYT3xIWyUg4g2dTcaF7z1VZx8H4GmimoCDmia5kieKdAKOY5JiSMrRiwIDgbSfF7BlSPtDFagq_jabXDFmHYxXPb9lyH5JN4FBhIVQ2Ut3haMAn1sosH13Rolw65N-w36TTzN-f-9WHbg2m45639NEfpQgaCRKsqFVcjN2bgxRQL69IEZ1K2fCdLWZkMgxf168BwaLN3hjtZH_zln9bJYjm7ksI0uNc_g4cQeA==");
        // modify block hash to an incorrect one
        JSONObject block2 = ((JSONObject)((JSONArray) blockchain.get("chain")).get(1));
        ((JSONObject)((JSONArray) block2.get("all_tx")).get(0)).put("sig",1523500375458L);
        assertFalse(blockChainHelper.isChainValid(blockchain));
    }

    @Test
    public void testBlock2Transaction1IncorrectTimeStamp(){
        BlockChain blockChainHelper = new BlockChain();
        JSONObject blockchain = blockChainHelper.decode("eJyFk9tum0AQht9lr7mYw85heZWqigBDbClxqxg1lSK_ewcCBddJuldoF3a--f7hLXXH5nRO9be31Dw9PYy_58eXvvuVai-SRd3Ji1WpeR5TLbCsKo2n5z7VCcVJCDJsK1Xp2FyOcZhbMnOFdP1epZ8_XmNrOj0dUg1_XwLrilu2OBibl8d-nG5N1-qG6HJ6TDUKcRCwBVepVkpGViRAp9gb-oCiDL4AI6lnL1nvgKc7TAiLaVS-9OfDvx0vfB1k46Y9zEjvNZUwxx1MbOU_ZhCA0OMGl4KbmaYVHfDgOzMEtLjBzQ2IDAeUvRv4WE60D0ScobCtbgqIFQgHBVc35kiyMGc3LYT3xIWyUg4g2dTcaF7z1VZx8H4GmimoCDmia5kieKdAKOY5JiSMrRiwIDgbSfF7BlSPtDFagq_jabXDFmHYxXPb9lyH5JN4FBhIVQ2Ut3haMAn1sosH13Rolw65N-w36TTzN-f-9WHbg2m45639NEfpQgaCRKsqFVcjN2bgxRQL69IEZ1K2fCdLWZkMgxf168BwaLN3hjtZH_zln9bJYjm7ksI0uNc_g4cQeA==");
        // modify block hash to an incorrect one
        JSONObject block2 = ((JSONObject)((JSONArray) blockchain.get("chain")).get(1));
        ((JSONObject)((JSONArray) block2.get("all_tx")).get(0)).put("time","1582520300000000000");
        assertFalse(blockChainHelper.isChainValid(blockchain));
    }

    @Test
    public void testBlockIDWrongOrder(){
        BlockChain blockChainHelper = new BlockChain();
        JSONObject blockchain = blockChainHelper.decode("eJydlNtuGkEMht9lrvfCx7HNq1RVtCcSpCStAkoqRXn3ehfCQkCh6nIDM8z48_db-176h3bzXFY_3kv7-Hi3-zN_fRn717JCCHMRAAKOprRPu7JSODxN2W2exrIqqE5KILA8pSkP7fYhNynGrjeO8vGzKb9_veWScG5vhrKC478Aw9dqY27s2pf7cTeteflozqC2m_tkQrbqGhOaNkdQBkwEsnBuynpMLkGkmDBnbOHAipUvqBXEaw2MrNeU7fg8XPR9gGTvvY9hPWPt6zq6ZUkLQr7lJ8IDWTkL6eJnqNImfHfiB2d_kyBcBOU5GQL-RRDXbEqFQCeoa6B7P8xS9dMPUXUgli_UmFpVMT8-U-_9nN92QOyRhrC8-Vs9pNf1YAUIBsBKzIsebWubWcuJHjrIWSqDcB4LP5PTXZVTrWY7RhWPs-PZnNrc6qcaNBDDI7GGaL3graBmbPlTTgbnbA4PfK32HVIfM9GMEU6VI2ow83czjJGzPqHuSdCDCO0SRQVSgBrjjRmusO7EpPu_kIgyJK-SY1xlCSmCZW3cn4bklylBOw7m5ymN85nn8e3uuDa1MC-cvonCUo9CZOnPXFhUJeEvGCWyFwU288U4QhUhFxR3-y56NzmWyEtC-es7g0gdU1miAN2Ivlo7rFvQbPPjL8p2W4Q=");
        assertFalse(blockChainHelper.isChainValid(blockchain));
    }

    // New Transaction can be empty
//    @Test
//    public void testNewTransactionsMissingTransaction(){
//        BlockChain blockChainHelper = new BlockChain();
//        JSONObject blockchain = blockChainHelper.decode("eJyFkktu4zAMQO-itRf8iKToqxSDwlHkJkCbDhpjOkCRuw9jxGP3A0QrgaSkxyd-pHoYjqfUP3yk4fn5cfo7b99a_ZN6BLeSMwABe5eGlyn1ArfVpen40lKfUAoJQYZ1pS4dhvMhkuRtV409XX516ffre4QyR_q4Tz38rwL0Moq1SEzD21ObrrF9unSfoM7Hp9STihh5UUbubpximItxJCwwxxZULO7quEKbKDF_gyZhR2REpHj83E77b23fGFV3mHmUmWrxwyULshHqXT_uHkykGkdWP5lrk1J3Gz-0-MHVT65qNaq2fvRHP8icS2AVC0uLIDNmVADPajdBGExaFkFZwBWsfIGOBiVnMS7o9_yMowGI0B0_JD_7weuHqRM7ycYP5cpNKm38zD919UMbP9GgEnzy0-Yzp_b-uMauBufIdsw1AGOCQGKoFh9QGJjsK6QCSRYyEsxx_eUfClDS-A==");
//        assertFalse(blockChainHelper.isChainValid(blockchain));
//    }

    @Test
    public void testEmptyNewTransactions(){
        BlockChain blockChainHelper = new BlockChain();
        JSONObject blockchain = blockChainHelper.decode("eJyVkNtqwkAQQP9lnvMw173kV4qUJG5UqLbU0BbEf-9Eo1FsKR32YZkLc-YcoFs3mx3UTwdoXl6eh6_T9710H1AT5phUERklV9BsB6gNp6hg2GwL1ECW2BgV54AK1s1-7UXOpe2iZDguKnh7_fSUipc3S6jx2oWUU2-xeGFo3ldlGHMBjtUd1H6zGpkwcULSpMGZzqDRzEElqmqsoC-OxdEiZ56oRZCMWB-gOWfNGCSy-fJ92S0fzr5ewkWt2Inq_36cm4JGf4xp9lOarpXM_Y0fm_TQrCeKs_f9nZ70sx5TZnYqDOliJ_mVFHNADhc7QTgnpYnZJ_w6TA_IisZsJob0h53IkrUnOkGdSdTFkkgKKHZFERpHA2mgCQUvEPaLuOD9ajGPS_-gkLaNfV6mG5vCk06edWLbN1jync7mNLMrn89zroMpMwpeHL8BwfDObg==");
        assertFalse(blockChainHelper.isChainValid(blockchain));
    }

    // new transaction can have multiple rewards
//    @Test
//    public void testNewTransactionsMultipleRewards(){
//        BlockChain blockChainHelper = new BlockChain();
//        JSONObject blockchain = blockChainHelper.decode("eJyFlEtu4zAMQO-itRf8iqKvMhgUSiw3AdrMoAmmAxS9e2XXiW04bbUyRIl6fBL9FvaHfDyF9tdbyE9PD5f_4-dL2f8LLYJbEgEgYG9Cfr6EVmEaTbgcn0toA2oiJRCYR2jCIZ8PNUhedntjD--_m_D3z2udEq7hYxdauK0C9NSrlRq45JfHchnm6p5mBXU-PoZWTKI7SDKv-z85E6iBuxITN6EvlSopR6eZGWMUtw2zEDNhZIsD87mcuk3VE2JUihGkG6E-j40s4IiGNGT-1g5WAnM2T7XU2U4hLZ0nWdghmOzgbAf6ApBwZae7aydJFK2FqbI2929x1KMkQFc9VQ1T2hJr9SPOFfnmZl3yxIed7PrcL9UYgCg7oSBOp5B-oSYCs7BpiiKzmqypYy55oQZ1UkOzGtLIaZdXauJdNchiGh0IE1Nzl3NUgxiNkl6pXayK2DBrzSOKSCY3Oetk13cT1XLJaSHnXlt9ZYfqXTK5IuvYN1NWTaIxd0s713fDsxzZK-2YlnJw3HEqrw-zLwvTzI-9HzEOjbRhFE7D03bgn-qsreb1qnWTwgXYDFVo-Fe8fwAsqShx");
//        assertFalse(blockChainHelper.isChainValid(blockchain));
//    }

    @Test
    public void testNewTransactionIncorrectTimeStamp(){
        BlockChain blockChainHelper = new BlockChain();
        JSONObject blockchain = blockChainHelper.decode("eJyFk9tum0AQht9lr7mYw85heZWqigBDbClxqxg1lSK_ewcCBddJuldoF3a--f7hLXXH5nRO9be31Dw9PYy_58eXvvuVai-SRd3Ji1WpeR5TLbCsKo2n5z7VCcVJCDJsK1Xp2FyOcZhbMnOFdP1epZ8_XmNrOj0dUg1_XwLrilu2OBibl8d-nG5N1-qG6HJ6TDUKcRCwBVepVkpGViRAp9gb-oCiDL4AI6lnL1nvgKc7TAiLaVS-9OfDvx0vfB1k46Y9zEjvNZUwxx1MbOU_ZhCA0OMGl4KbmaYVHfDgOzMEtLjBzQ2IDAeUvRv4WE60D0ScobCtbgqIFQgHBVc35kiyMGc3LYT3xIWyUg4g2dTcaF7z1VZx8H4GmimoCDmia5kieKdAKOY5JiSMrRiwIDgbSfF7BlSPtDFagq_jabXDFmHYxXPb9lyH5JN4FBhIVQ2Ut3haMAn1sosH13Rolw65N-w36TTzN-f-9WHbg2m45639NEfpQgaCRKsqFVcjN2bgxRQL69IEZ1K2fCdLWZkMgxf168BwaLN3hjtZH_zln9bJYjm7ksI0uNc_g4cQeA==");
        // modify new transaction timestamp to an incorrect one
        JSONObject newTransaction1 = ((JSONObject)((JSONArray) blockchain.get("new_tx")).get(0));
        newTransaction1.put("time","1582521603026667063");
        assertFalse(blockChainHelper.isChainValid(blockchain));
    }

    @Test
    public void testNewTransactionsRewardIncorrectTimeStamp(){
        BlockChain blockChainHelper = new BlockChain();
        JSONObject blockchain = blockChainHelper.decode("eJyFk9tum0AQht9lr7mYw85heZWqigBDbClxqxg1lSK_ewcCBddJuldoF3a--f7hLXXH5nRO9be31Dw9PYy_58eXvvuVai-SRd3Ji1WpeR5TLbCsKo2n5z7VCcVJCDJsK1Xp2FyOcZhbMnOFdP1epZ8_XmNrOj0dUg1_XwLrilu2OBibl8d-nG5N1-qG6HJ6TDUKcRCwBVepVkpGViRAp9gb-oCiDL4AI6lnL1nvgKc7TAiLaVS-9OfDvx0vfB1k46Y9zEjvNZUwxx1MbOU_ZhCA0OMGl4KbmaYVHfDgOzMEtLjBzQ2IDAeUvRv4WE60D0ScobCtbgqIFQgHBVc35kiyMGc3LYT3xIWyUg4g2dTcaF7z1VZx8H4GmimoCDmia5kieKdAKOY5JiSMrRiwIDgbSfF7BlSPtDFagq_jabXDFmHYxXPb9lyH5JN4FBhIVQ2Ut3haMAn1sosH13Rolw65N-w36TTzN-f-9WHbg2m45639NEfpQgaCRKsqFVcjN2bgxRQL69IEZ1K2fCdLWZkMgxf168BwaLN3hjtZH_zln9bJYjm7ksI0uNc_g4cQeA==");
        // modify new transaction timestamp to an incorrect one
        JSONObject rewardTransaction = ((JSONObject)((JSONArray) blockchain.get("new_tx")).get(1));
        rewardTransaction.put("time","1582521636327155516");
        assertFalse(blockChainHelper.isChainValid(blockchain));
    }

    @Test
    public void testBlockValidationHappy2(){
        // with empty new transactions
        BlockChain blockChainHelper = new BlockChain();
        JSONObject blockchain = blockChainHelper.decode("eJyF00tu2zAQBuC7cK3FPDkcXaUIApqmYgOJW8RCUiDw3UsLqiXHbs2VMENBPz-OvkLZ5f0h9D--Qn59fR5_T4_vtXyEHsEtiQAQsHchv42hV5hXF8b9Ww19QE2kBALLCl3Y5eOuNcnrphh7OD114dfPz1YSbu39NvRw2QXoaVCrrTHm95c6nms5nLqrUMf9S-hFHcATmop2c85oURlbmQS7MNSWStyNIM6hxYiV1G8yk6eEgKDk7dvHetjenHqOeA64LVqnUBcelLZZXAEf8bg7IXJKrHHhYReXwrbioVkHVzpSSh3KlU69q8Pi0YmSgEl3_xYnnnZmNJ4zxyRo8XviM0uMIpEMHuHQwLlEgxXO9Z1M3yG9b4PqiZGYIkhabIYND22qNiubONvQYgND3vB0e48mh43NHSOA2f9sqN2Jk_0dHWLDZPF75iYDjJiiYXw0OgrZsur16Nz8Wf_iIfSosXUTiC88MW9V7IoHZfbhxYe9DHVqXHxweuVQP59Xpblw9no6_QGcEAoy");
        assertTrue(blockChainHelper.isChainValid(blockchain));
    }

    @Test
    public void testBlockValidationHappy3(){
        // with 1 rewards at new transaction
        BlockChain blockChainHelper = new BlockChain();
        JSONObject blockchain = blockChainHelper.decode("eJyFktFuwjAMRf_Fz32wYyd2-ivThNI2QCVgE1RjEuLfF0oZMNCw8hBdx_aRbw7QLlO_gfrtAGm1mg3f43Wb2y-oCaOaCKJDjhWk9QC1xykqGPp1hhrIm_MOBa8BFSzTblmSLuamVY5wfK_g82NfJOGS7juo8fcVUrS511wSQ9ou8nDSFI7VHdSuX5yYnIkGr17NVc9B57lwiXj24UKtQQ3DIzORxsCscmLe5U330GxC7EwwWOdGqDMJu0AuOGNG_x-JZwp22Z_jGBA9PaIE01jKicMLlOAaIhYZUc5jS1W0shUhx6-MijFaOSpO7WpUapomW25vjCKSySm6OtUkwdTYnVN5LNrk_exWm5TbD_UEk4h8NPkDSYhlGcGxKlPpfvwBcHSswQ==");
        assertTrue(blockChainHelper.isChainValid(blockchain));
    }

    @Test
    public void testBlockValidationHappy4(){
        // with 2 new rewards and 1 new transaction in new transactions
        BlockChain blockChainHelper = new BlockChain();
        JSONObject blockchain = blockChainHelper.decode("eJyVktFOwzAMRf8lz32wYzuO-ysITVmXskkwEKsACe3fcauODopA5Kmy29vjHL-Hbl8Ox9DevIdyf78Z3qbH59q9hBbBNDMDRCBrQnkYQiswnyYMh4ca2oCSo0RgWE5owr6c9t6MVredkoXzbROeHl-9xOTtwy608PkWoOVetHpjKM93dRhrJZybL1Cnw11okzAg5xi5-Zmyrw6FFDPMxIwKiiRrYGE1ISAZgU_1uFuFzXxiKNilfiKaMNBIUMRM5QokAedE7PPiDEJoBkwzioOretQKhTiSGSdF_AOlVCw1-R2OKP_XhAAoSWMcYRdN27LrsIvlStNEMmrCRVMHtu17udaE0yfH-rq5Ks2Fb7tE2a9GmPMFEgGJc9YVo2iyzJjAzSxzGmJC1YScLgks5KlxNSViREZTAMqLNDWP8FtOOctv22NRnPMiLWfjqInW_6BxD7Mn_rU_pSTZdtGl3Z4_AF_t3WM=");
        assertTrue(blockChainHelper.isChainValid(blockchain));
    }

    @Test
    public void testBlockValidationHappy5(){
        // random correct case
        BlockChain blockChainHelper = new BlockChain();
        JSONObject blockchain = blockChainHelper.decode("eJyFkt1O6zAMgN8l173wb2z3VRBCPV3LJsFArAIkxLsf09OdbgyxXEVOYn_-nI_Sb7vdvrQ3H6V7eLib3ufty9C_lhYhzEUACDia0j1OpVVYVlOm3eNQ2oLqpAQC6ypN2XaHbR5SDH964yift015fnrLkHAe7zalhf-3AMNHtSEPpu7lfpi-YvmmOYM67O6TSUUNKmEVtGYB9YoUKqJVsSnjkFiMRChHasnLCJUvoDmIGIDFahY_DPvNRdsLowfaKFxnqqMfySTJkzn4mp8Ii9xaZNrVz8hdr9XGEz9HPbjqMe7Jqpzp6eYn--Htbo31ZYmcCFNyc4fgtNP8PNhZGKYh9Lq0QYHpFOq3LhCYUIM9zoWdeViokZW4Y52F_ZsdciataGy_zo6SjcEWFHSzLGsXKBVdsX6ZhSuz65W0Gzd-MrvIoZtZVsZjISJwF9DvhRJb86ew1PzZazMuIWLOofCr18ST0KWG10xT46KEQA3HGspwReuI3tsIqfX28y_5be44");
        assertTrue(blockChainHelper.isChainValid(blockchain));
    }
}
