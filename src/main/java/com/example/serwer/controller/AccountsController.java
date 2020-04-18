package com.example.serwer.controller;


import com.example.serwer.exception.ResourceNotFoundException;
import com.example.serwer.model.Accounts;
import com.example.serwer.repository.AccountsRepository;
import com.example.serwer.repository.UploadsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import javax.validation.Valid;
import java.util.List;
import java.util.logging.Logger;

@RestController
//@RequestMapping("/accounts")
public class AccountsController {

    @Autowired
    private AccountsRepository accountsRepository;

    @GetMapping("/accounts")
    public List<Accounts> getAllAccounts() {
        return accountsRepository.findAll();
    }

    @PostMapping("/accounts")
    public Accounts createAccount(@Valid @RequestBody Accounts account) {
        return accountsRepository.save(account);
    }

    @GetMapping("/accounts/{id}")
    public Accounts getAccountById(@PathVariable(value = "id") Long accountId) {
        return accountsRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Note", "id", accountId));
    }

    @PutMapping("/accounts/{id}")
    public Accounts updateAccount(@PathVariable(value = "id") Long accountId,
                                  @Valid @RequestBody Accounts accountDetails) {

        Accounts account = accountsRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));

        //
        account.setRank(accountDetails.getRank());
        int curelements = account.getElements();
        account.setElements(curelements + accountDetails.getElements());


        Accounts updatedAccounts = accountsRepository.save(account);
        return updatedAccounts;
    }

    @DeleteMapping("/accounts/{id}")
    public ResponseEntity<?> deleteAccount(@PathVariable(value = "id") Long accountId) {
        Accounts account = accountsRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));

        accountsRepository.delete(account);

        return ResponseEntity.ok().build();
    }


    /*public List<Accounts> findByName(Accounts account) {
        return accountsRepository.findAll(Example.of(account));
    }*/
}