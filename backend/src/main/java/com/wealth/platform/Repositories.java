package com.wealth.platform;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
interface UserRepository extends JpaRepository<User,Long>{ Optional<User> findByEmail(String email); }
interface PortfolioRepository extends JpaRepository<PortfolioItem,Long>{ List<PortfolioItem> findByOwnerId(Long ownerId); }
interface TransactionRepository extends JpaRepository<Transaction,Long>{ List<Transaction> findByOwnerIdOrderByDateDesc(Long ownerId); }
