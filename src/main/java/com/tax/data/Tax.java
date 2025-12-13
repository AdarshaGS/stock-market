package com.tax.data;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "user_tax_details")
@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Tax {
    
    @Id
    private Long id;

    
}
