// src/main/java/com/contact/manager/repositories/EmployeeRepository.java
package com.contact.manager.repositories;

import com.contact.manager.entities.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
}