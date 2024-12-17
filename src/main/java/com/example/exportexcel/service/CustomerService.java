package com.example.exportexcel.service;

import com.example.exportexcel.entity.Customer;

public interface CustomerService {
    Customer get(Long id);
    Customer delete(Long id);
    Customer save(Customer customer);
}