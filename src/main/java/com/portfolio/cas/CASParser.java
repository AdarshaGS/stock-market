package com.portfolio.cas;

import com.aa.data.FIPayload;
import com.aa.data.FIType;
import com.aa.mock.MockFIDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CASParser {

    private final MockFIDataService dataService;

    public FIPayload parseCAS(String fileName) {
        // Simulate CAS Parsing from a file
        // In reality, this would use a PDF parsing library like Apache PDFBox or Tabula

        // For the mock, we just generate Mutual Fund data since CAS is primarily for
        // MFs
        Map<FIType, List<Object>> data = dataService.generateMockData(List.of(FIType.MUTUAL_FUNDS));

        return FIPayload.builder()
                .consentId("CAS-UPLOAD-" + UUID.randomUUID().toString())
                .financialData(data)
                .build();
    }
}
