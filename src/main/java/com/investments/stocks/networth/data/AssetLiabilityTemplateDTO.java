package com.investments.stocks.networth.data;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AssetLiabilityTemplateDTO {
    private List<EntityTemplateDTO> assets;
    private List<EntityTemplateDTO> liabilities;
}
