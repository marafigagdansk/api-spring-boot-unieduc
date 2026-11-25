package br.com.unicuritiba.financas.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.unicuritiba.financas.models.Transaction;

/**
 * Repositório Spring Data JPA para operações SQL na tabela transactions.
 */
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

	List<Transaction> findAllByOrderByDateDescCreatedAtDescIdDesc();

	@Query("""
			SELECT t FROM Transaction t
			WHERE (:type IS NULL OR t.type = :type)
			  AND (:search IS NULL OR LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%'))
			       OR LOWER(t.category) LIKE LOWER(CONCAT('%', :search, '%')))
			ORDER BY t.date DESC, t.createdAt DESC, t.id DESC
			""")
	List<Transaction> searchTransactions(@Param("type") String type, @Param("search") String search);
}
