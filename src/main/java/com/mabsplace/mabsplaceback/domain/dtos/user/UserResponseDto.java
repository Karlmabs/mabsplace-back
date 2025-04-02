package com.mabsplace.mabsplaceback.domain.dtos.user;

import com.mabsplace.mabsplaceback.domain.dtos.discount.DiscountResponseDto;
import com.mabsplace.mabsplaceback.domain.dtos.payment.PaymentResponseDto;
import com.mabsplace.mabsplaceback.domain.dtos.promoCode.PromoCodeResponseDto;
import com.mabsplace.mabsplaceback.domain.dtos.role.RoleResponseDto;
import com.mabsplace.mabsplaceback.domain.dtos.subscription.SubscriptionResponseDto;
import com.mabsplace.mabsplaceback.domain.dtos.userProfile.UserProfileDTO;
import com.mabsplace.mabsplaceback.domain.dtos.wallet.WalletResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserResponseDto implements Serializable{

    private Long id;
    private String username;
    private String email;
    private String phonenumber;
    private String firstname;
    private String lastname;
    private String contact;
    private String image;
    private String pushToken;
    private UserProfileDTO userProfile;
    private Set<RoleResponseDto> roles = new HashSet<>();
    private WalletResponseDto wallet;
    private List<SubscriptionResponseDto> subscriptions;
    private List<PaymentResponseDto> payments;
    private PromoCodeResponseDto promoCode;
    private Set<DiscountResponseDto> discounts;
    private Long referrerId;
    private String referralCode;
    private List<UserResponseDto> referrals;
    private UserResponseDto referrer;

}
