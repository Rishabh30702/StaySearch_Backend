package com.example.StaySearch.StaySearchBackend.ContactUs;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Contact_Repository extends JpaRepository<Contact_Entity, Integer> {
}
