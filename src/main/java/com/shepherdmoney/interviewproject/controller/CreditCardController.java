package com.shepherdmoney.interviewproject.controller;

import com.shepherdmoney.interviewproject.model.BalanceHistory;
import com.shepherdmoney.interviewproject.model.CreditCard;
import com.shepherdmoney.interviewproject.model.User;
import com.shepherdmoney.interviewproject.repository.CreditCardRepository;
import com.shepherdmoney.interviewproject.repository.UserRepository;
import com.shepherdmoney.interviewproject.vo.request.AddCreditCardToUserPayload;
import com.shepherdmoney.interviewproject.vo.request.UpdateBalancePayload;
import com.shepherdmoney.interviewproject.vo.response.CreditCardView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


@RestController
public class CreditCardController {
    private static final Logger LOGGER = Logger.getLogger(CreditCardController.class.getName());

    // TODO: wire in CreditCard repository here (~1 line)
    @Autowired
    private CreditCardRepository creditCardRepository;
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/credit-card")
    public ResponseEntity<Integer> addCreditCardToUser(@RequestBody AddCreditCardToUserPayload payload) {
        // TODO: Create a credit card entity, and then associate that credit card with user with given userId
        //       Return 200 OK with the credit card id if the user exists and credit card is successfully associated with the user
        //       Return other appropriate response code for other exception cases
        //       Do not worry about validating the card number, assume card number could be any arbitrary format and length
        LOGGER.info("Received request to add credit card with number [" + payload.getCardNumber() + "] to user with id [" + payload.getUserId() + "]");
        try {
            CreditCard creditCard = new CreditCard();
            creditCard.setIssuanceBank(payload.getCardIssuanceBank());
            creditCard.setNumber(payload.getCardNumber());

            User owner = userRepository.findById(payload.getUserId()).orElse(null);
            if (owner == null) {
                LOGGER.warning("User with id [" + payload.getUserId() + "] does not exist");
                return ResponseEntity.badRequest().build();
            }

            // Check if the credit card already exists for the user
            // Do not allow addition of this card even for a different bank since card numbers should, in practive, be unique
            if (owner.getCreditCards().stream().anyMatch(card -> card.getNumber().equals(payload.getCardNumber()))) {
                LOGGER.warning("Credit card with number [" + payload.getCardNumber() + "] already exists for user with id [" + payload.getUserId() + "]");
                return ResponseEntity.badRequest().build();
            }

            creditCard.setUser(owner);
            owner.addCreditCard(creditCard);

            userRepository.save(owner);
            CreditCard savedCreditCard = creditCardRepository.save(creditCard);

            LOGGER.info("Credit card with id [" + savedCreditCard.getId() + "] added successfully to user with id [" + payload.getUserId() + "]");
            return ResponseEntity.ok(savedCreditCard.getId());
        } catch (Exception e) {
            LOGGER.severe("Error adding credit card with number [" + payload.getCardNumber() + "] to user with id [" + payload.getUserId() + "]");
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/credit-card:all")
    public ResponseEntity<List<CreditCardView>> getAllCardOfUser(@RequestParam int userId) {
        // TODO: return a list of all credit card associated with the given userId, using CreditCardView class
        //       if the user has no credit card, return empty list, never return null
        LOGGER.info("Received request to retrieve all credit cards of user with id [" + userId + "]");
        try {
            // Retrieve all credit cards associated with the given userId
            User owner = userRepository.findById(userId).orElse(null);
            if (owner == null) {
                LOGGER.warning("User with id [" + userId + "] does not exist");
                return ResponseEntity.badRequest().build();
            }

            List<CreditCard> creditCards = owner.getCreditCards();
            if (creditCards == null || creditCards.isEmpty()) {
                LOGGER.info("User with id [" + userId + "] has no credit cards");
                return ResponseEntity.ok(List.of());
            }

            List<CreditCardView> creditCardViews = creditCards.stream()
                    .map(creditCard -> new CreditCardView(creditCard.getIssuanceBank(), creditCard.getNumber()))
                    .toList();
            LOGGER.info("Retrieved all credit cards of user with id [" + userId + "]");
            return ResponseEntity.ok(creditCardViews);
        } catch (Exception e) {
            LOGGER.severe("Error retrieving all credit cards of user with id [" + userId + "]");
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/credit-card:user-id")
    public ResponseEntity<Integer> getUserIdForCreditCard(@RequestParam String creditCardNumber) {
        // TODO: Given a credit card number, efficiently find whether there is a user associated with the credit card
        //       If so, return the user id in a 200 OK response. If no such user exists, return 400 Bad Request
        LOGGER.info("Received request to retrieve user id for credit card with number [" + creditCardNumber + "]");
        try {
            CreditCard creditCard = creditCardRepository.findAll().stream()
                    .filter(card -> card.getNumber().equals(creditCardNumber))
                    .findFirst().orElse(null);
            if (creditCard == null) {
                LOGGER.warning("Credit card with number [" + creditCardNumber + "] does not exist");
                return ResponseEntity.badRequest().build();
            }

            LOGGER.info("Retrieved user id " + creditCard.getUser().getId() + " for credit card with number [" + creditCardNumber + "]");
            return ResponseEntity.ok(creditCard.getUser().getId());
        } catch (Exception e) {
            LOGGER.severe("Error retrieving user id for credit card with number [" + creditCardNumber + "]");
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/credit-card:update-balance")
    public ResponseEntity<String> postMethodName(@RequestBody UpdateBalancePayload[] payload) {
        //TODO: Given a list of transactions, update credit cards' balance history.
        //      1. For the balance history in the credit card
        //      2. If there are gaps between two balance dates, fill the empty date with the balance of the previous date
        //      3. Given the payload `payload`, calculate the balance different between the payload and the actual balance stored in the database
        //      4. If the different is not 0, update all the following budget with the difference
        //      For example: if today is 4/12, a credit card's balanceHistory is [{date: 4/12, balance: 110}, {date: 4/10, balance: 100}],
        //      Given a balance amount of {date: 4/11, amount: 110}, the new balanceHistory is
        //      [{date: 4/12, balance: 120}, {date: 4/11, balance: 110}, {date: 4/10, balance: 100}]
        //      Return 200 OK if update is done and successful, 400 Bad Request if the given card number
        //        is not associated with a card.
        LOGGER.info("Received request to update balance for multiple credit cards");

        int totalRequests = payload.length;
        List<Integer> badRequestIds = new ArrayList<>();

        for (int i = 0; i < totalRequests; i++) {
            LOGGER.info("Received request to update balance for credit card with number [" + payload[i].getCreditCardNumber() + "]");
            UpdateBalancePayload pl = payload[i];

            try {
                CreditCard creditCard = creditCardRepository.findAll().stream()
                        .filter(card -> card.getNumber().equals(pl.getCreditCardNumber()))
                        .findFirst().orElse(null);
                if (creditCard == null) {
                    LOGGER.warning("Credit card with number [" + pl.getCreditCardNumber() + "] does not exist");
                    return ResponseEntity.badRequest().build();
                }

                BalanceHistory balanceHistory = new BalanceHistory();
                balanceHistory.setDate(pl.getBalanceDate());
                balanceHistory.setBalance(pl.getBalanceAmount());
                balanceHistory.setCard(creditCard);
                creditCard.addBalanceHistory(balanceHistory);

                creditCardRepository.save(creditCard);
                LOGGER.info("Updated balance for credit card with number [" + pl.getCreditCardNumber() + "]");
            } catch (Exception e) {
                LOGGER.severe(e.getMessage());
                LOGGER.severe("Error updating balance for credit card with number [" + pl.getCreditCardNumber() + "]");
                badRequestIds.add(i);
            }
        }

        if (badRequestIds.isEmpty()) {
            LOGGER.info("Update balance successful");
            return ResponseEntity.ok().body("Update balance successful");
        } else {
            LOGGER.warning("Bad Request for the following requests: " + badRequestIds);
            return ResponseEntity.badRequest().body("Bad Request for the following requests: " + badRequestIds);
        }
    }

}
