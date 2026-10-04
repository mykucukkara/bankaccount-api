package tr.edu.sakarya.bil002.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tr.edu.sakarya.bil002.model.Transaction;
import tr.edu.sakarya.bil002.model.TransactionStatus;
import tr.edu.sakarya.bil002.model.TransactionType;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByAccountNumberOrderByTimestampDescIdDesc(String accountNumber);

    List<Transaction> findByAccountNumberAndTypeAndStatus(String accountNumber, TransactionType type,
                                                          TransactionStatus status);
}
