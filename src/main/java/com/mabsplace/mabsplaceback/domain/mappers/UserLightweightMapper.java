package com.mabsplace.mabsplaceback.domain.mappers;

import com.mabsplace.mabsplaceback.domain.dtos.user.UserLightweightResponseDto;
import com.mabsplace.mabsplaceback.domain.dtos.userProfile.UserProfileSummaryDto;
import com.mabsplace.mabsplaceback.domain.dtos.wallet.WalletSummaryDto;
import com.mabsplace.mabsplaceback.domain.entities.Role;
import com.mabsplace.mabsplaceback.domain.entities.User;
import org.mapstruct.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserLightweightMapper {

    @Mapping(target = "userProfile", expression = "java(mapUserProfileSummary(user))")
    @Mapping(target = "wallet", expression = "java(mapWalletSummary(user))")
    @Mapping(target = "referrerId", expression = "java(mapReferrer(user))")
    @Mapping(target = "referrerName", expression = "java(mapReferrerName(user))")
    @Mapping(target = "subscriptionCount", expression = "java(getSubscriptionCount(user))")
    @Mapping(target = "paymentCount", expression = "java(getPaymentCount(user))")
    @Mapping(target = "roleNames", expression = "java(mapRoleNames(user))")
    UserLightweightResponseDto toDto(User user);

    default UserProfileSummaryDto mapUserProfileSummary(User user) {
        if (user == null || user.getUserProfile() == null) {
            return null;
        }
        return new UserProfileSummaryDto(
                user.getUserProfile().getId(),
                user.getUserProfile().getName()
        );
    }

    default WalletSummaryDto mapWalletSummary(User user) {
        if (user == null || user.getWallet() == null) {
            return null;
        }
        String currencySymbol = user.getWallet().getCurrency() != null ? 
                user.getWallet().getCurrency().getSymbol() : null;
        return new WalletSummaryDto(
                user.getWallet().getId(),
                user.getWallet().getBalance(),
                currencySymbol
        );
    }

    default Long mapReferrer(User user) {
        if (user == null || user.getReferrer() == null) {
            return null;
        }
        return user.getReferrer().getId();
    }

    default String mapReferrerName(User user) {
        if (user == null || user.getReferrer() == null) {
            return "No referrer";
        }
        return user.getReferrer().getUsername();
    }

    default Integer getSubscriptionCount(User user) {
        if (user == null || user.getSubscriptions() == null) {
            return 0;
        }
        return user.getSubscriptions().size();
    }

    default Integer getPaymentCount(User user) {
        if (user == null || user.getPayments() == null) {
            return 0;
        }
        return user.getPayments().size();
    }

    default Set<String> mapRoleNames(User user) {
        if (user == null || user.getRoles() == null) {
            return Set.of();
        }
        return user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }

    List<UserLightweightResponseDto> toDtoList(List<User> users);
}
