package com.mabsplace.mabsplaceback.domain.mappers;

import com.mabsplace.mabsplaceback.domain.dtos.wallet.WalletRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.wallet.WalletResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.User;
import com.mabsplace.mabsplaceback.domain.entities.Wallet;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface WalletMapper {

  Wallet toEntity(WalletRequestDto walletRequestDto);

  @Mapping(target = "userId", expression = "java(mapUser(wallet.getUser()))")
  WalletResponseDto toDto(Wallet wallet);

  default Long mapUser(User user) {
    if (user == null) {
      return null;
    }
    return user.getId();
  }

  List<WalletResponseDto> toDtoList(List<Wallet> wallets);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  Wallet partialUpdate(WalletRequestDto walletRequestDto, @MappingTarget Wallet wallet);

}
