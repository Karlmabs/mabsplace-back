package com.mabsplace.mabsplaceback.domain.dtos.digitalgoods;

import lombok.*;

import java.io.Serializable;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class OrderRequestDto implements Serializable {
    private Long userId;
    private Long productId;
    private Map<String, String> customerInputData; // Données saisies par le client (email, téléphone, etc.)
}
