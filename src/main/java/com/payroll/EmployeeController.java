package com.payroll;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/employees")
class EmployeeController {

  @Autowired
  private final EmployeeRepository repository;
  
  private final EmployeeModelAssembler assembler;

  EmployeeController(EmployeeRepository repository,EmployeeModelAssembler assembler) {
    this.repository = repository;
    this.assembler = assembler;
  }
  
//  EmployeeController(EmployeeRepository repository) {
//	    this.repository = repository;
//	  }


  // Aggregate root
  // tag::get-aggregate-root[]
  @GetMapping("/")
  CollectionModel<EntityModel<Employee>> all() {

    List<EntityModel<Employee>> employees = repository.findAll().stream()
        .map(assembler::toModel)
        .collect(Collectors.toList());

    return CollectionModel.of(employees, linkTo(methodOn(EmployeeController.class).all()).withSelfRel());
  }
  // end::get-aggregate-root[]
  
//  @GetMapping("/employees")
//  List<Employee> all() {
//    return repository.findAll();
//  }
  
  
  @PostMapping("/")
  ResponseEntity<?> newEmployee(@RequestBody Employee newEmployee) {

    EntityModel<Employee> entityModel = assembler.toModel(repository.save(newEmployee));

    return ResponseEntity //
        .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()) //
        .body(entityModel);
  }

//  @PostMapping("/employees")
//  Employee newEmployee(@RequestBody Employee newEmployee) {
//    return repository.save(newEmployee);
//  }

  // Single item
  
  @GetMapping("/{id}")
  EntityModel<Employee> one(@PathVariable Long id) {

    Employee employee = repository.findById(id) //
        .orElseThrow(() -> new EmployeeNotFoundException(id));

    return assembler.toModel(employee);
  }
  
//  @GetMapping("/employees/{id}")
//  EntityModel<Employee> one(@PathVariable Long id) {
//
//    Employee employee = repository.findById(id) //
//        .orElseThrow(() -> new EmployeeNotFoundException(id));
//  }

  @PutMapping("/{id}")
  Employee replaceEmployee(@RequestBody Employee newEmployee, @PathVariable Long id) {
    
    return repository.findById(id)
      .map(employee -> {
        employee.setName(newEmployee.getName());
        employee.setRole(newEmployee.getRole());
        return repository.save(employee);
      })
      .orElseGet(() -> {
        newEmployee.setId(id);
        return repository.save(newEmployee);
      });
  }

  @DeleteMapping("/{id}")
  void deleteEmployee(@PathVariable Long id) {
    repository.deleteById(id);
  }
}