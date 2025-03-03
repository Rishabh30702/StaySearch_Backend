package com.example.StaySearch.StaySearchBackend.ContactUs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("v1/contact")
public class Contact_Controller {

    @Autowired
    private Contact_Service contactService;

    //Gateway to save the query to database
    @PostMapping("/saveQuery")
    private ResponseEntity<?> saveQuery(@RequestBody Contact_Entity contactEntity){
        return new ResponseEntity<>(contactService.sendQuery(contactEntity), HttpStatus.OK);
    }

}
