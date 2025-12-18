package com.mabsplace.mabsplaceback.domain.dtos.digitalgoods;

import lombok.*;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomInputFieldDto implements Serializable {

    private String fieldName;        // Nom du champ (ex: "email", "phoneNumber")
    private String fieldLabel;       // Libellé affiché à l'utilisateur
    private String fieldType;        // Type: text, email, tel, number, select
    private Boolean isRequired;      // Champ obligatoire ou non
    private String placeholder;      // Texte d'aide
    private List<String> options;    // Options pour les champs de type select
    private ValidationRules validationRules; // Règles de validation

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ValidationRules implements Serializable {
        private String pattern;      // Regex de validation
        private String errorMessage; // Message d'erreur personnalisé
        private Integer minLength;   // Longueur minimale
        private Integer maxLength;   // Longueur maximale
    }
}
