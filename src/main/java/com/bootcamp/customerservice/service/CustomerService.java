package com.bootcamp.customerservice.service;

import com.bootcamp.customerservice.entity.Customer;
import com.bootcamp.customerservice.entity.VIPCustomer;
import com.bootcamp.customerservice.api.CustomersApiDelegate;
import com.bootcamp.customerservice.model.CustomerDTO;
import com.bootcamp.customerservice.model.CustomerType;
import com.bootcamp.customerservice.repository.CustomerRepository;
import com.bootcamp.customerservice.service.exceptions.*;
import com.bootcamp.customerservice.webclient.BankAccountWebClient;
import com.bootcamp.customerservice.webclient.CreditServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;
import java.util.function.Predicate;


/**
 * Service class for managing customers.
 * This service handles CRUD operations for customer data,
 * interacts with the customer repository, and integrates with
 * other services for validation and data enrichment.
 */


@Slf4j
@Service
public class CustomerService implements CustomersApiDelegate {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private BankAccountWebClient bankAccountWebClient;

    @Autowired
    private CreditServiceClient creditServiceClient;


    /**
     * Creates a new customer.
     * This method takes a Mono of CustomerDTO, converts it to a Customer entity,
     * saves it to the repository, and then converts it back to DTO to return.
     *
     * @param customerDTOMono A Mono stream of CustomerDTO.
     * @return ResponseEntity containing the created CustomerDTO or an error message.
     */
    @Override
    public Mono<ResponseEntity<CustomerDTO>> createCustomer(Mono<CustomerDTO> customerDTOMono, ServerWebExchange exchange) {
        return customerDTOMono
                .map(this::convertToEntity)
                .flatMap(this::validateCustomerType)
                .flatMap(customerRepository::save)
                .map(this::convertToDto)
                .map(savedCustomerDTO -> {
                    log.info("Customer created successfully: {}", savedCustomerDTO);
                    return ResponseEntity.status(HttpStatus.CREATED).body(savedCustomerDTO);
                })
                .onErrorResume(WebExchangeBindException.class, e -> {
                    log.error("Validation error: {}", e.getMessage());
                    return Mono.just(ResponseEntity.badRequest().body(new CustomerDTO()));
                })
                .onErrorResume(e -> {
                    log.error("Error creating customer: {}", e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }


    /**
     * Deletes a customer by their ID.
     * This method finds a customer by ID and deletes them if found.
     *
     * @param customerId The ID of the customer to delete.
     * @return ResponseEntity indicating success or failure of the operation.
     */
    @Override
    public Mono<ResponseEntity<Void>> deleteCustomer(String customerId, ServerWebExchange exchange) {
        return customerRepository.findById(customerId)
                .flatMap(customer ->
                        customerRepository.delete(customer)
                                .then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT))))
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND))
                .doOnError(e -> log.error("Error deleting customer: {}", e.getMessage()));
    }


    /**
     * Retrieves a customer by their ID.
     * This method finds a customer by their ID and returns their data as DTO.
     *
     * @param customerId The ID of the customer to retrieve.
     * @return ResponseEntity containing the CustomerDTO or a not found status.
     */
    @Override
    public Mono<ResponseEntity<CustomerDTO>> getCustomerById(String customerId, ServerWebExchange exchange) {
        return customerRepository.findById(customerId)
                .map(this::convertToDto)
                .map(customerDTO -> {
                    log.info("Customer found: {}", customerDTO);
                    return ResponseEntity.ok(customerDTO);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Customer not found: {}", customerId);
                    return Mono.just(ResponseEntity.notFound().build());
                }))
                .doOnError(e -> log.error("Error retrieving customer: {}", e.getMessage()));
    }


    /**
     * Lists all customers.
     * This method retrieves all customers from the repository and returns them as a Flux of CustomerDTO.
     *
     * @return ResponseEntity containing a Flux of CustomerDTOs.
     */
    @Override
    public Mono<ResponseEntity<Flux<CustomerDTO>>> listCustomers(ServerWebExchange exchange) {
        Flux<CustomerDTO> customerDTOFlux = customerRepository.findAll()
                .map(this::convertToDto)
                .doOnEach(customerSignal -> log.info("Listing customer: {}", customerSignal.get()));
        return Mono.just(ResponseEntity.ok(customerDTOFlux))
                .doOnError(e -> log.error("Error listing customers: {}", e.getMessage()));
    }


    /**
     * Updates a customer's information.
     * This method updates the information of an existing customer with the provided data.
     *
     * @param customerId      The ID of the customer to update.
     * @param customerDTOMono A Mono stream of CustomerDTO with the updated information.
     * @return ResponseEntity containing the updated CustomerDTO or an error message.
     */
    @Override
    public Mono<ResponseEntity<CustomerDTO>> updateCustomer(String customerId, Mono<CustomerDTO> customerDTOMono, ServerWebExchange exchange) {
        return customerRepository.findById(customerId)
                .switchIfEmpty(Mono.error(new CustomerNotFoundException("Customer not found with ID: " + customerId)))
                .flatMap(existingCustomer -> customerDTOMono
                        .map(this::convertToEntity)
                        .flatMap(this::validateCustomer)
                        .doOnNext(updatedCustomer -> updateCustomerFields(existingCustomer, updatedCustomer))
                        .flatMap(customerRepository::save)
                        .map(this::convertToDto)
                        .map(ResponseEntity::ok))
                .doOnError(e -> log.error("Error updating customer: {}", e.getMessage()));
    }



    private Mono<Customer> validateCustomerType(Customer customer) {
        Predicate<CustomerType> isValidType = type -> type == CustomerType.PERSONAL || type == CustomerType.BUSINESS;

        return Mono.just(customer)
                .filter(c -> isValidType.test(c.getType()))
                .switchIfEmpty(Mono.defer(() -> {
                    log.error("Invalid customer type for creation: {}", customer.getType());
                    return Mono.error(new InvalidCustomerTypeException("Creation of VIP or PYME customers is not allowed."));
                }));
    }


    private void updateCustomerFields(Customer existingCustomer, Customer updatedCustomer) {
        existingCustomer.setName(updatedCustomer.getName());
        existingCustomer.setEmail(updatedCustomer.getEmail());
        existingCustomer.setType(updatedCustomer.getType());
    }


    private Mono<Customer> validateCustomer(Customer customer) {
        Predicate<Customer> isVIPCustomer = c -> c.getType() == CustomerType.VIP;
        Predicate<Customer> isPymeCustomer = c -> c.getType() == CustomerType.PYME;

        return Mono.just(customer)
                .flatMap(c -> validateCustomerType(c, isVIPCustomer, this::validateVIPCustomer))
                .flatMap(c -> validateCustomerType(c, isPymeCustomer, this::validatePymeCustomer))
                .switchIfEmpty(Mono.just(customer));
    }

    private Mono<Customer> validateCustomerType(Customer customer, Predicate<Customer> predicate, Function<Customer, Mono<Customer>> validationFunction) {
        return predicate.test(customer) ? validationFunction.apply(customer) : Mono.just(customer);
    }

    private Mono<Customer> validateVIPCustomer(Customer customer) {

        return Mono.zip(
                creditServiceClient.hasCreditCard(customer.getId()),
                bankAccountWebClient.getSavingAccountBalance(customer.getId())
        ).flatMap(tuple -> {
            boolean hasCreditCard = tuple.getT1();
            double balance = tuple.getT2();

            if (!hasCreditCard) {
                log.warn("Validation failed for VIP customer {}: No credit card associated", customer.getId());
                return Mono.error(new NoCreditCardException("VIP customers must have a credit card."));
            }
            if (balance < VIPCustomer.MINIMUM_SAVINGS_AMOUNT) {
                log.warn("Validation failed for VIP customer {}: Insufficient savings balance", customer.getId());
                return Mono.error(new InsufficientBalanceException("VIP customers must have a minimum savings amount of " + VIPCustomer.MINIMUM_SAVINGS_AMOUNT));
            }
            log.info("VIP customer {} validated successfully", customer.getId());
            return Mono.just((Customer) customer);
        }).onErrorResume(e -> {
            log.error("Error during validation of VIP customer {}: {}", customer.getId(), e.getMessage());
            return Mono.error(e);
        });
    }

    private Mono<Customer> validatePymeCustomer(Customer customer) {
        return Mono.zip(
                creditServiceClient.hasCreditCard(customer.getId()),
                bankAccountWebClient.hasCurrentAccount(customer.getId())
        ).flatMap(tuple -> {
            boolean hasCreditCard = tuple.getT1();
            boolean hasCurrentAccount = tuple.getT2();

            if (!hasCreditCard) {
                log.warn("Validation failed for PYME customer {}: No credit card associated", customer.getId());
                return Mono.error(new NoCreditCardException("VIP customers must have a credit card."));
            }
            if (!hasCurrentAccount){
                log.warn("Validation failed for PYME customer {}: No current account associated", customer.getId());
                return Mono.error(new PymeRequirementsNotMetException("PYME customers must have a current account and a credit card."));
            }
            return Mono.just(customer);
        }).onErrorResume(e -> {
            log.error("Error during validation of PYME customer {}: {}", customer.getId(), e.getMessage());
            return Mono.error(e);
        });
    }


    private Customer convertToEntity(CustomerDTO dto) {
        return Customer.builder()
                .id(dto.getId())
                .name(dto.getName())
                .email(dto.getEmail())
                .type(dto.getType())
                .build();
    }

    private CustomerDTO convertToDto(Customer customer) {
        return new CustomerDTO()
                .id(customer.getId())
                .name(customer.getName())
                .email(customer.getEmail())
                .type(customer.getType());
    }
}
