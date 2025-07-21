package com.mabsplace.mabsplaceback.domain.dtos.userProfile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileSummaryDto implements Serializable {
    private Long id;
    private String name;
}
