package com.example.serwer.repository;

import com.example.serwer.model.Accounts;
import com.example.serwer.payload.Uploads;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UploadsRepository extends JpaRepository<Uploads, Long> {
}
