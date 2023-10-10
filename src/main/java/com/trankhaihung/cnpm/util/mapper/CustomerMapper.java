package com.trankhaihung.cnpm.util.mapper;

import com.trankhaihung.cnpm.dto.CustomerDTO;
import com.trankhaihung.cnpm.entity.Customer;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {
    public Customer toEntity(CustomerDTO customerDTO) {
        Customer customer = new Customer();
        customer.setAddress(customerDTO.getAddress());
        customer.setEmail(customerDTO.getEmail());
        customer.setFirstName(customerDTO.getFirstName());
        customer.setLastName(customerDTO.getLastName());
        customer.setPhoneNumber(customerDTO.getPhoneNumber());
        customer.setMessage(customerDTO.getMessage());
        return customer;
    }
}
