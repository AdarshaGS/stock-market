package com.stocks.thirdParty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ThirdPartyResponse {
    private String companyName;
    private CompanyProfile companyProfile;
    private CurrentPrice currentPrice;


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CompanyProfile {
        private String companyDescription;
        private String mgIndustry;
        // private PeerCompanyList peerCompanyList;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PeerCompanyList {
        public String tickerId;
        public String companyName;
        public String marketCap;
        public String priceEarningRatio;
        // public String price;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CurrentPrice {
        private Double BSE;
        private Double NSE;
    }

}


