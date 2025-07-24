package com.mabsplace.mabsplaceback.domain.dtos.user;

import com.mabsplace.mabsplaceback.domain.dtos.userProfile.UserProfileSummaryDto;
import com.mabsplace.mabsplaceback.domain.dtos.wallet.WalletSummaryDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserLightweightResponseDto implements Serializable {

    private Long id;
    private String username;
    private String email;
    private String firstname;
    private String lastname;
    private String phonenumber;
    private String image;
    private String referralCode;
    private Long referrerId;
    private String referrerName;
    private UserProfileSummaryDto userProfile;
    private WalletSummaryDto wallet;
    private Integer subscriptionCount;
    private Integer paymentCount;
    private Set<String> roleNames;

}
