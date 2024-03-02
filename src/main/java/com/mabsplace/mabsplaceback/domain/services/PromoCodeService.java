package com.mabsplace.mabsplaceback.domain.services;

import com.mabsplace.mabsplaceback.domain.entities.Discount;
import com.mabsplace.mabsplaceback.domain.entities.PromoCode;
import com.mabsplace.mabsplaceback.domain.entities.User;
import com.mabsplace.mabsplaceback.domain.repositories.DiscountRepository;
import com.mabsplace.mabsplaceback.domain.repositories.PromoCodeRepository;
import com.mabsplace.mabsplaceback.domain.repositories.UserRepository;
import com.mabsplace.mabsplaceback.exceptions.ResourceNotFoundException;
import com.mabsplace.mabsplaceback.utils.Utils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PromoCodeService {

    private final PromoCodeRepository promoCodeRepository;
    private final DiscountRepository discountRepository;

    private final UserRepository userRepository; // Assume this repository is defined

    public PromoCodeService(PromoCodeRepository promoCodeRepository, DiscountRepository discountRepository, UserRepository userRepository) {
        this.promoCodeRepository = promoCodeRepository;
        this.discountRepository = discountRepository;
        this.userRepository = userRepository;
    }

    public void generatePromoCode(User owner) {
        // Generate a random code
        String code = Utils.generateUniquePromoCode();

        //check if the code already exists
        if(promoCodeRepository.findByCode(code).isPresent()){
            generatePromoCode(owner);
        }

        // Create a new promo code
        PromoCode promoCode = new PromoCode();
        promoCode.setCode(code);
        promoCode.setOwner(owner);
        promoCode.setUsedCount(0);

        // Save the promo code
        promoCodeRepository.save(promoCode);

    }

    public void registerUserWithPromoCode(String code, User newUser) {
        promoCodeRepository.findByCode(code).ifPresent(promoCode -> {
            // Increment used count
            promoCode.setUsedCount(promoCode.getUsedCount() + 1);
            promoCodeRepository.save(promoCode);

            // Apply discount logic for the owner based on usedCount
            applyDiscountToOwner(promoCode);

            // Link the new user to other relevant data as needed
            // For example, setting the newUser's referring promo code
            // Not shown here for brevity

        });
    }

    private void applyDiscountToOwner(PromoCode promoCode) {
        User owner = promoCode.getOwner();
        int referralCount = promoCode.getUsedCount();

        // Determine the discount amount based on referralCount
        // For simplicity, let's say 5% discount per referral, max 20%
        double discountPercentage = Math.min(5.0 * referralCount, 20.0);

        // Check if the user already has a discount, update it or create a new one
        Optional<Discount> existingDiscount = discountRepository.findByUser(owner);
        Discount discount;
        if (existingDiscount.isPresent()) {
            discount = existingDiscount.get();
            discount.setAmount(discountPercentage);
        } else {
            discount = new Discount();
            discount.setUser(owner);
            discount.setAmount(discountPercentage);
        }

        // Set the expiration date for the discount
        discount.setExpirationDate(LocalDateTime.now().plusDays(30)); // 30 days from now

        discountRepository.save(discount);
    }

    public List<PromoCode> getAllPromoCodes() {
        return promoCodeRepository.findAll();
    }

    public PromoCode getPromoCode(Long id) {
        return promoCodeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("PromoCode", "id", id));
    }

    // Other methods
}
