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
    // private StockDetailsReusableData stockDetailsReusableData;
    // private Double marketCap;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CompanyProfile {
        private String companyDescription;
        private String mgIndustry;
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
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CurrentPrice {
        private Double BSE;
        private Double NSE;
    }

    // @Data
    // @NoArgsConstructor
    // @AllArgsConstructor
    // @Builder
    // public static class StockDetailsReusableData {
    // private Double marketCap;
    // }

}
