package com.example.StaySearch.StaySearchBackend.ContactUs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Contact_Service {

    @Autowired
    private Contact_Repository contactRepository;

    //Function to send the query
    public Contact_Entity sendQuery(Contact_Entity contactEntity){
        contactEntity.setVersion(0);  // Ensure initial version is set
        return contactRepository.save(contactEntity);
    }

}
